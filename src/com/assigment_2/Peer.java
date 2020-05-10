package com.assigment_2;

import com.assigment_2.Chunk.BackUpChunk;
import com.assigment_2.Chunk.Chunk;
import com.assigment_2.Protocol.*;
import com.assigment_2.Storage.FileInfo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Peer implements InterfacePeer {
    private final String id;
    private final Double version;

    private final MulticastBackupChannel MDB;
    private final MulticastControlChannel MC;
    private MulticastDataRecoveryChannel MDR;
    private final ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

    public Peer(Double version, String id, MulticastControlChannel MC, MulticastBackupChannel MDB, MulticastDataRecoveryChannel MDR) {
        this.version = version;
        this.id = id;
        this.MDB = MDB;
        this.MC = MC;
        this.MDR = MDR;
    }

    @Override
    public void backup(String file_path, int replication_degree) {
        System.out.println("\nBACKUP SERVICE");
        System.out.println(" > File path: " + file_path);
        System.out.println(" > Replication Degree: " + replication_degree);
        System.out.println();

        exec.execute(new Thread(() -> MDB.backupFile(this.version, this.id, file_path, replication_degree)));
    }

    @Override
    public void deletion(String file_path) {
        System.out.println("\nDELETION SERVICE");
        System.out.println(" > File path: " + file_path);
        System.out.println();

        exec.execute(new Thread(() -> MC.deleteFile(this.version, this.id, file_path)));
    }

    @Override
    public void restore(String file_path) {
        System.out.println("\nRESTORE SERVICE");
        System.out.println(" > File path: " + file_path);
        System.out.println();

        exec.execute(new Thread(() -> MC.restoreFile(this.version, this.id, file_path)));
    }

    @Override
    public void reclaim(String disk_space) {
        System.out.println("\nRECLAIM SERVICE");
        System.out.println(" > Disk space: " + disk_space);
        System.out.println();

        PeerClient.getStorage().setOverallSpace(Integer.parseInt(disk_space));
    }

    @Override
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
