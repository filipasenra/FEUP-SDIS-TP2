package com.assigment_2;

import com.assigment_2.Protocol.MulticastBackupChannel;
import com.assigment_2.Protocol.MulticastControlChannel;
import com.assigment_2.Protocol.MulticastDataRecoveryChannel;
import com.assigment_2.Storage.Storage;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/*
* Class that initiates a Peer
* */
public class PeerClient {

    private final static String serializeObjectName = "Storage";
    static private Double version;
    private static String id;
    private static MulticastBackupChannel MDB;
    public static MulticastControlChannel MC;
    public static MulticastDataRecoveryChannel MDR;

    private static Storage storage = new Storage();

    private static final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

    public static void main(String[] args) {
        if(!parseArgs(args))
            System.exit(-1);
    }

    private static boolean parseArgs(String[] args) {

        if(args.length != 9){
            System.err.println("Usage: PeerClient <version> <server id> <access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>");
            return false;
        }

        version = Double.parseDouble(args[0]);

        if(version != 1 && version != 2){
            System.err.println("Usage: There are only 2 versions: 1.0 and 2.0");
            return false;
        }

        id = args[1];
        String remote_object_name = args[2];
        String MCAddress = args[3];
        int MCPort = Integer.parseInt(args[4]);
        String MDBAddress = args[5];
        int MDBPort = Integer.parseInt(args[6]);
        String MDRAddress = args[7];
        int MDRPort = Integer.parseInt(args[8]);

        MDB = new MulticastBackupChannel(MDBAddress, MDBPort);
        MC = new MulticastControlChannel(MCAddress, MCPort);
        MDR = new MulticastDataRecoveryChannel(MDRAddress, MDRPort);

        Peer obj = new Peer(version, id, MC, MDB, MDR);

        try {
            InterfacePeer peer = (InterfacePeer) UnicastRemoteObject.exportObject(obj, 0);
            Registry rmiReg  = LocateRegistry.getRegistry();
            rmiReg.rebind(remote_object_name, peer);

        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        getStorageFromFile();

        //Saves storage before shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(PeerClient::saveStorageIntoFile));

        System.out.println("Peer " + getId() + " ready");

        exec.execute(MDB);
        exec.execute(MC);
        exec.execute(MDR);

        return true;
    }

    public static MulticastControlChannel getMC() {
        return MC;
    }

    public static MulticastBackupChannel getMDB() {
        return MDB;
    }

    public static MulticastDataRecoveryChannel getMDR() {
        return MDR;
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
