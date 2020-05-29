package com.assigment_2.Storage;

import com.assigment_2.PeerClient;
import com.assigment_2.Protocol.Removed;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.math.BigInteger;
<<<<<<< HEAD
=======
import java.util.List;
import java.util.Map;
>>>>>>> 7515dc0f889b229ed3a976f0421b9a5ad110118f
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Storage implements Serializable {
    private int overallSpace;
    private int occupiedSpace;

    //files he has sent a backup
    private final ConcurrentHashMap<BigInteger, FileInfo> backedUpFiles = new ConcurrentHashMap<>();

    //files he has stored
    private final ArrayList<BigInteger> storedFiles = new ArrayList<>();

    //rep of files stored
    private final ConcurrentHashMap<BigInteger, Integer> storedFilesReplicationDegree = new ConcurrentHashMap<>();

    //buffer to store files while they are being received
    private final ConcurrentHashMap<BigInteger, ArrayList<byte[]>> bufferFiles = new ConcurrentHashMap<>();

    //buffer for file path
    private final ConcurrentHashMap<BigInteger, String> bufferFilePath = new ConcurrentHashMap<>();

    public Storage() {
        this.overallSpace = -1;
        this.occupiedSpace = 0;
    }

    public void setOccupiedSpace(int occupiedSpace) {
        this.occupiedSpace = occupiedSpace;
    }

    public int getOccupiedSpace() {
        return this.occupiedSpace;
    }

    public ConcurrentHashMap<BigInteger, Integer> getStoredFilesReplicationDegree(){ return this.storedFilesReplicationDegree; }

    public int getOverallSpace() {
        return this.overallSpace;
    }

    public ConcurrentHashMap<BigInteger, FileInfo> getBackedUpFiles() {
        return backedUpFiles;
    }

    public boolean addBackedUpFile(BigInteger fileId, FileInfo fileInfo) {
        if (!this.backedUpFiles.containsKey(fileId)) {
            this.backedUpFiles.put(fileId, fileInfo);
            return true;
        } else {
            return false;
        }
    }

    public void addFilePathToBuffer(BigInteger fileId, String filepath){

        this.bufferFilePath.put(fileId, filepath);

    }

    public void removeFilePathFromBuffer(BigInteger fileId){
        this.bufferFilePath.remove(fileId);
    }

    public String getFilePathFromBuffer(BigInteger fileId){
        return this.bufferFilePath.get(fileId);
    }

    public boolean removeBufferedFile(BigInteger fileId) {
        if (this.bufferFiles.containsKey(fileId)) {
            this.bufferFiles.remove(fileId);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeBackedUpFile(BigInteger fileId) {
        if (this.backedUpFiles.containsKey(fileId)) {
            this.backedUpFiles.remove(fileId);
            return true;
        } else {
            return false;
        }
    }

    public boolean hasFileStored(BigInteger id) {

        return this.storedFiles.contains(id) || this.storedFilesReplicationDegree.containsKey(id);

    }

    public ArrayList<BigInteger> getStoredFiles() {
        return storedFiles;
    }

    public boolean addStoredFile(BigInteger fileId, Integer repDegree) {
        if (!this.storedFiles.contains(fileId)) {
            this.storedFiles.add(fileId);
            this.storedFilesReplicationDegree.put(fileId, repDegree);
            return true;
        }

        return false;
    }

    public boolean removeStoredFiles(BigInteger fileId){

        if(this.storedFiles.contains(fileId)){
            this.storedFilesReplicationDegree.remove(fileId);
            this.storedFiles.remove(fileId);

            File file = new File(PeerClient.getId() + "/" + fileId);

            file.delete();
            PeerClient.getStorage().setOccupiedSpace((int) (PeerClient.getStorage().getOccupiedSpace() - file.length()));

            return true;
        }

        return false;
    }

    public void addToBuffer(BigInteger fileId, byte[] data, int chunkNo) {

        if (!this.bufferFiles.containsKey(fileId)) {
            this.bufferFiles.put(fileId, new ArrayList<>());

            //Remover o buffer com os pedaços de informação se ao fim de um tempo nao tiver feito backup
            PeerClient.getExec().schedule(() -> {
                removeBufferedFile(fileId);
            }, 60, TimeUnit.SECONDS);
        }

        if (chunkNo + 1 == this.bufferFiles.get(fileId).size())
            this.bufferFiles.get(fileId).add(data);
        else if (chunkNo < this.bufferFiles.get(fileId).size())
            this.bufferFiles.get(fileId).set(chunkNo, data);
        else {
            for (int i = this.bufferFiles.get(fileId).size(); i < chunkNo; i++) {
                this.bufferFiles.get(fileId).add(null);
            }

            this.bufferFiles.get(fileId).add(data);
        }
    }

    public ArrayList<byte[]> getBufferFromFile(BigInteger fileId) {
        return this.bufferFiles.get(fileId);
    }


    public List<Future<?>> setOverallSpace(int targetSpace, ScheduledThreadPoolExecutor exec) {

        try {
            targetSpace = targetSpace * 1000; //to kB
            System.out.println("[RECLAIM] Overall space before new setting: " + overallSpace);
            System.out.println("[RECLAIM] Occupied space before new setting: " + occupiedSpace);
            System.out.println("[RECLAIM] Target new overall space: " + targetSpace);

            this.overallSpace = targetSpace;

            var futures = new ArrayList<Future<?>>();
            while (targetSpace < occupiedSpace) {

                if (this.storedFiles.size() == 0) {
                    System.err.println("Eliminated all chunks and still over capacity!\n");
                }

                BigInteger fileId = this.storedFiles.iterator().next();
                System.out.println("FileNo: " + fileId);
                String filename = PeerClient.getId() + "/" + fileId + ".ser";

                File file = new File(PeerClient.getId() + "/" + fileId);

                byte[] fileData = Files.readAllBytes(file.toPath());

                //Send new backup message for this file
                var future = exec.submit(
                        new Removed(fileId, fileData, file.toPath().toString(),
                                storedFilesReplicationDegree.get(fileId))
                );
                futures.add(future);

                storedFiles.remove(fileId);
                storedFilesReplicationDegree.remove(fileId);
                this.setOccupiedSpace(this.getOccupiedSpace() - fileData.length);
                file.delete();

            }

            System.out.println("[RECLAIM] RECLAIMED SUCCESS: New Storage Size is " + overallSpace);
            return futures;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}

