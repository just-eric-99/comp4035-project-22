import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class BTree {
    private Node root = null;
    private static final double minFillFactor = 0.5;
    private static final int fanout = 5;
    // In this project, record id is static
    private static final int rid = 0;

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

        void insert(String key) {
            // move keys and childNodes to right
            if (keys[0] == null) {
                keys[0] = key;
                return;
            }
            for (int i = 0; i < keys.length; i++) {
                if (key.compareTo(keys[i]) < 0) {
                    keys[i + 1] = keys[i];
                    keys[i] = key;
                }
            }
        }

        @Override
        List<String> search(Node node, java.lang.String key1, java.lang.String key2) {
            for (int i = 0; i < keys.length; i++) {
                if (key1.compareTo(keys[i]) < 0) {
                    return search(childNodes[i], key1, key2);
                } else if (key1.compareTo(keys[i]) == 0 || i == keys.length - 1 || keys[i + 1] == null) {
                    return search(childNodes[++i], key1, key2);
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
            // TODO fix search leaf node logic
            return null;
        }
    }

    public BTree(String filename) throws FileNotFoundException {
        System.out.println("Building an initial B+-tree... Launching the B+-tree test program...");
        File f = new File(filename);
        Scanner s = new Scanner(f);
        while (s.hasNextLine()) {
            insert(s.nextLine());
        }
        s.close();
    }

    void insert(String key) {
        if (root == null) {
            root = new LeafNode();
            ((LeafNode) root).insert(key);
            return;
        }
        insert(root, key);
    }

    String insert(Node node, String key) {

        /*
            cases: root and leaf
                        fill factor < 1 => insert to leaf node, no string return 
                        fill factor = 1 => split, insert to leaf node, set root = node
                   root and index
                        find leaf node to insert
                        if string is null => return
                        if string is not null =>
                             if fill factor < 1 => insert to own keys, adjust child node
                             if fill factor = 1 => split, insert to own key, set root = node, adjust child node


                   non-root and leaf
                        fill factor < 1 => insert to leaf node, no string return
                        fill factor = 1 => insert to leaf node, split, and return string to be inserted to parent
                   non-root and index
                        find leaf node to insert
         */
        if (root == node && node instanceof LeafNode) {
            // root and leaf

            if (!node.isFull()) {
                ((LeafNode) node).insert(key);
            } else {
                LeafNode leftNode = new LeafNode();
                LeafNode rightNode = new LeafNode();

                int mid = fanout / 2;

                for (int i = 0; i < mid; i++) {
                    leftNode.keys[i] = node.keys[i];
                    leftNode.rids[i] = ((LeafNode) node).rids[i];
                }

                for (int i = mid; i < fanout - 1; i++) {
                    rightNode.keys[i - mid] = node.keys[i];
                    rightNode.rids[i - mid] = ((LeafNode) node).rids[i];
                }

                if (key.compareTo(leftNode.keys[mid - 1]) < 0) {
                    leftNode.insert(key);
                    rightNode.insert(leftNode.keys[mid]);
                    leftNode.keys[mid] = null;
                } else {
                    rightNode.insert(key);
                }

                leftNode.rightLeafNode = rightNode;
                rightNode.leftLeafNode = leftNode;

                root = new IndexNode();
                root.keys[0] = rightNode.keys[0];
                ((IndexNode) root).childNodes[0] = leftNode;
                ((IndexNode) root).childNodes[1] = rightNode;
                return null;
            }
        } else if (root == node && node instanceof IndexNode) {
            String stringToInsert = null;
            boolean isInserted = false;
            for (int i = 0; i < node.keys.length; i++) {
                if (key.compareTo(node.keys[i]) < 0) {
                    stringToInsert = insert(((IndexNode) node).childNodes[i], key);
                    isInserted = true;
                    break;
                }
            }

            for (int i = 0; i < ((IndexNode) node).childNodes.length; i++) {
//                if (((IndexNode) node).childNodes[i] == null) {
//                    ((IndexNode) node).childNodes[i] = ((IndexNode) node).childNodes[i - 1].rightLeafNode;
//                    break;
//                }
            }

            if (!isInserted) {
                stringToInsert = insert(((IndexNode) node).childNodes[node.keys.length], key);
            }

            if (stringToInsert != null) {
                if (node.fillFactor() < 0) {
                    ((IndexNode) node).insert(stringToInsert);
                }
            } else {
                return null;
            }

        } else if (node instanceof LeafNode) {
            if (node.fillFactor() < 1) {
                ((LeafNode) node).insert(key);
            } else {
                // todo

            }
        } else if (node instanceof IndexNode) {
            if (node.fillFactor() < 1) {
                ((IndexNode) node).insert(key);
            } else {
                // todo
            }
        }


        return null;
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

            if (root.keys[0] == null)
                root = in.childNodes[0];
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

    public void printNode() {
        // TODO
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
        runTest();

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

        bPlusTree.insert("1");
        bPlusTree.insert("2");
        bPlusTree.insert("3");
        bPlusTree.insert("4");

        bPlusTree.printTree();

        bPlusTree.insert("5");

//        LeafNode leafNode1 = new LeafNode();
//        leafNode1.keys[0] = "ab";
//        leafNode1.keys[1] = "abc";
//
//        LeafNode leafNode2 = new LeafNode();
//        leafNode2.keys[0] = "b";
//        leafNode2.keys[1] = "bab";
//        leafNode2.keys[2] = "babc";
//
//        LeafNode leafNode3 = new LeafNode();
//        leafNode3.keys[0] = "c";
//        leafNode3.keys[1] = "ca";
//
//        IndexNode indexNode1 = new IndexNode();
//        indexNode1.keys[0] = "b";
//        indexNode1.keys[1] = "c";
//        indexNode1.childNodes[0] = leafNode1;
//        indexNode1.childNodes[1] = leafNode2;
//        indexNode1.childNodes[2] = leafNode3;
//
//        LeafNode leafNode4 = new LeafNode();
//        leafNode4.keys[0] = "xa";
//        leafNode4.keys[1] = "xb";
//
//        LeafNode leafNode5 = new LeafNode();
//        leafNode5.keys[0] = "ya";
//        leafNode5.keys[1] = "yb";
//        leafNode5.keys[2] = "yc";
//
//        LeafNode leafNode6 = new LeafNode();
//        leafNode6.keys[0] = "za";
//        leafNode6.keys[1] = "zb";
//
//        IndexNode indexNode2 = new IndexNode();
//        indexNode2.keys[0] = "ya";
//        indexNode2.keys[1] = "za";
//        indexNode2.childNodes[0] = leafNode4;
//        indexNode2.childNodes[1] = leafNode5;
//        indexNode2.childNodes[2] = leafNode6;
//
//        bPlusTree.root = new IndexNode();
//        bPlusTree.root.keys[0] = "xa";
//        ((IndexNode) bPlusTree.root).childNodes[0] = indexNode1;
//        ((IndexNode) bPlusTree.root).childNodes[1] = indexNode2;
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete bab:");
//        bPlusTree.delete("bab");
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete b:");
//        bPlusTree.delete("b");
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete za:");
//        bPlusTree.delete("za");
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete yc:");
//        bPlusTree.delete("yc");
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete c:");
//        bPlusTree.delete("c");
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete xa:");
//        bPlusTree.delete("xa");
//        bPlusTree.printTree();
//
//        System.out.println();
//        System.out.println("Delete yb:");
//        bPlusTree.delete("yb");
//        bPlusTree.printTree();
    }
}
















