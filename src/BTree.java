import java.util.List;

public class BTree {
    private Node root = null;

    private interface Node {
        double fillFactor();
    }

    private class IndexNode implements Node {
        String[] keys;
        Node[] childNodes;

        LeafNode find(String key, Node node) {
            if (node instanceof LeafNode)
                return ((LeafNode) node).find(key);

            for (int i = 0; i < keys.length; i++) {
                if (key.compareTo(keys[i]) < 0)
                    return find(key, childNodes[i]);
                else if (key.compareTo(keys[i]) == 0 || i == keys.length - 1)
                    return find(key, childNodes[i + 1]);
            }
            throw new KeyNotFoundException(key);
        }

        @Override
        public double fillFactor() {
            int total = 0;
            for (String k : keys) {
                if (k != null) total += 1;
            }
            return total / keys.length;
        }
    }

    private class LeafNode implements Node {
        Record[] records;
        LeafNode leftLeafNode;
        LeafNode rightLeafNode;

        LeafNode find(String key) {
            for (Record r : records)
                if (r.equals(key)) return this;
            throw new KeyNotFoundException(key);
        }

        @Override
        public double fillFactor() {
            int total = 0;
            for (Record r : records) {
                if (r != null) total += 1;
            }
            return total / records.length;
        }
    }

    private class Record {
        String key;
        int rid = 0;

        public boolean equals(String key) {
            return this.key.equals(key);
        }
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
        if (root == null) return;
        delete(key, root);
    }

    private void delete(String key, Node node) {
        if (node instanceof LeafNode) {
            LeafNode ln = (LeafNode) node;
            int pos = 0;

            // Find the record and delete
            while (pos < ln.records.length) {
                if (ln.records[pos].equals(key)) {
                    ln.records[pos] = null;
                    break;
                } else if (pos == ln.records.length - 1)
                    throw new KeyNotFoundException(key);
                pos++;
            }

            // Shift the record position
            while (pos < ln.records.length - 1) {
                ln.records[pos] = ln.records[pos - 1];
            }

            ln.records[ln.records.length - 1] = null;
            return;
        }

        IndexNode in = (IndexNode) node;

        for (int i = 0; i < in.keys.length; i++) {
            if (key.compareTo(in.keys[i]) < 0)
                delete(key, in.childNodes[i]);
            else if (key.compareTo(in.keys[i]) == 0 || i == in.keys.length - 1)
                delete(key, in.childNodes[i + 1]);
        }

        // TODO
        // Check fill factor
    }

    public List<String> search(String key1, String key2) {
        // TODO
        return null;
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
        // TODO
    }

    public void printNode() {
        // TODO
    }

    public static class KeyNotFoundException extends RuntimeException {
        public KeyNotFoundException(String key) {
            super(key + " is not found in the tree.");
        }
    }

    public static void main(String[] args) {

    }
}
