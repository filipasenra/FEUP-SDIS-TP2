package com.assigment_2;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfacePeer extends Remote {

    void backup(String file_path, int replication_degree) throws RemoteException;

    void deletion(String file_path) throws RemoteException;

    void restore(String file_path) throws RemoteException;

    void reclaim(String file_path) throws RemoteException;

    String state() throws IOException;
}
