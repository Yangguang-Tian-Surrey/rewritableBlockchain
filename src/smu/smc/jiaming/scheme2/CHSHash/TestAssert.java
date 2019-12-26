package smu.smc.jiaming.scheme2.CHSHash;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme2.CHSHash.tree.CHHashTree;

public class TestAssert {

    public static void testHashS(){
        PrimeGroup group = PrimeGroup.newInstance("group");

        KeyGen kgen = new KeyGen(group);
        KeyGen.KeyPair party = kgen.genKeyPair();
        Element sk = party.getSk();
        Element pk = party.getPk();
        Element z = group.newRandomZElement();
        Element gz = group.getG().pow(z);

        Element x1 = group.newRandomZElement();
        Element x2 = group.newRandomZElement();
        Element r1 = group.newRandomZElement();

        CHHashSEngine ch = new CHHashSEngine();
        ch.init(group);
        ch.setup(sk, z, true);

        Element r2 = ch.collision(x1, r1, x2);
        Element hash = ch.hash(x2, r2);

        ch.clear();
        ch.setup(pk, gz, false);
        System.out.println(ch.hash(x1, r1).isEqual(hash));

    }

    public static void test(){
        long t1, t2;

        PrimeGroup group = PrimeGroup.newInstance("group");

        KeyGen kgen = new KeyGen(group);
        //Setup
        // pk, sk for a party
        t1 = System.nanoTime();
        KeyGen.KeyPair party = kgen.genKeyPair();
        t2 = System.nanoTime();
        System.out.println("generate pk, sk --> " + (t2-t1) + "ns");
        Element sk = party.getSk();
        Element pk = party.getPk();

        // initialize a tree
        int depth = 10;
        int width = 2;
        int range = 10;
        t1 = System.nanoTime();
        CHHashTree tree = CHHashTree.newInstance(depth, width, range, group);
        t2 = System.nanoTime();
        System.out.println("generate a tree --> " + (t2-t1) + "ns");

        //KeyGen
        // esk, epk for user
        t1 = System.nanoTime();
        KeyGen.KeyPair user = kgen.genKeyPair();
        t2 = System.nanoTime();
        System.out.println("generate epk, esk --> " + (t2-t1) + "ns");
        Element esk = user.getSk();
        Element epk = user.getPk();

        // ask, apk -- m -- by user
        t1 = System.nanoTime();
        CHHashInTree.AssertionKeyPair akpair = kgen.genAssertionKeyPair(tree.getRoot(), pk, esk, true);
        akpair = kgen.genAssertionKeyPair(tree.getRoot(), sk, epk, false);
        t2 = System.nanoTime();
        System.out.println("generate apk, ask --> " + (t2-t1) + "ns");
        Element ask = akpair.getAsk();
        CHHashInTree.AssertionKeyPair.PK apk = akpair.getApk();

        // gen 2 candidate messages -- by user
        t1 = System.nanoTime();
        KeyGen.CandidateMessage cm1 = kgen.genCandidateMessage(group.newRandomZElement(), pk, user);
        t2 = System.nanoTime();
        System.out.println("generate a candidate message --> " + (t2-t1) + "ns");
        KeyGen.CandidateMessage cm2 = kgen.genCandidateMessage(group.newRandomZElement(), pk, user);

        CHHashInTree chash = new CHHashInTree();
        chash.init(group, tree);
        boolean flag;

        // hash cm1, pk, esk, ask, flag = 3
        chash.setup(pk, esk, ask, 3);
        t1 = System.nanoTime();
        CHHashInTree.ResHash rh1 = chash.genhash(cm1);
        t2 = System.nanoTime();
        System.out.println("hash a message --> " + (t2-t1) + "ns");

        // verify rh1, pk, null, apk, flag = 0
        chash.clear();
        chash.setup(null, null, apk, 0);
        t1 = System.nanoTime();
        flag = chash.verify(rh1);
        t2 = System.nanoTime();
        System.out.println("verify a message --> " + (t2-t1) + "ns");
        System.out.println(flag);
        if(!flag)
            return;

        // adapt to cm2  rh1, cm2, sk, epk, ask, flag = 9 by authority
        chash.clear();
        chash.setup(sk, epk, ask, 5);
        t1 = System.nanoTime();
        CHHashInTree.ResHash rh2 = chash.adapt(rh1, cm2);
        t2 = System.nanoTime();
        System.out.println("adapt a message --> " + (t2-t1) + "ns");

        // verify rh2, pk, null, apk, flag = 0
        chash.clear();
        chash.setup(pk, null, apk, 0);
        System.out.println(chash.verify(rh2));

        // correctness; rh1, rh1, pk, null, apk, flag = 0
        chash.clear();
        chash.setup(pk, null, apk, 0);
        t1 = System.nanoTime();
        System.out.println(chash.correct(rh1, rh2));
        t2 = System.nanoTime();
        System.out.println("correctness --> " + (t2-t1) + "ns");
    }

    public static void main(String[] args){
        test();
//        testHashS();
    }
}
