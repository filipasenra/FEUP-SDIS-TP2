package com.assigment_2;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfacePeer extends Remote {

    void backup(String file_path, int replication_degree) throws Exception;

    void deletion(String file_path) throws Exception;

    void restore(String file_path) throws Exception;

    void reclaim(String file_path) throws RemoteException;

    String state() throws IOException;
}
