package smu.smc.jiaming.scheme1;

public class BlockChain {
    private Block curBlock;
    private Block newBlock;
    private DataDao dataDao;

    private BlockChain(){

    }

    public static BlockChain newInstance(DataDao dataDao) {
        BlockChain bc = new BlockChain();
        bc.curBlock = dataDao.selectLatestBlock();
        bc.newBlock = Block.newNext(bc.curBlock, null);
        bc.dataDao = dataDao;
        return bc;
    }

    public synchronized boolean replaceChain(Block newBlock){
        if(!newBlock.isValid(curBlock))
            return false;
        this.curBlock = newBlock;
        this.newBlock = newBlock.newNext(this.curBlock, null);
        return true;
    }

    public Block getnewBlock() {
        return newBlock;
    }

    /**
     * @param len start from 1
     * */
    public boolean verify(int len){
        if(this.curBlock == null)
            return true;
        if(len < 1)
            return true;
        Block block = this.curBlock;
        Block preblock;
        boolean flag = true;
        for(int i = 0; i < len; i++){
            preblock = this.dataDao.selectBlockbyHash(this.curBlock.getPreHash());
            flag = flag & block.isValid(preblock);
            if(!flag)break;
            block = preblock;
        }
        return flag;
    }

    public void updateDatabase(){
        this.dataDao.insert(this.curBlock);
    }
}
