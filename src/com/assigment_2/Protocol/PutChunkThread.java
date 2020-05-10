package com.assigment_2.Protocol;

import com.assigment_2.PeerClient;

import java.util.concurrent.TimeUnit;

public class PutChunkThread implements Runnable {
    private final int replicationDeg;
    byte[] message;
    int counter;
    int delay;
    String fileId;
    int chunkNo;

    public PutChunkThread(int replicationDeg, byte[] message, String fileId, int chunkNo) {

        this.replicationDeg = replicationDeg;
        this.message = message;
        this.counter = 1;
        this.delay = 1;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {

        int numStoredTimes = PeerClient.getStorage().getBackUpChunk(this.fileId, this.chunkNo).getNumStoredTimes();

        if (numStoredTimes < replicationDeg) {


            System.out.println("\t> File: " + this.fileId + " Chunk No: " + chunkNo);
            System.out.println("\t\tStored: " + numStoredTimes + " times");
            System.out.println("\t\tDesired Replication Degree: " + replicationDeg);
            System.out.println("\t\tAttempt: " + this.counter);
            System.out.println("\t\tNext Delay: " + this.delay);
            System.out.println();

            PeerClient.getExec().execute(new Thread(() -> PeerClient.getMDB().sendMessage(message)));

           if (this.counter < 5) {
                PeerClient.getExec().schedule(this, this.delay, TimeUnit.SECONDS);
           } else {
               PeerClient.getStorage().getBackUpChunk(this.fileId, this.chunkNo).makeInactive();
           }

            this.counter++;
            this.delay = 2 * this.delay;

        } else {
            PeerClient.getStorage().getBackUpChunk(this.fileId, this.chunkNo).makeInactive();
            System.out.println(" > Replication Degree has been met of chunk " + chunkNo + " of file " + fileId);
        }
    }
}
