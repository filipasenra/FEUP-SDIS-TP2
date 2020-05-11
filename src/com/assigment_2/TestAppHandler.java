package com.assigment_2;

import com.assigment_2.SSLEngine.SSLEngineClient;

import java.io.IOException;
import java.rmi.RemoteException;

public class TestAppHandler extends SSLEngineClient {

    // TODO: adjust functions to send message to peer
    //  call to write in backup needs to be adjusted with the info of file
    //  other functions need to send to peer (server) the info of chunks

    /**
     * Initiates the engine to run as a client using peer information, and allocates space for the
     * buffers that will be used by the engine.
     *
     * @param protocol      The SSL/TLS protocol to be used. Java 1.6 will only run with up to TLSv1 protocol. Java 1.7 or higher also supports TLSv1.1 and TLSv1.2 protocols.
     * @param address The IP address of the peer.
     * @param port          The peer's port that will be used.
     * @throws Exception
     */
    public TestAppHandler(String protocol, String address, int port) throws Exception {
        super(protocol, address, port);
    }

    public boolean doBackup(String[] arguments){

        if(arguments.length != 2) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <node_ip> <node_port> BACKUP <file_path> <replication_degree>");
            return false;
        }

        try {
            this.connect();

            this.write("OLA");
            this.read();

            this.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean doDeletion(String[] arguments) {
        if(arguments.length != 1) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <node_ip> <node_port> DELETE <file_path>");
            return false;
        }

        //try {
            //TODO: change this
           // peer.deletion(arguments[0]);
        /*} catch (RemoteException e) {
            e.printStackTrace();
        }*/

        return true;
    }

    public boolean doRestore (String[] arguments) {
        if(arguments.length != 1) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <node_ip> <node_port> RESTORE <file_path>");
            return false;
        }

        //try {
            //TODO: change this
            //peer.restore(arguments[0]);
        /*} catch (RemoteException e) {
            e.printStackTrace();
        }*/

        return true;
    }

    public boolean doReclaim (String[] arguments) {
        if(arguments.length != 1) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <node_ip> <node_port> RECLAIM <disk_space>");
            return false;
        }

        //try {
            //TODO: change this
            //peer.reclaim(arguments[0]);
        /*} catch (RemoteException e) {
            e.printStackTrace();
        }*/

        return true;
    }

    public boolean doState(String[] arguments) {

        if(arguments.length != 0) {
            System.err.println("Wrong no. of arguments");
            System.err.println("Usage: <node_ip> <node_port> STATE");
            return false;
        }

        //try {
            //TODO: change this
            //System.out.println(peer.state());
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/

        return true;
    }
}
