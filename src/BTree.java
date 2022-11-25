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
        // TODO
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
