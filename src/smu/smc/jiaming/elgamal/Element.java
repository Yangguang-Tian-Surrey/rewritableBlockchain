package smu.smc.jiaming.elgamal;

import java.math.BigInteger;

public class Element {
    private int bitLength;
    private BigInteger order;
    private BigInteger val;

    public Element(BigInteger order){
        this.order = order;
        this.val = BigInteger.ONE;
    }

    public Element(int bitLength, BigInteger order, BigInteger val){
        this.bitLength = bitLength;
        this.order = order;
        this.val = val.mod(order);
    }

    public void setVal(BigInteger val){
        this.val = val.mod(this.order);
    }

    public Element add(Element a){
        if(!this.order.equals(a.order)){
            throw new RuntimeException("can not add an element with a different order");
        }
        BigInteger val = this.val.add(a.val).mod(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element add(BigInteger a){
        BigInteger val = this.val.add(a).mod(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element sub(Element a){
        if(!this.order.equals(a.order)){
            throw new RuntimeException("can not sub an element with a different order");
        }
        BigInteger val = this.val.subtract(a.val).mod(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element mul(Element a){
        if(!this.order.equals(a.order)){
            throw new RuntimeException("can not mul an element with a different order");
        }
        BigInteger val = this.val.multiply(a.val).mod(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element invert(){
        BigInteger val = this.val.modInverse(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element div(Element a){
        if(!this.order.equals(a.order)){
            throw new RuntimeException("can not div an element with a different order");
        }
        BigInteger val = this.val.multiply(a.invert().val).mod(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element negate(){
        BigInteger val = this.val.negate().modInverse(this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element pow(Element a){
        BigInteger val = this.val.modPow(a.val, this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element pow(long a){
        BigInteger val = this.val.modPow(BigInteger.valueOf(a), this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public Element pow(BigInteger a){
        BigInteger val = this.val.modPow(a, this.order);
        return new Element(this.bitLength, this.order, val);
    }

    public boolean isEqual(Element a){
        if(a == null) return false;
        //if(!a.order.equals(this.order)) return false;
        if(!a.val.equals(this.val)) return false;
        return true;
    }

    public Element set(Element a){
        return new Element(this.bitLength, this.order, a.val);
    }

    public byte[] toBytes(){
        return this.val.toByteArray();
    }

    public void setBytes(byte[] bytes){
        this.val = new BigInteger(bytes).mod(this.order);
    }

    public byte[] xor(Element a){
        if(a == null)
            return null;
        return this.val.xor(a.val).toByteArray();
    }

    /**
     * it is possible to fail!
     * for exmple, when p=11, 4 xor 8 mod p = 1; 1 xor 4 mod p = 5 != 8
     * */
   /* public Element xore(Element a){
        BigInteger val = this.val.xor(a.val);
        return new Element(this.bitLength, this.order, val);
    }*/

    public Element xor(byte[] bytes){
        BigInteger val = this.val.xor(new BigInteger(bytes));
        return new Element(this.bitLength, this.order, val);
    }

    @Override
    public String toString(){
        return this.val.toString();
    }

    public int getBitLength() {
        return bitLength;
    }

    public BigInteger getOrder() {
        return order;
    }

    public BigInteger getVal() {
        return val;
    }
}
