package com.assigment_2;

import com.assigment_2.Chord.Node;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.Storage.Storage;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
* Class that initiates a Peer
* */
public class PeerClient {

    public static final int M = 8;
    static private Double version;
    private static String id;
    private static String address;
    private static int port;

    private static Storage storage = new Storage();

    private static Node node;

    private static SimpleNode simpleNode;

    private static final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);
    private static Peer obj;

    public static void main(String[] args) throws Exception {
        if(!parseArgs(args))
            System.exit(-1);
    }

    private static boolean parseArgs(String[] args) throws Exception {

        if(args.length != 4 && args.length != 6){
            System.err.println("Usage: PeerClient <version> <server id> <address> <port> [<random_node_address> <random_node_port>]");
            return false;
        }

        version = Double.parseDouble(args[0]);

        if(version != 1 && version != 2){
            System.err.println("Usage: There are only 2 versions: 1.0 and 2.0");
            return false;
        }

        id = args[1];
        address = args[2];
        port = Integer.parseInt(args[3]);

        if(args.length == 6){
            simpleNode = new SimpleNode(args[4], Integer.parseInt(args[5]), M);
        }

        node = new Node(address, port, M);
        if (!node.join(simpleNode))
            return false;

        node.printInfo();

        exec.scheduleAtFixedRate(node, 1500, 3500, TimeUnit.MILLISECONDS);

        obj = new Peer(version, id, "TLSv1.2", address, port);

        exec.execute(obj);

        try {
            InterfacePeer peer = (InterfacePeer) UnicastRemoteObject.exportObject(obj, 0);
            Registry rmiReg  = LocateRegistry.getRegistry();
            rmiReg.rebind(id, peer);

        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                var rmiReg = LocateRegistry.getRegistry();
                rmiReg.unbind(id);
                if (obj.isActive()) {
                    obj.shutdown();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }));

        System.out.println("Peer " + getId() + " ready");


        return true;
    }

    public static Node getNode() {
        return node;
    }

    public static String getId() {
        return id;
    }

    public static Storage getStorage() {
        return storage;
    }

    public static ScheduledThreadPoolExecutor getExec() {
        return exec;
    }

    public static Peer getObj() {
        return obj;
    }
}
