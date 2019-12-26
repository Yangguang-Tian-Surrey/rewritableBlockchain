package smu.smc.jiaming.scheme2.CHSHash;


import smu.smc.jiaming.elgamal.Element;

public class CHHashSEngine extends CHHashEngine {
    private Element z;

    @Override
    public void clear(){
        super.clear();
        this.z = null;
    }

    //flag - true, hash; flag - false, extract
    public void setup(Element key, Element z, boolean flag){
        if(flag)
            super.setup(key.div(z), flag);
        else
            super.setup(key, flag);
        this.z = z;
    }

    @Override
    public Element hash(Element x, Element r){
        if(this.isHasSK())
            // g^(x*z+(sk/z)*z*y)
            return this.getG().getG().pow(x.mul(this.z).add(this.getKey().mul(r).mul(this.z)));
        else
            // (g^z)^x * pk^y
            return this.z.pow(x).mul(this.getKey().pow(r));
    }
}
