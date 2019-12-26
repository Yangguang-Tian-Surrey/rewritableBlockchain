package smu.smc.jiaming.scheme2;

import smu.smc.jiaming.scheme1.HashUtils;
import smu.smc.jiaming.scheme1.PrintUtils;
import smu.smc.jiaming.scheme1.TX;
import smu.smc.jiaming.scheme2.CHSHash.CHHashInTree;
import smu.smc.jiaming.scheme2.CHSHash.KeyGen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class RewritableTX extends TX {
    private KeyGen.CandidateMessage[] cms;

    private CHHashInTree.ResHash resHash;

    public RewritableTX(byte[] reciever, byte[] data, long timestamp, byte[] hash) {
        super(reciever, data, timestamp, hash);
    }

    public static RewritableTX newInstance(byte[] reciever, byte[] data, long timestamp,
                                           KeyGen.CandidateMessage[] cms){

        RewritableTX rtx = new RewritableTX(reciever, data, timestamp, null);
        rtx.cms = cms;
        return rtx;
    }

    public boolean genAssertion(CHHashInTree che, int mi){
        if(mi < 0 || mi >= cms.length)
            return false;
        this.resHash = che.genhash(this.cms[mi]);
        return true;
    }

    public RewritableTX adapt(CHHashInTree che, int mi){
        if(mi < 0 || mi >= cms.length)
            return null;
        if(cms[mi].getM().equals(this.resHash.getM().getX()))
            return this;
        CHHashInTree.ResHash rh = che.adapt(this.resHash, this.cms[mi]);
        RewritableTX rtx = newInstance(this.getReciever(),
                this.getData(), this.getTimestamp(), this.cms);
        rtx.resHash = rh;
        return rtx;
    }

    public boolean verify(CHHashInTree che){
        if(!che.verify(this.resHash))
            return false;
        byte[] hash = this.getHash();
        if(!Arrays.equals(hash, this.toHash())){
            this.setHash(hash);
            return false;
        }
        return true;
    }

    public byte[] toHash(){
        super.toHash();
        MessageDigest md = HashUtils.mdSHA256;
        if(md != null)
            md.reset();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bs = null;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.write(super.getHash());
            for(int i = 0; i < cms.length; i++)
                oos.write(cms[i].getM().toBytes());
            oos.write(resHash.getH().toBytes());
            oos.flush();
            bs = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        md.update(bs);
        super.setHash(md.digest());
        return super.getHash();
    }

    @Override
    public String toString(String sym, boolean ifhash){
        String res = "";
        res += sym + "{\n";
        res += sym + "    reciever:    " + PrintUtils.bytes2Hex(this.getReciever()) + "\n";
        res += sym + "    data:        " + PrintUtils.bytes2Hex(this.getData()) + "\n";
        res += sym + "    Candidate M: {\n";
        for(int i = 0; i < this.cms.length; i++)
            res += sym + "                     "
                    + PrintUtils.bytes2Hex(this.cms[i].getM().toBytes()) + "\n";
        res += sym + "                 }\n";
        res += sym + "    Assertion:   " + this.resHash.toString() + "\n";
        if(ifhash)
            res += sym + "    hash:        " + PrintUtils.bytes2Hex(this.getHash()) + "\n";
        res += sym + "    timestamp:   " + this.getTimestamp() + "\n";
        res += sym + "}";
        return res;
    }

    public CHHashInTree.ResHash getResHash() {
        return resHash;
    }
}
