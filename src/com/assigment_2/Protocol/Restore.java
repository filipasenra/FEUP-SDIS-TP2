package com.assigment_2.Protocol;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;
import java.math.BigInteger;

public class Restore implements Runnable {
    private final String filepath;
    BigInteger fileId;
    BigInteger successorId;
    SimpleNode sn;
    BigInteger firstPeer;

    public Restore(BigInteger fileId, String filepath) {
        this.fileId = fileId;
        this.successorId = fileId;
        this.sn = PeerClient.getNode().find_successor(fileId);
        this.filepath = filepath;
    }

    @Override
    public void run() {

        try {
            while (true) {

                this.sn = this.sn.getSuccessor();

                //CONFIRMA SE É O PRÓPRIO
                if (this.sn.getId().equals(PeerClient.getNode().getId()))
                    this.sn = PeerClient.getNode().getSuccessor();

                //CONFIRMA SE É O PRIMEIRO
                if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                    System.out.println("Impossible to recover file because no peer seemed to have it backed up!");
                    break;
                } else if (firstPeer == null)
                    firstPeer = this.sn.getId();

                System.out.println("SUCCESSOR de " + this.successorId + " is " + sn.getId());

                byte[] message;
                message = MessageFactoryChord.createMessage(3, "RESTORE", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort());
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

                if (messageFactoryChord.messageType.equals("SENDING_FILE")) {
                    System.out.println("Waiting for peer to send the file...");
                    PeerClient.getStorage().addFilePathToBuffer(fileId, filepath);
                    break;
                }

                this.successorId = this.sn.getId();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
