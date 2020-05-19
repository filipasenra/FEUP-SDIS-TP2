package com.assigment_2;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.Executors;
import com.assigment_2.Protocol.Backup;
import com.assigment_2.Storage.FileInfo;
import com.assigment_2.SSLEngine.SSLEngineServer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import com.assigment_2.Chord.ReceivedChordMessagesHandler;

public class Peer extends SSLEngineServer implements InterfacePeer {
    private final String id;
    private final Double version;
    private final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

    /**
     * Server is designed to apply an SSL/TLS protocol and listen to an IP address and port.
     *
     * @param version     -  version of the protocol
     * @param id          - unique identification of peer.
     * @param protocol    - the SSL/TLS protocol that this server will be configured to apply.
     * @param hostAddress - the IP address this server will listen to.
     * @param port        - the port this server will listen to.
     * @throws Exception
     */
    public Peer(Double version, String id, String protocol, String hostAddress, int port) throws Exception {
        super(protocol, hostAddress, port, new ReceivedChordMessagesHandler());
        this.id = id;
        this.version = version;
    }

    public void backup(String filepath, int replicationDegree) throws Exception {
        System.out.println("\nBACKUP SERVICE");
        System.out.println(" > File path: " + filepath);
        System.out.println(" > Replication Degree: " + replicationDegree);
        System.out.println();

        File file = new File(filepath);
        BigInteger fileId = Backup.generateFileId(file.getName(), file.lastModified(), file.getParent());
        FileInfo fileInfo = new FileInfo(filepath, fileId, replicationDegree);
        byte[] fileData = Files.readAllBytes(file.toPath());

       if (PeerClient.getStorage().addBackedUpFile(fileId, fileInfo))
           exec.execute(new Backup(fileId, fileData, replicationDegree));

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

        //PeerClient.getStorage().setOverallSpace(Integer.parseInt(disk_space));
    }

    public String state() {

        StringBuilder state = new StringBuilder();

        state.append("> Service State Info of Peer: ").append(this.id).append("\n");

        state.append("\tInitiated Backed Up Files: \n");

        if (PeerClient.getStorage().getBackedUpFiles().size() == 0) {
            state.append("\t\tNone\n");
        }

        for (Map.Entry<BigInteger, FileInfo> entryFileInfo : PeerClient.getStorage().getBackedUpFiles().entrySet()) {
            state.append("\t\tPathname: ").append(entryFileInfo.getValue().pathname).append("\n");
            state.append("\t\tcom.assigment_2.Protocol.Backup Service Id: ").append(entryFileInfo.getValue().id).append("\n");
            state.append("\t\tDesired Replication Degree: ").append(entryFileInfo.getValue().replication_degree).append("\n");

            /*state.append("\t\t> File's Chunks: \n");

            for (Map.Entry<Integer, BackUpChunk> chunkEntry : entryFileInfo.getValue().backedUpChunk.entrySet()) {

                state.append("\t\t\tId: ").append(chunkEntry.getValue().getId()).append("\n");
                state.append("\t\t\t\tSize (in KBytes): ").append(chunkEntry.getValue().getData().length).append("\n");
                state.append("\t\t\t\tPerceived Replication Degree: ").append(chunkEntry.getValue().getNumStoredTimes()).append("\n");
            }*/

            state.append("\n");
        }

        /*state.append("\tStoredChunks\n");

        if (PeerClient.getStorage().getStoredChunks().size() == 0) {
            state.append("\t\tNone\n");
        }

        for (Map.Entry<Pair<String, Integer>, Chunk> chunkEntry : PeerClient.getStorage().getStoredChunks().entrySet()) {
            state.append("\t\tId: ").append(chunkEntry.getValue().getId()).append("\n");
            state.append("\t\t\tSize (in KBytes): ").append(chunkEntry.getValue().getData().length).append("\n");
            state.append("\t\t\t\tPerceived Replication Degree: ").append(chunkEntry.getValue().getNumStoredTimes()).append("\n");
        }*/

        return state.toString();
    }
}
