# RewritableBlockChain
using java implements an immutable blockchain system as scheme1 that mimics basic functionalities of blockchain system, and a rewritable blockchain system as scheme2 which is based on rewritable transactions. Both of them are based on Proof of Work (PoW) consensus. 

# Elgamal Group
The cryptography hash functions are based on a Elgamal Group. The group could be initialized with a bit-length.
```java
int bit_len = 128;
PrimeGroup primeGroup = PrimeGroup.newInstance(bit_len);
```
The group can be saved into a file for reuse.
```java
primeGroup.save2File(filePath);
primeGroup = PrimeGroup.newInstance(filePath);
```
The gruop generator can be obtain by the following code.
```java
Element g = primeGroup.getG();
```
An instance of Elgamal key pair generation function is followed.
```java
Element sk = primeGroup.newRandomZElement();
Element pk = g.pow(sk);
```

# Chameleon Hash
Chameleon Hash function provides that two different messages can be hashed into the same value with two different nonce using a secret key. In the scheme2, we use a message-controlled chameleon hash MCH based function to replace the SHA256 in scheme1. 
## Chameleon Hash 
The orign chameleon hash function setup.
```java
CHHashEngine ch = new CHHashEngine();
ch.init(primeGroup);
// setup with sk for generating hash values, finding collisions.
ch.setup(sk, true);
//setup with pk for generating hash values
ch.setup(pk, false);
//setup for sk extraction by two messages and two nonces
ch.setup(null, false);
```
chameleon hash generation.
```java
Element m;
Element nounce;
......
ch.setup(sk, true);
Element hash = ch.hash(m, nonce);
```
collision computation
```java
Element m2;
Element nonce2;
......
ch.setup(sk, true);
Element nonce2 = ch.collision(m, nonce, m2); // it makes ch.hash(m, nonce) = ch.hash(m2, nonce2)
```
sk extraction
```java
Element sk = ch.collision(m, nonce, m2, nonce2); 
```

## Message-Controlled Chameleon Hash (MCH)
The message-controlled chameleon hash generates a hash value by a binary tree.
The Chameleon Hash Tree generation
```java
int depth;      // the depth of the tree
int width = 2;  // the number of children of each node, we use a binary tree for set it 2
int range;      // the number of dummy message of each node
CHHashTree tree = CHHashTree.newInstance(depth, width, range, primeGroup);
```
MCH.keygen
```java
KeyGen kgen = new KeyGen(primeGroup);
// authority key pair
KeyGen.KeyPair authority = kgen.genKeyPair();
Element ausk = authority.getSK();
Element aupk = authority.getPK();
......
// user key pair
KeyGen.KeyPair user = kgen.genKeyPair();
Element usk = user.getSK();
Element upk = user.getPK();
......
// assertion key pair
CHHashInTree.AssertionKeyPair akpair = kgen.genAssertionKeyPair(tree.getRoot(), ausk, upk, false);
Element ask = akpair.getAsk();
CHHashInTree.AssertionKeyPair.PK apk = akpair.getApk();
```
MCH.MessageGen
```java
KeyGen.CandidateMessage cm1 = kgen.genCandidateMessage(group.newRandomZElement(), aupk, user);
KeyGen.CandidateMessage cm2 = kgen.genCandidateMessage(group.newRandomZElement(), aupk, user);
```
MCH.Setup
```java
CHHashInTree chash = new CHHashInTree();
chash.init(primeGroup, tree);
```
MCH.Hash
```java
chash.setup(aupk, usk, ask, 3);
CHHashInTree.ResHash rh1 = chash.genhash(cm1);
```
MCH.Verify
```java
chash.setup(null, null, apk, 0);
boolean flag = chash.verify(rh1);
```
MCH.Adapt
```java
chash.setup(ausk, upk, ask, 5);
CHHashInTree.ResHash rh2 = chash.adapt(rh1, cm2);
```
MCH.Correctness
```java
chash.setup(aupk, null, apk, 0);
boolean flag = chash.correct(rh1, rh2);
```
# Immutable Blockchain System
We use a `MinerThread` class to represent the miner parties of a blockchain system, and a `UserClient` class to demonstrate a user party
of a blockchain system. Both of them are threads to simulate processes in the network. A `MinerManageThread` class is implemented to perform broadcasting messages between miners. Besides, A `UserClientThread` class is implemented to perform broadcasting messages between users. There is also a `MinerManageThread.Transactions` class to simulate a tunnel between miners and users.
A sample code to run the system is showed in `smu.smc.jiaming.scheme1.Test`. It startups 10 miners and 1 user.
```sh
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
```
The `UserClient` class also provides a command line interface.
```sh
1.new TX 2.new TXes 3.show generates 4.send 5.quit
```
The instruction `1` is to generate a new transaction by the userClient. The instruction `2` is to generate a number `n` of new transactions, the `n` is entered following. The instruction `3` is to show the previous instruction generation result, it maybe a transaction or `n` trnsactions. The instruction `4` is to send previous generation result to the miniers. The instruction `5` is to shutdown the program.

# Rewritable Blockchain System
The scheme2 impelemntations is similar to the scheme1 excepts the transaction could be `RewritableTX`. We also demonstrates a sample code of a rewritable blockchain system.
```java
MinerManageThread.Transactions tx = new MinerManageThread.Transactions();
UserClient uc = new UserClient(aupk, user,
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
```
We Override the `UserClient` and `UserClientThread`. It also has a command line interface.
```sh
1.new TX 2.new RewritableTX 3.new MixedTXes 4.show generates 5.send 6.quit
```
The instruction `1` is the same as that in scheme1. The instruction `2` is to generate a new rewritable transaction by the userClient, the user needs to provide the number of candidate messages, e.g. `2 3`. The instruction `3` is to generate a number of new transactions, the user needs to provide the number of total transactions, the number of rewritable transactions, and the number of candidate messages of each rewritable transaction, e.g. `3 100 10 2` . The instruction `4` `5` `6` are the same as the instruction `3` `4` `5` in the scheme1, correspondingly.
