import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class BTree {
    private static final double minFillFactor = 0.5;
    private static final int fanout = 5;
    // In this project, record id is static
    private static final int rid = 0;

    private Node root = null;
    private int height = 0;


    private static abstract class Node {
        String[] keys = new String[fanout - 1];

        double fillFactor() {
            int count = 0;
            for (String k : keys) {
                if (k == null) break;
                count++;
            }
            System.out.println("current fillFactor: " + ((double) count / keys.length));
            return (double) count / keys.length;
        }

        boolean isFull() {
            return keys[keys.length - 1] != null;
        }

        void sort() {
            Arrays.sort(keys);
        }

        void shiftLeft() {
            shiftLeft(0);
        }

        void shiftLeft(int start) {
            Util.shiftLeft(keys, start);
        }

        void shiftRight() {
            shiftRight(0);
        }

        void shiftRight(int start) {
            Util.shiftRight(keys, start);
        }

        abstract List<String> search(Node node, String key1, String key2);
    }

    private static class IndexNode extends Node {
        Node[] childNodes = new Node[fanout];

        @Override
        void shiftLeft(int start) {
            super.shiftLeft(start);
            Util.shiftLeft(childNodes, start);
        }

        @Override
        void shiftRight(int start) {
            super.shiftRight(start);
            Util.shiftRight(childNodes, start);
        }

        void shiftRightKey(int start) {
            super.shiftRight(start);
            Util.shiftRight(keys, start);
        }

        int insert(String key) {
            // move keys and childNodes to right
            if (keys[0] == null) {
                keys[0] = key;
                return 0;
            }

            int i = 0;

            // 1 2 5 6 <-
            while (i < keys.length) {
                if (keys[i] == null) {
                    keys[i] = key;
                    return i;
                }
                if (key.compareTo(keys[i]) < 0) {
                    Util.shiftRight(keys, i);
                    Util.shiftRight(childNodes, i + 1);
                    keys[i] = key;
                    return i;
                }
                i++;
            }
            return 0;
        }

        @Override
        List<String> search(Node node, String key1, String key2) {
            if (node instanceof LeafNode) {
                LeafNode ln = (LeafNode) node;
                return ln.search(node, key1, key2);
            }

            IndexNode in = (IndexNode) node;
            for (int i = 0; i < in.keys.length; i++) {
                if (key1.compareTo(in.keys[i]) < 0) {
                    return search(in.childNodes[i], key1, key2);
                } else if (key1.compareTo(in.keys[i]) == 0 || i == in.keys.length - 1 || in.keys[i + 1] == null) {
                    return search(in.childNodes[++i], key1, key2);
                }
            }
            return new LinkedList<>();
        }
    }

    private static class LeafNode extends Node {
        LeafNode leftLeafNode;
        LeafNode rightLeafNode;
        Integer[] rids = new Integer[fanout - 1];

        void delete(String key) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == null) break;

                if (keys[i].equals(key)) {
                    shiftLeft(i);
                    return;
                }
            }
            throw new KeyNotFoundException(key);
        }

        @Override
        void shiftLeft(int start) {
            super.shiftLeft(start);
            Util.shiftLeft(rids, start);
        }

        @Override
        void shiftRight(int start) {
            super.shiftRight(start);
            Util.shiftRight(rids, start);
        }

        boolean keyIsFull() {
            return keys[keys.length - 1] != null;
        }

        void insert(String key) {
            if (keys[0] == null) {
                keys[0] = key;
                return;
            }
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == null) {
                    keys[i] = key;
                    return;
                } else if (key.compareTo(keys[i]) < 0) {
                    shiftRight(i);
                    keys[i] = key;
                    return;
                }
            }
        }

        @Override
        List<String> search(Node node, String key1, String key2) {
            List<String> result = new LinkedList<>();
            LeafNode ln = (LeafNode) node;
            while (ln != null) {
                for (String k : ln.keys) {
                    if (k == null) break;

                    if (key2.compareTo(k) < 0)
                        return result;

                    if (key1.compareTo(k) <= 0 && key2.compareTo(k) >= 0)
                        result.add(k);
                }
                ln = ln.rightLeafNode;
            }
            return result;
        }
    }

    public BTree(String filename) throws FileNotFoundException {
        System.out.println("Building an initial B+-tree... Launching the B+-tree test program...");
        File f = new File(filename);
        Scanner s = new Scanner(f);
        int i = 0;
        while (s.hasNextLine()) {
            System.out.println("round: " + (++i));
            if (i == 16) break;
            insert(s.nextLine());
        }
        s.close();
    }

    public BTree() {}

    void insert(String key) {
        if (root == null) {
            root = new LeafNode();
            ((LeafNode) root).insert(key);
            return;
        }
        insert(root, key);
    }

    Node insert(Node node, String key) {
        if (node instanceof LeafNode) {
            if (node.isFull()) {
                if (node == root) {
                    IndexNode newRoot = new IndexNode();
                    Node newLeaf = splitLeafNode((LeafNode) node, key);

                    newRoot.childNodes[0] = ((LeafNode) node);
                    newRoot.childNodes[1] = newLeaf;
                    newRoot.keys[0] = newLeaf.keys[0];
                    root = newRoot;

                    return newRoot;
                }

                return splitLeafNode((LeafNode) node, key);
            } else {
                ((LeafNode) node).insert(key);
                return null;
            }
        }

        IndexNode in = (IndexNode) node;

        Node childNode = null;
        int i = 0;
        for (; i < in.keys.length; i++) {
            if (key.compareTo(in.keys[i]) < 0) {
                childNode = insert(in.childNodes[i], key);
                break;
            } else if (key.compareTo(in.keys[i]) == 0 || i == in.keys.length - 1 || in.keys[i + 1] == null) {
                childNode = insert(in.childNodes[i + 1], key);
                break;
            }
        }

        // check overflow

        if (childNode != null) {
            // handle overflow
            if (!node.isFull()) {
                int index = ((IndexNode) node).insert(childNode.keys[0]);
                ((IndexNode) node).childNodes[index + 1] = childNode;

            } else {
                // if full
                if (node == root) {
                    root = new IndexNode();
                    Node newRightNode = splitIndexNode((IndexNode) node, childNode);
                    ((IndexNode) root).childNodes[0] = node;
                    ((IndexNode) root).childNodes[1] = newRightNode;
                    root.keys[0] = newRightNode.keys[0];
                    return root;
                } else {
                    return splitIndexNode((IndexNode) node, childNode);
                }
            }
        } else {
            return null;
        }
        return null;
    }

    Node splitLeafNode(LeafNode fullLeafNode, String key) {
        LeafNode newLeafNode = new LeafNode();
        int mid = fanout / 2;

        // 2, 3 -> 0, 1
        for (int i = mid; i < fullLeafNode.keys.length; i++) {
            newLeafNode.keys[i - mid] = fullLeafNode.keys[i];
            newLeafNode.rids[i - mid] = fullLeafNode.rids[i];

            fullLeafNode.keys[i] = null;
            fullLeafNode.rids[i] = null;
        }

        if (key.compareTo(fullLeafNode.keys[mid - 1]) < 0) {
            fullLeafNode.insert(key);
            newLeafNode.insert(fullLeafNode.keys[mid]);
            fullLeafNode.keys[mid] = null;
        } else {
            newLeafNode.insert(key);
        }

        fullLeafNode.rightLeafNode = newLeafNode;
        newLeafNode.leftLeafNode = fullLeafNode;

        return newLeafNode;
    }

    Node splitIndexNode(IndexNode fullIndexNode, Node childNode) {
        System.out.println("inside splitIndexNode");
        IndexNode newIndexNode = new IndexNode();
        int mid = fanout / 2;

        for (int i = mid; i < fullIndexNode.keys.length; i++) {
            newIndexNode.keys[i - mid] = fullIndexNode.keys[i];
            newIndexNode.childNodes[i - mid] = fullIndexNode.childNodes[i];

            fullIndexNode.keys[i] = null;
            fullIndexNode.childNodes[i] = null;
        }

        if (childNode.keys[0].compareTo(fullIndexNode.keys[mid - 1]) < 0) {
            fullIndexNode.insert(childNode.keys[0]);
            fullIndexNode.childNodes[fullIndexNode.insert(childNode.keys[0])] = childNode;
            newIndexNode.insert(fullIndexNode.keys[mid]);
            newIndexNode.childNodes[newIndexNode.insert(fullIndexNode.keys[mid])] = fullIndexNode.childNodes[mid];
            fullIndexNode.keys[mid] = null;
            fullIndexNode.childNodes[mid] = null;
        } else {
            newIndexNode.insert(childNode.keys[0]);
            newIndexNode.childNodes[newIndexNode.insert(childNode.keys[0])] = childNode;
        }

        return newIndexNode;
    }

    private void splitRootNode(Node root) {
        // split and return ?
    }

    private void splitInternalNode() {

    }

    private void splitLeafNode() {

    }

    public void delete(String key) {
        if (root == null) return;
        delete(key, root);
    }

    private void delete(String key, Node node) {
        if (node instanceof LeafNode) {
            ((LeafNode) node).delete(key);
            return;
        }

        IndexNode in = (IndexNode) node;

        int i = 0;

        for (; i < in.keys.length; i++) {
            if (key.compareTo(in.keys[i]) < 0) {
                delete(key, in.childNodes[i]);
                break;
            } else if (key.compareTo(in.keys[i]) == 0 || i == in.keys.length - 1 || in.keys[i + 1] == null) {
                delete(key, in.childNodes[++i]);
                break;
            }
        }

        Node currentNode = in.childNodes[i];

        // Check fill factor
        // Not underflow
        if (currentNode.fillFactor() >= minFillFactor)
            return;

        Node leftSibling = null;
        Node rightSibling = null;
        if (i - 1 >= 0)
            leftSibling = in.childNodes[i - 1];

        if (i + 1 < node.keys.length)
            rightSibling = in.childNodes[i + 1];

        // Underflow
        // If have left Sibling
        if (leftSibling != null && leftSibling.fillFactor() - (double) 1 / (fanout - 1) >= minFillFactor) {
            for (int j = 0; j < leftSibling.keys.length; j++) {
                // Find last item
                if (j == leftSibling.keys.length - 1 || leftSibling.keys[j + 1] == null) {
                    currentNode.shiftRight();
                    currentNode.keys[0] = leftSibling.keys[j];

                    if (currentNode instanceof LeafNode) {
                        ((LeafNode) currentNode).rids[0] = ((LeafNode) leftSibling).rids[j];
                        ((LeafNode) leftSibling).rids[j] = null;
                    } else if (currentNode instanceof IndexNode) {
                        ((IndexNode) currentNode).childNodes[0] = ((IndexNode) leftSibling).childNodes[j + 1];
                        ((IndexNode) leftSibling).childNodes[j + 1] = null;
                    }
                    leftSibling.keys[j] = null;

                    // Update key
                    if (i > 0) in.keys[i - 1] = currentNode.keys[0];
                    break;
                }
            }
        } else if (rightSibling != null && rightSibling.fillFactor() - (double) 1 / (fanout - 1) >= minFillFactor) {
            for (int j = 0; j < currentNode.keys.length; j++) {
                // Find first space
                if (currentNode.keys[j] == null) {
                    currentNode.keys[j] = rightSibling.keys[0];

                    if (currentNode instanceof LeafNode) {
                        ((LeafNode) currentNode).rids[j] = ((LeafNode) rightSibling).rids[0];
                    } else if (currentNode instanceof IndexNode) {
                        ((IndexNode) node).childNodes[j + 1] = ((IndexNode) rightSibling).childNodes[0];
                    }
                    rightSibling.shiftLeft();

                    // Update key
                    in.keys[i] = rightSibling.keys[0];
                    break;
                }
            }
        } else {
            // Merge
            if (currentNode instanceof LeafNode) {
                if (leftSibling == null) {
                    Util.mergeArray(currentNode.keys, rightSibling.keys);
                    Util.mergeArray(((LeafNode) currentNode).rids, ((LeafNode) rightSibling).rids);
                    ((LeafNode) currentNode).rightLeafNode = ((LeafNode) rightSibling).rightLeafNode;
                    Util.shiftLeft(in.childNodes, i + 1);
                    Util.shiftLeft(in.keys, i);
                } else {
                    Util.mergeArray(leftSibling.keys, currentNode.keys);
                    Util.mergeArray(((LeafNode) leftSibling).rids, ((LeafNode) currentNode).rids);
                    ((LeafNode) leftSibling).rightLeafNode = ((LeafNode) currentNode).rightLeafNode;
                    Util.shiftLeft(in.childNodes, i);
                    Util.shiftLeft(in.keys, i - 1);
                }

            } else if (currentNode instanceof IndexNode) {
                if (rightSibling == null) {
                    in.shiftRight();
                    currentNode.keys[0] = in.keys[i - 1];
                    Util.mergeArray(leftSibling.keys, currentNode.keys);
                    Util.mergeArray(((IndexNode) leftSibling).childNodes, ((IndexNode) currentNode).childNodes);
                    Util.shiftLeft(in.childNodes, i);
                    Util.shiftLeft(in.keys, i - 1);
                } else {
                    currentNode.keys[Util.findFirstSpace(currentNode.keys)] = in.keys[i];
                    Util.mergeArray(currentNode.keys, rightSibling.keys);
                    Util.mergeArray(((IndexNode) currentNode).childNodes, ((IndexNode) rightSibling).childNodes);
                    Util.shiftLeft(in.childNodes, i + 1);
                    Util.shiftLeft(in.keys, i);
                }
            }

            if (root.keys[0] == null) {
                root = in.childNodes[0];
                height--;
            }
        }
    }

    public List<String> search(String key1, String key2) {
        return root.search(root, key1, key2);
    }

//    private Node findParent(Node node) {
//
//    }

//    private Node find(String key, Node node) {
//
//    }

    public void dumpStatistics() {
        // TODO
    }

    public void printTree() {
        printTree(root);
    }

    public void printTree(Node n) {
        // TODO
        for (int i = 0; i < n.keys.length; i++) {
            if (n.keys[i] != null)
                System.out.print(n.keys[i] + " ");
        }
        System.out.println();

        if (n instanceof IndexNode) {
            for (Node node : ((IndexNode) n).childNodes) {
                if (node != null)
                    printTree(node);
            }
        }
    }

    public void printNode(Node node) {
        String[] keys = node.keys;
        System.out.print("[");
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null) break;
            System.out.println(keys[i]);
            if (i < keys.length - 1 || keys[i + 1] == null)
                System.out.print(", ");
        }
        System.out.print("]");
    }

    public static class KeyNotFoundException extends RuntimeException {
        public KeyNotFoundException(String key) {
            super("The key " + key + " is not in the B+-tree.");
        }
    }

    private static void interact(BTree bTree) {
        // Start monitor user command
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("Waiting for your commands: ");
            String input = in.nextLine();
            String[] tokens = input.split("\\s+");
            switch (tokens[0].toLowerCase().trim()) {
                case "insert":
                    insertCommand(tokens, bTree);
                    break;
                case "delete":
                    deleteCommand(tokens, bTree);
                    break;
                case "search":
                    searchCommand(tokens, bTree);
                    break;
                case "print":
                    printCommand(tokens, bTree);
                    break;
                case "stats":
                    statsCommand(tokens, bTree);
                    break;
                case "quit":
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void insertCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.insert(tokens[1]);
    }

    private static void deleteCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.delete(tokens[1]);
    }

    private static void searchCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 3) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.search(tokens[1], tokens[2]);
    }

    private static void printCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.printTree();
    }

    private static void statsCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.dumpStatistics();
    }

    public static void main(String[] args) {
//        runTest();
        runTest2();
        System.out.println("finished");
        // User Interface
        if (args.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        // Validate argument
        String filename = args[0];
        if (filename.equals("-help")) {
            System.out.println("Usage: btree [fname]");
            System.out.println("fname: the name of the data file storing the search key values");
            return;
        }

        try {
            // Build B+ tree
            BTree bTree = new BTree(filename);
            interact(bTree);
        } catch (FileNotFoundException e) {
            System.out.println("File does not exists.");
        }
    }

    private static void runTest() {
        // Testing
        BTree bPlusTree = null;
        try {
            bPlusTree = new BTree("123.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        LeafNode leafNode1 = new LeafNode();
        leafNode1.keys[0] = "ab";
        leafNode1.keys[1] = "abc";

        LeafNode leafNode2 = new LeafNode();
        leafNode2.keys[0] = "b";
        leafNode2.keys[1] = "bab";
        leafNode2.keys[2] = "babc";

        LeafNode leafNode3 = new LeafNode();
        leafNode3.keys[0] = "c";
        leafNode3.keys[1] = "ca";

        IndexNode indexNode1 = new IndexNode();
        indexNode1.keys[0] = "b";
        indexNode1.keys[1] = "c";
        indexNode1.childNodes[0] = leafNode1;
        indexNode1.childNodes[1] = leafNode2;
        indexNode1.childNodes[2] = leafNode3;

        LeafNode leafNode4 = new LeafNode();
        leafNode4.keys[0] = "xa";
        leafNode4.keys[1] = "xb";

        LeafNode leafNode5 = new LeafNode();
        leafNode5.keys[0] = "ya";
        leafNode5.keys[1] = "yb";
        leafNode5.keys[2] = "yc";

        LeafNode leafNode6 = new LeafNode();
        leafNode6.keys[0] = "za";
        leafNode6.keys[1] = "zb";

        IndexNode indexNode2 = new IndexNode();
        indexNode2.keys[0] = "ya";
        indexNode2.keys[1] = "za";
        indexNode2.childNodes[0] = leafNode4;
        indexNode2.childNodes[1] = leafNode5;
        indexNode2.childNodes[2] = leafNode6;

        leafNode1.rightLeafNode = leafNode2;
        leafNode2.rightLeafNode = leafNode3;
        leafNode3.rightLeafNode = leafNode4;
        leafNode4.rightLeafNode = leafNode5;
        leafNode5.rightLeafNode = leafNode6;

        bPlusTree.root = new IndexNode();
        bPlusTree.root.keys[0] = "xa";
        ((IndexNode) bPlusTree.root).childNodes[0] = indexNode1;
        ((IndexNode) bPlusTree.root).childNodes[1] = indexNode2;
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Search bab - y:");
        List<String> result = bPlusTree.search("bab", "y");
        for (String s : result)
            System.out.print(s + " ");
        System.out.println();

        System.out.println();
        System.out.println("Delete bab:");
        bPlusTree.delete("bab");
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Delete b:");
        bPlusTree.delete("b");
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Delete za:");
        bPlusTree.delete("za");
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Delete yc:");
        bPlusTree.delete("yc");
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Delete c:");
        bPlusTree.delete("c");
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Delete xa:");
        bPlusTree.delete("xa");
        bPlusTree.printTree();

        System.out.println();
        System.out.println("Delete yb:");
        bPlusTree.delete("yb");
        bPlusTree.printTree();
    }

    private static void runTest2() {
        BTree bPlusTree = new BTree();

        bPlusTree.insert("ac");
        bPlusTree.insert("qk");
        bPlusTree.insert("jj");
        bPlusTree.insert("ip");
        bPlusTree.insert("yl");
        bPlusTree.insert("pc");
        bPlusTree.insert("qn");
        bPlusTree.insert("ls");
        bPlusTree.insert("qo");
        bPlusTree.insert("xw");
        bPlusTree.insert("jf");
        bPlusTree.insert("qt");
        bPlusTree.insert("pz");
        bPlusTree.insert("ft");
        bPlusTree.insert("ck");
        bPlusTree.insert("ch");
        bPlusTree.insert("nt");
        bPlusTree.insert("tg");
        bPlusTree.insert("ok");
        bPlusTree.insert("hv");


    }
}
















