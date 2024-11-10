########## INPORT ##########

from collections import deque

"""
SPO
SOP
OPS
OSP
PSO
POS
"""

########## FONCTIONS ##########

def print_bplus_tree(tree):
    """Affiche le B+ Tree dans la console, avec chaque niveau sur une ligne séparée."""
    if not tree.root:
        print("L'arbre est vide.")
        return

    queue = deque([(tree.root, 0)])
    current_level = 0
    level_nodes = []

    while queue:
        node, level = queue.popleft()

        if level != current_level:
            print("Niveau", current_level, ":", " | ".join(str(n.keys) for n in level_nodes))
            level_nodes = []
            current_level = level

        level_nodes.append(node)

        if not node.leaf:
            for child in node.children:
                queue.append((child, level + 1))

    print("Niveau", current_level, ":", " | ".join(str(n.keys) for n in level_nodes))


########## CLASS ##########

class BPlusTreeNode:
    """
    Classe d'un noeud de l'arbre.
    """
    def __init__(self, leaf=False):
        self.leaf = leaf
        self.keys = []
        self.children = []

class BPlusTree:
    """
    Classe de l'arbre.
    """
    def __init__(self, t):
        """
        Constructeur.
        """
        self.root = BPlusTreeNode(leaf=True)
        self.t = t
    
    def insert(self, triplet):
        """
        Méthode d'insertion dans le B+ tree
        """
        root = self.root
        if len(root.keys) == (2 * self.t) - 1:
            new_root = BPlusTreeNode()
            new_root.children.append(self.root)
            self._split_child(new_root, 0)
            self.root = new_root
        self._insert_non_full(self.root, triplet)

    def _insert_non_full(self, node, triplet):
        if node.leaf:
            node.keys.append(triplet)
            node.keys.sort()
        else:
            i = len(node.keys) - 1
            while i >= 0 and triplet < node.keys[i]:
                i -= 1
            i += 1
            if len(node.children[i].keys) == (2 * self.t) - 1:
                self._split_child(node, i)
                if triplet > node.keys[i]:
                    i += 1
            self._insert_non_full(node.children[i], triplet)

    def _split_child(self, parent, index):
        t = self.t
        node = parent.children[index]
        new_node = BPlusTreeNode(leaf=node.leaf)
        parent.keys.insert(index, node.keys[t-1])
        parent.children.insert(index + 1, new_node)
        new_node.keys = node.keys[t:]
        node.keys = node.keys[:t-1]
        if not node.leaf:
            new_node.children = node.children[t:]
            node.children = node.children[:t]

    def search_prefix(self, prefix):
        results = []
        self._search_prefix(self.root, prefix, results)
        return results
    
    def _search_prefix(self, node, prefix, results):
        i = 0
        while i < len(node.keys) and self._is_prefix(node.keys[i], prefix):
            if self._matches_prefix(node.keys[i], prefix):
                if node.leaf:
                    results.append(node.keys[i])
            i += 1
        if not node.leaf:
            for child in node.children:
                self._search_prefix(child, prefix, results)

    def _is_prefix(self, triplet, prefix):
        """Vérifie si 'prefix' est un préfixe de 'triplet'."""
        return triplet[:len(prefix)] == prefix
    
    def _matches_prefix(self, triplet, prefix):
        """Vérifie si 'triplet' commence par 'prefix'."""
        return triplet[:len(prefix)] == prefix


########## MAIN ##########
