package com.assigment_2.Storage;

import com.assigment_2.Chunk.BackUpChunk;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class FileInfo implements Serializable {

    public String pathname;
    public String id;
    public int replication_degree;

    public final ConcurrentHashMap<Integer, BackUpChunk> backedUpChunk = new ConcurrentHashMap<>();

    public FileInfo(String pathname, String id, int replication_degree) {
        this.pathname = pathname;
        this.id = id;
        this.replication_degree = replication_degree;
    }
}
