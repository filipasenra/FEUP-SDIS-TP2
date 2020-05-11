package com.assigment_2.Protocol;

import com.assigment_2.Chunk.BackUpChunk;
import com.assigment_2.Chunk.Chunk;
import com.assigment_2.Pair;
import com.assigment_2.PeerClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ReceivedMessagesHandler implements Runnable {
    MessageFactory messageFactory;
    byte[] message;

    public ReceivedMessagesHandler(byte[] message) {
        this.message = message;

    }

    @Override
    public void run() {

        this.messageFactory = new MessageFactory();

        if (!messageFactory.parseMessage(this.message)) {
            return;
        }

        //Ignore messages
        if (PeerClient.getId().equals(messageFactory.senderId)) {
            //System.out.println("Ignored Message");
            return;
        }

        switch (messageFactory.messageType) {
            case "PUTCHUNK":
                managePutChunk();
                break;
            case "STORED":
                manageStored();
                break;
            case "GETCHUNK":
                manageGetChunk();
                break;
            case "CHUNK":
                manageChunk();
                break;
            case "DELETE":
                manageDeletion();
                break;
            case "REMOVED":
                manageRemove();
                break;
            case "PORT":
                managePort();
                break;
            default:
                System.err.println("NOT A VALID PROTOCOL: " + this.messageFactory.messageType);
        }
    }

    private void managePutChunk() {

        System.out.println(" > RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId + " " + this.messageFactory.chunkNo + " " + this.messageFactory.replicationDeg);

        Random random = new Random();

        PeerClient.getExec().schedule(new Thread(() -> {
            Chunk chunk = new Chunk(this.messageFactory.version, this.messageFactory.senderId, this.messageFactory.fileId, this.messageFactory.chunkNo, this.messageFactory.replicationDeg);
            PeerClient.getStorage().addChunkToStorage(chunk, this.messageFactory.data); }),
                random.nextInt(401), TimeUnit.MILLISECONDS);

        }

    private void manageStored() {

        System.out.println(" > RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId + " " + this.messageFactory.chunkNo);

        PeerClient.getStorage().updateStoredChunksCounter(this.messageFactory.fileId, this.messageFactory.chunkNo, this.messageFactory.senderId);
    }

    private void manageDeletion() {
        System.out.println(" > RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId);

        if (PeerClient.getStorage().deleteFileFromStoredChunks(this.messageFactory.fileId))
            System.out.println(" > DELETED " + this.messageFactory.fileId);
        else
            System.out.println(" > FAILED TO DELETED " + this.messageFactory.fileId);
    }


    private void manageRemove() {

        System.out.println(" > RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId + " " + this.messageFactory.chunkNo);

        BackUpChunk chunk = PeerClient.getStorage().getBackUpChunk(this.messageFactory.fileId, this.messageFactory.chunkNo);
        //Checks if remove is from one of its files
        if (chunk != null && !chunk.isActive()) {

            PeerClient.getStorage().decrementCountOfChunk(this.messageFactory.fileId, this.messageFactory.chunkNo, this.messageFactory.senderId);

            int numStoredTimes = PeerClient.getStorage().getBackUpChunk(chunk.fileId, chunk.chunkNo).getNumStoredTimes();

            if (numStoredTimes < chunk.replicationDeg) {

                byte[] message = MessageFactory.createMessage(chunk.version, "PUTCHUNK", PeerClient.getId(), chunk.fileId, chunk.chunkNo, chunk.replicationDeg, chunk.data);

                System.out.println(" > SENDING MESSAGE: " + chunk.version + " PUTCHUNK " + PeerClient.getId() + " " + chunk.fileId + " " + chunk.chunkNo + " " + chunk.replicationDeg);

                Random random = new Random();
                PeerClient.getExec().schedule(new PutChunkThread(chunk.replicationDeg, message, chunk.fileId, chunk.chunkNo), random.nextInt(401), TimeUnit.MILLISECONDS);
            }
        }
    }

    private void manageGetChunk() {

        System.out.println(" > RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId + " " + this.messageFactory.chunkNo);

        //TODO: change this
        //PeerClient.getMDR().sendChunk(this.messageFactory.version, this.messageFactory.fileId, this.messageFactory.chunkNo);

    }

    private void manageChunk() {

        System.out.println(" > RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId + " " + this.messageFactory.chunkNo);
        PeerClient.getStorage().addRecoveredChunk(this.messageFactory.fileId, this.messageFactory.chunkNo, this.messageFactory.data);

        if(!PeerClient.getStorage().pendingChunks.contains(new Pair<>(this.messageFactory.fileId, this.messageFactory.chunkNo)))
        {
            return;
        }

        PeerClient.getStorage().pendingChunks.remove(new Pair<>(this.messageFactory.fileId, this.messageFactory.chunkNo));
    }


    private void managePort() {

        System.out.println("\t > ENHANCEMENT RECEIVED: " + this.messageFactory.version + " " + this.messageFactory.messageType + " " + this.messageFactory.senderId + " " + this.messageFactory.fileId + " " + this.messageFactory.chunkNo + " " + this.messageFactory.replicationDeg);

        if(!PeerClient.getStorage().pendingChunks.contains(new Pair<>(this.messageFactory.fileId, this.messageFactory.chunkNo)))
        {
            return;
        }

        PeerClient.getStorage().pendingChunks.remove(new Pair<>(this.messageFactory.fileId, this.messageFactory.chunkNo));


        String s = new String(this.messageFactory.data);
        String[] info = s.split(" ");
        int port = Integer.parseInt(info[0]);
        String host = info[1];

        try {
        Socket servidor = new Socket(host, port);

        ObjectInputStream entrada = new ObjectInputStream(servidor.getInputStream());

        byte[] data = (byte[]) entrada.readObject();
        entrada.close();

        PeerClient.getStorage().addRecoveredChunk(this.messageFactory.fileId, this.messageFactory.chunkNo, data);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
