package smu.smc.jiaming.scheme2;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme1.DataDao;
import smu.smc.jiaming.scheme1.MinerManageThread;
import smu.smc.jiaming.scheme1.SQLiteDataDao;
import smu.smc.jiaming.scheme1.ProofOfWork;
import smu.smc.jiaming.scheme1.MinerThread;
import smu.smc.jiaming.scheme2.CHSHash.CHHashInTree;
import smu.smc.jiaming.scheme2.CHSHash.KeyGen;
import smu.smc.jiaming.scheme2.CHSHash.tree.CHHashTree;

import java.sql.SQLException;

public class Test {
    public static void main(String[] args){
        PrimeGroup group = PrimeGroup.newInstance("group");

        KeyGen kgen = new KeyGen(group);
        KeyGen.KeyPair party = kgen.genKeyPair();
        Element sk = party.getSk();
        Element pk = party.getPk();

        // initialize a tree
        int depth = 10;
        int width = 2;
        int range = 10;
        CHHashTree tree = CHHashTree.newInstance(depth, width, range, group);

        //KeyGen
        // esk, epk for user
        KeyGen.KeyPair user = kgen.genKeyPair();
        Element esk = user.getSk();
        Element epk = user.getPk();

        // ask, apk -- m -- by user
        CHHashInTree.AssertionKeyPair akpair =
                kgen.genAssertionKeyPair(tree.getRoot(), pk, esk, true);
        // ask, apk -- m -- by authority
        akpair = kgen.genAssertionKeyPair(tree.getRoot(), sk, epk, false);
        Element ask = akpair.getAsk();
        CHHashInTree.AssertionKeyPair.PK apk = akpair.getApk();



        MinerManageThread.Transactions tx = new MinerManageThread.Transactions();

        UserClient uc = new UserClient(pk, user,
                akpair, tree, group, tx);
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
            MinerThread miner = MinerThread.newInstance(
                    "miner"+(i+1), pow, miners, dataDao);
        }

        miners.starts();

        uct.start();
    }
}
