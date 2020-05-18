package com.assigment_2.Chord;

import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.MessagesHandler;

import javax.net.ssl.SSLEngine;
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
                case "UPDATE_PREDECESSOR":
                    manageUpdatePredecessor();
                    break;
                case "UPDATE_FINGERTABLE":
                    manageUpdateFingerTable();
                    break;
                case "BACKUP":
                    manageBackup();
                    break;
                default:
                    System.err.println("NOT A VALID PROTOCOL: " + this.messageFactoryChord.messageType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manageUpdateFingerTable() throws Exception {

        PeerClient.getNode().update_finger_table(new SimpleNode(messageFactoryChord.address, messageFactoryChord.port), messageFactoryChord.i_finger_table);

        byte[] message = MessageFactoryChord.createMessage(3, "FINGERTABLE_UPDATED");
        PeerClient.getObj().write(socketChannel, engine, message);
    }

    private void manageUpdatePredecessor() throws Exception {

        PeerClient.getNode().setPredecessorObj(new SimpleNode(messageFactoryChord.address, messageFactoryChord.port));

        byte[] message = MessageFactoryChord.createMessage(3, "PREDECESSOR_UPDATED");
        PeerClient.getObj().write(socketChannel, engine, message);

    }

    private void manageFindPredecessor() throws Exception {

        BigInteger request_id = messageFactoryChord.getRequestId();
        SimpleNode node = PeerClient.getNode().find_predecessor(request_id);

        byte[] message = MessageFactoryChord.createMessage(3, "PREDECESSOR", request_id, node.getAddress(), node.getPort());
        PeerClient.getObj().write(socketChannel, engine, message);

    }

    private void manageFindSuccessor() throws Exception {

        BigInteger request_id = messageFactoryChord.getRequestId();

        SimpleNode node = PeerClient.getNode().find_successor(request_id);

        byte[] message = MessageFactoryChord.createMessage(3, "SUCCESSOR", request_id, node.getAddress(), node.getPort());
        PeerClient.getObj().write(socketChannel, engine, message);

    }

    private void manageBackup() throws Exception {

        String filename = PeerClient.getId() + "/" + messageFactoryChord.requestId;

        File file = new File(filename);
        try {

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(messageFactoryChord.data);

                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
