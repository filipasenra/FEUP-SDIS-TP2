package com.assigment_2.Chord;

import com.assigment_2.Peer;
import com.assigment_2.PeerClient;
import com.assigment_2.Protocol.Backup;
import com.assigment_2.SSLEngine.MessagesHandler;

import javax.net.ssl.SSLEngine;
import java.awt.desktop.SystemSleepEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ReceivedChordMessagesHandler implements MessagesHandler {
    MessageFactoryChord messageFactoryChord;
    SocketChannel socketChannel;
    SSLEngine engine;
    byte[] message;

    public void run(SocketChannel socketChannel, SSLEngine engine, byte[] message) {
        this.messageFactoryChord = new MessageFactoryChord();
        this.socketChannel = socketChannel;
        this.engine = engine;
        this.message = message;


        if (!messageFactoryChord.parseMessage(this.message)) {
            return;
        }

        try {
            switch (messageFactoryChord.messageType) {
                case "FIND_SUCCESSOR":
                    manageFindSuccessor();
                    break;
                case "FIND_PREDECESSOR":
                    manageFindPredecessor();
                    break;
                case "NOTIFY":
                    manageNotify();
                    break;
                case "RECEIVED_BACKUP":
                    System.out.println("RECEIVED A CONFIRMATION OF BACKUP!");
                    break;
                case "BACKUP":
                    manageBackup();
                    break;
                case "DELETE":
                    manageDelete();
                    break;
                case "GET_SUCCESSOR":
                    manageGetSuccessor();
                    break;
                default:
                    System.err.println("NOT A VALID PROTOCOL: " + this.messageFactoryChord.messageType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manageNotify() throws Exception {

        PeerClient.getNode().notify(new SimpleNode(messageFactoryChord.address, messageFactoryChord.port, PeerClient.getNode().getM()));

        byte[] message = MessageFactoryChord.createMessage(3, "OK");

        PeerClient.getObj().write(socketChannel, engine, message);
    }

    private void manageFindPredecessor() throws Exception {

        BigInteger request_id = messageFactoryChord.getRequestId();
        SimpleNode predecessor = PeerClient.getNode().predecessor;

        byte[] message;
        if (predecessor != null)
            message = MessageFactoryChord.createMessage(3, "PREDECESSOR", request_id, predecessor.getAddress(), predecessor.getPort());
        else
            message = MessageFactoryChord.createMessage(3, "NOTFOUND");

        PeerClient.getObj().write(socketChannel, engine, message);

    }

    private void manageFindSuccessor() throws Exception {

        BigInteger request_id = messageFactoryChord.getRequestId();

        SimpleNode node = PeerClient.getNode().find_successor(request_id);

        byte[] message = MessageFactoryChord.createMessage(3, "SUCCESSOR", request_id, node.getAddress(), node.getPort());
        PeerClient.getObj().write(socketChannel, engine, message);

    }

    private void manageGetSuccessor() throws Exception {
        BigInteger request_id = messageFactoryChord.getRequestId();

        SimpleNode node = PeerClient.getNode().getSuccessor();

        byte[] message = MessageFactoryChord.createMessage(3, "SUCCESSOR_", request_id, node.getAddress(), node.getPort());
        PeerClient.getObj().write(socketChannel, engine, message);
    }

    private void manageDelete() throws Exception {

        if (PeerClient.getStorage().getStoredFiles().remove(messageFactoryChord.requestId)) {
            File file = new File(PeerClient.getId() + "/" + messageFactoryChord.requestId);
            file.delete();

            byte[] message = MessageFactoryChord.createMessage(3, "DELETED", messageFactoryChord.requestId, messageFactoryChord.address, messageFactoryChord.port);
            PeerClient.getObj().write(socketChannel, engine, message);
        } else {
            byte[] message = MessageFactoryChord.createMessage(3, "NOT_DELETED", messageFactoryChord.requestId, messageFactoryChord.address, messageFactoryChord.port);
            PeerClient.getObj().write(socketChannel, engine, message);
        }
    }

    private void manageBackup() throws Exception {

        PeerClient.getStorage().addToBuffer(messageFactoryChord.requestId, messageFactoryChord.data, messageFactoryChord.chunkNo);

        //Quando recebeu o ultimo pedaço de informação
        if (messageFactoryChord.data.length < Backup.backupDataSize) {

            String filename = PeerClient.getId() + "/" + messageFactoryChord.requestId;

            File file = new File(filename);
            try {
                if (file.exists()) {
                    file.delete();
                }

                file.getParentFile().mkdirs();
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream(filename);

                for (byte[] data : PeerClient.getStorage().getBufferFromFile(messageFactoryChord.requestId)) {
                    if (data == null) {

                        byte[] message = MessageFactoryChord.createMessage(3, "BACKUP_FAILED", PeerClient.getNode().id);
                        PeerClient.getObj().write(socketChannel, engine, message);

                        fos.close();
                        file.delete();

                        PeerClient.getStorage().removeBufferedFile(messageFactoryChord.requestId);

                        System.err.println("An error occurred while receiving file data and some data is missing.");
                        return;
                    }
                    fos.write(data);
                }

                fos.close();

                PeerClient.getStorage().addStoredFile(messageFactoryChord.requestId);
                PeerClient.getStorage().removeBufferedFile(messageFactoryChord.requestId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        byte[] message = MessageFactoryChord.createMessage(3, "BACKUP_COMPLETE", PeerClient.getNode().id);
        PeerClient.getObj().write(socketChannel, engine, message);


    }

}
