package com.assigment_2.Chord;

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

        //TODO: probably improve this id
        this.id = createId(address, port, m);
    }

    public BigInteger getId() {
        return id;
    }

    private BigInteger createId(String address, int port, int m) {

        String input = address + "-" + port;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(input.getBytes());
            return new BigInteger(1,encoded).mod(BigInteger.valueOf((long) Math.pow(2, m)));
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1,"0".getBytes());
        }
    }

    //ask node to find id's successor
    public SimpleNode find_successor(BigInteger id) throws Exception {

        byte[] message = MessageFactoryChord.createMessage(3, "FIND_SUCCESSOR", id);


        SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
        client.connect();
        client.write(message);
        client.read();
        client.shutdown();

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        if (messageFactoryChord.messageType.equals("SUCCESSOR")) {

            return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port, m);

        } else {
            throw new IllegalStateException("ERROR: Didn't received a SUCCESSOR answer to FIND_SUCCESSOR");
        }

    }

    //ask node n to find id's predecessor
    protected SimpleNode find_predecessor(BigInteger id) throws Exception {

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

        } else {

            System.out.println(new String(client.getPeerAppData().array()));
            throw new IllegalStateException("ERROR: Didn't received a PREDECESSOR answer to FIND_PREDECESSOR");

        }

    }

    protected void update_finger_table(SimpleNode s, int i) throws Exception {


        byte[] message = MessageFactoryChord.createMessage(3, "UPDATE_FINGERTABLE", s.id, s.address, s.port, i);

        SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
        client.connect();
        client.write(message);
        client.read();
        client.shutdown();

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        if (!messageFactoryChord.messageType.equals("FINGERTABLE_UPDATED")) {

            throw new IllegalStateException("ERROR: Didn't received a PREDECESSOR answer to FIND_PREDECESSOR");

        }

    }

    public void set_predecessor(SimpleNode s) throws Exception {

        byte[] message = MessageFactoryChord.createMessage(3, "UPDATE_PREDECESSOR", s.id, s.address, s.port);

        SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
        client.connect();
        client.write(message);
        client.read();
        client.shutdown();

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        if (!messageFactoryChord.messageType.equals("PREDECESSOR_UPDATED")) {

            throw new IllegalStateException("ERROR: Didn't received a PREDECESSOR_UPDATED answer to UPDATE_PREDECESSOR");

        }


    }


    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}