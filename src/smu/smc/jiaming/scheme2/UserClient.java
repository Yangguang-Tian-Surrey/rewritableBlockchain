package smu.smc.jiaming.scheme2;

import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme1.MinerManageThread;
import smu.smc.jiaming.scheme1.TX;
import smu.smc.jiaming.scheme2.CHSHash.CHHashInTree;
import smu.smc.jiaming.scheme2.CHSHash.KeyGen;
import smu.smc.jiaming.scheme2.CHSHash.tree.CHHashTree;

import java.math.BigInteger;

public class UserClient extends smu.smc.jiaming.scheme1.UserClient {

    private Element pk;
    private KeyGen.KeyPair eks;
    private CHHashInTree.AssertionKeyPair aks;
    private PrimeGroup G;
    private CHHashInTree chhash;

    public UserClient(MinerManageThread.Transactions tx) {
        super(tx);
    }

    public UserClient(Element pk, KeyGen.KeyPair eks,
                      CHHashInTree.AssertionKeyPair aks, CHHashTree tree, PrimeGroup G,
                      MinerManageThread.Transactions tx){
        super(tx);
        this.pk = pk;
        this.eks = eks;
        this.aks = aks;
        this.chhash = new CHHashInTree();
        this.chhash.init(G, tree);
        this.G = G;
    }



    public RewritableTX newRandomRTX(int n){
        TX t = this.newRandomTX();

        KeyGen kgen = new KeyGen(this.G);
        KeyGen.CandidateMessage[] cms = new KeyGen.CandidateMessage[n];
        for(int i = 0; i < n; i++)
            cms[i] = kgen.genCandidateMessage(this.G.newRandomZElement(), pk, eks);

        RewritableTX rtx = RewritableTX.newInstance(t.getReciever(),
                t.getData(), t.getTimestamp(), cms);
        this.chhash.clear();
        this.chhash.setup(pk, eks.getSk(), aks.getAsk(), 3);
        rtx.genAssertion(this.chhash, 0);
        rtx.toHash();
        return rtx;
    }


    public TX[] newRandomMixedTXes(int n, int p, int m){
        if(n < p)
            return null;
        TX[] tx = new TX[n];
        for (int i = 0; i < n; i++) {
            if(i < p)
                tx[i] = newRandomRTX(m);
            else
                tx[i] = newRandomTX();
        }
        return tx;
    }
}
