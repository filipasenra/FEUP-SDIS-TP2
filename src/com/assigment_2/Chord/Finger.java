package com.assigment_2.Chord;
import java.math.BigInteger;

public class Finger {

    BigInteger start;
    SimpleNode node;

    public Finger(SimpleNode node) {
        this.node = node;
        this.start = node.id;
    }
}
