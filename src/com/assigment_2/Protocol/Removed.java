package com.assigment_2.Protocol;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;
import com.assigment_2.Storage.FileInfo;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

public class Removed implements Runnable{
    String filepath;
    byte[] fileData;
    BigInteger fileId;
    int replicationDegree;
    int perceivedRepDegree;
    public final static int backupDataSize = 16000;
    SimpleNode sn;
    BigInteger successorId;
    BigInteger firstPeer;


    public Removed(BigInteger fileId, byte[] fileData, String filepath, int replicationDegree) throws Exception {
        this.perceivedRepDegree = 0;
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
            System.out.println("There aren't any peers available for backup!");
            return;
        }

        while (this.perceivedRepDegree < this.replicationDegree) {
            this.sn = this.sn.getSuccessor();

            //CONFIRMA SE É O PRÓPRIO
            if (this.sn.getId().equals(PeerClient.getNode().getId()))
                this.sn = PeerClient.getNode().getSuccessor();

            //CONFIRMA SE É O PRIMEIRO
            if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                break;
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
            System.out.println("SucessorId: " + this.successorId);

            if (this.perceivedRepDegree < this.replicationDegree) {
                this.successorId = this.sn.getId();
            }
        }

        PeerClient.getStorage().addBackedUpFile(fileId, new FileInfo(filepath, fileId, replicationDegree));

    }

}

