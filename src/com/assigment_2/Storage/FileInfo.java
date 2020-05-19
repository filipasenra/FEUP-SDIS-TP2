package com.assigment_2.Storage;

import java.io.Serializable;
import java.math.BigInteger;

public class FileInfo implements Serializable {

    public String pathname;
    public BigInteger id;
    public int replication_degree;

    public FileInfo(String pathname, BigInteger id, int replication_degree) {
        this.pathname = pathname;
        this.id = id;
        this.replication_degree = replication_degree;
    }
}
