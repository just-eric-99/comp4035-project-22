import java.util.List;

public class BTree {

    private interface Node {}

    private class IndexNode implements Node {
        String[] keys;
        Node[] childNodes;
    }

    private class LeafNode implements Node {
        Record[] records;
        LeafNode leftLeafNode;
        LeafNode rightLeafNode;
    }

    private class Record {
        String key;
        int rid = 0;
    }

    public BTree(String filename) {
        // TODO Verify the file exist
    }

    public void insert(String key) {
        /*
        if this == root node,
            if root is full,
                split root node in leaf 1 and leaf 2

        else:
            if this is full,
                split this node to leaf 1 and leaf 2
                redistribute keys in leaf 1 to leaf 1 and 2
                move index to leaf 1 and leaf 2 parent (store parent info)



         */

    }

//    private void insertLeadNode(Record record) {
//
//    }
//
//    private void insertInternalNode (Record record) {
//
//    }


    private void splitRootNode(Node root) {
        // split and return ?
    }

    private void splitInternalNode() {

    }

    private void splitLeafNode() {

    }

    public void delete(String key) {
        // TODO
    }

    public List<String> search(String key1, String key2) {
        // TODO
        return null;
    }

    public void dumpStatistics() {
        // TODO
    }

    public void printTree() {
        // TODO
    }

    public void printNode() {
        // TODO
    }

    public static void main(String[] args) {

    }
}
