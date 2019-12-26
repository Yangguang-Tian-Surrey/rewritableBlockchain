package smu.smc.jiaming.scheme1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block implements Cloneable {
    private final static String SHA256 = "SHA-256";
    private final static int MAXTX = 100;

    private long index;
    private long timestamp;
    private long nonce;
    private byte[] hash;
    private byte[] data;
    private byte[] preHash;

    private byte[] rootHash;
    private List<TX> txes;

    private String HashAlgorithm;

    private Block(long index, long timestamp, long nonce, byte[] hash, byte[] data,
                  byte[] preHash, byte[] rootHash, List<TX> txes, String HashAlgorithm) {
        this.index = index;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.hash = hash;
        this.data = data;
        this.preHash = preHash;
        this.rootHash = rootHash;
        this.txes = txes;
        this.HashAlgorithm = HashAlgorithm==null?SHA256:HashAlgorithm;
    }

    public static Block newNext(Block block, byte[] data){
        byte[] preHash = new byte[]{0x00};
        long index = 1;
        String Algorithm = null;
        if(block != null) {
            preHash = block.hash;
            index = block.index + 1;
            Algorithm = block.HashAlgorithm;
        }
        long timestamp = System.currentTimeMillis();
        return newInstance(index, timestamp, 0, null, data, preHash, Algorithm);
    }

    public static Block newInstance(long index, long timestamp, long nonce, byte[] hash,
                                    byte[] data, byte[] preHash, byte[] rootHash,
                                    List<TX> txes, String HashAlgorithm) {
        return new Block(index, timestamp, nonce, hash, data,
                preHash, rootHash, txes, HashAlgorithm);
    }

    public static Block newInstance(long index, long timestamp, long nonce, byte[] hash,
                                    byte[] data, byte[] preHash, String HashAlgorithm) {
        return new Block(index, timestamp, nonce, hash, data,
                preHash, null, new ArrayList<TX>(), HashAlgorithm);
    }

    public void creaseNonce(){
        this.nonce++;
    }

    public byte[] toHash() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HashAlgorithm);
        return toHash(md);
    }

    public byte[] toHash(MessageDigest md){
        md.reset();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bs = null;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeLong(index);
            oos.writeLong(timestamp);
            oos.writeLong(nonce);
            if(data != null)
                oos.write(data);
            oos.write(preHash);
            oos.write(rootHash);
            oos.flush();
            bs = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        md.update(bs);
        return md.digest();
    }

    public byte[] genRootHash() throws NoSuchAlgorithmException {
        if(txes == null)
            return null;
        if(txes.size() == 0)
            return null;
        int len = txes.size();
        int offset = 0;
        byte[][] hashes = new byte[len][];
        for(int i = 0; i < len; i++)
            hashes[i] = txes.get(i).getHash();

        MessageDigest md = MessageDigest.getInstance(this.HashAlgorithm);

        while(len > 1){
            md.reset();
            offset = len%2;
            len = len/2;
            for(int i = 0; i < len; i++){
                md.update(hashes[2*i]);
                md.update(hashes[2*i + 1]);
                hashes[i] = md.digest();
            }
            if(offset == 1) {
                hashes[len] = hashes[2 * len];
                len++;
            }
        }
        return hashes[0];
    }

    public boolean isValid(Block preBlock){
        if(preBlock == null){
            if(this.index != 1)
                return false;
            if(!Arrays.equals(this.preHash, new byte[]{0x00}))
                return false;
        }else{
            if(this.index != preBlock.index + 1)
                return false;
            if(!Arrays.equals(this.preHash, preBlock.hash))
                return false;
        }
        try {
            if(!Arrays.equals(this.toHash(), this.hash))
                return false;
            if(!Arrays.equals(this.genRootHash(), this.rootHash))
                return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return true;
    }

    public byte[] getPreHash() {
        return preHash;
    }

    public byte[] getHash() {
        return hash;
    }

    public synchronized void acceptTX(TX tx) {
        byte[] hash = tx.getHash();
        if(!Arrays.equals(hash, tx.toHash()))
            return;
        this.txes.add(tx);
        if(this.txes.size() >= MAXTX)
            this.notifyAll();
    }

    public synchronized void acceptTXes(TX[] txes) {
        byte[] hash;
        for(int i = 0 ; i < txes.length; i++) {
            hash = txes[i].getHash();
            if(!Arrays.equals(hash, txes[i].toHash()))
                continue;
            this.txes.add(txes[i]);
        }
        if(this.txes.size() >= MAXTX)
            this.notifyAll();
    }

    @Override
    protected Block clone() throws CloneNotSupportedException {
        return (Block) super.clone();
    }

    public String getHashAlgorithm() {
        return HashAlgorithm;
    }

    public List<TX> getTxes() {
        return txes;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public void setRootHash(byte[] rootHash) {
        this.rootHash = rootHash;
    }

    public long getNonce() {
        return nonce;
    }

    public String toString(){
        String res = "";
        res += "{\n";
        res += "    index:        " + this.index + "\n";
        res += "    nonce:        " + this.nonce + "\n";
        res += "    timestamp:    " + this.timestamp + "\n";
        res += "    hash:         " + PrintUtils.bytes2Hex(this.hash) + "\n";
        res += "    rootHash:     " + PrintUtils.bytes2Hex(this.rootHash) + "\n";
        res += "    preHash:      " + PrintUtils.bytes2Hex(this.preHash) + "\n";
        res += "}";
        return res;
    }

    public long getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getRootHash() {
        return rootHash;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setPreHash(byte[] preHash) {
        this.preHash = preHash;
    }

    public void setTxes(List<TX> txes) {
        this.txes = txes;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        HashAlgorithm = hashAlgorithm;
    }

    public void setTimeStamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
