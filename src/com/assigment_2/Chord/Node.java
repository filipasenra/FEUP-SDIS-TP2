package com.assigment_2.Chord;
import java.math.BigInteger;

public class Node extends SimpleNode implements Runnable {

    SimpleNode predecessor;
    SimpleNode successor;
    private final Finger[] fingerTable;
    int next = 0;

    public Node(String address, int port, int m) {
        super(address, port, m);

        this.fingerTable = new Finger[m];
        this.successor = this;
        this.predecessor = null;

        for(int i = 0; i < this.fingerTable.length; i++){
            this.fingerTable[i] = new Finger(this, calculate_start(i));
        }

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

            predecessor = null;
            successor = n_.find_successor(this.id);

        }

    }

    private BigInteger calculate_start(int k){
        return this.id.add(new BigInteger("2").pow(k)).mod(new BigInteger("2").pow(this.fingerTable.length));
    }

    //called periodically.
    // verifies n's immediate successor and tell the successor about n
    public void stabilize() throws Exception {

        SimpleNode x = (this.successor.id.equals(this.id)) ? this.predecessor : find_predecessor(this.successor.id);

        if (x != null && !this.id.equals(x.id) && (this.id.equals(this.successor.id) || isBetween(x.id, this.id, successor.id))) {
            this.successor = x;
            fingerTable[0].node = x;
        }

        if(!this.successor.id.equals(this.id))
            this.successor.notifyIntern(this);


    }

    //n' thinks it might be our predecessor
    public void notify(SimpleNode n_) {

        if(n_ == null)
            return;

        if(n_.id.equals(this.id))
            return;

        if(this.predecessor == null)
            this.predecessor = n_;
        else if( isBetween(n_.id, this.predecessor.id, this.id))
            this.predecessor = n_;

    }

    //called periodically. refreshes finger table entries.
    //next stores the index of the next finger to fix
    public void fix_fingers() throws Exception {

        //find last node p whose i_th finger might be n
        SimpleNode p = find_successor(this.id.add(new BigInteger("2").pow(next)));

        this.fingerTable[next] = new Finger(p, calculate_start(next));

        next++;

        if(next >= m)
            next = 0;

    }

    //called periodically. checks whether predecessor has failed
    public void check_predecessor() {

        if(false /* failed */)
            predecessor = null;

    }

    static boolean isBetween(BigInteger value, BigInteger min, BigInteger max){

        if (min.compareTo(max) > 0)
            return (value.compareTo(min) >= 0) || (value.compareTo(max) <= 0);

        return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
    }

    public void printInfo() {

        System.out.println("Node " + this.id);

        System.out.println("  PREDECESSOR: " + ((this.predecessor != null) ? this.predecessor.id : ""));
        System.out.println("  SUCCESSOR: " + this.successor.id);

        System.out.println("  FINGER TABLE of " + this.id + ":");

        for (Finger finger : this.fingerTable) {
            System.out.println("     start: " + finger.start + " Id:" + finger.node.id);
        }

        System.out.println();
    }

    @Override
    public void run() {

        try {

            this.fix_fingers();
            this.stabilize();
            this.printInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
