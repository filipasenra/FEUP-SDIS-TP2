package com.assigment_2.Chord;
import java.math.BigInteger;

public class Finger {

    BigInteger start;
    SimpleNode node;

    public Finger(SimpleNode node, BigInteger start) {
        this.node = node;
        this.start = start;
    }

    public BigInteger getStart() {
        return start;
    }

    public SimpleNode getNode() {
        return node;
    }
}
