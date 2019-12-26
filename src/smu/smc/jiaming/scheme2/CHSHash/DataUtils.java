package smu.smc.jiaming.scheme2.CHSHash;

import smu.smc.jiaming.elgamal.Element;

public class DataUtils {
    public static class Tuple{
        private Element x;
        private Element y;

        public Tuple(Element x, Element y) {
            this.x = x;
            this.y = y;
        }

        public Element getX() {
            return x;
        }

        public Element getY() {
            return y;
        }

        public boolean isEqual(Tuple tuple){
            if(tuple == null)
                return false;
            if(!this.x.isEqual(tuple.getX()))
                return false;
            if(!this.y.isEqual(tuple.getY()))
                return false;
            return true;
        }
    }
}
