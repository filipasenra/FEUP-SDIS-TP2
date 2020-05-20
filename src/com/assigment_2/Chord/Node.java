package com.assigment_2.Chord;
import java.math.BigInteger;

public class Node extends SimpleNode {

    SimpleNode predecessor;
    SimpleNode successor;
    private final Finger[] fingerTable;

    public Node(String address, int port, int m) {
        super(address, port, m);

        this.fingerTable = new Finger[m];

    }

    public Finger[] getFingerTable() {
        return fingerTable;
    }

    //ask node to find id's successor
    public SimpleNode find_successor(BigInteger id) throws Exception {

        if(id.equals(this.id))
            return this;

        if(isBetween(id, this.id, this.successor.id))
            return this.successor;

        SimpleNode n_ = this.closest_preceding_finger(id);

        if(this.id.equals(n_.id))
            return this;

        return n_.find_successor(id);

    }

    //ask node to find id's successor
    public SimpleNode find_predecessor(BigInteger id) throws Exception {

        if(id.equals(this.id))
            return this.predecessor;

        if(isBetween(id, this.predecessor.id, this.id))
            return this.predecessor;

        SimpleNode n_ = this.closest_preceding_finger(id);

        if(this.id.equals(n_.id))
            return this;

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
    public void join(SimpleNode n_) throws Exception {

        if(n_ != null){

                init_finger_table(n_);
                update_others();
            //move keys in (predecessor, n] from successor

        } else{
            //n is the only node in the network
            for(int i = 0; i < this.fingerTable.length; i++){

                this.fingerTable[i] = new Finger(this, calculate_start(i));
            }
            this.predecessor = this;
            this.successor = this;
        }

    }

    //initialize finger table of local node
    //n_ is an arbitrary node already in the network
    private void init_finger_table(SimpleNode n_) throws Exception {

        this.successor = n_.find_successor(this.id);
        this.fingerTable[0] = new Finger(successor, calculate_start(0));
        this.predecessor = n_.find_predecessor(successor.id);
        successor.set_predecessor(this);

        for(int i = 0; i < this.fingerTable.length-1; i++){

            BigInteger start = calculate_start(i+1);
            if(isBetween(start, this.id, this.fingerTable[i].node.id))
                this.fingerTable[i + 1] = new Finger(this.fingerTable[i].node, start);
            else {
                this.fingerTable[i + 1] = new Finger(n_.find_successor(start), start);
            }

        }
    }

    private BigInteger calculate_start(int k){
        return this.id.add(new BigInteger("2").pow(k)).mod(new BigInteger("2").pow(this.fingerTable.length));
    }

    //update all nodes whose finger tables should refer to n
    private void update_others() throws Exception {

        for(int i = 0; i < this.fingerTable.length; i++){

            //find last node p whose i_th finger might be n
            SimpleNode p = find_predecessor(this.id.subtract(new BigInteger("2").pow(i)));

            p.update_finger_table(this, i);
        }

    }

    //if s is i_th finger of n, update n's finger table with s
    public void update_finger_table(SimpleNode s, int i) throws Exception {

        if( isBetween(s.id, this.id, this.fingerTable[i].node.id)){
            this.fingerTable[i].node = s;
            SimpleNode p = this.predecessor; //get first node preceding n

            p.update_finger_table(s, i);
        }

    }

    static boolean isBetween(BigInteger value, BigInteger min, BigInteger max){
        return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
    }

    public void setPredecessorObj(SimpleNode predecessor) {
        this.predecessor = predecessor;
    }
}
