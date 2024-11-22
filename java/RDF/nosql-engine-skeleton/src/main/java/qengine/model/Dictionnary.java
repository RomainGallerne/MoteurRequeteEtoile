package main.java.qengine.model;

import fr.boreal.model.logicalElements.api.Term;
import main.java.qengine.exceptions.KeyNotFoundException;
import main.java.qengine.exceptions.ValueNotFoundException;

import java.util.*;

public class Dictionnary {
    private LinkedHashMap<Term, Integer> dictionary = new LinkedHashMap<>();

    /// Ajoute une term au dictionnaire s'il n'est pas présent
    ///
    /// Incrémente sa fréquence sinon
    public void addTerm(Term term) {
        dictionary.put(term, dictionary.getOrDefault(term, 0) + 1);
    }

    /// Organise les terms en fonction de leur fréquence par ordre décroissant.
    ///
    /// Créé l'ordre pour accéder plus rapidement à un élément selon sa récurrence
    public void createCodex() {
        List<Map.Entry<Term, Integer>> entryList = new ArrayList<>(dictionary.entrySet());

        // Trier la liste par ordre décroissant des valeurs
        entryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Reconstruire une LinkedHashMap triée
        LinkedHashMap<Term, Integer> sortedDictionary = new LinkedHashMap<>();
        for (Map.Entry<Term, Integer> entry : entryList) {
            sortedDictionary.put(entry.getKey(), entry.getValue());
        }

        this.dictionary = sortedDictionary;
    }

    /// Donne la clé dans le dictionnaire depuis un terme.
    ///
    /// Accès max en O(n) mais à calculer précisément
    public int getKey(Term term) throws KeyNotFoundException {
        if (dictionary.containsKey(term)) {
            return dictionary.get(term);
        }

        throw new KeyNotFoundException(term);
    }

    /// Donne la valeur depuis une clé.
    ///
    /// Accès instantanée -> O(1)
    public Term getValue(int index) throws ValueNotFoundException {
        if(dictionary.containsValue(index)) {
            return (Term) dictionary.keySet().toArray()[index];
        }

        throw new ValueNotFoundException(index);
    }

    /// Encode un triplet RDF par ses clés
    ///
    /// Accès max en O(n) mais à calculer précisément
    public int[] encodeTriplet(RDFAtom triplet) throws KeyNotFoundException {
        int[] tripletInt = new int[triplet.getTerms().length];
        int index = 0;

        for (Term term : triplet.getTerms()) {
            if (term.isLiteral()) {
                tripletInt[index++] = getKey(term);
            }
        }

        return tripletInt;

    }

    /// Décode un triplet de clé et renvoie un triplet RDF
    ///
    /// Accès en 3*O(1) = O(1)
    public RDFAtom decodeTriplet(int[] triplet) throws ValueNotFoundException{
        return new RDFAtom(getValue(triplet[0]), getValue(triplet[1]), getValue(triplet[2]));
    }

    public String toString() {
        String string = "";
        for (Term key : dictionary.keySet()) {
            string = string.concat(key.toString() + " : " + dictionary.get(key).toString() + "\n");
        }
        return string;
    }
}