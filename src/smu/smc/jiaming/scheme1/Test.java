package smu.smc.jiaming.scheme1;

import java.sql.SQLException;

public class Test {
    public static void main(String[] args){
        MinerManageThread.Transactions tx = new MinerManageThread.Transactions();

        UserClient uc = new UserClient(tx);
        UserClientThread uct = new UserClientThread(uc);

        MinerManageThread miners = new MinerManageThread(tx);
        DataDao dataDao = null;
        try {
            dataDao = new SQLiteDataDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        ProofOfWork pow = new ProofOfWork(3, (byte) 0);
        for(int i = 0; i < 10; i++) {
            MinerThread miner = MinerThread.newInstance("miner"+(i+1), pow, miners, dataDao);
        }

        miners.starts();

        uct.start();
    }
}
