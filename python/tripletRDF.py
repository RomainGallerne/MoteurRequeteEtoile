"""
Classe TripletRDF créé rapidemment pour simuler la vrai classe Java
"""


########## CLASS ##########

class TripletRDF:
    def __init__(self, s: str, p: str, o: str):
        self.s = s
        self.p = p
        self.o = o

    def __str__(self):
        return f"<{self.s}, {self.p}, {self.o}>"
    
    def get_triplet(self):
        return (self.s, self.p, self.o)
    
########## FONCTION ##########    

def build_dico():
    liste_tripletRDF = []
    with open('tripletRDF.txt', 'r') as f:
        for ligne in f:
            termes = ligne.strip()[1:-1].split(', ')
            liste_tripletRDF.append(TripletRDF(termes[0], termes[1], termes[2]))
    return liste_tripletRDF

########## MAIN ##########    

liste_tripletRDF = build_dico()