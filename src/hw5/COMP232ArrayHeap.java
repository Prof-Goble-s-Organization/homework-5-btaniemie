package hw5;

import java.util.NoSuchElementException;

/**
 * Implementation of the COMP232PriorityQueue interface that uses a binary 
 * heap with an array backing store. 
 * 
 * @author William Goble
 * @author Dickinson College
 */
public class COMP232ArrayHeap<K extends Comparable<K>, V> implements COMP232PriorityQueue<K, V> {
    /*
	 * NOTE: We could implement this directly on top of an array. However that
	 * would just mean reimplementing much of the work already done in
	 * ArrayList. In particular, by using the COMP232ArrayList class we do not
	 * have to reimplement the array resizing operations.
	 */
    private COMP232ArrayList<HeapNode<K, V>> tree;

    /**
     * Construct a new empty COMP232ArrayHeap
     */
    public COMP232ArrayHeap() {
        tree = new COMP232ArrayList<HeapNode<K, V>>();
    }

    public COMP232ArrayHeap(K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Length of keys and values must be the same.");
        }
        if (keys.length == 0) {
            throw new IllegalArgumentException("keys and values must not by empty");
        }

        // Add the <key, value> pairs to the tree
        tree = new COMP232ArrayList<HeapNode<K, V>>();
        for (int i = 0; i < keys.length; i++) {
            tree.add(new HeapNode<K,V>(keys[i], values[i]));
        }

        // verify that we have a valid heap
        if(!checkHeapProperty()) {
            throw new IllegalArgumentException("Heap is not valid");
        }
    }

    /**
     * Get the index where the left child of the node at index i is stored
     */
    private int getLeftChildIndex(int i) {
        return 2 * i + 1;
    }

    /**
     * Get the index where the right child of the node at index i is stored
     */
    private int getRightChildIndex(int i) {
        return 2 * i + 2;
    }

    /**
     * Get the index where the parent of the node at index i is stored
     * @param i the target index
     * @return the index of the current parent
     */
    private int getParentIndex(int i) {
        return (i - 1) / 2;
    }

    /**
     * Check if the node at index is a leaf node. A node is a leaf if both of 
     * its children are not in the tree. (i.e. have an index that is >= than 
     * the number of nodes in the tree.)
     * @param i
     * @return
     */
    private boolean isLeaf(int i) {
        int leftChildIndex = getLeftChildIndex(i);
        int rightChildIndex = getRightChildIndex(i);
        return leftChildIndex >= tree.size() && rightChildIndex >= tree.size();
    }

    public void add(K key, V value) {
        /*
         * Place the node at the end of the heap, i.e. the first empty spot in 
         * level order. Since the heap is always a complete tree, and we are 
         * using the array based binary tree representation is just the first 
         * empty element in the array.
         * 
         * This new node may not be in a proper location, i.e. it will not be 
         * larger than its  parents. To fix this we "percolate" the newly added 
         * node up the tree.
	 * I recommend creating a helper function to assist with the percolation.
         */
        HeapNode<K,V> addedNode = new HeapNode<K,V>(key, value);
        tree.add(addedNode);
        percolateUp(tree.size() - 1);
    }

    private void percolateUp(int percolatingNodeIndex) {
        if (percolatingNodeIndex == 0) {
            return;
        } else {
            int parentIndex = getParentIndex(percolatingNodeIndex);
            HeapNode<K, V> percNode = tree.get(percolatingNodeIndex);
            HeapNode<K, V> parentNode = tree.get(parentIndex);

            if (percNode.key.compareTo(parentNode.key) > 0) {
                swap(percolatingNodeIndex, parentIndex);
                percolateUp(parentIndex);
            }
        }
    }

    /**
     * Helper method that swaps two elements of the tree
     * @param indexA
     * @param indexB
     */
    private void swap(int indexA, int indexB) {
        HeapNode<K, V> nodeA = tree.get(indexA);
        HeapNode<K, V> nodeB = tree.get(indexB);

        tree.set(indexA, nodeB);
        tree.set(indexB, nodeA);
    }

    /**
     * @{inheritDoc}
     */
    public V remove() {
        if (tree.size() == 0) {
            return null;
        } else {
            /*
             * To remove from the top of the heap, we replace the root element 
             * with the last leaf of the tree. This will likely mean that the 
             * key at the root is not in the correct location. This means its 
             * children will be larger than it. To fix this we let the key at 
             * the root "trickle down" the tree.
             */

             /*
              * Swap the last leaf with the root, then remove the last leaf. 
              * Save the value from the root so we can return it at the end.
              */
              swap(0, tree.size() - 1);
              V maxValue = tree.remove(tree.size() - 1).value;

              // trickle the node down from the root
              trickleDown(0);

              return maxValue;
        }
    }

    /**
     * Trickly the node at tricklingNodeIndex down the tree. The node trickles 
     * down if it's key is smaller than the key of either child. To trickle 
     * down a node switches  places with its larger child.
     * 
     * @param tricklingNodeIndex
     */
    private void trickleDown(int tricklingNodeIndex) {
        if(!isLeaf(tricklingNodeIndex)) {
            /*
             * Get the index of child of the trickling node that has the larger 
             * key.
             */
            int largerChildIndex = getLargerChildIndex(tricklingNodeIndex);
            HeapNode<K, V> trickNode = tree.get(tricklingNodeIndex);
            HeapNode<K, V> largerChildNode = tree.get(largerChildIndex);

            /*
             * If the key of the trickling node is smaller tyhan the larger key 
             * among its children. then swap it with that child, and trickle 
             * down from there.
             */
            if (trickNode.key.compareTo(largerChildNode.key) < 0) {
                swap(tricklingNodeIndex, largerChildIndex);
                trickleDown(largerChildIndex);
            }
        }
    }

    /**
     * Get the index of the child with the larger key value. We can assume that 
     * there is at least one child.
     * @param parentIndex
     * @return
     */
    private int getLargerChildIndex(int parentIndex) {
        int leftChildIndex = getLeftChildIndex(parentIndex);
        int rightChildIndex = getRightChildIndex(parentIndex);

        if (leftChildIndex >= tree.size()) {
            return rightChildIndex;         // No left child
        } else if (rightChildIndex >= tree.size())  {
            return leftChildIndex;          // No right child
        } else {
            // two children exist
            HeapNode<K, V> leftChild = tree.get(leftChildIndex);
            HeapNode<K, V> rightchild = tree.get(rightChildIndex);

            if (leftChild.key.compareTo(rightchild.key) > 1) {
                return leftChildIndex;      // left child has a larger key
            } else {
                return rightChildIndex;     // right child is larger
            }
        }
    }

    /**
     * Adjust the priority of the node with the specified value. This method 
     * throws an IllegalStateException if the heap is empty. This method has no 
     * effect if the specified value does not appear in the heap.
     * 
     * @param value
     *          The value for which to adjust the priority
     * @param newKey
     *          The new key (i.e. priority) for the value
     * @throws IllegalStateException
     *          Thrown if the heap is empty
     */
    public void adjustPriority(V value, K newKey) {
        // Intentionally not implemented -- see homework assignment
        if (tree.size() == 0) {
            throw new IllegalStateException("Heap is empty");
        }

        int foundIndex = -1;

        for (int i = 0; i < tree.size(); i++) {
            if (tree.get(i).value.equals(value)) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex == -1){
            throw new NoSuchElementException("Node DNE");
        }

        HeapNode<K,V> node = tree.get(foundIndex);
        node.key = newKey;

        if (newKey.compareTo(node.key) < 0) {
            percolateUp(foundIndex);
        } else {
            trickleDown(foundIndex);
        }

        /*
         * Find the node with the value -- Hint: Just search through the array!
         * Replace its key and then move the node to a valid location within the
         * tree. Hint: If you factor out your percolate functionality from the 
         * add method in a way similar to how the trickleDown method was factored 
         * out of remove, then you can use those two methods to move the node to
         * a proper location.
         */
    }

    /**
     * {@inheritDoc}
     */
    public V peek() {
        if (tree.size() == 0) {
            return null;
        } else {
            return tree.get(0).value;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return tree.size();
    }

    /**
     * Helper method that checks that the heap property is preserved. That is 
     * that every parent's key is larger than its children's keys, as defined by 
     * the compareTo method. This is used by the tests to check the interal 
     * structure of the heap.
     */
    boolean checkHeapProperty() {
        return checkHeapPropertyHelper(0);
    }

    private boolean checkHeapPropertyHelper(int nodeIndex) {
        // traverse the heap, checking the heap property at each node
        if (nodeIndex > tree.size()) {
            return true;    // off tree
        } else {
            // Note: Works on root because (0 - 1) / 2 = 0
            // so root is compared to itself
            HeapNode<K, V> node = tree.get(nodeIndex);
            int parentIndex = getParentIndex(nodeIndex);
            HeapNode<K, V> parentNode = tree.get(parentIndex);

            if (node.key.compareTo(parentNode.key) > 0) {
                return false;
            } else {
                int leftChildIndex = getLeftChildIndex(nodeIndex);
                int rightChildIndex = getRightChildIndex(nodeIndex);

                return checkHeapPropertyHelper(leftChildIndex) && checkHeapPropertyHelper(rightChildIndex);
            }
        }
    }

    private static class HeapNode<K, V> {
        public K key;
        public V value;

        public HeapNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
