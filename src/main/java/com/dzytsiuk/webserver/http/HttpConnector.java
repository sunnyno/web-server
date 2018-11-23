package com.dzytsiuk.webserver.http;

import com.dzytsiuk.webserver.context.ApplicationContainer;
import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.processor.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpConnector implements HttpConnectorRemote {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ApplicationContainer applicationContainer = ApplicationContainer.getInstance();
    private int port;
    private boolean isShutDown = false;
    private ServerSocket serverSocket;

    public HttpConnector(int port) {
        this.port = port;
    }

    public boolean connect() {
        List<Socket> openSockets = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            log.info("Server is listening on port " + port);
            while (!isShutDown) {
                try {
                    Socket socket = serverSocket.accept();
                    openSockets.add(socket);
                    HttpProcessor httpProcessor = new HttpProcessor(socket);
                    executorService.execute(httpProcessor);
                } catch (Exception e) {
                    if (!isShutDown) {
                        throw new HttpException(StandardHttpStatus.INTERNAL_SERVER_ERROR, e);
                    }
                } finally {
                    if (isShutDown) {
                        log.info("Shutting down processes");
                        for (Socket openSocket : openSockets) {
                            openSocket.close();
                        }
                    }
                }
            }
            return true;
        } catch (IOException e) {
            throw new HttpException(StandardHttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public void shutDown() {
        log.info("Server is shutting down");
        isShutDown = true;
        applicationContainer.shutDownApps();
        executorService.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server socket", e);
        }
    }
}
