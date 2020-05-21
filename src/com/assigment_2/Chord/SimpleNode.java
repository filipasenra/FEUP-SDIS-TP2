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
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
    protected SimpleNode find_predecessor() throws Exception {

        byte[] message = MessageFactoryChord.createMessage(3, "FIND_PREDECESSOR", id);

        SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
        client.connect();
        client.write(message);
        client.read();
        client.shutdown();

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        System.out.println("MESSAGE: " + new String(client.getPeerAppData().array()));

        if (messageFactoryChord.messageType.equals("PREDECESSOR")) {

            return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port, m);

        } else if(messageFactoryChord.messageType.equals("NOTFOUND")) {

            return null;

        } else {

            System.out.println(new String(client.getPeerAppData().array()));
            throw new IllegalStateException("ERROR: Didn't received a PREDECESSOR OR NOTFOUND answer to FIND_PREDECESSOR");

        }

    }


    protected void notifyIntern(SimpleNode node) throws Exception {

        if(this.id.equals(node.id))
            return;

        byte[] message = MessageFactoryChord.createMessage(3, "NOTIFY", node.id, node.address, node.port);

        SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
        client.connect();
        client.write(message);
        client.read();
        client.shutdown();

        MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
        messageFactoryChord.parseMessage(client.getPeerAppData().array());

        if (messageFactoryChord.messageType.equals("OK")) {

            return;

        } else {

            System.out.println(new String(client.getPeerAppData().array()));
            throw new IllegalStateException("ERROR: Didn't received a OK answer to FIND_PREDECESSOR");

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