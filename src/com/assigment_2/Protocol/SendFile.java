package com.assigment_2.Protocol;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;
import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Storage.FileInfo;

public class SendFile implements Runnable {
    byte[] fileData;
    BigInteger fileId;
    String address;
    int port;
    public final static int backupDataSize = 16000;


    public SendFile(BigInteger fileId, byte[] fileData, String address, int port) throws Exception {
        this.fileData = fileData;
        this.fileId = fileId;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            int chunkNo = 0;

            for (int i = 0; i < this.fileData.length; i += backupDataSize, chunkNo++) {

                byte[] message;

                if (i + backupDataSize <= this.fileData.length) {
                    message = MessageFactoryChord.createMessage(3, "RESTORING", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), 0, chunkNo, Arrays.copyOfRange(this.fileData, i, i + backupDataSize));
                } else {
                    message = MessageFactoryChord.createMessage(3, "RESTORING", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), 0, chunkNo, Arrays.copyOfRange(this.fileData, i, this.fileData.length));
                }


                SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);

                try {
                    client.connect();
                    client.write(message);
                    client.read();
                    client.shutdown();
                } catch (Exception e) {
                    break;
                }


                MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
                messageFactoryChord.parseMessage(client.getPeerAppData().array());

                if (!messageFactoryChord.messageType.equals("RESTORE_COMPLETE")) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
