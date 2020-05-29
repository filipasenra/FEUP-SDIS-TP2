package com.assigment_2.Chord;

import com.assigment_2.PeerClient;
import com.assigment_2.SSLEngine.SSLEngineClient;

import java.math.BigInteger;
import java.security.MessageDigest;

public class SimpleNode {

    BigInteger id;
    String address;
    int port;
    int m;

    public SimpleNode(String address, int port, int m) {
        this.address = address;
        this.port = port;
        this.m = m;

        this.id = createId(address, port, m);
    }

    public BigInteger getId() {
        return id;
    }

    private BigInteger createId(String address, int port, int m) {

        String input = address + "-" + port;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes());
            return new BigInteger(1,encoded).mod(BigInteger.valueOf((long) Math.pow(2, m)));
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1,"0".getBytes());
        }
    }

    //TODO: DILLING WITH EXCEPTION
    //ask node to get id's successor
    public SimpleNode getSuccessor() {

        byte[] message = MessageFactoryChord.createMessage(3, "GET_SUCCESSOR", this.id);

        SSLEngineClient client;

        try {
            client = new SSLEngineClient("TLSv1.2", this.address, this.port);
            client.connect();
            client.write(message);
            client.read();
            client.shutdown();
        } catch (Exception e){

            return PeerClient.getNode().find_successor(this.id);

        }

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        if (messageFactoryChord.messageType.equals("SUCCESSOR_")) {

            return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port, m);

        } else {
            throw new IllegalStateException("ERROR: Didn't received a SUCCESSOR answer to GET_SUCCESSOR");
        }
    }

    //ask node to find id's successor
    public SimpleNode find_successor(BigInteger id) {

        byte[] message = MessageFactoryChord.createMessage(3, "FIND_SUCCESSOR", id);


        SSLEngineClient client;

        try {
            client = new SSLEngineClient("TLSv1.2", this.address, this.port);

            client.connect();
            client.write(message);
            client.read();
            client.shutdown();
        } catch (Exception e) {
            return null;
        }

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        if (messageFactoryChord.messageType.equals("SUCCESSOR")) {

            return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port, m);

        } else {
            throw new IllegalStateException("ERROR: Didn't received a SUCCESSOR answer to FIND_SUCCESSOR");
        }
    }

    //ask node n to find id's predecessor
    protected SimpleNode find_predecessor() {

        try {
            byte[] message = MessageFactoryChord.createMessage(3, "FIND_PREDECESSOR", id);

            SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
            client.connect();
            client.write(message);
            client.read();
            client.shutdown();

            MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
            messageFactoryChord.parseMessage(client.getPeerAppData().array());

            if (messageFactoryChord.messageType.equals("PREDECESSOR")) {

                return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port, m);

            } else if (messageFactoryChord.messageType.equals("NOTFOUND")) {

                return null;

            } else {

                System.out.println(new String(client.getPeerAppData().array()));
                throw new IllegalStateException("ERROR: Didn't received a PREDECESSOR OR NOTFOUND answer to FIND_PREDECESSOR");

            }

        } catch (Exception e) {
            return null;
        }

    }


    protected boolean notifyIntern(SimpleNode node) {

        if(this.id.equals(node.id))
            return true;

        byte[] message = MessageFactoryChord.createMessage(3, "NOTIFY", node.id, node.address, node.port);

        try {
            SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
            client.connect();
            client.write(message);
            client.read();
            client.shutdown();

            MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
            messageFactoryChord.parseMessage(client.getPeerAppData().array());

            if (messageFactoryChord.messageType.equals("OK")) {

                return true;

            } else {

                System.out.println(new String(client.getPeerAppData().array()));
                throw new IllegalStateException("ERROR: Didn't received a OK answer to FIND_PREDECESSOR");

            }
        } catch (Exception e) {
            return false;
        }

    }

    protected boolean is_alive() {

        byte[] message = MessageFactoryChord.createMessage(3, "CHECK_UP", this.id);

        try {
        SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
        if(!client.connect())
            return false;

        client.write(message);
        client.read();
        client.shutdown();


        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        return messageFactoryChord.messageType.equals("I_AM_OK");

        } catch (Exception e) {
            return false;
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getM() {
        return m;
    }
}