package com.assigment_2.Protocol;

import com.assigment_2.Chunk.BackUpChunk;
import com.assigment_2.Storage.FileInfo;
import com.assigment_2.PeerClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class MulticastBackupChannel extends MulticastChannel {

    public MulticastBackupChannel(String INETAddress, int port) {
        super(INETAddress, port);
    }

    public void backupFile(double version, String senderId, String filepath, int replicationDeg) {

        File file = new File(filepath);

        try (
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            int chunkNr = 0;
            int bytesAmount;
            byte[] buffer = new byte[sizeOfChunks];
            String fileID = this.generateId(file.getName(), file.lastModified(), file.getParent());

            PeerClient.getStorage().addBackedUpFiles(fileID, new FileInfo(filepath, fileID, replicationDeg));

            while ((bytesAmount = bis.read(buffer)) > 0) {

                byte[] data = Arrays.copyOf(buffer, bytesAmount);
                byte[] message = MessageFactory.createMessage(version, "PUTCHUNK", senderId, fileID, chunkNr, replicationDeg, data);

                BackUpChunk chunk = PeerClient.getStorage().getBackUpChunk(fileID, chunkNr);
                if((chunk == null || !chunk.isActive())) {

                    System.out.println(" > SENDING MESSAGE: " + version + " PUTCHUNK " + senderId + " " + fileID + " " + chunkNr + " " + replicationDeg);
                    PeerClient.getStorage().addChunkToBackUp(fileID, chunkNr, new BackUpChunk(version, senderId, fileID, chunkNr, replicationDeg, data));
                    PeerClient.getExec().execute(new PutChunkThread(replicationDeg, message, fileID, chunkNr));

                }
                chunkNr++;
            }


            //needs empty chunk
            if((file.length() % sizeOfChunks) == 0){
                System.out.println(" > SENDING MESSAGE: " + version + " PUTCHUNK " + senderId + " " + fileID + " " + chunkNr + " " + replicationDeg);
                byte[] emptyData = {};
                byte[] message = MessageFactory.createMessage(version, "PUTCHUNK", senderId, fileID, chunkNr, replicationDeg, emptyData);

                BackUpChunk chunk = PeerClient.getStorage().getBackUpChunk(fileID, chunkNr);
                if(chunk == null || !chunk.isActive()) {
                    PeerClient.getStorage().addChunkToBackUp(fileID, chunkNr, new BackUpChunk(version, senderId, fileID, chunkNr, replicationDeg, emptyData));
                    PeerClient.getStorage().getBackUpChunk(fileID, chunkNr).makeActive();
                    PeerClient.getExec().execute(new PutChunkThread(replicationDeg, message, fileID, chunkNr));
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}