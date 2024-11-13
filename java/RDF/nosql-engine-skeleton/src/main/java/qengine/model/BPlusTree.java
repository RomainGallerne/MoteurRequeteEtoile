package main.java.qengine.model;

import java.util.*;

public class BPlusTree {

    private BPlusTreeNode root;
    private int t;

    public BPlusTree(int t) {
        this.root = new BPlusTreeNode(true);
        this.t = t;
    }

    public void printBPlusTree() {
        if (this.root == null) {
            System.out.println("L'arbre est vide.");
            return;
        }

        Queue<BPlusTreeNode> queue = new LinkedList<>();
        Map<BPlusTreeNode, Integer> levels = new HashMap<>();
        queue.add(this.root);
        levels.put(this.root, 0);

        int currentLevel = -1;
        List<BPlusTreeNode> levelNodes = new ArrayList<>();

        while (!queue.isEmpty()) {
            BPlusTreeNode node = queue.poll();
            int level = levels.get(node);

            if (level != currentLevel) {
                if (currentLevel != -1) {
                    // Affichage du niveau courant
                    System.out.print("Niveau " + currentLevel + " : ");
                    for (BPlusTreeNode n : levelNodes) {
                        // Affichage des clés de chaque noeud du niveau
                        System.out.print("[");
                        for (int[] key : n.getKeys()) {
                            System.out.print(Arrays.toString(key) + " ");
                        }
                        System.out.print("] | ");
                    }
                    System.out.println();
                }
                levelNodes = new ArrayList<>();
                currentLevel = level;
            }

            levelNodes.add(node);

            if (!node.isLeaf()) {
                for (BPlusTreeNode child : node.getChildren()) {
                    queue.add(child);
                    levels.put(child, level + 1);
                }
            }
        }

        System.out.print("Niveau " + currentLevel + " : ");
        for (BPlusTreeNode n : levelNodes) {
            System.out.print("[");
            for (int[] key : n.getKeys()) {
                System.out.print(Arrays.toString(key) + " ");
            }
            System.out.print("] | ");
        }
        System.out.println();
    }

    public List<int[]> searchPrefix(int[] prefix) {
        List<int[]> results = new ArrayList<>();
        searchPrefix(this.root, prefix, results);
        return results;
    }

    private void searchPrefix(BPlusTreeNode node, int[] prefix, List<int[]> results) {
        int i = 0;
        while (i < node.getKeys().size()) {
            int[] key = node.getKeys().get(i);

            if (isPrefix(key, prefix)) {
                if (matchesPrefix(key, prefix) && node.isLeaf()) {
                    results.add(key);
                }
            } else if (key[0] > prefix[0]) {
                break;
            }
            i++;
        }

        if (!node.isLeaf()) {
            for (BPlusTreeNode child : node.getChildren()) {
                searchPrefix(child, prefix, results);
            }
        }
    }

    private boolean isPrefix(int[] triplet, int[] prefix) {
        return Arrays.equals(Arrays.copyOfRange(triplet, 0, Math.min(prefix.length, triplet.length)), prefix);
    }

    private boolean matchesPrefix(int[] triplet, int[] prefix) {
        return Arrays.equals(Arrays.copyOfRange(triplet, 0, prefix.length), prefix);
    }

    public void insert(int[] triplet) {
        BPlusTreeNode root = this.root;
        if (root.getKeys().size() == (2 * t) - 1) {
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.addChild(root);
            splitChild(newRoot, 0);
            this.root = newRoot;
        }
        insertNonFull(this.root, triplet);
    }

    private void insertNonFull(BPlusTreeNode node, int[] triplet) {
        if (node.isLeaf()) {
            node.getKeys().add(triplet);
            Arrays.sort(node.getKeys().toArray(new int[0][]), (a, b) -> compareTriplets(a, b));
        } else {
            int i = node.getKeys().size() - 1;
            while (i >= 0 && compareTriplets(triplet, node.getKeys().get(i)) < 0) {
                i--;
            }
            i++;
            if (node.getChildren().get(i).getKeys().size() == (2 * t) - 1) {
                splitChild(node, i);
                if (compareTriplets(triplet, node.getKeys().get(i)) > 0) {
                    i++;
                }
            }
            insertNonFull(node.getChildren().get(i), triplet);
        }
    }

    private void splitChild(BPlusTreeNode parent, int index) {
        int t = this.t;
        BPlusTreeNode node = parent.getChildren().get(index);
        BPlusTreeNode newNode = new BPlusTreeNode(node.isLeaf());

        parent.getKeys().add(index, node.getKeys().get(t - 1));
        parent.getChildren().add(index + 1, newNode);

        newNode.getKeys().addAll(node.getKeys().subList(t, node.getKeys().size()));
        node.getKeys().subList(t - 1, node.getKeys().size()).clear();

        if (!node.isLeaf()) {
            newNode.getChildren().addAll(node.getChildren().subList(t, node.getChildren().size()));
            node.getChildren().subList(t, node.getChildren().size()).clear();
        }
    }

    private int compareTriplets(int[] triplet1, int[] triplet2) {
        for (int i = 0; i < Math.min(triplet1.length, triplet2.length); i++) {
            int compare = Integer.compare(triplet1[i], triplet2[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return Integer.compare(triplet1.length, triplet2.length);
    }
}


