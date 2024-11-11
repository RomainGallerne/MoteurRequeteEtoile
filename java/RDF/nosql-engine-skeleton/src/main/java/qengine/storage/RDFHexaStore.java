package main.java.qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import org.apache.commons.lang3.NotImplementedException;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.model.StarQuery;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Implémentation d'un HexaStore pour stocker des RDFAtom.
 * Cette classe utilise six index pour optimiser les recherches.
 * Les index sont basés sur les combinaisons (Sujet, Prédicat, Objet), (Sujet, Objet, Prédicat),
 * (Prédicat, Sujet, Objet), (Prédicat, Objet, Sujet), (Objet, Sujet, Prédicat) et (Objet, Prédicat, Sujet).
 */
public class RDFHexaStore implements RDFStorage {
    private LinkedHashMap<Term, Integer> dictionary = new LinkedHashMap<>();

    @Override
    public boolean add(RDFAtom atom) { //TODO
        throw new NotImplementedException();
    }

    public void addTerm(Term term) {
        dictionary.put(term, dictionary.getOrDefault(term, 0) + 1);
    }

    private void createCodex() {
        // Créer une liste d'entrées (clé, fréquence)
        List<Map.Entry<Term, Integer>> entries = new ArrayList<>(dictionary.entrySet());

        // Trier la liste par fréquence décroissante
        entries.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

        // Réinitialiser la map et ajouter les éléments triés dans l'ordre
        dictionary = new LinkedHashMap<>();
        for (Map.Entry<Term, Integer> entry : entries) {
            dictionary.put(entry.getKey(), entry.getValue());
        }
    }

    private int getKey(Term term) {
        return IntStream.range(0, new ArrayList<>(dictionary.keySet()).size())
                .filter(i -> new ArrayList<>(dictionary.keySet()).get(i).equals(term))
                .findFirst()
                .orElse(-1);
    }

    private Term getValue(int index) {
        return (Term) dictionary.keySet().toArray()[index];
    }

    @Override
    public long size() {
        throw new NotImplementedException();
    }

    @Override
    public Iterator<Substitution> match(RDFAtom atom) {
        throw new NotImplementedException();
    }

    @Override
    public Iterator<Substitution> match(StarQuery q) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<Atom> getAtoms() {
        throw new NotImplementedException();
    }
}
