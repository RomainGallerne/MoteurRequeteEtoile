########## INPORT ##########

from collections import Counter
from tripletRDF import TripletRDF, liste_tripletRDF
from hexastore import BPlusTree, print_bplus_tree

########## VARIABLES GLOBALES ##########

dico_triee = []

########## FONCTIONS ##########

def build_dico():
    """
    Construction du dictionnaire à partir des triplets RDF.
    Retourne la liste des termes ordonnée
    """
    compteur = Counter()
    for triplet in liste_tripletRDF:
        termes = TripletRDF.get_triplet(triplet)
        compteur.update(termes)

    return [tuple[0] for tuple in compteur.most_common()]

def getKey(terme : str) -> int:
    """
    Donne la clé dans le dictionnaire depuis un terme.
    Accès max en O(n) mais à calculer précisément
    """
    for i in range(len(dico_triee)):
        if(dico_triee[i] == terme):
            return i
    return -1    

def getValue(cle : int) -> str:
    """
    Donne la valeur depuis une clé.
    Accès instantanée -> O(1)
    """
    try:
        return dico_triee[cle]
    except Exception:
        return -1
    
def coder_triplet(triplet : TripletRDF) -> tuple:
    """
    Encode un triplet RDF par ses clés
    Accès max en O(n) mais à calculer précisément
    """
    termes = TripletRDF.get_triplet(triplet)
    return (
        getKey(termes[0]),
        getKey(termes[1]),
        getKey(termes[2])
        )

def decoder_triplet(triplet : tuple) -> TripletRDF:
    """
    Décode un triplet de clé et renvoie un triplet RDF
    Accès en 3*O(1) = O(1)
    """
    return TripletRDF(
        getValue(triplet[0]),
        getValue(triplet[1]),
        getValue(triplet[2])
        )

########## MAIN ##########

dico_triee = build_dico()
liste_tripletCODE = [coder_triplet(triplet) for triplet in liste_tripletRDF]

print(liste_tripletCODE)

print("\n----------------------\n")

print(getValue(2)) #Charlie
print(getKey("estAmis")) #6

print("\n----------------------\n")

bptree = BPlusTree(3)
for tuple in liste_tripletCODE:
    bptree.insert(tuple)

#Recherche avec préfixe (1)
print(bptree.search_prefix((1,)))
#Recherche avec préfixe (1, 9)
print(bptree.search_prefix((1, 9)))
#Recherche avec préfixe (2)
print(bptree.search_prefix((2,)))
