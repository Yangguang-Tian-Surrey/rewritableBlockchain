package smu.smc.jiaming.elgamal;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class TestElgamal {
    private static void test1(){
        //PrimeGroup group = PrimeGroup.newInstance(128);
        //group.save2File("group");
        PrimeGroup group = PrimeGroup.newInstance("group");

        Element e1 = group.newRandomZElement();
        Element e2 = e1.invert();
        System.out.println(e1.mul(e2).toString());

        Element m = group.newRandomZElement();
        Element r = group.newRandomZElement();
        Element s = group.newRandomZElement();
        Element g_s = group.getG().pow(s);
        //enc(m, g_s, r) -> (c, g_r)
        Element c = m.mul(g_s.pow(r));
        Element g_r = group.getG().pow(r);
        //dec(c, g_r, s) -> m
        Element pm = c.mul(g_r.pow(s).invert());
        System.out.println(pm.isEqual(m));

        byte[] hash = m.xor(r);
        Element rt = m.xor(hash);
        System.out.println(rt.isEqual(r));

        /*boolean flag = true;
        while(flag) {
            m = group.newRandomElement();
            r = group.newRandomElement();
            Element rh = m.xore(r);
            rt = m.xore(rh);
            flag = rt.isEqual(r);
        }

        System.out.println(m);
        System.out.println(r);
        System.out.println(flag);*/
    }

    public static void test2(){
        PrimeGroup group = PrimeGroup.newInstance("group");
        group.save2File("group");
        Element e1 = group.newRandomZElement();
        Element e2 = group.newRandomZElement();
        Element g_e1 = group.getG().pow(e1);
        System.out.println(group.getG().pow(e1.mul(e2)).isEqual(g_e1.pow(e2)));
        System.out.println(e1.getVal().gcd(e1.getOrder()));
        System.out.println(e1.invert());
    }

    public static void main(String[] args){
        test2();
    }
}
