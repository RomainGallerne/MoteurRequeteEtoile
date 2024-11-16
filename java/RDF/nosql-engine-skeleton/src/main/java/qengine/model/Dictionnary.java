package main.java.qengine.model;

import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.logicalElements.impl.ConstantImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityLiteralImpl;
import main.java.qengine.parser.RDFAtomParser;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Dictionnary {
    private LinkedHashMap<Term, Integer> dictionary = new LinkedHashMap<>();

    /// Ajoute une term au dictionnaire s'il n'est pas présent
    ///
    /// Incrémente sa fréquence sinon
    public void addTerm(Term term) {
        dictionary.put(term, dictionary.getOrDefault(term, 0) + 1);
    }

    /// Permet d'ajouter plusieurs termes en un appel
    public void addTerms(List<Term> terms) {
        for (Term term : terms) {
            addTerm(term);
        }
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
    public int getKey(Term term) {
        return IntStream.range(0, new ArrayList<>(dictionary.keySet()).size())
                .filter(i -> new ArrayList<>(dictionary.keySet()).get(i).equals(term))
                .findFirst()
                .orElse(-1);
    }

    /// Donne la valeur depuis une clé.
    ///
    /// Accès instantanée -> O(1)
    public Term getValue(int index) {
        return (Term) dictionary.keySet().toArray()[index];
    }

    /// Encode un triplet RDF par ses clés
    ///
    /// Accès max en O(n) mais à calculer précisément
    public int[] encodeTriplet(RDFAtom triplet) {
        return Arrays.stream(triplet.getTerms())
                .filter(term -> !(term instanceof Variable && term.label().startsWith("?")))
                .mapToInt(this::getKey)
                .toArray();
    }

    /// Décode un triplet de clé et renvoie un triplet RDF
    ///
    /// Accès en 3*O(1) = O(1)
    public RDFAtom decodeTriplet(int[] triplet) {
        return new RDFAtom(getValue(triplet[0]), getValue(triplet[1]), getValue(triplet[2]));
    }

    public String toString(){
        String string = "";
        for (Term key : dictionary.keySet()) {
            string = string.concat(key.toString() + " : " + dictionary.get(key).toString()+"\n");
        }
        return string;
    }
}