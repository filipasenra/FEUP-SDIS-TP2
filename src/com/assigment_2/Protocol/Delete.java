package com.assigment_2.Protocol;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;

import java.awt.desktop.SystemSleepEvent;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

public class Delete implements Runnable {
    BigInteger fileId;
    int replicationDegree;
    int deletedCounter;
    SimpleNode sn;
    BigInteger successorId;
    BigInteger firstPeer;

    public Delete(BigInteger fileId, int replicationDegree){
        this.deletedCounter = 0;
        this.fileId = fileId;
        this.successorId = fileId;
        this.replicationDegree = replicationDegree;
        this.sn = PeerClient.getNode().find_successor(fileId);
    }

    @Override
    public void run() {

        try {
            while (this.deletedCounter < this.replicationDegree) {
                System.out.println("DELETE: " + this.deletedCounter);
                PeerClient.getStorage().removeBackedUpFile(this.fileId);

                this.sn = this.sn.getSuccessor();

                //CONFIRMA SE É O PRIMEIRO
                if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                    System.out.println("Not all stored copies were deleted!");
                    break;
                } else if (firstPeer == null)
                    firstPeer = this.sn.getId();

                System.out.println("SUCCESSOR de " + this.successorId + " is " + sn.getId());

                byte[] message;


                message = MessageFactoryChord.createMessage(3, "DELETE", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort());
                SSLEngineClient client;

                try {
                    client = new SSLEngineClient("TLSv1.2", sn.getAddress(), sn.getPort());

                    client.connect();
                    client.write(message);
                    client.read();
                    client.shutdown();

                    MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
                    messageFactoryChord.parseMessage(client.getPeerAppData().array());

                    if (messageFactoryChord.messageType.equals("DELETED")) {
                        this.deletedCounter++;
                    }

                }catch (Exception e) {
                    this.sn = PeerClient.getNode().find_successor(this.sn.getId());
                    continue;
                }

                if (this.deletedCounter < this.replicationDegree) {
                    this.successorId = this.sn.getId();
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
