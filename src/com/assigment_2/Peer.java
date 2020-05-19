package com.assigment_2;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.ReceivedChordMessagesHandler;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.Chunk.BackUpChunk;
import com.assigment_2.Chunk.Chunk;
import com.assigment_2.SSLEngine.SSLEngineClient;
import com.assigment_2.SSLEngine.SSLEngineServer;
import com.assigment_2.Storage.FileInfo;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Peer extends SSLEngineServer implements InterfacePeer{
    private final String id;
    private final Double version;

    private final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

    /**
     * Server is designed to apply an SSL/TLS protocol and listen to an IP address and port.
     *
     * @param version    -  version of the protocol
     * @param id - unique identification of peer.
     * @param protocol    - the SSL/TLS protocol that this server will be configured to apply.
     * @param hostAddress - the IP address this server will listen to.
     * @param port        - the port this server will listen to.
     * @throws Exception
     */
    public Peer( Double version, String id, String protocol, String hostAddress, int port) throws Exception {
        super(protocol, hostAddress, port, new ReceivedChordMessagesHandler());
        this.id = id;
        this.version = version;
    }

    //TODO: Change this to the future backup file ?

    protected BigInteger generateFileId(String filename, long lastModified, String owner) {

        String input = filename + lastModified + owner;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(input.getBytes());
            return new BigInteger(1,encoded);
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1,"0".getBytes());
        }
    }

    //===========================================================

    public void backup(String filepath, int replication_degree) throws Exception {
        System.out.println("\nBACKUP SERVICE");
        System.out.println(" > File path: " + filepath);
        System.out.println(" > Replication Degree: " + replication_degree);
        System.out.println();

        File file = new File(filepath);
        byte[] fileData = Files.readAllBytes(file.toPath());
        BigInteger fileID = this.generateFileId(file.getName(), file.lastModified(), file.getParent());
        SimpleNode sn = PeerClient.getNode().find_successor(fileID);

        byte[] message = MessageFactoryChord.createMessage(3, "BACKUP", fileID, PeerClient.getNode().getAddress(), PeerClient.getNode().getPort(), fileData);
        SSLEngineClient client = new SSLEngineClient("TLSv1.2", sn.getAddress(), sn.getPort());
        client.connect();
        client.write(message);
        client.read();
        client.shutdown();

        //TODO: change this
        //exec.execute(new Thread(() -> MDB.backupFile(this.version, this.id, file_path, replication_degree)));
    }

    public void deletion(String file_path) {
        System.out.println("\nDELETION SERVICE");
        System.out.println(" > File path: " + file_path);
        System.out.println();

        //TODO: change this
        //exec.execute(new Thread(() -> MC.deleteFile(this.version, this.id, file_path)));
    }

    public void restore(String file_path) {
        System.out.println("\nRESTORE SERVICE");
        System.out.println(" > File path: " + file_path);
        System.out.println();

        //TODO: change this
        //exec.execute(new Thread(() -> MC.restoreFile(this.version, this.id, file_path)));
    }

    public void reclaim(String disk_space) {
        System.out.println("\nRECLAIM SERVICE");
        System.out.println(" > Disk space: " + disk_space);
        System.out.println();

        PeerClient.getStorage().setOverallSpace(Integer.parseInt(disk_space));
    }

    public String state() throws IOException {

        StringBuilder state = new StringBuilder();

        state.append("> Service State Info of Peer: ").append(this.id).append("\n");

        state.append("\tInitiated Backed Up Files: \n");

        if(PeerClient.getStorage().getBackedUpFiles().size() == 0) {
            state.append("\t\tNone\n");
        }

        for (Map.Entry<String, FileInfo> entryFileInfo: PeerClient.getStorage().getBackedUpFiles().entrySet()) {
            state.append("\t\tPathname: ").append(entryFileInfo.getValue().pathname).append("\n");
            state.append("\t\tBackup Service Id: ").append(entryFileInfo.getValue().id).append("\n");
            state.append("\t\tDesired Replication Degree: ").append(entryFileInfo.getValue().replication_degree).append("\n");

            state.append("\t\t> File's Chunks: \n");

            for(Map.Entry<Integer, BackUpChunk> chunkEntry: entryFileInfo.getValue().backedUpChunk.entrySet()){

                state.append("\t\t\tId: ").append(chunkEntry.getValue().getId()).append("\n");
                state.append("\t\t\t\tSize (in KBytes): ").append(chunkEntry.getValue().getData().length).append("\n");
                state.append("\t\t\t\tPerceived Replication Degree: ").append(chunkEntry.getValue().getNumStoredTimes()).append("\n");
            }

            state.append("\n");
        }

        state.append("\tStoredChunks\n");

        if(PeerClient.getStorage().getStoredChunks().size() == 0) {
            state.append("\t\tNone\n");
        }

        for (Map.Entry<Pair<String, Integer>, Chunk> chunkEntry: PeerClient.getStorage().getStoredChunks().entrySet()) {
            state.append("\t\tId: ").append(chunkEntry.getValue().getId()).append("\n");
            state.append("\t\t\tSize (in KBytes): ").append(chunkEntry.getValue().getData().length).append("\n");
            state.append("\t\t\t\tPerceived Replication Degree: ").append(chunkEntry.getValue().getNumStoredTimes()).append("\n");
        }

        return state.toString();
    }
}
