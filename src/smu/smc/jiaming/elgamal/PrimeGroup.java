package smu.smc.jiaming.elgamal;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PrimeGroup {
    private int bitLength;
    // Zp
    private BigInteger p;
    // Gq
    private BigInteger q;
    private BigInteger g;
    private Element ge;
    private Random random;

    /**
     * p = 2*q + 1; p, q is prime number
     * if g = h^2 mod p, and g != 1
     * g is a generator
     * g^a mod p, and the order is q - 1
     * */
    public static PrimeGroup newInstance(int bitLength, Random random){
        BigInteger p,q,h,g;
        while(true) {
            p = BigInteger.probablePrime(bitLength, random);
            if(!p.isProbablePrime(100)) {
                continue;
            }
            q = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
            if(!q.isProbablePrime(100)) {
                continue;
            }
            while(true){
                h = new BigInteger(bitLength, random).mod(p);
                if(h.equals(BigInteger.ONE))
                    continue;
                g = h.pow(2).mod(p);
                if(g.equals(BigInteger.ONE))
                    continue;
                else{
                    return new PrimeGroup(bitLength, p, q, g, random);
                }
            }
        }
    }

    public static PrimeGroup newInstance(int bitLength){
        return newInstance(bitLength, new SecureRandom());
    }

    public static PrimeGroup newInstance(String params){
        Map<String, BigInteger> paramsMap = IOUtils.loadParams(params);
        if(paramsMap == null)
            return null;
        BigInteger bitlen = paramsMap.get("bitLength");
        if(bitlen == null){
            System.out.println("Can not find a number named bitLength!");
            return null;
        }
        int bitLength = bitlen.intValue();
        BigInteger p = paramsMap.get("p");
        if(p == null){
            System.out.println("Can not find a number named p!");
            return null;
        }
        BigInteger q = paramsMap.get("q");
        if(q == null){
            System.out.println("Can not find a number named q!");
            return null;
        }
        BigInteger g = paramsMap.get("g");
        if(g == null){
            System.out.println("Can not find a number named g!");
            return null;
        }
        String log = "";
        log += "-------------------------------------------------------------\n";
        log += "bitLength  " + bitLength + "\n";
        log += "p          " + p + "\n";
        log += "q          " + q + "\n";
        log += "g          " + g + "\n";
        log += "-------------------------------------------------------------\n";
        System.out.println(log);
        return new PrimeGroup(bitLength, p, q, g, new SecureRandom());
    }

    private PrimeGroup(int bitLength, BigInteger p, BigInteger q, BigInteger g, Random random){
        this.p = p;
        this.q = q;
        this.g = g;
        this.random = random;
        this.bitLength = bitLength;
        this.ge = new Element(this.bitLength, this.p, g);
    }

    public Element getG() {
        return this.ge;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getg() {
        return g;
    }

    // Group G element g^x mod p, order is q
    public Element newRandomGElement(){
        BigInteger val = new BigInteger(bitLength, random).mod(p);
        return new Element(this.bitLength, this.p, val);
    }

    // Zp element x mod q, order is q
    public Element newRandomZElement(){
        BigInteger val = new BigInteger(bitLength, random).mod(p);
        return new Element(this.bitLength, this.q, val);
    }

    public Element newGElement(){
        return new Element(this.bitLength, this.p, BigInteger.ZERO);
    }

    public Element newZElement(){
        return new Element(this.bitLength, this.q, BigInteger.ZERO);
    }

    public Element newZeroGElement(){
        return this.newGElement();
    }

    public Element newOneGElement(){
        return new Element(this.bitLength, this.p, BigInteger.ONE);
    }

    public Element newZeroZElement(){
        return this.newZElement();
    }

    public Element newOneZElement(){
        return new Element(this.bitLength, this.q, BigInteger.ONE);
    }

    public Element newGElementFromBytes(byte[] bytes){
        return new Element(this.bitLength, this.p, new BigInteger(bytes));
    }

    public Element newZElementFromBytes(byte[] bytes){
        return new Element(this.bitLength, this.q, new BigInteger(bytes));
    }

    public void save2File(String path){
        Map<String, BigInteger> params = new HashMap<>();
        params.put("bitLength", BigInteger.valueOf(this.bitLength));
        params.put("p        ", this.p);
        params.put("q        ", this.q);
        params.put("g        ", this.g);
        IOUtils.saveParams(path, params);
    }
}
