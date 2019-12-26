package smu.smc.jiaming.scheme2.CHSHash.tree;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.scheme2.CHSHash.DataUtils;

public abstract class Node {
    private DataUtils.Tuple[] dummy;
    private int level;
    private int index;
    private Element id;

    public Node(int level, int index){
        this.level = level;
        this.index = index;
    }

    public Node(Element[] xi, Element[] ri, int level, int index) {
        this.dummy = new DataUtils.Tuple[xi.length];
        for(int i = 0; i < xi.length; i++){
            this.dummy[i] = new DataUtils.Tuple(xi[i], ri[i]);
        }
        this.level = level;
        this.index = index;
    }

    public Node(DataUtils.Tuple[] dummy, int level, int index) {
        this.dummy = dummy;
        this.level = level;
        this.index = index;
    }

    public boolean isValueEqual(Node node){
        if(node == null) return false;
        DataUtils.Tuple[] dummy = node.getDummy();
        if(this.level != node.level || this.index != node.index)
            return false;
        for(int i = 0 ; i < dummy.length; i++){
            if(!this.dummy[i].getX().isEqual(dummy[i].getX())
                    || !this.dummy[i].getY().isEqual(dummy[i].getY()))
                return false;
        }
        return true;
    }

    public abstract Node copy();

    public int getDepth(){
        return 0;
    }

    public DataUtils.Tuple[] getDummy() {
        return dummy;
    }

    public int getLevel() {
        return level;
    }

    public int getIndex() {
        return index;
    }

    public void setDummy(DataUtils.Tuple[] dummy) {
        this.dummy = dummy;
    }

    public Element getId() {
        return id;
    }

    public void setId(Element id) {
        this.id = id;
    }
}
