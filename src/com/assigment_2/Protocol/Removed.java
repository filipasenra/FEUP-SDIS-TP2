package com.assigment_2.Protocol;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;
import com.assigment_2.Storage.FileInfo;
import java.math.BigInteger;
import java.util.Arrays;

public class Removed implements Runnable{
    String filepath;
    byte[] fileData;
    BigInteger fileId;
    int replicationDegree;
    public final static int backupDataSize = 16000;
    SimpleNode sn;
    BigInteger successorId;
    BigInteger firstPeer;


    public Removed(BigInteger fileId, byte[] fileData, String filepath, int replicationDegree){

        this.fileData = fileData;
        this.fileId = fileId;
        this.successorId = fileId;
        this.filepath = filepath;
        this.replicationDegree = replicationDegree;
        this.sn = PeerClient.getNode().getSuccessor();
    }

    @Override
    public void run() {


        if (PeerClient.getNode().getId().equals(PeerClient.getNode().getSuccessor().getId())) {
            System.err.println("[FAILED MESSAGE] There aren't any peers available for backup! Try later!");
            return;
        }

        while (true) {
            System.out.println("[REMOVED]");

            this.sn = this.sn.getSuccessor();

            //check if we are the successor
            if (this.sn.getId().equals(PeerClient.getNode().getId()))
                this.sn = PeerClient.getNode().getSuccessor();

            //check if we have completed a full loop around the circle
            if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                System.out.println("[WARNING MESSAGE] REMOVED COMPLETED!");
            } else if (firstPeer == null)
                firstPeer = this.sn.getId();

            PeerClient.getNode().printInfo();


            int chunkNo = 0;
            boolean successful = true;

            for (int i = 0; i < this.fileData.length; i += backupDataSize, chunkNo++) {

                byte[] message;

                if (i + backupDataSize <= this.fileData.length) {
                    message = MessageFactoryChord.createMessage(3, "REMOVED", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), this.replicationDegree, chunkNo, Arrays.copyOfRange(this.fileData, i, i + backupDataSize));
                } else {
                    message = MessageFactoryChord.createMessage(3, "REMOVED", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), this.replicationDegree, chunkNo, Arrays.copyOfRange(this.fileData, i, this.fileData.length));
                }


                SSLEngineClient client;

                try {

                    client = new SSLEngineClient("TLSv1.2", sn.getAddress(), sn.getPort());
                    client.connect();
                    client.write(message);
                    client.read();
                    client.shutdown();
                } catch (Exception e) {
                    this.sn = PeerClient.getNode().find_successor(this.sn.getId());
                    continue;
                }

                MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
                messageFactoryChord.parseMessage(client.getPeerAppData().array());

                if (!messageFactoryChord.messageType.equals("BACKUP_COMPLETE")) {
                    successful = false;
                    break;
                }
            }

            if (successful) {
                break;
            }

            this.successorId = this.sn.getId();
        }


        System.out.println("[SUCCESS MESSAGE] BACKUP COMPLETED!");

    }

}

