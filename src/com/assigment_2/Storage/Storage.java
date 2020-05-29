package com.assigment_2.Storage;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chunk.Chunk;
import com.assigment_2.Pair;
import com.assigment_2.PeerClient;
import com.assigment_2.Protocol.Backup;
import com.assigment_2.Protocol.Removed;

import java.awt.desktop.SystemSleepEvent;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ConcurrentHashMap<BigInteger, Integer> storedFilesReplicationDegree = new ConcurrentHashMap<BigInteger, Integer>();

    //buffer to store files while they are being received
    private final ConcurrentHashMap<BigInteger, ArrayList<byte[]>> bufferFiles = new ConcurrentHashMap<>();

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

    public FileInfo getFileInfo(String filepath) {
        Set<BigInteger> keys = backedUpFiles.keySet();

        for (BigInteger key : keys) {
            FileInfo curr = backedUpFiles.get(key);

            if (curr.pathname.equals(filepath))
                return curr;
        }

        return null;
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


    public void setOverallSpace(int targetSpace, ScheduledThreadPoolExecutor exec) {

        try {
            targetSpace = targetSpace * 1000; //to kB
            System.out.println("Overall space before new setting: " + overallSpace);
            System.out.println("Occupied space before new setting: " + occupiedSpace);
            System.out.println("Target new overall space: " + targetSpace);

            this.overallSpace = targetSpace;


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
                exec.execute(new Removed(fileId, fileData, file.toPath().toString(), storedFilesReplicationDegree.get(fileId)));

                storedFiles.remove(fileId);
                storedFilesReplicationDegree.remove(fileId);
                this.setOccupiedSpace(this.getOccupiedSpace() - fileData.length);
                file.delete();

            }
            System.out.println(" > RECLAIM: New Storage Size is " + overallSpace);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /*
    public void decrementCountOfChunk(String fileId, int chunkNo, String senderId) {

        if(this.backedUpFiles.containsKey(fileId)){
            if(this.backedUpFiles.get(fileId).backedUpChunk.containsKey(chunkNo)){
                this.backedUpFiles.get(fileId).backedUpChunk.get(chunkNo).peersBackingUpChunk.remove(senderId);
            }
        }

    }

    public ConcurrentHashMap<Pair<String, Integer>, byte[]> getRecoveredChunks() {
        return recoveredChunks;
    }

    public ConcurrentHashMap<Pair<String, Integer>, Chunk> getStoredChunks(){
        return storedChunks;
    }

    public void addRecoveredChunk(String fileId, int chunkNo, byte[] data) {
        Pair<String, Integer> pair = new Pair<>(fileId, chunkNo);
        if(!recoveredChunks.containsKey(pair)) {
            recoveredChunks.put(pair, data);
        }
    }

    public void addChunkToStorage(Chunk chunk, byte[] data) {

        if ((overallSpace != -1) && ((this.overallSpace - this.occupiedSpace) < data.length)) {
            System.err.println("Peer doesn't have space for chunk number " + chunk.chunkNo + " of " + chunk.fileId + " from " + chunk.senderId);
            return;
        }

        Pair<String, Integer> key = new Pair<>(chunk.fileId, chunk.chunkNo);

        if (!storedChunks.containsKey(key)) {

            //Checking if chunk has already been saved by enough peers
            if(PeerClient.getVersion() == 2) {

                if (this.chunksGlobalCounter.containsKey(key)) {

                    System.out.println(this.chunksGlobalCounter.get(key) + " " + chunk.replicationDeg);
                    if(this.chunksGlobalCounter.get(key) >= chunk.replicationDeg - 1)
                    {
                        System.out.println("\t > ENHANCEMENT: Not saving chunk " + chunk.fileId + "_" + chunk.chunkNo + ". Saved " + this.chunksGlobalCounter.get(key) + " times. ");
                        return;
                    }
                }
            }

            //SEND CHUNK STORAGE CONFIRMATION MESSAGE
            PeerClient.getMC().confirmStore(chunk.version, PeerClient.getId(), chunk.fileId, chunk.chunkNo);

            chunk.peersBackingUpChunk.add(PeerClient.getId());
            this.storedChunks.put(key, chunk);
            this.occupiedSpace += data.length;

            String filename = PeerClient.getId() + "/" + chunk.fileId + "_" + chunk.chunkNo;

            File file = new File(filename);
            try {

                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();

                    FileOutputStream fos = new FileOutputStream(filename);
                    fos.write(data);

                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void updateStoredChunksCounter(String fileId, int chunkNo, String senderId) {

        if(this.backedUpFiles.containsKey(fileId)){
            if(this.backedUpFiles.get(fileId).backedUpChunk.containsKey(chunkNo)){
                if(!this.backedUpFiles.get(fileId).backedUpChunk.get(chunkNo).peersBackingUpChunk.contains(senderId))
                    this.backedUpFiles.get(fileId).backedUpChunk.get(chunkNo).peersBackingUpChunk.add(senderId);
            }
        } else {
            if(this.storedChunks.containsKey(new Pair<>(fileId, chunkNo))){
                this.storedChunks.get(new Pair<>(fileId, chunkNo)).peersBackingUpChunk.add(senderId);
            }


            if(PeerClient.getVersion() == 2) {
                Pair<String, Integer> key = new Pair<>(fileId, chunkNo);

                if (this.chunksGlobalCounter.containsKey(key)) {
                    int n = this.chunksGlobalCounter.get(key);
                    this.chunksGlobalCounter.put(key, n + 1);
                } else {
                    this.chunksGlobalCounter.put(key, 1);
                }

                System.out.println("\t > ENHANCEMENT: Chunk " + fileId + "_" + chunkNo + " saved " + this.chunksGlobalCounter.get(key) + " times. ");
            }
        }
    }

    public void deleteFileFromBackUpChunks(String fileId) {

        this.backedUpFiles.remove(fileId);

        ArrayList<Pair<String, Integer>> keysGlobalCounter = new ArrayList<>(this.chunksGlobalCounter.keySet());

        for (Pair<String, Integer> key : keysGlobalCounter) {
            if (key.getKey().equals(fileId)) {
                chunksGlobalCounter.remove(key);
            }
        }
    }

    public boolean deleteFileFromStoredChunks(String fileId) {

        ArrayList<Pair<String, Integer>> keys = new ArrayList<>(storedChunks.keySet());

        for (Pair<String, Integer> key : keys) {
            if (key.getKey().equals(fileId)) {
                Chunk chunkToEliminate = storedChunks.get(key);

                int dataSize = 0;

                try {
                    dataSize = chunkToEliminate.getData().length;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!chunkToEliminate.deleteData())
                    return false;

                storedChunks.remove(key);
                this.occupiedSpace -= dataSize;
            }
        }

        ArrayList<Pair<String, Integer>> keysGlobalCounter = new ArrayList<>(this.chunksGlobalCounter.keySet());

        for (Pair<String, Integer> key : keysGlobalCounter) {
            if (key.getKey().equals(fileId)) {
                chunksGlobalCounter.remove(key);
            }
        }

        return true;
    }

    public void addChunkToBackUp(String fileId, int chunkNo, BackUpChunk chunk) {

        if (!this.backedUpFiles.containsKey(fileId)) {
            return;
        }

        if (!this.backedUpFiles.get(fileId).backedUpChunk.containsKey(chunkNo)) {
            this.backedUpFiles.get(fileId).backedUpChunk.put(chunkNo, chunk);
        }

    }*/

    /*public BackUpChunk getBackUpChunk(String fileId, int chunkNo) {

        if(!this.backedUpFiles.containsKey(fileId))
            return null;

        if(!this.backedUpFiles.get(fileId).backedUpChunk.containsKey(chunkNo))
            return null;

        return this.backedUpFiles.get(fileId).backedUpChunk.get(chunkNo);
    }

    public ConcurrentHashMap<String, FileInfo> getBackedUpFiles() {
        return backedUpFiles;
    }

    public void addPendingChunks(Pair<String, Integer> pendingChunksPair) {
        this.pendingChunks.add(pendingChunksPair);
    }*/
}

