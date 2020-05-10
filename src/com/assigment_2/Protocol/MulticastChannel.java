package com.assigment_2.Protocol;

import com.assigment_2.PeerClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MulticastChannel implements Runnable {

    protected final static int sizeOfChunks = 64000;
    protected ScheduledThreadPoolExecutor exec;
    private int port;
    private InetAddress address;

    public MulticastChannel(String INETAddress, int port) {
        try {

            this.port = port;
            this.address = InetAddress.getByName(INETAddress);
            this.exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(byte[] message) {

        try {
            //opening DatagramSocket to send message
            DatagramSocket senderSocket = new DatagramSocket();

            DatagramPacket msgPacket = new DatagramPacket(message, message.length, this.address, this.port);
            senderSocket.send(msgPacket);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Listening
    @Override
    public void run() {

        //buffer to save the incoming bytes
        byte[] buffer = new byte[65000];

        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try {
            //Joint the Multicast group.

            MulticastSocket receiverSocket = new MulticastSocket(this.port);
            receiverSocket.joinGroup(address);

            while (true) {
                DatagramPacket msgPacket = new DatagramPacket(buffer, buffer.length);
                receiverSocket.receive(msgPacket);

                byte[] bufferCopy = Arrays.copyOf(buffer, msgPacket.getLength());

                PeerClient.getExec().execute(new ReceivedMessagesHandler(bufferCopy));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    protected String generateId(String filename, long lastModified, String owner) {

        String fileID = filename + '-' + lastModified + '-' + owner;

        return sha256(fileID);
    }

    protected String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte singleByte : hash) {
                String hex = Integer.toHexString(0xff & singleByte);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
