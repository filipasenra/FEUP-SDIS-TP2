package com.assigment_2.Chunk;

public class BackUpChunk extends Chunk {

    public byte[] data;
    boolean active = false;

    public BackUpChunk(double version, String senderId, String fileId, int chunkNo, int replicationDeg, byte[] data) {
        super(version, senderId, fileId, chunkNo, replicationDeg);
        this.data = data;
    }

    @Override
    synchronized public boolean deleteData() {
        this.data = null;
        return true;
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    synchronized public void makeInactive() {
        active = false;
    }

    synchronized public void makeActive() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }
}
