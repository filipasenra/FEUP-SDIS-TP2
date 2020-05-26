package com.assigment_2.Chord;

import java.math.BigInteger;
import java.util.Arrays;

public final class MessageFactoryChord {
    public static String CRLF = "\r\n";
    public double version;
    public String messageType;
    public BigInteger requestId;
    public String address;
    public int port;
    public int repDegree;
    public int chunkNo;
    public byte[] data;

    public MessageFactoryChord(){}

    public static byte[] createMessage(double version, String messageType, BigInteger requestId, String address, int port, int repDegree, int chunkNo, byte[] body) {
        byte[] header = createMessage(version, messageType, requestId, address, port, repDegree, chunkNo);
        byte[] message = new byte[header.length + body.length];

        System.arraycopy(header,0,message,0, header.length);
        System.arraycopy(body,0,message,header.length,body.length);

        return message;
    }

    public BigInteger getRequestId() {
        return requestId;
    }

    public static byte[] createMessage(double version, String messageType, BigInteger requestId){

        return (version + " " + messageType + " " + requestId + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, BigInteger requestId, String address, int port, int repDegree, int chunkNo) {

        return (version + " " + messageType + " " + requestId + " " + address + " " + port + " " + repDegree + " " + chunkNo + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, BigInteger requestId, String address, int port) {

        return (version + " " + messageType + " " + requestId + " " + address + " " + port + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType){

        return (version + " " + messageType + CRLF + CRLF).getBytes();
    }

    public boolean parseMessage(byte[] message) {


        int i;
        for (i = 0; i < (message.length - 3); i++) {
            if (i + 3 > message.length)
                return false;

            if (message[i] == 0xD && message[i + 1] == 0xA && message[i + 2] == 0xD && message[i + 3] == 0xA) {
                break;
            }

        }

        if(i + 4 < message.length)
            this.data = Arrays.copyOfRange(message, i + 4, message.length);

        byte[] header = Arrays.copyOfRange(message, 0, i);

        String headerString = new String(header);
        String trimmedString = headerString.trim();
        String[] headerArray = trimmedString.split(" ");

        for(int j = 0; j<headerArray.length; j++) {

            switch (j) {
                case 0:
                    this.version = Double.parseDouble(headerArray[j]);
                    break;
                case 1:
                    this.messageType = headerArray[j];
                    break;
                case 2:
                    this.requestId = new BigInteger(headerArray[j]);
                    break;
                case 3:
                    this.address = headerArray[j];
                    break;
                case 4:
                    this.port = Integer.parseInt(headerArray[j]);
                    break;
                case 5:
                    this.repDegree = Integer.parseInt(headerArray[j]);
                    break;
                case 6:
                    this.chunkNo = Integer.parseInt(headerArray[j]);
                    break;

            }

        }

        return true;
    }
}

