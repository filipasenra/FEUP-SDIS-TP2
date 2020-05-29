package com.assigment_2.Protocol;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;

import java.math.BigInteger;

public class DeleteResponsability implements Runnable {
    BigInteger fileId;
    int deletedCounter;
    SimpleNode sn;
    BigInteger firstPeer;

    public DeleteResponsability(BigInteger fileId) {
        this.deletedCounter = 0;
        this.fileId = fileId;
        this.sn = PeerClient.getNode().find_successor(fileId);
    }

    @Override
    public void run() {

        while (true) {
            System.out.println("[DELETE RESPONSIBILITY]");

            this.sn = this.sn.getSuccessor();

            //check if we are the successor
            if (this.sn.getId().equals(PeerClient.getNode().getId()))
                this.sn = PeerClient.getNode().getSuccessor();

            //check if we have completed a full loop around the circle
            if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                System.err.println("[WARNING MESSAGE] DELETE WAS NOT COMPLETED: couldn't find a peer with this peer backed up!");
                break;
            } else if (firstPeer == null)
                firstPeer = this.sn.getId();

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
                System.err.println("[SUCCESS MESSAGE] DELETE WAS RESPONSIBILITY ACCEPTED");
                break;
            }
        }

    }

}

