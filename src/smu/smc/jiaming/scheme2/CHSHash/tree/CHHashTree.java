package smu.smc.jiaming.scheme2.CHSHash.tree;


import smu.smc.jiaming.elgamal.Element;
import smu.smc.jiaming.elgamal.PrimeGroup;
import smu.smc.jiaming.scheme2.CHSHash.DataUtils;
import smu.smc.jiaming.scheme2.CHSHash.HashUtils;

import java.math.BigInteger;
import java.util.Random;

public class CHHashTree {
    private int depth;
    private Node root;
    private int width;
    private int range;
    private Random random;

    private CHHashTree(int depth, int width, int range, Node root, Random random) {
        this.depth = depth;
        this.root = root;
        this.width = width;
        this.range = range;
        this.random = random;
    }

    private static int[] randomIndex(int len, int range, Random random){
        int[] res = new int[len];
        for(int i = 0; i < len; i++){
            res[i] = random.nextInt(range);
        }
        return res;
    }

    public static CHHashTree newInstance(int depth, int width, int range, PrimeGroup G){
        if(depth <= 1) return null;
        Node root = new NoLeafNode(0, 0);
        if(buildTree(root, depth, width, range, G, G.newZeroZElement()))
            return new CHHashTree(depth, width, range, root, new Random());
        return null;
    }

    public int getDepth() {
        return depth;
    }

    private Node[] getNodes(Node root, int[] index){
        int len = index.length;
        int depth = root.getDepth();
        if(len > depth - 1)
            return null;
        if(len == 0)
            return new Node[]{root.copy()};
        Node[] res = new Node[len + 1];
        Node tmp = root;
        res[0] = root.copy();
        for(int i = 0; i < len; i++){
            tmp = ((NoLeafNode)tmp).getChildren()[index[i]];
            res[i+1] = tmp.copy();
        }
        return res;
    }

    public Path getRandomPath(){
        int[] index = randomIndex(this.depth - 1, this.width, this.random);
        Node[] nodes = this.getNodes(this.root, index);
        int[] eleIndex = randomIndex(this.depth, this.range, this.random);
        int junction = this.random.nextInt(this.depth - 1);
        return new Path(nodes, this.depth, eleIndex, junction);
    }

    public Path getAdaptPath(Path path){
        int junction = path.getJunction();
        if(junction >= depth - 1 || junction < 0)
            return null;
        if(!this.invalidPath(path))
            return null;
        int len = this.depth - junction - 1;

        Node[] nodes = new Node[this.depth];
        // int[] eleIndex = new int[this.depth];
        int[] eleIndex = randomIndex(this.depth, this.range, this.random);

        NoLeafNode junkNode = (NoLeafNode) root;

        // before junk node
        for(int i = 0; i < junction; i++){
            nodes[i] = path.nodes[i].copy();
            junkNode = (NoLeafNode) junkNode.getChildren()[path.nodes[i+1].getIndex()];
        }

        // junk node in tree
        /*if(junction > 0)
            junkNode = (NoLeafNode) junkNode.getChildren()[path.nodes[junction].getIndex()];*/

        int[] index = randomIndex(len, this.width, this.random);
        Node[] subNodes = this.getNodes(junkNode, index);

        // junk node
        nodes[junction] = subNodes[0];
        eleIndex[junction] = path.eleIndex[junction];

        // after junk node
        for(int i = junction + 1; i < this.depth; i++){
            nodes[i] = subNodes[i - junction];
        }

        return new Path(nodes, this.depth, eleIndex, junction);
    }

    public boolean invalidPath(Path path){
        if(path == null) return false;
        Node[] nodes = path.getNodes();
        int[] eleIndex = path.getEleIndex();
        int depth = path.getDepth();
        if(depth != this.depth) return false;
        if(depth != nodes.length || depth != eleIndex.length) return false;
        Node node = root;
        for(int i = 0 ; i < depth; i++){
            if(i != 0)
                node = ((NoLeafNode)node).getChildren()[nodes[i].getIndex()];
            if(!node.isValueEqual(nodes[i]))
                return false;
        }
        return true;
    }

    public static boolean buildTree(Node node, int depth, int width, int range, PrimeGroup G, Element base){
        DataUtils.Tuple[] dummy = new DataUtils.Tuple[range];
        node.setId(base.add(BigInteger.valueOf(node.getLevel())));
        for(int i = 0; i < range; i++){
            dummy[i] = new DataUtils.Tuple(
                    HashUtils.hashP(node.getId(), node.getIndex(),
                            (byte) 0, G.newZElement()),
                    HashUtils.hashP(node.getId(), node.getIndex(),
                            (byte) 1, G.newZElement()));
        }
        node.setDummy(dummy);

        int childLevel = node.getLevel() + 1;
        if(childLevel == depth)
            return true;
        boolean flag = true;
        Node[] children = new Node[width];
        base = base.add(BigInteger.valueOf(width).pow(node.getLevel()));
        for(int i = 0; i < width; i++){
            if(childLevel == depth - 1) {
                children[i] = new LeafNode(childLevel, i);
            }else{
                children[i] = new NoLeafNode(childLevel, i);
            }
            flag = flag & buildTree(children[i], depth, width, range, G, base);
        }
        if(flag)
            ((NoLeafNode)node).setChildren(children);
        return flag;
    }

    public Node getRoot() {
        return root;
    }

    public int getWidth() {
        return width;
    }

    public int getRange() {
        return range;
    }

    public Random getRandom() {
        return random;
    }

    public static class Path{
        private Node[] nodes;
        private int depth;
        private int[] eleIndex;
        private int junction;

        public Path(Node[] nodes, int depth, int[] eleIndex, int junction) {
            this.nodes = nodes;
            this.depth = depth;
            this.eleIndex = eleIndex;
            this.junction = junction;
        }

        public DataUtils.Tuple getDummy(int level){
            return this.nodes[level].getDummy()[this.eleIndex[level]];
        }

        public Node[] getNodes() {
            return nodes;
        }

        public int getDepth() {
            return depth;
        }

        public int[] getEleIndex() {
            return eleIndex;
        }

        public int getJunction() {
            return junction;
        }
    }
}
