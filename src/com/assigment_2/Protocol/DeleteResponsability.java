package com.assigment_2.Protocol;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;

import java.math.BigInteger;

public class DeleteResponsability implements Runnable {
    BigInteger fileId;
    int replicationDegree;
    int deletedCounter;
    SimpleNode sn;
    BigInteger successorId;
    BigInteger firstPeer;

    public DeleteResponsability(BigInteger fileId) {
        this.deletedCounter = 0;
        this.fileId = fileId;
        this.successorId = fileId;
        this.sn = PeerClient.getNode().find_successor(fileId);
    }

    @Override
    public void run() {

        while (true) {
            this.sn = this.sn.getSuccessor();

            //CONFIRMA SE É O PRÓPRIO
            if (this.sn.getId().equals(PeerClient.getNode().getId()))
                this.sn = PeerClient.getNode().getSuccessor();

            //CONFIRMA SE É O PRIMEIRO
            if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                System.out.println("Not all stored copies were deleted!");
                break;
            } else if (firstPeer == null)
                firstPeer = this.sn.getId();

            System.out.println("SUCCESSOR de " + this.successorId + " is " + sn.getId());

            byte[] message;

            message = MessageFactoryChord.createMessage(3, "DELETE_RESPONSABILITY", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort());

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

            if (messageFactoryChord.messageType.equals("DELETED_RESPONSABILITY_ACCEPTED")) {
                System.out.println("Received deleted responsability accepted");
                break;
            }
        }

    }

}

