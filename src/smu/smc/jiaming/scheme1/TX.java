package smu.smc.jiaming.scheme1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TX {

    private byte[] reciever;
    private byte[] data;
    private long timestamp;
    private byte[] hash;

    public TX(byte[] reciever, byte[] data, long timestamp, byte[] hash){
        this.reciever = reciever;
        this.data = data;
        this.timestamp = timestamp;
        this.hash = hash;
    }

    public static TX newInstance(byte[] reciever, byte[] data){
        return new TX(reciever, data, System.nanoTime(), null);
    }

    public static TX newInstance(byte[] reciever, byte[] data, long timestamp, byte[] hash){
        return new TX(reciever, data, timestamp, hash);
    }

    public String toString(String sym, boolean ifhash){
        String res = "";
        res += sym + "{\n";
        res += sym + "    reciever:    " + PrintUtils.bytes2Hex(this.reciever) + "\n";
        res += sym + "    data:        " + PrintUtils.bytes2Hex(this.data) + "\n";
        if(ifhash)
            res += sym + "    hash:        " + PrintUtils.bytes2Hex(this.hash) + "\n";
        res += sym + "    timestamp:   " + this.timestamp + "\n";
        res += sym + "}";
        return res;
    }

    public String toString(){
        return toString("", false);
    }

    public String toString(String sym){
        return toString(sym, false);
    }

    public String toString(boolean ifHash){
        return toString("", ifHash);
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getReciever() {
        return reciever;
    }

    public byte[] getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] toHash(){
        MessageDigest md = HashUtils.mdSHA256;
        if(md != null)
            md.reset();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bs = null;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.write(reciever);
            oos.write(data);
            oos.writeLong(timestamp);
            oos.flush();
            bs = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        md.update(bs);
        this.hash = md.digest();
        return this.hash;
    }
}
