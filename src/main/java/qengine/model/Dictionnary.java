package qengine.model;

import fr.boreal.model.logicalElements.api.Term;
import qengine.exceptions.ValueNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public class Dictionnary {
    private LinkedHashMap<Term, Integer> dictionary = new LinkedHashMap<>();
    private List<Term> keys;
    private final Map<Term, Integer> termToIndexMap = new HashMap<>();

    /// Remplit un index inversté pour une récupération facilité des clés.
    public void initializeKeyMap() {
        for (int i = 0; i < keys.size(); i++) {
            termToIndexMap.put(keys.get(i), i);
        }
    }

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
        this.keys = new ArrayList<>(dictionary.keySet());
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
    /// Accès instantané -> O(1)
    public int getKey(Term term) {
        if (!term.isLiteral()) {
            return -1;
        }
        return termToIndexMap.getOrDefault(term, -1);
    }

    /// Donne la valeur depuis une clé.
    ///
    /// Accès instantanée -> O(1)
    public Term getValue(int index) throws ValueNotFoundException {

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
        List<Integer> encodedTerms = new ArrayList<>(terms.length); // Prédéfinir la capacité

        Map<Term, Integer> cache = new HashMap<>();
        for (Term term : terms) {
            if (term.isLiteral()) {
                int term_key = cache.computeIfAbsent(term, this::getKey);
                encodedTerms.add(term_key);
            }
        }

        return encodedTerms.stream().mapToInt(Integer::intValue).toArray();
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