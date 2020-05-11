package com.assigment_2;

import com.assigment_2.Chord.Node;
import com.assigment_2.SSLEngine.ServerRunnable;
import com.assigment_2.Storage.Storage;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/*
* Class that initiates a Peer
* */
public class PeerClient {

    private static final int M = 8;
    private static ServerRunnable serverRunnable;
    private final static String serializeObjectName = "Storage";
    static private Double version;
    private static String id;
    private static String address;
    private static int port;

    private static Node node;

    private static Storage storage = new Storage();

    private static final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

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

        node = new Node(address, port, M);

        serverRunnable = new ServerRunnable(new Peer("TLSv1.2", address, port, version, id));

        exec.execute(serverRunnable);
        //TODO: adjust read function in peer to behave as we'd like

        getStorageFromFile();

        //Saves storage before shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(PeerClient::saveStorageIntoFile));

        System.out.println("Peer " + getId() + " ready");

        return true;
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

    //saves this peer storage in a file called storage.ser
    private static void saveStorageIntoFile() {

        serverRunnable.stop();

        try {
            String filename = PeerClient.getId() + "/" + serializeObjectName + ".ser";

            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                file.delete();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(storage);
            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void getStorageFromFile() {
        try {
            String filename = PeerClient.getId() + "/" + serializeObjectName + ".ser";

            File file = new File(filename);
            if (!file.exists()) {
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            storage = (Storage) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Double getVersion() {
        return version;
    }
}
