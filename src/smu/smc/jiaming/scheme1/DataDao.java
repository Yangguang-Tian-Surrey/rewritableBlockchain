package smu.smc.jiaming.scheme1;

public interface DataDao {
    public void insert(Block block);

    public Block selectLatestBlock();

    public Block selectBlockbyHash(byte[] Hash);

    public void insertTX(TX tx, long index);

    public void insertTXs(TX[] tx, long index);

    public TX selectTXbyHash(byte[] Hash);

    public TX[] selectTXesbyHashes(byte[][] Hashes);

    public TX[] selectTXesbyIndex(int index);
}
