package smu.smc.jiaming.scheme2.CHSHash;

import smu.smc.jiaming.elgamal.Element;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils extends smu.smc.jiaming.scheme1.HashUtils {

    public static Element hashEles(Element[] eles, Element out){
        if(mdSHA256 == null)
            return null;
        mdSHA256.reset();
        for(int i = 0; i < eles.length; i++)
            mdSHA256.update(eles[i].toBytes());
        byte[] rb = mdSHA256.digest();
        out.setBytes(rb);
        return out;
    }

    public static byte[] int2bytes(int n){
        return new byte[]{(byte) (n & 0xff), (byte) (n >> 8 & 0xff),
                (byte) (n >> 16 & 0xff), (byte) (n >> 32 & 0xff)};
    }

    public static Element hashP(Element nodeId, int index, byte flag, Element out){
        if(mdSHA256 == null)
            return null;
        mdSHA256.reset();
        mdSHA256.update(nodeId.toBytes());
        mdSHA256.update(int2bytes(index));
        mdSHA256.update(flag);
        byte[] r = mdSHA256.digest();
        out.setBytes(r);
        return out;
    }

    public static Element hashEles(Element ele, Element out){
        Element[] eles = new Element[]{ele};
        return hashEles(eles, out);
    }
}
