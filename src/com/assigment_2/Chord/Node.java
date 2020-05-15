package com.assigment_2.Chord;
import java.math.BigInteger;

public class Node extends SimpleNode {

    SimpleNode predecessor;
    SimpleNode successor;
    private Finger[] fingerTable;

    public Node(String address, int port, int m) {
        super(address, port);

        this.fingerTable = new Finger[m];

    }


    //ask node to find id's successor
    public SimpleNode find_successor(BigInteger id){

        if(id.equals(this.id))
            return this;

        if(isBetween(id, this.id, this.successor.id))
            return this.successor;


        SimpleNode n_ = this.closest_preceding_finger(id);

        if(id.equals(n_.id))
            return n_;

        return n_.find_successor(id);

    }

    //ask node to find id's successor
    public SimpleNode find_predecessor(BigInteger id){

        if(id.equals(this.id))
            return this.predecessor;

        if(isBetween(id, this.predecessor.id, this.id))
            return this.predecessor;

        SimpleNode n_ = this.closest_preceding_finger(id);

        if(id.equals(n_.id))
            return n_;

        return n_.find_predecessor(id);

    }

    //return closes finger preceding id
    private SimpleNode closest_preceding_finger(BigInteger id) {

        for(int i = this.fingerTable.length -1; i >= 0; i--){

            if(this.fingerTable[i] != null && isBetween(this.fingerTable[i].node.id, this.id, id))
                return this.fingerTable[i].node;

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
            for(int i = 0; i < this.fingerTable.length; i++){
                this.fingerTable[i].node = this;
            }
            this.predecessor = this;
        }

    }

    //initialize finger table of local node
    //n_ is an arbitrary node already in the network
    private void init_finger_table(Node n_) {

        this.fingerTable[1].node = n_.find_successor(this.fingerTable[1].start);
        this.predecessor = this.find_predecessor(this.successor.id);

        //TODO: send message for successor to make this node its predecessor
        //successor.predecessor = this;

        for(int i = 0; i < this.fingerTable.length-1; i++){
            if(isBetween(this.fingerTable[i+1].start, this.id, this.fingerTable[i].node.id))
                this.fingerTable[i + 1].node = this.fingerTable[i].node;
            else {
                this.fingerTable[i + 1].node = n_.find_successor(this.fingerTable[i+1].start);
            }

        }
    }

    //update all nodes whose finger tables should refer to n
    private void update_others() {

        for(int i = 0; i < this.fingerTable.length; i++){

            //find last node p whose i_th finger might be n
            SimpleNode p = find_predecessor(this.id.subtract(new BigInteger("2").pow(i - 1)));

            p.update_finger_table(this, i);
        }

    }

    //if s is i_th finger of n, update n's finger table with s
    public boolean update_finger_table(SimpleNode s, int i) {

        if( isBetween(s.id, this.id, this.fingerTable[i].node.id)){
            this.fingerTable[i].node = s;
            SimpleNode p = this.predecessor; //get first node preceding n

            return p.update_finger_table(s, i);
        }

        return false;

    }

    static boolean isBetween(BigInteger value, BigInteger min, BigInteger max){
        return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
    }
}
