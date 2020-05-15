package com.assigment_2.Chord;

import com.assigment_2.Chord.Node;

import java.math.BigInteger;

public class Finger {

    BigInteger start;
    SimpleNode node;

    public Finger(SimpleNode node) {
        this.node = node;
        this.start = node.id;
    }
}
