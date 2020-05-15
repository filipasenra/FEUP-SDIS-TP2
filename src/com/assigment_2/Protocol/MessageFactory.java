package com.assigment_2.Protocol;

import java.math.BigInteger;
import java.util.Arrays;

public final class MessageFactory {
    static String CRLF = "\r\n";
    double version;
    String messageType;
    String senderId;
    String fileId;
    int chunkNo;
    int replicationDeg;
    byte[] data;

    public MessageFactory(){}

    public static byte[] createMessage(double version, String messageType, String senderId, String fileId, int chunkNo){
        return (version + " " + messageType + " " + senderId + " " + fileId + " " + chunkNo + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, String senderId, String fileId){
        return (version + " " + messageType + " " + senderId + " " + fileId + " " + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, String senderId, String fileId, int chunkNo, int replicationDeg){

        return (version + " " + messageType + " " + senderId + " " + fileId + " " + chunkNo + " " + replicationDeg + CRLF + CRLF).getBytes();
    }

    public static byte[] createMessage(double version, String messageType, String senderId, String fileId, int chunkNo, int replicationDeg, byte[] body){

        byte[] header = createMessage(version, messageType, senderId, fileId, chunkNo, replicationDeg);
        byte[] message = new byte[header.length + body.length];

        System.arraycopy(header,0,message,0, header.length);
        System.arraycopy(body,0,message,header.length,body.length);

        return message;
    }

    public static byte[] createMessage(double version, String messageType, String senderId, String fileId, int chunkNo, byte[] body){

        byte[] header = createMessage(version, messageType, senderId, fileId, chunkNo);
        byte[] message = new byte[header.length + body.length];

        System.arraycopy(header,0,message,0, header.length);
        System.arraycopy(body,0,message,header.length,body.length);

        return message;
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
                    this.senderId = headerArray[j];
                    break;
                case 3:
                    this.fileId = headerArray[j];
                    break;
                case 4:
                    this.chunkNo = Integer.parseInt(headerArray[j]);
                case 5:
                    this.replicationDeg = Integer.parseInt(headerArray[j]);
                    break;
                default:
                    return false;

            }

        }

        return true;
    }
}
