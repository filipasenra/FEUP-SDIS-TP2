package com.assigment_2.Protocol;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MulticastDataRecoveryChannel extends MulticastChannel {

    public MulticastDataRecoveryChannel(String INETAddress, int port) {
        super(INETAddress, port);
    }

    public void sendChunk(double version, String fileId, int chunkNo) {
        Random random = new Random();
        this.exec.schedule(new GetChunkThread(version, fileId, chunkNo), random.nextInt(401), TimeUnit.MILLISECONDS);
    }
}
