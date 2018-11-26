package com.dzytsiuk.webserver;

import com.dzytsiuk.webserver.app.scanner.WebAppScanner;
import com.dzytsiuk.webserver.http.connector.HttpConnector;
import com.dzytsiuk.webserver.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Starter {
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Logger log = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) throws RemoteException {
        int port = Integer.parseInt(AppUtil.getApplicationProperty("server.port"));
        int rmiPort = Integer.parseInt(AppUtil.getApplicationProperty("rmi.port"));

        WebAppScanner webAppScanner = new WebAppScanner();
        HttpConnector httpConnector = new HttpConnector(port);
        registerHttpConnectorRemote(httpConnector, rmiPort);

        webAppScanner.initialScan();
        executorService.execute(webAppScanner::scan);
        httpConnector.connect();
        executorService.shutdownNow();
        UnicastRemoteObject.unexportObject(httpConnector, true);
    }

    private static void registerHttpConnectorRemote(HttpConnector connector, int port) {
        try {
            Remote exportObject = UnicastRemoteObject.exportObject(connector, 0);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("HttpConnector", exportObject);
            log.info("HttpConnector registered as RMI server");
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
