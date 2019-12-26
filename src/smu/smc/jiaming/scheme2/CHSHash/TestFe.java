package smu.smc.jiaming.scheme2.CHSHash;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;

public class TestFe {
    // fe
    private static Element getCHKey(Element ask, Element[] alpha, Element m, int level, int position, int depth, Element fe){
        Element e = getEl(m, level, position, fe);
        // fe = sk + sum(1->k)(alpha*(e^j))
        Element sum = ask;
        for(int i = 0; i < depth; i++){
            sum = sum.add(alpha[i].mul(e.pow(i)));
        }

        return sum;
    }

    // Fe
    private static Element getCHKeyp(Element apk, Element[] C, Element m, int level, int position, int depth, Element fe){
        Element e = getEl(m, level, position, fe);
        // Fe = pk * product(1->k)((g^alpha)^(e^j))
        Element product = apk;
        for(int j = 0; j < depth; j++){
            product = product.mul(C[j].pow(e.pow(j)));
        }
        return product;
    }

    // el
    private static Element getEl(Element m, int level, int position, Element e){
        if(HashUtils.mdSHA256 == null)
            return null;
        HashUtils.mdSHA256.reset();
        // e = H(m, level, position)
        HashUtils.mdSHA256.update(m.toBytes());
        HashUtils.mdSHA256.update(int2bytes(level));
        HashUtils.mdSHA256.update(int2bytes(position));
        byte[] eh = HashUtils.mdSHA256.digest();
        e.setBytes(eh);
        return e;
    }


    private static byte[] int2bytes(int n){
        return new byte[]{(byte) (n & 0xff), (byte) (n >> 8 & 0xff),
                (byte) (n >> 16 & 0xff), (byte) (n >> 32 & 0xff)};
    }

    public static void test(){
        int n = 10;
        PrimeGroup group = PrimeGroup.newInstance("group");
        KeyGen kgen = new KeyGen(group);
        Element[] c = new Element[n];
        Element[] gc = new Element[n];
        KeyGen.KeyPair party = null;
        for(int i = 0 ; i < n; i++){
            party = kgen.genKeyPair();
            c[i] = party.getSk();
            gc[i] = party.getPk();
        }

        party = kgen.genKeyPair();
        Element ask = party.getSk();
        Element apk = party.getPk();

        Element m = group.newRandomZElement();

        Element fe = group.newZElement();

        Element e = getCHKey(ask, c, m, 4, 8, n, fe);
        Element ge = getCHKeyp(apk, gc, m, 4, 8, n, fe);
        System.out.println(ge.isEqual(group.getG().pow(e)));

    }

    public static void main(String[] args){
        test();
    }

}
