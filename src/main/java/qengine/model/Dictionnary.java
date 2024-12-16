package qengine.model;

import fr.boreal.model.logicalElements.api.Term;
import qengine.exceptions.ValueNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        this.dictionary = dictionary.entrySet()
                .parallelStream() // Utilise un ParallelStream pour le tri
                .sorted(Map.Entry.<Term, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Résolution des doublons (inutile ici)
                        LinkedHashMap::new // Conserver l'ordre
                ));
    }

    /// Donne la clé dans le dictionnaire depuis un terme.
    /// Renvoie -1 si la clé n'existe pas.
    ///
    /// Accès max en O(n) en théorie mais O(log n) en pratique
    /// car plus le terme est fréquent, plus il est en haut du dictionnaire.
    public int getKey(Term term) {
        if(!term.isLiteral()) {return -1;}
        List<Term> keys = new ArrayList<>(dictionary.keySet());

        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(term)) {
                return i;
            }
        }

        return -1;
    }

    /// Donne la valeur depuis une clé.
    ///
    /// Accès instantanée -> O(1)
    public Term getValue(int index) throws ValueNotFoundException {
        List<Term> keys = new ArrayList<>(dictionary.keySet());

        if (index < 0 || index >= keys.size()) {
            throw new ValueNotFoundException(index);
        }

        return keys.get(index);
    }


    /// Encode un triplet RDF par ses clés
    ///
    /// Accès max en O(n) mais à calculer précisément
    public int[] encodeTriplet(RDFAtom triplet) {
        Term[] terms = triplet.getTerms();
        List<Integer> encodedTerms = new ArrayList<>();

        for (Term term : terms) {
            if (term.isLiteral()) {
                int term_key = getKey(term);
                encodedTerms.add(term_key);
            }
        }

        int[] result = new int[encodedTerms.size()];
        for (int i = 0; i < encodedTerms.size(); i++) {
            result[i] = encodedTerms.get(i);
        }

        return result;
    }


    /// Décode un triplet de clé et renvoie un triplet RDF
    ///
    /// Accès en 3*O(1) = O(1)
    public RDFAtom decodeTriplet(int[] triplet) throws ValueNotFoundException {
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