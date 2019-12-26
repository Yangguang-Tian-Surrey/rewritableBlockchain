package smu.smc.jiaming.scheme1;

import java.math.BigInteger;
import java.util.Random;

public class UserClient {
    private MinerManageThread.Transactions tx;
    private Random rand = new Random();

    public UserClient(MinerManageThread.Transactions tx) {
        this.tx = tx;
    }

    public TX newRandomTX(){
        byte[] r = new BigInteger(64, rand).toByteArray();
        byte[] d = new BigInteger(256, rand).toByteArray();
        TX tx = TX.newInstance(r, d);
        tx.toHash();
        return tx;
    }

    public TX[] newRandomTXes(int n){
        TX[] tx = new TX[n];
        for (int i = 0; i < n; i++) {
            tx[i] = newRandomTX();
        }
        return tx;
    }

    public void sendTX(TX tx){
            this.tx.add(tx);
    }

    public void sendTXes(TX[] tx){
            this.tx.add(tx);
    }

    public MinerManageThread.Transactions getTx() {
        return tx;
    }
}
