package smu.smc.jiaming.scheme2.CHSHash;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme2.CHSHash.tree.Node;

public class KeyGen {
    private PrimeGroup G;

    public KeyGen(PrimeGroup G){
        this.G = G;
    }

    public KeyPair genKeyPair(){
        // sk random
        Element sk = G.newRandomZElement();
        // pk = g^sk
        Element pk = G.getG().pow(sk);
        return new KeyPair(sk, pk);
    }

    public CHHashInTree.AssertionKeyPair genAssertionKeyPair(Node node, Element k, Element ek, boolean flag){
        Element[] y = new Element[node.getDummy().length];
        Element g = this.G.getG();
        Element pk, g_esk_sk;
        if(flag){
            pk = k;
            g_esk_sk = pk.pow(ek);
        }else{
            pk = g.pow(k);
            g_esk_sk = ek.pow(k);
        }
        for(int i = 0; i < y.length; i++){
            // y[i] = g^xi * g^(s*ri)
            y[i] = g.pow(node.getDummy()[i].getX()).mul(pk.pow(node.getDummy()[i].getY()));
        }
        // z = hash(y)
        Element z = HashUtils.hashEles(y, this.G.newZElement());
        // ask = hash(g^(s*esk))
        Element ask = HashUtils.hashEles(g_esk_sk, this.G.newZElement());
        // gks = g^ask
        Element gsk = g.pow(ask);
        return new CHHashInTree.AssertionKeyPair(ask, gsk, z);
    }

    public CandidateMessage genCandidateMessage(Element m, Element pk, KeyPair ek){
        Element z = this.G.newRandomZElement();
        Element zc = this.G.newGElement().set(z);
        // zc = z*g^(s*esk)
        zc = pk.pow(ek.getSk()).mul(zc);
        return new CandidateMessage(m, ek.getPk(), zc);
    }

    public static class KeyPair{
        private Element sk;
        private Element pk;

        public KeyPair(Element sk, Element pk) {
            this.sk = sk;
            this.pk = pk;
        }

        public Element getSk() {
            return sk;
        }

        public Element getPk() {
            return pk;
        }
    }


    public static class CandidateMessage {
        private Element m;
        private DataUtils.Tuple C;

        public CandidateMessage(Element m, Element epk, Element zc) {
            this.m = m;
            this.C = new DataUtils.Tuple(epk, zc);
        }

        public CandidateMessage(Element m, DataUtils.Tuple c) {
            this.m = m;
            C = c;
        }

        public Element getM() {
            return m;
        }

        public DataUtils.Tuple getC() {
            return C;
        }
    }
}
