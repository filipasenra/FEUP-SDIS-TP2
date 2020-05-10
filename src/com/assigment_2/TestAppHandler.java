package com.assigment_2;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;

public class TestAppHandler {

    InterfacePeer peer;

    public TestAppHandler(String rmi_peer_ap) {

        Registry reg;
        try {
            reg = LocateRegistry.getRegistry();
            this.peer = (InterfacePeer) reg.lookup(rmi_peer_ap);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public boolean doBackup(String[] arguments){

        if(arguments.length != 2) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <rmi_peer_ap> BACKUP <file_path> <replication_degree>");
            return false;
        }

        try {
            peer.backup(arguments[0], Integer.parseInt(arguments[1]));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean doDeletion(String[] arguments) {
        if(arguments.length != 1) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <rmi_peer_ap> DELETE <file_path>");
            return false;
        }

        try {
            peer.deletion(arguments[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean doRestore (String[] arguments) {
        if(arguments.length != 1) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <rmi_peer_ap> RESTORE <file_path>");
            return false;
        }

        try {
            peer.restore(arguments[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean doReclaim (String[] arguments) {
        if(arguments.length != 1) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <rmi_peer_ap> RECLAIM <disk_space>");
            return false;
        }

        try {
            peer.reclaim(arguments[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean doState(String[] arguments) {

        if(arguments.length != 0) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <rmi_peer_ap> STATE");
            return false;
        }

        try {
            System.out.println(peer.state());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
