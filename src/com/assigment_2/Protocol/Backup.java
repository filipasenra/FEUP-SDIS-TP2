package com.assigment_2.Protocol;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.Peer;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;
import com.assigment_2.Chord.MessageFactoryChord;

public class Backup implements Runnable{
    byte[] fileData;
    BigInteger fileId;
    int replicationDegree;

    public Backup(BigInteger fileId, byte[] fileData, int replicationDegree) throws IOException {

        this.fileData = fileData;
        this.fileId = fileId;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
        try {
            SimpleNode sn = PeerClient.getNode().find_successor(this.fileId);
            System.out.println("SUCCESSOR of " + this.fileId + " is " + sn.getId());
            byte[] message = MessageFactoryChord.createMessage(3, "BACKUP", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), this.fileData);
            SSLEngineClient client = new SSLEngineClient("TLSv1.2", sn.getAddress(), sn.getPort());

            client.connect();
            client.write(message);
            client.read();
            client.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static BigInteger generateFileId(String filename, long lastModified, String owner) {
        try {
            String input = filename + lastModified + owner;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(input.getBytes());

            return new BigInteger(1,encoded).mod(BigInteger.valueOf((long) Math.pow(2, PeerClient.M)));
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1,"0".getBytes());
        }
    }
}
