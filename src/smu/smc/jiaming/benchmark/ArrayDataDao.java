package smu.smc.jiaming.benchmark;

import smu.smc.jiaming.scheme1.Block;
import smu.smc.jiaming.scheme1.DataDao;
import smu.smc.jiaming.scheme1.TX;

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayDataDao implements DataDao {
    private ArrayList<Block> blocks = new ArrayList<>();
    private ArrayList<TX> txes = new ArrayList<>();
    private ArrayList<Long> tx_block = new ArrayList<>();

    @Override
    public void insert(Block block) {
        this.blocks.add(block);
        this.insertTXs(block.getTxes().toArray(new TX[0]), block.getIndex());
    }

    @Override
    public Block selectLatestBlock() {
        if(blocks.size() < 1)
            return null;
        return blocks.get(blocks.size()-1);
    }

    @Override
    public Block selectBlockbyHash(byte[] Hash) {
        for(Block b : blocks){
            if(Arrays.equals(b.getHash(), Hash));
                return b;
        }
        return null;
    }

    @Override
    public void insertTX(TX tx, long index) {
        this.txes.add(tx);
        this.tx_block.add(index);
    }

    @Override
    public void insertTXs(TX[] tx, long index) {
        int len = tx.length;
        for(int i = 0; i < len; i++)
            insertTX(tx[i], index);
    }

    @Override
    public TX selectTXbyHash(byte[] Hash) {
        int len = this.txes.size();
        for(int i = 0; i < len; i++){
            if(Arrays.equals(this.txes.get(i).getHash(), Hash));
            return this.txes.get(i);
        }
        return null;
    }

    @Override
    public TX[] selectTXesbyHashes(byte[][] Hashes) {
        int len = Hashes.length;
        int len2 = this.txes.size();
        TX[] txes = new TX[len];
        for(int i = 0; i < len; i++){
            for(int j = 0; j < len2; j++) {
                if (Arrays.equals(this.txes.get(j).getHash(), Hashes[i])) {
                    txes[i] = this.txes.get(j);
                    break;
                }
            }
        }
        return txes;
    }

    @Override
    public TX[] selectTXesbyIndex(int index) {
        int end = this.tx_block.lastIndexOf(index);
        int start = this.tx_block.indexOf(index);
        TX[] txes = new TX[end - start + 1];
        for(int i = start; i <= end; i++ ){
            txes[i-start] = this.txes.get(i);
        }
        return txes;
    }
}
