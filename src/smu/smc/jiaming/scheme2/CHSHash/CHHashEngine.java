package smu.smc.jiaming.scheme2.CHSHash;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CHHashEngine {
    private PrimeGroup G;
    private Element key;
    private boolean hasSK = false;
    private byte[] hash;
    private MessageDigest md;

    public void reset(){
        this.G = null;
        this.clear();
        this.md = null;
    }

    public void clear(){
        this.key = null;
        this.hasSK = false;
        this.md.reset();
        this.hash = null;

    }

    public PrimeGroup getG() {
        return G;
    }

    public Element getKey() {
        return key;
    }

    public boolean isHasSK() {
        return hasSK;
    }

    public void init(PrimeGroup G){
        this.G = G;
        try {
            this.md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    //flag - true, hash; flag - false, extract
    public void setup(Element key, boolean flag){
        this.key = key;
        this.hasSK = flag;
    }

    public Element hash(Element x, Element r){
        if(this.hasSK)
            // g^(x+sk*y)
            return this.G.getG().pow(x.add(this.key.mul(r)));
        else
            // g^x * pk^y
            return this.G.getG().pow(x).mul(this.key.pow(r));
    }

    public void update(byte[] m){
        md.update(m);
    }

    public Element dofinal(byte[] m, Element r){
        md.update(m);
        return this.dofinal(r);
    }

    public Element dofinal(Element r){
        this.hash = md.digest();
        Element m = this.G.newZElementFromBytes(this.hash);
        return this.hash(m, r);
    }

    public Element collision(Element x0, Element r0, Element x1){
        if(!this.hasSK)
            return null;
        return key.invert().mul(x0.sub(x1)).add(r0);
    }

    public Element extract(Element x0, Element r0, Element x1, Element r1){
        if(x0.isEqual(x1) && r0.isEqual(r1)) {
            return null;
        }
        Element h1 = hash(x0, r0);
        Element h2 = hash(x1, r1);
        if(h1.isEqual(h2)) {
            //sk = (x0-x1)/(r1-r0)
            h1 = x0.sub(x1);
            h2 = r1.sub(r0);
            h2 = h2.invert();
            return h1.mul(h2);
        }else
            return null;
    }
}
