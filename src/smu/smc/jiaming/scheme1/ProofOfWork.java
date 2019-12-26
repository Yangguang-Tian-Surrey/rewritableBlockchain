package smu.smc.jiaming.scheme1;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class ProofOfWork {
    private int difficulty;
    private byte challenge;

    public ProofOfWork(int difficulty, byte challenge) {
        this.difficulty = difficulty;
        this.challenge = challenge;
    }

    public Block ProofOfWorkForNewBlock(Block newBlock, Sig sig) throws NoSuchAlgorithmException {
        sig.flag = true;
        boolean f = false;
        byte[] hash = null;
        int diff;
        newBlock.setTimeStamp(System.nanoTime());
        while(sig.flag){
            hash = newBlock.toHash();
            for(diff = 0; diff < hash.length; diff++){
                if(hash[diff] != challenge)
                    break;
            }
            if(diff >= difficulty) {
                f = true;
                break;
            }
            newBlock.creaseNonce();
        }
        if(!f) return null;
        newBlock.setHash(hash);
        return newBlock;
    }

    public void interrupt(Sig sig){
        sig.flag = false;
    }

    public static class Sig{
        public boolean flag = false;
    }

}
