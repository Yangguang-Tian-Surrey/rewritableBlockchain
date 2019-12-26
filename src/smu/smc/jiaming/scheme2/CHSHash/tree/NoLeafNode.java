package smu.smc.jiaming.scheme2.CHSHash.tree;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.scheme2.CHSHash.DataUtils;

public class NoLeafNode extends Node {
    private Node[] children;

    public NoLeafNode(Element[] xi, Element[] ri, int level, int index, Node[] children) {
        super(xi, ri, level, index);
        this.children = children;
    }

    public NoLeafNode(DataUtils.Tuple[] dummy, int level, int index, Node[] children) {
        super(dummy, level, index);
        this.children = children;
    }

    public NoLeafNode(int level, int index) {
        super(level, index);
    }

    public Node copy(){
        DataUtils.Tuple[] copyd = new DataUtils.Tuple[this.getDummy().length];
        for(int i = 0; i < copyd.length; i++){
            copyd[i] = new DataUtils.Tuple(
                    this.getDummy()[i].getX(),
                    this.getDummy()[i].getY());
        }
        return new NoLeafNode(copyd, this.getLevel(), this.getIndex(), null);
    }

    public int getDepth(){
        int depth = 0;
        int temp;
        for(int i = 0 ; i < this.children.length; i++){
            temp = this.children[i].getDepth();
            if(temp > depth)
                depth = temp;
        }
        return depth + 1;
    }

    public Node[] getChildren() {
        return children;
    }

    public void setChildren(Node[] children) {
        this.children = children;
    }
}
