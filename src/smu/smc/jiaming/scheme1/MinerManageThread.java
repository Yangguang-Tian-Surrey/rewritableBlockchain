package smu.smc.jiaming.scheme1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinerManageThread extends Thread{
    private List<MinerThread> miners;
    private Transactions tx;

    public MinerManageThread(Transactions tx, List<MinerThread> miners){
        this.tx = tx;
        this.miners = miners;
    }

    public MinerManageThread(Transactions tx){
        this.tx = tx;
        this.miners = new ArrayList<>();
    }

    public void addMiner(MinerThread miner, boolean flag){
        this.miners.add(miner);
        if (flag) miner.start();
    }

    public void removeMiner(MinerThread miner){
        this.miners.remove(miner);
        miner.interrupt();
    }

    public void starts(){
        for (MinerThread miner: miners) {
            miner.start();
        }
        this.start();
    }

    public void stops(){
        for (MinerThread miner: miners) {
            miner.interrupt();
        }
    }

    public boolean broadcast(Block block) {
        boolean flag = true;
        for (MinerThread miner: miners) {
            try {
                flag = flag && miner.acceptBlock(block.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void broadcast(TX tx){
        for (MinerThread miner: miners) {
            miner.recieveTX(tx);
        }
    }

    public void broadcast(TX[] tx){
        for (MinerThread miner: miners) {
            miner.recieveTXes(tx);
        }
    }

    @Override
    public void run(){
        while(true && !isInterrupted()){
            try {
                synchronized (this.tx) {
                    this.tx.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if(tx.flag) {
                stops();
                return;
            }
            if(tx.tx.size() == 1) {
                this.broadcast(tx.tx.get(0));
            }
            else {
                TX[] tx = new TX[this.tx.tx.size()];
                this.broadcast((TX[]) this.tx.tx.toArray(tx));
                this.tx.tx.clear();
            }
        }
    }

    public static class Transactions {
        private List<TX> tx = new ArrayList<>();
        private boolean flag = false;

        public synchronized void add(TX tx){
            this.tx.add(tx);
            this.notifyAll();
        }
        public synchronized void add(TX[] tx){
            this.tx.addAll(Arrays.asList(tx));
            this.notifyAll();
        }

        public synchronized void stop(){
            this.flag = true;
            this.notifyAll();
        }
    }
}
