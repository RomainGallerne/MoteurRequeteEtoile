package main.java.qengine.model;

import java.util.ArrayList;
import java.util.List;

public class BPlusTreeNode {
    /**
     * Indicateur si le noeud est une feuille.
     */
    private boolean leaf;

    /**
     * Liste des clés (triplets encodés en int[]).
     */
    private List<int[]> keys;

    /**
     * Liste des enfants de ce noeud (pour les noeuds internes).
     */
    private List<BPlusTreeNode> children;

    /**
     * Constructeur pour créer un noeud, avec une option pour spécifier si c'est une feuille.
     *
     * @param leaf True si ce noeud est une feuille, sinon False.
     */
    public BPlusTreeNode(boolean leaf) {
        this.leaf = leaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public boolean isLeaf() {
        return leaf;
    }

    public List<int[]> getKeys() {
        return keys;
    }

    public List<BPlusTreeNode> getChildren() {
        return children;
    }

    public void addChild(BPlusTreeNode child) {
        children.add(child);
    }
}
