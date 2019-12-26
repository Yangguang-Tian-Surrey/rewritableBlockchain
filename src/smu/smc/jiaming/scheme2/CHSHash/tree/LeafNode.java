package smu.smc.jiaming.scheme2.CHSHash.tree;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.scheme2.CHSHash.DataUtils;

public class LeafNode extends Node {
    public LeafNode(Element[] xi, Element[] ri, int level, int index) {
        super(xi, ri, level, index);
    }

    public LeafNode(DataUtils.Tuple[] dummy, int level, int index) {
        super(dummy, level, index);
    }

    public LeafNode(int level, int index) {
        super(level, index);
    }

    public Node copy(){
        DataUtils.Tuple[] copyd = new DataUtils.Tuple[this.getDummy().length];
        for(int i = 0; i < copyd.length; i++){
            copyd[i] = new DataUtils.Tuple(
                    this.getDummy()[i].getX(),
                    this.getDummy()[i].getY());
        }
        return new LeafNode(copyd, this.getLevel(), this.getIndex());
    }

    public int getDepth(){
        return 1;
    }
}
