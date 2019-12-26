package smu.smc.jiaming.scheme2.CHSHash;


import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme2.CHSHash.tree.CHHashTree;
import smu.smc.jiaming.scheme2.CHSHash.tree.Node;

public class CHHashInTree {
    private PrimeGroup G;
    private Element ask;
    private AssertionKeyPair.PK apk;
    private Element pk;
    private Element sk;
    private Element esk;
    private Element epk;
    private boolean hasEsk = false;
    private boolean hasSk = false;
    private boolean hasAsk = false;
    private CHHashTree tree;
    private int depth;

    public void init(PrimeGroup G, CHHashTree tree){
        this.G = G;
        this.tree = tree;
        this.depth = tree.getDepth();
    }

    // deep
    public void reset(){
        this.G = null;
        this.tree = null;
        this.depth = 0;
        this.clear();
    }

    // shallow
    public void clear(){
        this.ask = null;
        this.apk = null;
        this.pk = null;
        this.sk = null;
        this.esk = null;
        this.epk = null;
        this.hasEsk = false;
        this.hasSk = false;
        this.hasAsk = false;
    }

    // for hash/adapt
    public void setup(Element k, Element ek, Element ask, int flag) {
        this.hasSk = (flag >> 2 & 0x01) == 1 && k != null;
        this.hasEsk = (flag >> 1 & 0x01) == 1 && ek != null;
        this.hasAsk = (flag & 0x01) == 1 && ask != null;
        if(this.hasSk)
            this.sk = k;
        else
            this.pk = k;
        if(this.hasEsk)
            this.esk = ek;
        else
            this.epk = ek;
        if(this.hasAsk)
            this.ask = ask;
    }

    public void setup(Element k, Element ek, AssertionKeyPair.PK apk, int flag){
        this.hasSk = (flag >> 2 & 0x01) == 1 && k != null;
        this.hasEsk = (flag >> 1 & 0x01) == 1 && ek != null;
        this.hasAsk = !((flag & 0x01) == 0 || apk != null);
        if(this.hasSk)
            this.sk = k;
        else
            this.pk = k;
        if(this.hasEsk)
            this.esk = ek;
        else
            this.epk = ek;
        this.apk = apk;
    }

    // hash
    public ResHash genhash(KeyGen.CandidateMessage cm){

        // check pk
        if(this.hasSk) {
            System.out.println("hasSk is false!");
            return null;
        }
        // check esk
        if(!this.hasEsk) {
            System.out.println("hasEsk is false!");
            return null;
        }
        // check ask
        if(!this.hasEsk) {
            System.out.println("hasAsk is false!");
            return null;
        }

        if(this.pk == null){
            System.out.println("can not find a pk!");
            return null;
        }
        if(this.esk == null){
            System.out.println("can not find a esk!");
            return null;
        }
        Element g = this.G.getG();
        if(!g.pow(this.esk).isEqual(cm.getC().getX())) {
            System.out.println("the esk is not competible to the cm.epk!");
            return null;
        }
        // check ask
        if(this.ask == null){
            System.out.println("can not find a ask!");
            return null;
        }
        if(!this.ask.isEqual(HashUtils.hashEles(this.pk.pow(this.esk), this.G.newZElement()))){
            System.out.println("the ask is not competible to the esk!");
            return null;
        }

        Element epk = cm.getC().getX();
        Element zc = cm.getC().getY();

        // message
        Element m = cm.getM();
        // retieve the z
        // decrypt zc
        Element z = zc.mul(this.pk.pow(this.esk).invert());
        z = this.G.newZElement().set(z);

        // gen path
        CHHashTree.Path path = this.tree.getRandomPath();
        // eleIndex
        int[] eleIndex = path.getEleIndex();

        // k-size coefficients
        Element[] alpha = new Element[this.depth];
        Element[] alpha_c = new Element[this.depth];
        byte[][] alpha_xor = new byte[this.depth][];

        for(int i = 0; i < depth; i++){
            alpha[i] = this.G.newRandomZElement();
            alpha_c[i] = g.pow(alpha[i]);
            alpha_xor[i] = alpha[i].xor(pk.pow(this.esk));
        }

        DataUtils.Tuple mz = new DataUtils.Tuple(m, z);
        Assertion.Tuple[] tuples = this.doFinal(path, mz, alpha, alpha_c, alpha_xor);

        Assertion tao = new Assertion(tuples, path);

        // R
        Element R = this.G.newRandomZElement();
        R = this.G.newRandomGElement().set(R);
        // Ri
        Element Ri = this.pk.pow(R);
        // h = g^(fe*m)*g^(s*R)
        int junction = path.getJunction();
        Element fe = this.getCHKey(this.ask, alpha, path.getDummy(junction).getX(), junction,
                path.getEleIndex()[junction], this.depth);
        Element h = g.pow(fe.mul(m)).mul(Ri);
        //Element h = tuples[junction].getFe().pow(m).mul(Ri);
        // after hash Candidate Message
        Element rc = R.mul(pk.pow(this.esk));
        Element gz = g.pow(z);

        DataUtils.Tuple tm = new DataUtils.Tuple(m, gz);
        DataUtils.Tuple C = new DataUtils.Tuple(epk, rc);

        ResHash rh = new ResHash(h, tm, C, Ri, tao, alpha_c, alpha_xor);
        return rh;
    }

    private Assertion.Tuple[] doFinal(CHHashTree.Path path, DataUtils.Tuple mz,
                            Element[] alpha, Element[] alpha_c, byte[][] alpha_xor){
        Element m = mz.getX();
        Element z = mz.getY();

        // eleIndex
        int[] eleIndex = path.getEleIndex();

        // Y(el)
        Element[] fe = new Element[this.depth];
        // F(el)
        Element[] Fe = new Element[this.depth];

        CHHashEngine cht = new CHHashSEngine();
        cht.init(this.G);
        CHHashEngine ch = new CHHashEngine();
        ch.init(this.G);
        CHHashEngine chs;
        Element Z = m;
        Element x;
        Element xi;
        Element ri;
        Element r;
        Element[] Y;
        Element y;
        Assertion.Tuple[] tuples = new Assertion.Tuple[depth];
        // from leaf to root
        for(int i = depth - 1; i >= 0; i--){

            DataUtils.Tuple[] tuple = path.getNodes()[i].getDummy();
            xi = path.getDummy(i).getX();
            ri = path.getDummy(i).getY();

            x = i == depth - 1? m : xi;
            fe[i] = this.getCHKey(this.ask, alpha, x, i,
                    eleIndex[i], this.depth);
            Fe[i] = this.G.getG().pow(fe[i]);

            ch.clear();
            ch.setup(fe[i], true);

            if(i == this.depth - 1){
                chs = cht;
                chs.clear();
                ((CHHashSEngine)chs).setup(fe[i], z, true);
            }else{
                chs = ch;
            }
            r = chs.collision(xi, ri, Z);
            // Yj = H(ch(Z,y))
            y = chs.hash(Z, r);

            int range = tuple.length;
            Y = new Element[range];
            for(int j = 0; j < range; j++){
                if(j != eleIndex[i])
                    Y[j] = ch.hash(tuple[j].getX(), tuple[j].getY());
                else
                    Y[j] = y;
            }
            Z = HashUtils.hashEles(Y, this.G.newZElement());
            Y[eleIndex[i]] = Fe[i];
            tuples[i] = new Assertion.Tuple(r, Y, eleIndex[i]);
        }

        return tuples;
    }

    public boolean verify(ResHash rh){
        Element gsk;
        if(this.hasAsk){
            gsk = this.G.getG().pow(this.ask);
        }else if(this.apk == null){
            System.out.println("can not find an ask or apk!");
            return false;
        }else{
            gsk = this.apk.getGsk();
        }

        Element m = rh.M.getX();
        Element gz = rh.M.getY();

        // h == g^(Fe*m)
        Element Fe = rh.assertion.tuples[rh.assertion.path.getJunction()].getFe();
        if(!rh.h.isEqual(Fe.pow(m).mul(rh.R))) {
            System.out.println("verify h failed!");
            return false;
        }

        // verify secret key shares from leaf node to root node
        Element product, el;
        int pos;
        for(int i = 0; i < this.depth; i++){
            Fe = rh.assertion.tuples[i].getFe();
            el = i == this.depth - 1? m:rh.getAssertion().path.getDummy(i).getX();
            pos = rh.getAssertion().tuples[i].eleIndex;
            product = this.getCHKeyp(gsk, rh.getAlpha_c(), el, i, pos, this.depth);

            if(!product.isEqual(Fe)) {
                System.out.println("verify chemonleon secret key failed!");
                return false;
            }
        }

        /**
         * verify the chemoleon hash tree
         * using chemoleon hash pk = Fe and zpk = g^z
         * **/
        // verify H(y1, y2, ... ) = Z
        CHHashEngine cht = new CHHashSEngine();
        cht.init(this.G);
        CHHashEngine ch = new CHHashEngine();
        ch.init(this.G);
        CHHashEngine chs;
        Element Z = m;
        Element x;
        Element xi;
        Element ri;
        Element r;
        Element[] Y;
        Element y;
        int eleIndex;
        // from leaf to root
        for(int i = depth - 1; i >= 0; i--){

            DataUtils.Tuple[] tuple = rh.assertion.path.getNodes()[i].getDummy();
            xi = rh.assertion.path.getDummy(i).getX();
            ri = rh.assertion.path.getDummy(i).getY();

            x = i == depth - 1? m : xi;
            Fe = this.getCHKeyp(gsk, rh.getAlpha_c(), x, i,
                    rh.getAssertion().tuples[i].eleIndex, this.depth);

            ch.clear();
            ch.setup(Fe, false);

            if(i == this.depth - 1){
                chs = cht;
                chs.clear();
                ((CHHashSEngine)chs).setup(Fe, gz, false);
            }else{
                chs = ch;
            }
            r = rh.assertion.tuples[i].r;
            // Yj = H(ch(Z,y))
            y = chs.hash(Z, r);

            int range = tuple.length;
            eleIndex = rh.assertion.tuples[i].eleIndex;
            Y = new Element[range];
            for(int j = 0; j < range; j++){
                if(j != eleIndex)
                    Y[j] = ch.hash(tuple[j].getX(), tuple[j].getY());
                else
                    Y[j] = y;
            }
            Z = HashUtils.hashEles(Y, this.G.newZElement());

        }

        // root node's dummy's chhashes Z
        Element Z0 = this.G.newZElement();
        Node root = rh.assertion.path.getNodes()[0];
        Fe = this.getCHKeyp(gsk, rh.getAlpha_c(),
                rh.assertion.path.getDummy(0).getX(), 0,
                rh.assertion.path.getEleIndex()[0], this.depth);
        ch.clear();
        ch.setup(Fe, false);
        Y = new Element[root.getDummy().length];
        for(int i = 0 ; i < root.getDummy().length; i++){
            Y[i] = ch.hash(root.getDummy()[i].getX(), root.getDummy()[i].getY());
        }
        Z0 = HashUtils.hashEles(Y, Z0);

        if(!Z0.isEqual(Z)) {
            System.out.println("verify chemonleon hash tree failed!");
            System.out.println(Z0);
            System.out.println(Z);
            return false;
        }
        return true;
    }

    // adapt
    public ResHash adapt(ResHash rh, KeyGen.CandidateMessage cm){
        if(!this.verify(rh)){
            return null;
        }

        Element pk = this.hasSk?this.G.getG().pow(this.sk):this.pk;

        Element[] alpha = new Element[this.depth];
        byte[][] C_xor = rh.alpha_xor;
        // c_c = epk^sk
        Element c_c = cm.getC().getX().pow(this.sk);
        for(int i = 0; i < this.depth; i++){
            alpha[i] = this.G.newZElement().set(c_c.xor(C_xor[i]));
        }


        Element g = this.G.getG();

        Element epk = cm.getC().getX();
        Element zc = cm.getC().getY();

        // message
        Element m = cm.getM();
        // retieve the z
        // decrypt zc
        Element z = this.G.newZElement().set(zc.mul(this.epk.pow(this.sk).invert()));

        // generate adapt path
        CHHashTree.Path path = this.tree.getAdaptPath(rh.assertion.path);
        int junction = path.getJunction();

        DataUtils.Tuple mz = new DataUtils.Tuple(m, z);
        Assertion.Tuple[] tuples = this.doFinal(path, mz, alpha, rh.alpha_c, rh.alpha_xor);

        Assertion tao = new Assertion(tuples, path);

        // R
        Element R = this.G.newZElement().set(
                rh.C.getY().mul(rh.C.getX().invert().pow(this.sk)));
        Element fej = this.getCHKey(this.ask, alpha, path.getDummy(junction).getX(), junction,
                path.getEleIndex()[junction], this.depth);
        R = R.add(rh.M.getX().sub(cm.getM()).mul(fej).div(this.sk));

        // Ri
        Element Ri = pk.pow(R);
        // h = g^(fej*m)*g^(s*R)
        Element h = g.pow(fej.mul(m)).mul(Ri);
        // after hash Candidate Message
        Element rc = this.G.newGElement().set(R).mul(this.epk.pow(this.sk));
        Element gz = g.pow(z);
        DataUtils.Tuple tm = new DataUtils.Tuple(m, gz);
        DataUtils.Tuple C = new DataUtils.Tuple(epk, rc);

        ResHash rrh = new ResHash(h, tm, C, Ri, tao, rh.alpha_c, rh.alpha_xor);
        return rrh;
    }

    public boolean correct(ResHash rh1, ResHash rh2){
        /*if(! (this.verify(rh1) && this.verify(rh2))){
            return false;
        }*/

        CHHashTree.Path p1 = rh1.assertion.path;
        CHHashTree.Path p2 = rh2.assertion.path;
        if(p1.getDepth() < p2.getDepth()){
            p1 = p2;
            p2 = rh1.assertion.path;
        }

        if(p1.getJunction() != p2.getJunction())
            return false;
        int junction = p1.getJunction();
        if(p1.getEleIndex()[junction] != p2.getEleIndex()[junction])
            return false;
        if(p1.getNodes()[junction].getIndex() != p2.getNodes()[junction].getIndex())
            return false;
        if(!rh1.h.isEqual(rh2.h))
            return false;
        return true;
    }

    // fe
    private Element getCHKey(Element ask, Element[] alpha, Element m, int level, int position, int depth){
        Element e = this.getEl(m, level, position);
        // fe = sk + sum(1->k)(alpha*(e^j))
        Element sum = ask;
        for(int i = 0; i < depth; i++){
            sum = sum.add(alpha[i].mul(e.pow(i)));
        }

        return sum;
    }

    // Fe
    private Element getCHKeyp(Element apk, Element[] C, Element m, int level, int position, int depth){
        Element e = this.getEl(m, level, position);
        // Fe = pk * product(1->k)((g^alpha)^(e^j))
        Element product = apk;
        for(int j = 0; j < depth; j++){
            product = product.mul(C[j].pow(e.pow(j)));
        }
        return product;
    }

    // el
    private Element getEl(Element m, int level, int position){
        if(HashUtils.mdSHA256 == null)
            return null;
        HashUtils.mdSHA256.reset();
        // e = H(m, level, position)
        HashUtils.mdSHA256.update(m.toBytes());
        HashUtils.mdSHA256.update(HashUtils.int2bytes(level));
        HashUtils.mdSHA256.update(HashUtils.int2bytes(position));
        byte[] eh = HashUtils.mdSHA256.digest();
        Element e = this.G.newZElementFromBytes(eh);
        return e;
    }

    public static class Assertion{
        private Tuple[] tuples;
        private CHHashTree.Path path;

        public Assertion(Tuple[] tuples, CHHashTree.Path path) {
            this.tuples = tuples;
            this.path = path;
        }

        public static class Tuple{
            private Element r;
            private Element[] Y;
            private int eleIndex;

            public Tuple(Element r, Element[] Y, int eleIndex) {
                this.r = r;
                this.Y = Y;
                this.eleIndex = eleIndex;
            }

            public Element getFe(){
                return Y[eleIndex];
            }

            public Element getR() {
                return r;
            }

            public Element[] getY() {
                return Y;
            }

            public int getEleIndex() {
                return eleIndex;
            }
        }
    }

    public static class AssertionKeyPair{
        private Element ask;
        private PK apk;

        public AssertionKeyPair(Element ask, PK apk) {
            this.ask = ask;
            this.apk = apk;
        }

        public AssertionKeyPair(Element ask, Element gsk, Element z) {
            this.ask = ask;
            this.apk = new PK(gsk, z);
        }

        public Element getAsk() {
            return ask;
        }

        public PK getApk() {
            return apk;
        }

        public static class PK{
            private Element gsk;
            private Element Z;

            public PK(Element gsk, Element z) {
                this.gsk = gsk;
                this.Z = z;
            }

            public Element getGsk() {
                return gsk;
            }

            public Element getZ() {
                return Z;
            }
        }
    }

    public static class ResHash{
        private Element h;
        private Element R;
        private DataUtils.Tuple C;
        private DataUtils.Tuple M;
        private Assertion assertion;
        private Element[] alpha_c;
        private byte[][] alpha_xor;

        public ResHash(Element h, Element m, Element zc, Element epk, DataUtils.Tuple C, Element R, Assertion assertion,
                       Element[] alphac, byte[][] alpha_xor) {
            this.h = h;
            this.R = R;
            this.M = new DataUtils.Tuple(m, zc);
            this.C = C;
            this.assertion = assertion;
            alpha_c = alphac;
            this.alpha_xor = alpha_xor;
        }
        public ResHash(Element h, DataUtils.Tuple M, DataUtils.Tuple C, Element R, Assertion assertion,
                       Element[] alphac, byte[][] alpha_xor) {
            this.h = h;
            this.R = R;
            this.M = M;
            this.C = C;
            this.assertion = assertion;
            alpha_c = alphac;
            this.alpha_xor = alpha_xor;
        }

        public Element getH() {
            return h;
        }

        public Element getR() {
            return R;
        }

        public DataUtils.Tuple getC() {
            return C;
        }

        public DataUtils.Tuple getM() {
            return M;
        }

        public Assertion getAssertion() {
            return assertion;
        }

        public Element[] getAlpha_c() {
            return alpha_c;
        }

        public byte[][] getAlpha_xor() {
            return alpha_xor;
        }
    }
}
