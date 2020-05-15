package com.assigment_2.Chord;

import java.math.BigInteger;
import java.util.Arrays;

public final class MessageFactoryChord {
    static String CRLF = "\r\n";
    double version;
    String messageType;
    BigInteger requestId;
    String address;
    int port;
    int i;
    byte[] data;

    public MessageFactoryChord(){}

    public static byte[] createMessage(double version, String messageType, BigInteger requestId){

        return (version + " " + messageType + " " + requestId + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, BigInteger requestId, String address, int port, int i) {

        return (version + " " + messageType + " " + requestId + " " + address + " " + port + " " + i + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, BigInteger requestId, String address, int port) {

        return (version + " " + messageType + " " + requestId + " " + address + " " + port + CRLF + CRLF).getBytes();
    }

    public boolean parseMessage(byte[] message) {

        int i;
        for (i = 0; i < message.length; i++) {

            if (i + 3 > message.length)
                return false;

            if (message[i] == 0xD && message[i + 1] == 0xA && message[i + 2] == 0xD && message[i + 3] == 0xA) {
                break;
            }

        }

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
                case 4:
                    this.port = Integer.parseInt(headerArray[j]);
                    break;
                case 5:
                    this.i = Integer.parseInt(headerArray[j]);
                    break;
                default:
                    return false;

            }

        }

        return true;
    }
}
