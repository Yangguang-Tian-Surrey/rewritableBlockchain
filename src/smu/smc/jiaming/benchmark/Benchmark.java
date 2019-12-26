package smu.smc.jiaming.benchmark;

import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme1.*;
import smu.smc.jiaming.scheme2.CHSHash.CHHashInTree;
import smu.smc.jiaming.scheme2.CHSHash.KeyGen;
import smu.smc.jiaming.scheme2.CHSHash.tree.CHHashTree;
import smu.smc.jiaming.scheme2.RewritableTX;
import smu.smc.jiaming.scheme2.UserClient;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Benchmark {
    public int bitlength;
    public PrimeGroup group;
    public KeyGen.KeyPair ks;
    public KeyGen.KeyPair eks;
    public CHHashInTree.AssertionKeyPair aks;
    public CHHashTree tree;
    public CHHashInTree chhash;
    public UserClient client;
    public DataDao dataDao;
    public BlockChain blockChain;
    public ProofOfWork pow;

    public TX[] txes;

    public Benchmark(int bitlength){
        this.bitlength = bitlength;
        group = PrimeGroup.newInstance(bitlength);
        this.dataDao = new ArrayDataDao();
    }

    public Benchmark(String group){
        this.bitlength = bitlength;
        this.group = PrimeGroup.newInstance(group);
        this.dataDao = new ArrayDataDao();
        this.blockChain = BlockChain.newInstance(this.dataDao);
        this.pow = new ProofOfWork(2, (byte) 0);
    }

    public void initKeys(){

        KeyGen kgen = new KeyGen(group);
        ks = kgen.genKeyPair();

        //KeyGen
        // esk, epk for user
        eks = kgen.genKeyPair();

    }

    public void setup(int depth, int width, int range){
        tree = CHHashTree.newInstance(depth, width, range, group);

        KeyGen kgen = new KeyGen(group);
        // ask, apk -- m -- by user
        CHHashInTree.AssertionKeyPair akpair =
                kgen.genAssertionKeyPair(tree.getRoot(), ks.getPk(), eks.getSk(), true);

        chhash = new CHHashInTree();
        chhash.init(group, tree);

        //ask, apk by user or authority
        aks = kgen.genAssertionKeyPair(this.tree.getRoot(), ks.getPk(), eks.getSk(), true);

        this.client = new UserClient(ks.getPk(), eks, aks, tree, group,null);

    }

    public double testGenMixedTXes(int n, int p, int m, int times){
        double sum = 0;
        long t1, t2;
        TX[] txes = null;
        for (int i = 0; i < times; i++) {
            t1 = getTime();
            txes = this.client.newRandomMixedTXes(n, p, m);
            t2 = getTime();
            sum += t2 - t1;
        }
        sum = sum/times;
        this.txes = txes;
        return sum;

    }

    public void updadteBlockChain(){
        this.blockChain.getnewBlock().acceptTXes(this.txes);
        try {
            this.blockChain.getnewBlock().setRootHash(
                    this.blockChain.getnewBlock().genRootHash());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        ProofOfWork.Sig sig = new ProofOfWork.Sig();
        sig.flag = true;
        try {
            this.pow.ProofOfWorkForNewBlock(this.blockChain.getnewBlock(), sig);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.blockChain.replaceChain(this.blockChain.getnewBlock());
    }

    public String validateBlockChain(int len, int times){
        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            this.blockChain.verify(len);
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }



    public String testMerkle(TX[] txes, int times){
        Block block = Block.newInstance(10, getTime(),0,
                null, null, null, null, new ArrayList<TX>(), null);
        block.acceptTXes(txes);

        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for (int i = 0; i < times; i++) {
            t1 = getTime();
            try {
                block.genRootHash();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testTreeGen(int depth, int width, int range, int times){
        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            tree = CHHashTree.newInstance(depth, width, range, group);
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testGenRTX(int times, int cmno){
        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            client.newRandomRTX(cmno);
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testGenAssertion(RewritableTX rtx, int times){
        this.chhash.clear();
        this.chhash.setup(ks.getPk(), eks.getSk(), aks.getAsk(), 3);

        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            rtx.genAssertion(this.chhash, 0);
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testGenAdapt(RewritableTX rtx, int mi, int times){
        this.chhash.setup(ks.getSk(), eks.getPk(), aks.getAsk(), 5);

        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            rtx.adapt(this.chhash, mi);
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testVerify(RewritableTX rtx, int times){
        this.chhash.clear();
        this.chhash.setup(ks.getPk(), null, aks.getApk(), 0);

        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            if(!rtx.verify(this.chhash))
                return "verify failed!";
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testCorrectness(RewritableTX rtx1, RewritableTX rtx2, int times){
        this.chhash.clear();
        this.chhash.setup(ks.getPk(), null, aks.getApk(), 0);

        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        for(int i = 0; i < times; i++) {
            t1 = getTime();
            this.chhash.correct(rtx1.getResHash(), rtx2.getResHash());
            t2 = getTime();
            sum = sum.add(BigDecimal.valueOf(t2 - t1));
        }
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();
    }

    public String testGenTX(int times){
        BigDecimal sum = BigDecimal.ZERO;
        long t1, t2;
        t1 = getTime();
        client.newRandomTXes(times);
        t2 = getTime();
        sum = BigDecimal.valueOf(t2 - t1);
        sum = sum.divide(BigDecimal.valueOf(times));
        return sum.toString();

    }

    public static long getTime(){
        return System.nanoTime();
    }


    public static void main(String[] args){
        Benchmark benchmark = new Benchmark("group");
        int times = 1000000;

        benchmark.initKeys();
        //benchmark.setup(10, 2, 10);
        int[] depth = {5, 6, 7, 8, 9, 10};//{5, 6, 7, 8, 9, 10};
        int[] range = {5, 6, 7, 8, 9, 10};//{5, 6, 7, 8, 9, 10}
        int[] cmn = {2, 4, 6, 8, 10};//{2, 4, 6, 8, 10}
        String temp;
        RewritableTX rtx = null;
        RewritableTX rtx2 = null;
        benchmark.setup(depth[0], 2, range[0]);
        /*for(int i = 0; i < depth.length; i++){
            for(int j = 0; j < range.length; j++) {
                //temp = benchmark.testTreeGen(depth[i], 2, range[j], times);
                benchmark.setup(depth[i], 2, range[j]);
                rtx = benchmark.client.newRandomRTX(2);
                benchmark.chhash.setup(benchmark.ks.getSk(),
                      benchmark.eks.getPk(), benchmark.aks.getAsk(), 5);

                rtx2 = rtx.adapt(benchmark.chhash, 1);
                //temp = benchmark.testVerify(rtx, times);
                //temp = benchmark.testGenAdapt(rtx, 1, times);
                temp = benchmark.testCorrectness(rtx, rtx2, times);
                System.out.println(temp);
                *//*for(int m = 0; m < cmn.length; m++) {
                    temp = benchmark.testGenRTX(times, cmn[m]);
                    System.out.println(temp);
                }*//*
            }
            System.out.println("--------------------------------------------");
        }*/

        /*int transN = 100;
        int[] rtxN = {0, 2, 4, 6, 8, 10};//{0, 2, 4, 6, 8, 10}
        benchmark.setup(10, 2, 10);
        for(int i = 0; i < rtxN.length; i++){
            TX[] txes = benchmark.client.newRandomMixedTXes(transN, rtxN[i], 2);
            temp = benchmark.testMerkle(txes, times);
            System.out.println(temp);
        }*/

        /*int chainLen = 10;
        for(int i = 0; i < rtxN.length; i++){
            for(int j = 0; j < chainLen + 1; j++) {
                benchmark.txes = benchmark.client.newRandomMixedTXes(transN, rtxN[i], 2);
                benchmark.updadteBlockChain();
            }
            temp = benchmark.validateBlockChain(chainLen, times);
            System.out.println(temp);
        }*/


        temp = benchmark.testGenTX(times);
        System.out.println(temp);
    }
}
