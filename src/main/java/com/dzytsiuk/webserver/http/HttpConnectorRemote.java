package com.dzytsiuk.webserver.http;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HttpConnectorRemote extends Remote {
    void shutDown() throws RemoteException;
}
