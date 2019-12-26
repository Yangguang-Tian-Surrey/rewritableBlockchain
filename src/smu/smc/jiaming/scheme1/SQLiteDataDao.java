package smu.smc.jiaming.scheme1;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLiteDataDao implements DataDao{
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private Connection conn;

    public SQLiteDataDao() throws SQLException {
        this.conn = DriverManager.getConnection("jdbc:sqlite:data/data1.db");
    }


    @Override
    public void insert(Block block) {
        if(block.getTxes() != null || block.getTxes().size() > 0){
            TX[] tx = new TX[block.getTxes().size()];
            insertTXs(block.getTxes().toArray(tx), block.getIndex());
        }

        String sql = "insert into BLOCK(BLOCK_INDEX, TIME_STAMP, NONCE, " +
                "HASH, BLOCK_DATA, PREHASH, ROOTHASH) " +
                "values (?,?,?,?,?,?,?);";
        try {
            PreparedStatement pstate = this.conn.prepareStatement(sql);
            pstate.setLong(1, block.getIndex());
            pstate.setLong(2, block.getTimestamp());
            pstate.setLong(3, block.getNonce());
            pstate.setString(4, PrintUtils.bytes2Hex(block.getHash()));
            pstate.setString(5, PrintUtils.bytes2Hex(block.getData()));
            pstate.setString(6, PrintUtils.bytes2Hex(block.getPreHash()));
            pstate.setString(7, PrintUtils.bytes2Hex(block.getRootHash()));

            pstate.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Block selectLatestBlock() {
        String sql = "select max(BLOCK_INDEX) as BLOCK_INDEX, TIME_STAMP, NONCE," +
                "HASH, BLOCK_DATA, PREHASH, ROOTHASH from BLOCK;";
        Block block = null;
        try {
            PreparedStatement pstate = this.conn.prepareStatement(sql);
            ResultSet rs = pstate.executeQuery();
            if(rs.next()){
                block = convertBlock(rs);
                if(block.getIndex() == 0)
                    return null;
                List<TX> txes = Arrays.asList(selectTXesbyIndex((int) block.getIndex()));
                block.setTxes(txes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }

    @Override
    public Block selectBlockbyHash(byte[] Hash) {
        String sql = "select BLOCK_INDEX, TIME_STAMP, NONCE," +
                "BLOCK_DATA, PREHASH, ROOTHASH from BLOCK where HASH=?;";
        Block block = null;
        try {
            PreparedStatement pstate = this.conn.prepareStatement(sql);
            pstate.setString(1, PrintUtils.bytes2Hex(Hash));
            ResultSet rs = pstate.executeQuery();
            if(rs.next()){
                block = convertBlock(rs);
                List<TX> txes = Arrays.asList(selectTXesbyIndex((int) block.getIndex()));
                block.setHash(Hash);
                block.setTxes(txes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }

    @Override
    public void insertTX(TX tx, long index) {
        String sql = "insert into TX(RECIEVER, TX_DATA, TIME_STAMP, HASH, BLOCK_INDEX) " +
                "values (?, ?, ?, ?, ?);";
        try {
            PreparedStatement pstate = this.conn.prepareStatement(sql);
            pstate.setString(1,PrintUtils.bytes2Hex(tx.getReciever()));
            pstate.setString(2,PrintUtils.bytes2Hex(tx.getData()));
            pstate.setLong(3, tx.getTimestamp());
            pstate.setString(4,PrintUtils.bytes2Hex(tx.getHash()));
            pstate.setLong(5, index);

            pstate.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void insertTXs(TX[] tx, long index) {
        for(int i = 0; i < tx.length; i++){
            insertTX(tx[i], index);
        }
        return;
    }

    @Override
    public TX selectTXbyHash(byte[] Hash) {
        return null;
    }

    @Override
    public TX[] selectTXesbyHashes(byte[][] Hashes) {
        return new TX[0];
    }

    @Override
    public TX[] selectTXesbyIndex(int index) {
        String sql = "select RECIEVER, TX_DATA, TIME_STAMP, HASH from TX where BLOCK_INDEX=?;";
        TX[] txes = null;
        try {
            PreparedStatement pstate = this.conn.prepareStatement(sql);
            pstate.setLong(1, index);
            ResultSet rs = pstate.executeQuery();
            List<TX> list = new ArrayList<>();
            while(rs.next()){
                list.add(convertTX(rs));
            }
            txes = new TX[list.size()];
            txes = list.toArray(txes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txes;
    }

    private Block convertBlock(ResultSet rs){
        Block block = null;
        try {
            long index = rs.getLong("BLOCK_INDEX");
            byte[] data = PrintUtils.hex2Bytes(rs.getString("BLOCK_DATA"));
            long timestamp = rs.getLong("TIME_STAMP");
            byte[] hash = PrintUtils.hex2Bytes(rs.getString("HASH"));
            byte[] prehash = PrintUtils.hex2Bytes(rs.getString("PREHASH"));
            long nonce = rs.getLong("NONCE");
            byte[] roothash = PrintUtils.hex2Bytes(rs.getString("ROOTHASH"));
            block = Block.newInstance(index, timestamp, nonce, hash, data, prehash, roothash, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }

    private TX convertTX(ResultSet rs){
        TX tx = null;
        try {
            byte[] reciver = PrintUtils.hex2Bytes(rs.getString("RECIEVER"));
            byte[] data = PrintUtils.hex2Bytes(rs.getString("TX_DATA"));
            long timestamp = rs.getLong("TIME_STAMP");
            byte[] hash = PrintUtils.hex2Bytes(rs.getString("HASH"));
            tx = new TX(reciver, data, timestamp, hash);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tx;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.conn.close();
    }
}
