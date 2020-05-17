package com.assigment_2.Chord;

import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.MessagesHandler;

import javax.net.ssl.SSLEngine;
import java.math.BigInteger;
import java.nio.channels.SocketChannel;

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

}
