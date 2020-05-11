package com.assigment_2.Protocol;

import com.assigment_2.Chunk.Chunk;
import com.assigment_2.Pair;
import com.assigment_2.PeerClient;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static com.assigment_2.Protocol.MulticastChannel.sizeOfChunks;

public class GetChunkThread implements Runnable {
    double version;
    String senderId;
    String fileId;
    int chunkNo;

    public GetChunkThread(double version, String fileId, int chunkNo) {
        this.version = version;
        this.senderId = PeerClient.getId();
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        ConcurrentHashMap<Pair<String, Integer>, Chunk> storedChunks = PeerClient.getStorage().getStoredChunks();
        Pair<String, Integer> pair = new Pair<>(fileId, chunkNo);
        ConcurrentHashMap<Pair<String, Integer>, byte[]> recoveredChunks = PeerClient.getStorage().getRecoveredChunks();

        if (!recoveredChunks.containsKey(pair)) {
            Chunk chunk = storedChunks.get(new Pair<>(fileId, chunkNo));
            byte[] data = new byte[sizeOfChunks];
            try {
                data = chunk.getData();
            } catch (IOException e) {
                e.printStackTrace();
            }




            byte[] message = MessageFactory.createMessage(version, "CHUNK", senderId, fileId, chunkNo, data);

            if(version == 2){
                try {

                    ServerSocket servidor = new ServerSocket(0);
                    servidor.setSoTimeout(400);

                    int port = servidor.getLocalPort();
                    String host = InetAddress.getLocalHost().getHostName();

                    String dataPort = port + " " + host;

                    byte[] messagePort = MessageFactory.createMessage(version, "PORT", senderId, fileId, chunkNo, dataPort.getBytes());
                    sendTCPMessage(messagePort);

                    //Waiting for peer who requested chunk to accept
                    Socket cliente = servidor.accept();

                    System.out.println("\t > ENHANCEMENT SENDING MESSAGE THROUGH TCP " + PeerClient.getId() + " : " + version + " CHUNK " + senderId + " " + fileId + " " + chunkNo);
                    ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());

                    saida.flush();
                    saida.writeObject(data);

                    saida.close();
                    cliente.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                System.out.println(" > SENDING MESSAGE: " + version + " CHUNK " + senderId + " " + fileId + " " + chunkNo);

                //TODO: change this
                //PeerClient.getExec().execute(new Thread(() -> PeerClient.getMDR().sendMessage(message)));
            }
        }
    }

    public void sendTCPMessage(byte[] message){

        System.out.println(" > SENDING MESSAGE: " + version + " PORT " + senderId + " " + fileId + " " + chunkNo);

        //TODO: change this
        //PeerClient.getExec().execute(new Thread(() -> PeerClient.getMDR().sendMessage(message)));

    }
}
