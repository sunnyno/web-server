package com.dzytsiuk.webserver;

import com.dzytsiuk.webserver.http.HttpConnectorRemote;
import com.dzytsiuk.webserver.util.AppUtil;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ShutDown {

    public static void main(String[] args) throws RemoteException, NotBoundException {
        AppUtil.init();
        int rmiPort = Integer.parseInt(AppUtil.getApplicationProperty("rmi.port"));
        Registry registry = LocateRegistry.getRegistry(null, rmiPort);
        HttpConnectorRemote httpConnector = (HttpConnectorRemote) registry.lookup("HttpConnector");
        httpConnector.shutDown();
    }
}
