import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class BTree {
    private static final double MIN_FILL_FACTOR = 0.5;
    private static final int NODE_FANOUT = 5;
    // In this project, record id is static
    private static final int rid = 0;
    private Node root = null;
    private int totalNode = 0;
    private int height = 0;
    private int dataEntries = 0;
    private int indexEntries = 0;

    private static abstract class Node {
        String[] keys = new String[NODE_FANOUT - 1];

        // fillFactor * (fanout - 1)
        double fillFactor() {
            int count = 0;
            for (String k : keys) {
                if (k == null) break;
                count++;
            }
            return (double) count / keys.length;
        }

        boolean isFull() {
            return keys[keys.length - 1] != null;
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

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (String key : keys) {
                s.append(key == null ? "" : key + " ");
            }
            s = new StringBuilder(s.toString().trim());
            return "[" + s + "]";
        }
    }

    private static class IndexNode extends Node {
        Node[] childNodes = new Node[NODE_FANOUT];

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
            return new ArrayList<>();
        }
    }

    private static class LeafNode extends Node {
        LeafNode leftLeafNode;
        LeafNode rightLeafNode;
        Integer[] rids = new Integer[NODE_FANOUT - 1];

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

        boolean keyExist(String key) {
            for (String s : keys) {
                if (s == null) return false;
                if (key.compareTo(s) == 0)
                    return true;
            }
            return false;
        }

        void insert(String key) {
            if (keys[0] == null) {
                keys[0] = key;
                rids[0] = rid;
                return;
            }
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == null) {
                    keys[i] = key;
                    rids[i] = rid;
                    return;
                } else if (key.compareTo(keys[i]) < 0) {
                    shiftRight(i);
                    keys[i] = key;
                    rids[i] = rid;
                    return;
                }
            }
        }

        @Override
        List<String> search(Node node, String key1, String key2) {
            List<String> result = new ArrayList<>();
            LeafNode ln = (LeafNode) node;
            if (key1.compareTo(key2) <= 0) {
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
            } else {
                while (ln != null) {
                    for (int i = ln.keys.length - 1; i >= 0; i--) {
                        if (ln.keys[i] == null) continue;

                        if (key2.compareTo(ln.keys[i]) > 0)
                            return result;

                        if (key1.compareTo(ln.keys[i]) >= 0 && key2.compareTo(ln.keys[i]) <= 0)
                            result.add(ln.keys[i]);
                    }
                    ln = ln.leftLeafNode;
                }
            }
            return result;
        }
    }

    private static class OverflowData {
        String key;
        Node node;

        OverflowData(String key, Node node) {
            this.key = key;
            this.node = node;
        }
    }

    public BTree(String filename) throws FileNotFoundException {
        System.out.println("Building an initial B+-tree... Launching the B+-tree test program...");
        File f = new File(filename);
        Scanner s = new Scanner(f);
        int i = 0;
        while (s.hasNextLine()) {
            insert(s.nextLine());
        }
        s.close();
    }

    public void insert(String key) {
        if (root == null) {
            root = new LeafNode();
            totalNode++;
        }
        insert(root, key);
        dataEntries++;
    }

    private OverflowData insert(Node node, String key) {
        if (node instanceof LeafNode) {
            if (((LeafNode) node).keyExist(key))
                throw new DuplicateKeyException(key);

            if (node.isFull()) {
                if (node == root) {
                    IndexNode newRoot = new IndexNode();
                    totalNode++;
                    Node newLeaf = splitLeafNode((LeafNode) node, key);

                    newRoot.childNodes[0] = node;
                    newRoot.childNodes[1] = newLeaf;
                    newRoot.keys[0] = newLeaf.keys[0];
                    root = newRoot;
                    height++;
                    return null;
                }
                LeafNode right = splitLeafNode((LeafNode) node, key);
                return new OverflowData(right.keys[0], right);
            } else {
                ((LeafNode) node).insert(key);
                return null;
            }
        }

        IndexNode in = (IndexNode) node;

        OverflowData overflow = null;
        int i = 0;
        for (; i < in.keys.length; i++) {
            if (key.compareTo(in.keys[i]) < 0) {
                overflow = insert(in.childNodes[i], key);
                break;
            } else if (key.compareTo(in.keys[i]) == 0 || i == in.keys.length - 1 || in.keys[i + 1] == null) {
                overflow = insert(in.childNodes[++i], key);
                break;
            }
        }

        // check overflow
        if (overflow == null)
            return null;

        // handle overflow
        if (!node.isFull()) {
            int index = ((IndexNode) node).insert(overflow.key);
            ((IndexNode) node).childNodes[index + 1] = overflow.node;
            return null;
        }

        if (node == root) {
            root = new IndexNode();
            totalNode++;
            OverflowData data = splitIndexNode((IndexNode) node, overflow, i); // **
            ((IndexNode) root).childNodes[0] = node;
            ((IndexNode) root).childNodes[1] = data.node;
            root.keys[0] = data.key;
            height++;
            return null;
        } else {
            return splitIndexNode((IndexNode) node, overflow, i); // **
        }
    }

    private LeafNode splitLeafNode(LeafNode fullLeafNode, String key) {
        LeafNode newLeafNode = new LeafNode();
        totalNode++;
        indexEntries++;
        int mid = NODE_FANOUT / 2;

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

        LeafNode right = null;
        if (fullLeafNode.rightLeafNode != null) {
            right = fullLeafNode.rightLeafNode;
            right.leftLeafNode = newLeafNode;
        }

        fullLeafNode.rightLeafNode = newLeafNode;
        newLeafNode.leftLeafNode = fullLeafNode;
        newLeafNode.rightLeafNode = right;
        return newLeafNode;
    }

    private OverflowData splitIndexNode(IndexNode node, OverflowData overflow, int childPos) {
        IndexNode right = new IndexNode();
        totalNode++;
        int mid = NODE_FANOUT / 2;
        OverflowData last;
        if (childPos == NODE_FANOUT - 1) {
            last = overflow;
        } else  {
            last = new OverflowData(node.keys[node.keys.length - 1], node.childNodes[node.childNodes.length - 1]);
        }

        if (childPos < NODE_FANOUT - 1) {
            Util.shiftRight(node.keys, childPos);
            Util.shiftRight(node.childNodes, childPos + 1);
            node.keys[childPos] = overflow.key;
            node.childNodes[childPos + 1] = overflow.node;
        }

        OverflowData data = new OverflowData(node.keys[mid], right);
        right.childNodes[0] = node.childNodes[mid + 1];

        Util.shiftLeft(node.keys, mid);
        Util.shiftLeft(node.childNodes, mid + 1);
        node.keys[node.keys.length - 1] = last.key;
        node.childNodes[node.childNodes.length - 1] = last.node;

        for (int i = mid; i < node.keys.length; i++) {
            right.keys[i - mid] = node.keys[i];
            node.keys[i] = null;
        }

        for (int i = mid + 1; i < node.childNodes.length; i++) {
            right.childNodes[i - mid] = node.childNodes[i];
            node.childNodes[i] = null;
        }

        return data;
    }

    public void delete(String key) {
        if (root == null)
            throw new TreeIsEmptyException();

        delete(key, root);
        dataEntries--;

        if (root instanceof LeafNode && root.keys[0] == null) {
            root = null;
            totalNode--;
        }
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
        if (currentNode.fillFactor() >= MIN_FILL_FACTOR)
            return;

        Node leftSibling = null;
        Node rightSibling = null;
        if (i - 1 >= 0)
            leftSibling = in.childNodes[i - 1];

        if (i + 1 < node.keys.length)
            rightSibling = in.childNodes[i + 1];

        // Underflow
        // If have left Sibling
        if (leftSibling != null && leftSibling.fillFactor() - (double) 1 / (NODE_FANOUT - 1) >= MIN_FILL_FACTOR) {
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
        } else if (rightSibling != null && rightSibling.fillFactor() - (double) 1 / (NODE_FANOUT - 1) >= MIN_FILL_FACTOR) {
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
                    mergeLeafNode(currentNode, rightSibling);
                    Util.shiftLeft(in.childNodes, i + 1);
                    Util.shiftLeft(in.keys, i);
                } else {
                    mergeLeafNode(leftSibling, currentNode);
                    Util.shiftLeft(in.childNodes, i);
                    Util.shiftLeft(in.keys, i - 1);
                }
                indexEntries--;
            } else if (currentNode instanceof IndexNode) {
                if (rightSibling == null) {
                    Util.shiftRight(currentNode.keys);
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
            totalNode--;

            if (root.keys[0] == null) {
                root = in.childNodes[0];
                totalNode--;
                height--;
            }
        }
    }

    private void mergeLeafNode(Node left, Node right) {
        Util.mergeArray(left.keys, right.keys);
        Util.mergeArray(((LeafNode) left).rids, ((LeafNode) right).rids);
        ((LeafNode) left).rightLeafNode = ((LeafNode) right).rightLeafNode;
        LeafNode rightNode = ((LeafNode) right).rightLeafNode;
        if (rightNode != null && rightNode.rightLeafNode != null)
            rightNode.leftLeafNode = (LeafNode) left;
    }

    public List<String> search(String key1, String key2) {
        if (root == null)
            throw new TreeIsEmptyException();
        return root.search(root, key1, key2);
    }

    public void dumpStatistics() {
        double avgFillFactor = (double) (dataEntries + indexEntries) / (totalNode * (NODE_FANOUT - 1)) * 100;
        System.out.println("Statistics of the B+ Tree:");
        System.out.println("Total number of nodes: " + totalNode);
        System.out.println("Total number of data entries: " + dataEntries);
        System.out.println("Total number of index entries: " + indexEntries);
        System.out.printf("Average fill factor: %d", (int) avgFillFactor);
        System.out.println("%");
        System.out.println("Height of tree: " + height);
    }

    public void printTree() {
        if (root == null)
            throw new TreeIsEmptyException();
        printTree(root);
    }

    public void printTree(Node n) {
        Queue<Node> nodeQueue = new LinkedList<>();
        nodeQueue.add(n);
        StringBuilder stringBuilder = new StringBuilder();

        while (!nodeQueue.isEmpty()) {
            int size = nodeQueue.size();
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < size; i++) {
                Node node = nodeQueue.poll();
                if (node != null) {
                    if (i > 0) {
                        s.append(", ");
                    }
                    s.append(node);
                }

                if (node instanceof IndexNode) {
                    nodeQueue.addAll(Arrays.asList(((IndexNode) node).childNodes));
                }
            }

            stringBuilder.append(s).append("\n");
        }
        System.out.print(stringBuilder);
    }

    public static class KeyNotFoundException extends RuntimeException {
        public KeyNotFoundException(String key) {
            super("The key \"" + key + "\" is not in the B+-tree.");
        }
    }

    public static class DuplicateKeyException extends RuntimeException {
        public DuplicateKeyException(String key) {
            super("The key \"" + key + "\" is already in the B+-tree!");
        }
    }

    public static class TreeIsEmptyException extends RuntimeException {
        public TreeIsEmptyException() {
            super("The B+-tree is empty!");
        }
    }

    private static void interact(BTree bTree) {
        // Start monitor user command
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("Waiting for your commands: ");
            String input = in.nextLine();
            if (input.isEmpty()) {
                System.out.println("Invalid input.");
                continue;
            }

            String[] tokens = input.split("\\s+");
            switch (tokens[0].toLowerCase().trim()) {
                case "commands":
                    helpCommand(tokens);
                    break;
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
                    System.out.println("The program is terminated.");
                    return;

                default:
                    System.out.println("Invalid input.");
            }
            System.out.println();
        }
    }

    private static void helpCommand(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("Invalid input.");
            return;
        }
        System.out.println("The following commands are supported:");
        System.out.println("insert <key> <rid>");
        System.out.println("delete <key>");
        System.out.println("search <key1> <key2>");
        System.out.println("print");
        System.out.println("stats");
        System.out.println("quit");
    }

    private static void insertCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        String key = tokens[1];
        try {
            bTree.insert(key);
            System.out.println("The key " + key + " has been inserted in the B+-tree!");
        } catch (DuplicateKeyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        String key = tokens[1];
        try {
            bTree.delete(key);
            System.out.println("The key " + key + " has been deleted in the B+-tree.");
        } catch (KeyNotFoundException | TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void searchCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 3) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        try {
            List<String> result = bTree.search(tokens[1], tokens[2]);
            if (result.isEmpty()) {
                System.out.println("No result for range " + tokens[1] + " - " + tokens[2]);
                return;
            }

            System.out.println("Result (" + result.size() + " data(s)): ");
            System.out.println(result.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "))
            );
        } catch (TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        try {
            bTree.printTree();
        } catch (TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void statsCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.dumpStatistics();
    }

    public static void main(String[] args) {
        if (false) {
//            runTest();
//            runTest2();
            runTest3();
            System.out.println("finished");
            return;
        }

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
            bPlusTree = new BTree("data.txt");
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
        BTree bPlusTree = null;
        try {
            bPlusTree = new BTree("123.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

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

        bPlusTree.printTree();
    }

    private static void runTest3() {
        BTree bPlusTree = null;
        try {
            bPlusTree = new BTree("data.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println();

        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Search a - z");
        searchCommand(new String[]{"", "a", "z"}, bPlusTree);
        System.out.println();

        System.out.println("Search z - a");
        searchCommand(new String[]{"", "z", "a"}, bPlusTree);
        System.out.println();

        System.out.println("Search good - good");
        searchCommand(new String[]{"", "good", "good"}, bPlusTree);
        System.out.println();

        System.out.println("Insert likes");
        insertCommand(new String[]{"", "likes"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert hold");
        insertCommand(new String[]{"", "hold"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert fish");
        insertCommand(new String[]{"", "fish"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert general");
        insertCommand(new String[]{"", "general"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Search a - z");
        searchCommand(new String[]{"", "a", "z"}, bPlusTree);
        System.out.println();

        System.out.println("Search z - a");
        searchCommand(new String[]{"", "z", "a"}, bPlusTree);
        System.out.println();

        System.out.println("Search spark - fly");
        searchCommand(new String[]{"", "spark", "fly"}, bPlusTree);
        System.out.println();

        System.out.println("Search general - like");
        searchCommand(new String[]{"", "spark", "fly"}, bPlusTree);
        System.out.println();

        System.out.println("Insert apple");
        insertCommand(new String[]{"", "apple"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert as");
        insertCommand(new String[]{"", "as"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert at");
        insertCommand(new String[]{"", "at"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert ask");
        insertCommand(new String[]{"", "ask"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert asks");
        insertCommand(new String[]{"", "asks"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert ack");
        insertCommand(new String[]{"", "ack"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Search a - z");
        searchCommand(new String[]{"", "a", "z"}, bPlusTree);
        System.out.println();

        System.out.println("Search z - a");
        searchCommand(new String[]{"", "z", "a"}, bPlusTree);
        System.out.println();

        System.out.println("Insert are");
        insertCommand(new String[]{"", "are"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert again");
        insertCommand(new String[]{"", "again"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert hort");
        insertCommand(new String[]{"", "hort"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert hpv");
        insertCommand(new String[]{"", "hpv"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert yo");
        insertCommand(new String[]{"", "yo"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert zebra");
        insertCommand(new String[]{"", "zebra"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert zero");
        insertCommand(new String[]{"", "zero"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert you");
        insertCommand(new String[]{"", "you"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        System.out.println("Insert yuu");
        insertCommand(new String[]{"", "yuu"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Search a - zzz");
        searchCommand(new String[]{"", "a", "zzz"}, bPlusTree);
        System.out.println();

        System.out.println("Search zzz - a");
        searchCommand(new String[]{"", "zzz", "a"}, bPlusTree);
        System.out.println();

        System.out.println("Insert your");
        insertCommand(new String[]{"", "your"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zara");
        insertCommand(new String[]{"", "zara"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zbrush");
        insertCommand(new String[]{"", "zbrush"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zip");
        insertCommand(new String[]{"", "zip"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zurich");
        insertCommand(new String[]{"", "zurich"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zz");
        insertCommand(new String[]{"", "zz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zzz");
        insertCommand(new String[]{"", "zzz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zzzz");
        insertCommand(new String[]{"", "zzzz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zzzzz");
        insertCommand(new String[]{"", "zzzzz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zzzzzz");
        insertCommand(new String[]{"", "zzzzzz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Insert zzzzzzz");
        insertCommand(new String[]{"", "zzzzzzz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Search a - zzzzzzz");
        searchCommand(new String[]{"", "a", "zzzzzzz"}, bPlusTree);
        System.out.println();

        System.out.println("Search zzzzzzz - a");
        searchCommand(new String[]{"", "zzzzzzz", "a"}, bPlusTree);
        System.out.println();

        System.out.println("Delete zzzzzzzz");
        deleteCommand(new String[]{"", "zzzzzzzz"}, bPlusTree);
        bPlusTree.printTree();
        System.out.println();

        bPlusTree.dumpStatistics();
        System.out.println();

        System.out.println("Search a - zzzzzzz");
        searchCommand(new String[]{"", "a", "zzzzzzz"}, bPlusTree);
        System.out.println();

        System.out.println("Search zzzzzzz - a");
        searchCommand(new String[]{"", "zzzzzzz", "a"}, bPlusTree);
        System.out.println();

        List<String> keys = bPlusTree.search("a", "zzzzzzz");
//        for (int i = keys.size() - 1; i >= 0 ; i--) {
        for (int i = 0; i < keys.size() ; i++) {
            System.out.println("Delete " + keys.get(i));
            deleteCommand(new String[]{"", keys.get(i)}, bPlusTree);
            printCommand(new String[]{""}, bPlusTree);
            System.out.println();

            bPlusTree.dumpStatistics();
            System.out.println();

            System.out.println("Search a - zzzzzzz");
            searchCommand(new String[]{"", "a", "zzzzzzz"}, bPlusTree);
            System.out.println();

            System.out.println("Search zzzzzzz - a");
            searchCommand(new String[]{"", "zzzzzzz", "a"}, bPlusTree);
            System.out.println();
        }
    }
}
