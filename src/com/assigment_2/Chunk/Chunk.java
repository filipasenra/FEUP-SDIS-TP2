package com.assigment_2.Chunk;

import com.assigment_2.PeerClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class Chunk implements Serializable {

    public double version;
    public String senderId;
    public String fileId;
    public int chunkNo;
    public int replicationDeg;

    public CopyOnWriteArrayList<String> peersBackingUpChunk = new CopyOnWriteArrayList<>();

    public Chunk(double version, String senderId, String fileId, int chunkNo, int replicationDeg) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
    }

    synchronized public boolean deleteData() {

        File file = new File(PeerClient.getId() + "/" + fileId + "_" + chunkNo);

        return file.delete();
    }

    public byte[] getData() throws IOException {
        File file = new File(PeerClient.getId() + "/" + fileId + "_" + chunkNo);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        return data;
    }

    public int getNumStoredTimes() {

        return this.peersBackingUpChunk.size();
    }

    public String getId() {
        return this.fileId + "_" + this.chunkNo;
    }
}
