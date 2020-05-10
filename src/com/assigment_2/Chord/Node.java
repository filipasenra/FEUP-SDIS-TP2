package com.assigment_2.Chord;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Node {

    BigInteger id;
    String ip;
    int port;

    Node predecessor;
    Node successor;
    private Finger[] finger;

    public Node(String ip, int port, int m) {
        this.ip = ip;
        this.port = port;

        //TODO: probably improve this id
        this.id = createId(ip, port);

        this.finger = new Finger[m];

    }

    private BigInteger createId(String ip, int port) {

        String input = ip + "-" + port;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encoded = digest.digest(input.getBytes());
            return new BigInteger(1,encoded);
        } catch (Exception e) {
            e.printStackTrace();
            return new BigInteger(1,"0".getBytes());
        }
    }

    //ask node to find id's successor
    public Node find_successor(BigInteger id){

        Node n_ = find_predecessor(id);

        return n_.successor;

    }

    //ask node n to find id's predecessor
    private Node find_predecessor(BigInteger id) {

        Node n_ = this;

        while(!(isBetween(id, n_.id, n_.successor.id))){

            n_ = n_.closest_preceding_finger(id);

        }

        return n_;

    }

    //return closes finger preceding id
    private Node closest_preceding_finger(BigInteger id) {

        for(int i = this.finger.length -1; i >= 0; i--){

            if(finger[i] != null && isBetween(finger[i].node.id, this.id, id))
                return finger[i].node;

        }
        return this;
    }

    // node n joins the network
    // n_ is an arbitrary node in the network
    public void join(Node n_){

        if(n_ != null){

            init_finger_table(n_);
            update_others();
            //move keys in (predecessor, n] from successor

        } else{
            //n is the only node in the network
            for(int i = 0; i < this.finger.length; i++){
                finger[i].node = this;
            }
            this.predecessor = this;
        }

    }

    //initialize finger table of local node
    //n_ is an arbitrary node already in the network
    private void init_finger_table(Node n_) {

        this.finger[1].node = n_.find_successor(this.finger[1].start);
        predecessor = successor.predecessor;
        successor.predecessor = this;

        for(int i = 0; i < this.finger.length-1; i++){
            if(isBetween(finger[i+1].start, this.id, finger[i].node.id))
                finger[i + 1].node = finger[i].node;
            else {
                finger[i + 1].node = n_.find_successor(finger[i+1].start);
            }

        }
    }

    //update all nodes whose finger tables should refer to n
    private void update_others() {

        for(int i = 0; i < this.finger.length; i++){

            //find last node p whose i_th finger might be n
            Node p = find_predecessor(this.id.subtract(new BigInteger("2").pow(i - 1)));
            p.update_finger_table(this, i);
        }

    }

    //if s is i_th finger of n, update n's finger table with s
    private void update_finger_table(Node s, int i) {

        if( isBetween(s.id, this.id, this.finger[i].node.id)){
            finger[i].node = s;
            Node p = predecessor; //get first node preceding n
            p.update_finger_table(s, i);
        }

    }

    static boolean isBetween(BigInteger value, BigInteger min, BigInteger max){
        return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
    }
}
