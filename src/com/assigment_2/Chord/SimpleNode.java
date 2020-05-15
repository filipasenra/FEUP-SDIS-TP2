package com.assigment_2.Chord;

import com.assigment_2.SSLEngine.SSLEngineClient;

import java.math.BigInteger;
import java.security.MessageDigest;

public class SimpleNode {

    BigInteger id;
    String address;
    int port;

    public SimpleNode(String address, int port) {
        this.address = address;
        this.port = port;

        //TODO: probably improve this id
        this.id = createId(address, port);
    }


    private BigInteger createId(String address, int port) {

        String input = address + "-" + port;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(input.getBytes());
            return new BigInteger(1,encoded);
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1,"0".getBytes());
        }
    }

    //ask node to find id's successor
    public SimpleNode find_successor(BigInteger id){

        byte[] message = MessageFactoryChord.createMessage(3, "FINDSUCESSOR", id);

        try {
            SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
            client.connect();
            client.write(message);
            client.read();

            MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
            messageFactoryChord.parseMessage(client.getPeerAppData().array());

            if(messageFactoryChord.messageType.equals("SUCCESSOR")){

                return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port);

            } else {

                System.err.println("ERROR: Didn't received a SUCCESSOR answer to FINDSUCESSOR");
                return null;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    //ask node n to find id's predecessor
    protected SimpleNode find_predecessor(BigInteger id) {


        byte[] message = MessageFactoryChord.createMessage(3, "FINDPREDECESSOR", id);

        try {
            SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
            client.connect();
            client.write(message);
            client.read();

            MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
            messageFactoryChord.parseMessage(client.getPeerAppData().array());

            if(messageFactoryChord.messageType.equals("PREDECESSOR")){

                return new SimpleNode(messageFactoryChord.address, messageFactoryChord.port);

            } else {

                System.err.println("ERROR: Didn't received a PREDECESSOR answer to FINDPREDECESSOR");
                return null;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    protected boolean update_finger_table(SimpleNode s, int i) {


        byte[] message = MessageFactoryChord.createMessage(3, "UPDATEFINGERTABLE", s.id, s.address, s.port, i);

        try {
            SSLEngineClient client = new SSLEngineClient("TLSv1.2", this.address, this.port);
            client.connect();
            client.write(message);
            client.read();

            MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
            messageFactoryChord.parseMessage(client.getPeerAppData().array());

            if(messageFactoryChord.messageType.equals("FINGERTABLEUPDATED")){

                return true;
            } else {

                System.err.println("ERROR: Didn't received a FINGERTABLEUPDATED answer to UPDATEFINGERTABLE");
                return false;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }


}
