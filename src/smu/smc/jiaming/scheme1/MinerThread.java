package smu.smc.jiaming.scheme1;

import java.security.NoSuchAlgorithmException;

public class MinerThread extends Thread {
    private static Object FLAG;
    static{
        FLAG = new Object();
    }

    private BlockChain blockChain;
    private DataDao dataDao;
    private ProofOfWork pow;
    private ProofOfWork.Sig powSig = new ProofOfWork.Sig();
    private MinerManageThread miners;

    private long t1, t2, t3;

    private MinerThread(){

    }

    private MinerThread(int difficulty, byte challenge, MinerManageThread miners){
        pow = new ProofOfWork(difficulty, challenge);
        this.miners = miners;
    }

    private MinerThread(String name, ProofOfWork pow, MinerManageThread miners){
        super(name);
        this.pow = pow;
        this.miners = miners;
        this.miners.addMiner(this, false);
    }

    public static MinerThread newInstance(String name, ProofOfWork pow, MinerManageThread miners, DataDao dataDao){
        MinerThread miner = new MinerThread(name, pow, miners);
        miner.blockChain = BlockChain.newInstance(dataDao);
        return miner;
    }

    public boolean acceptBlock(Block block){
        boolean flag =  this.blockChain.replaceChain(block);
        //println("\n" + this.getName() + "--------------------- accept new block");
        if(flag) this.pow.interrupt(this.powSig);
        return flag;
    }

    public void recieveTX(TX tx){
        this.blockChain.getnewBlock().acceptTX(tx);
    }
    public void recieveTXes(TX[] txes){
        this.blockChain.getnewBlock().acceptTXes(txes);
    }

    @Override
    public void run() {
        Block block;
        long t1, t2;
        while(true && !this.isInterrupted()){
            block = this.blockChain.getnewBlock();
            try {
                synchronized(block) {
                    block.wait();
                    //println("\n" + this.getName() + "--------------------- start proofofwork!");
                    this.t1 = System.currentTimeMillis();
                    if(block.getHash() == null) {
                        t1 = System.nanoTime();
                        block.setRootHash(block.genRootHash());
                        t2 = System.nanoTime();
                        System.out.println("root hash --> " + (t2 - t1) + " ns");
                    }
                    block = pow.ProofOfWorkForNewBlock(block, this.powSig);
                    this.t2 = System.currentTimeMillis();
                    if(block == null){
                        println("\n" + this.getName() + "--------------------- proofofwork interrupted!!");
                        continue;
                    }
                    this.broadcast(block);
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println(this.getName() + " stop!");
                return;
            }

        }
    }

    public void broadcast(Block block){
        boolean flag = this.miners.broadcast(block);
        if(flag)
            this.blockChain.updateDatabase();

        String p = "\n" + this.getName() + "--------------start------------------\n"
                + "-- broadcast block " + flag + "\n";
        p += flag?"-- Time of POW:   " + (this.t2 - this.t1) + "ms\n"+block+"\n":"";
        p += this.getName() + "---------------end-------------------";
        println(p);
    }

    public static void println(String content){
        System.out.println(content);
    }
}
