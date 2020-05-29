package com.assigment_2.Protocol;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;
import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Storage.FileInfo;

public class Backup implements Runnable {
    String filepath;
    byte[] fileData;
    BigInteger fileId;
    int replicationDegree;
    int perceivedRepDegree;
    public final static int backupDataSize = 16000;
    SimpleNode sn;
    BigInteger successorId;
    BigInteger firstPeer;


    public Backup(BigInteger fileId, byte[] fileData, String filepath, int replicationDegree) {
        this.perceivedRepDegree = 0;
        this.fileData = fileData;
        this.fileId = fileId;
        this.successorId = fileId;
        this.filepath = filepath;
        this.replicationDegree = replicationDegree;
        this.sn = PeerClient.getNode().find_successor(fileId);
    }

    @Override
    public void run() {
        try {

            if (PeerClient.getNode().getId().equals(PeerClient.getNode().getSuccessor().getId())) {
                System.out.println("There aren't any peers available for backup!");
                return;
            }

            while (this.perceivedRepDegree < this.replicationDegree) {
                System.out.println("BACKUP");
                System.out.println(this.sn.getId());

                //PODE FALHAR AQUI
                this.sn = this.sn.getSuccessor();

                //CONFIRMA SE É O PRÓPRIO
                if (this.sn.getId().equals(PeerClient.getNode().getId()))
                    this.sn = PeerClient.getNode().getSuccessor();

                //CONFIRMA SE É O PRIMEIRO
                if (firstPeer != null && firstPeer.equals(this.sn.getId())) {
                    System.out.println("Replication degree not achieved!");
                    break;
                }
                else if (firstPeer == null)
                    firstPeer = this.sn.getId();

                System.out.println("SUCCESSOR de " + this.successorId + " is " + sn.getId());

                PeerClient.getNode().printInfo();


                int chunkNo = 0;
                boolean successful = true;

                for (int i = 0; i < this.fileData.length; i += backupDataSize, chunkNo++) {

                    byte[] message;

                    if (i + backupDataSize <= this.fileData.length) {
                        message = MessageFactoryChord.createMessage(3, "BACKUP", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), this.replicationDegree, chunkNo, Arrays.copyOfRange(this.fileData, i, i + backupDataSize));
                    } else {
                        message = MessageFactoryChord.createMessage(3, "BACKUP", this.fileId, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), this.replicationDegree, chunkNo, Arrays.copyOfRange(this.fileData, i, this.fileData.length));
                    }

                    SSLEngineClient client;

                    try {
                        client = new SSLEngineClient("TLSv1.2", sn.getAddress(), sn.getPort());

                        client.connect();
                        client.write(message);
                        client.read();
                        client.shutdown();
                    }catch (Exception e) {
                        successful = false;
                        break;
                    }

                    MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
                    messageFactoryChord.parseMessage(client.getPeerAppData().array());

                    if (!messageFactoryChord.messageType.equals("BACKUP_COMPLETE")) {
                       successful = false;
                       break;
                    }
                }


                if (successful) {
                    this.perceivedRepDegree++;
                }

                if (this.perceivedRepDegree < this.replicationDegree) {
                    this.successorId = this.sn.getId();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.perceivedRepDegree > 0 )
         PeerClient.getStorage().addBackedUpFile(fileId, new FileInfo(filepath, fileId, replicationDegree));

    }

    public static BigInteger generateFileId(String filename, long lastModified, String owner) {
        try {
            String input = filename + lastModified + owner;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(input.getBytes());

            return new BigInteger(1, encoded).mod(BigInteger.valueOf((long) Math.pow(2, PeerClient.M)));
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1, "0".getBytes());
        }
    }
}
