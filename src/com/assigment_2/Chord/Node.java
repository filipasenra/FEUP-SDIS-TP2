package com.assigment_2.Chord;
import java.math.BigInteger;
import java.util.Arrays;

public class Node extends SimpleNode implements Runnable {

    SimpleNode predecessor;
    private final Finger[] fingerTable;

    //the node’s first r successors
    private final SimpleNode[] listOfSuccessors;
    int next = 0;

    public Node(String address, int port, int m) {
        super(address, port, m);

        this.fingerTable = new Finger[m];
        this.listOfSuccessors = new SimpleNode[m / 2 + 1];
        this.predecessor = null;

        for(int i = 0; i < this.fingerTable.length; i++){
            this.fingerTable[i] = new Finger(this, calculate_start(i));
        }

        Arrays.fill(this.listOfSuccessors, this);

    }

    public Finger[] getFingerTable() {
        return fingerTable;
    }

    //ask node to find id's successor
    public SimpleNode find_successor(BigInteger id)  {

        if(id.equals(this.id))
            return this;

        if(isBetween(id, this.id, this.getSuccessor().id))
            return this.getSuccessor();

        SimpleNode n_ = this.closest_preceding_finger(id);

        if(this.id.equals(n_.id))
            return this;

        SimpleNode successor_node = n_.find_successor(id);

        //TODO: change this to a more elegant way like the report states
        if(successor_node == null || !successor_node.is_alive())
            return this.getSuccessor().find_successor(id);

        return successor_node;

    }

    public SimpleNode getSuccessor(){
        return this.listOfSuccessors[0];
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
    public boolean join(SimpleNode n_) {

        if(n_ != null){

            SimpleNode successor_of_peer = n_.find_successor(this.id);

            if(successor_of_peer == null) {
                System.err.println("Cannot communicate with given peer");
                return false;
            }

            this.listOfSuccessors[0] = successor_of_peer;
            predecessor = null;
        }

        return true;
    }

    private BigInteger calculate_start(int k){
        return this.id.add(new BigInteger("2").pow(k)).mod(new BigInteger("2").pow(this.fingerTable.length));
    }

    //called periodically.
    // verifies n's immediate successor and tell the successor about n
    public void stabilize() throws Exception {

        int i = 0;

        while(i + 1 < this.listOfSuccessors.length) {

            SimpleNode x;

            if(!this.getSuccessor().id.equals(this.id) && !this.listOfSuccessors[i].is_alive()){
                this.listOfSuccessors[i] = this.listOfSuccessors[i+1];
            }

            if (this.listOfSuccessors[i].id.equals(this.id))
                x = this.predecessor;
            else
                x = this.listOfSuccessors[i].find_predecessor();

            if (x != null && !this.id.equals(x.id) && (this.id.equals(this.listOfSuccessors[i].id) || isBetween(x.id, this.id, this.listOfSuccessors[i].id))) {
                this.listOfSuccessors[i] = x;
                this.listOfSuccessors[i+1] = (this.listOfSuccessors[i].id.equals(this.id)) ? this.listOfSuccessors[0] : this.listOfSuccessors[i].getSuccessor();

            if(i == 0)
                fingerTable[0].node = x;
            }

            if (!this.listOfSuccessors[i].id.equals(this.id) && i == 0)
                if (!this.listOfSuccessors[i].notifyIntern(this))
                    this.listOfSuccessors[i] = this;

             i++;
        }
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
    public void fix_fingers(){

        //find last node p whose i_th finger might be n
        SimpleNode p = find_successor(this.id.add(new BigInteger("2").pow(next)));

        this.fingerTable[next] = (p == null) ? null : new Finger(p, calculate_start(next));

        next++;

        if(next >= m)
            next = 0;
    }

    //called periodically. checks whether predecessor has failed
    public void check_predecessor(){

        if(this.predecessor != null && !this.predecessor.is_alive())
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
        System.out.println("  SUCCESSOR: " + this.getSuccessor().id);

        System.out.println("  FINGER TABLE of " + this.id + ":");

        for (Finger finger : this.fingerTable) {
            if(finger == null)
                continue;

            System.out.println("     start: " + finger.start + " Id:" + finger.node.id);
        }

        System.out.println();
    }

    @Override
    public void run() {

        try {

            this.check_predecessor();
            this.fix_fingers();
            this.stabilize();
            //this.printInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
