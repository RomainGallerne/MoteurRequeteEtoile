package main.java.qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import fr.boreal.model.logicalElements.impl.VariableImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityLiteralImpl;
import main.java.qengine.model.BPlusTree;
import main.java.qengine.model.Dictionnary;
import main.java.qengine.parser.RDFAtomParser;
import org.apache.commons.lang3.NotImplementedException;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.model.StarQuery;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation d'un HexaStore pour stocker des RDFAtom.
 * Cette classe utilise six index pour optimiser les recherches.
 * Les index sont basés sur les combinaisons (Sujet, Prédicat, Objet), (Sujet, Objet, Prédicat),
 * (Prédicat, Sujet, Objet), (Prédicat, Objet, Sujet), (Objet, Sujet, Prédicat) et (Objet, Prédicat, Sujet).
 */
public class RDFHexaStore implements RDFStorage {
    private Dictionnary dictionnary = new Dictionnary();
    private Set<RDFAtom> rdfAtoms = new HashSet<>();
    private BPlusTree bPlusTree;

    @Override
    public boolean add(RDFAtom atom) {
        boolean res = rdfAtoms.add(atom);
        if(res) {
            Arrays.stream(atom.getTerms())
                    .forEach(dictionnary::addTerm);
        }

        return res;
    }

    @Override
    public long size() {
        return rdfAtoms.size();
    }

    @Override
    public Iterator<Substitution> match(RDFAtom atom) {
        setup();
        int[] triplet = dictionnary.encodeTriplet(atom);

        List<int[]> results = bPlusTree.searchPrefix(triplet);

        Set<Substitution> uniqueSubstitutions = new HashSet<>();

        for (int[] result : results) {
            Substitution substitution = new SubstitutionImpl();

            for (int i = 0; i < result.length; i++) {

                if (atom.getTerms()[i] instanceof Variable) {
                    substitution.add((Variable) atom.getTerms()[i], dictionnary.getValue(result[i]));
                }
            }

            uniqueSubstitutions.add(substitution);
        }

        return uniqueSubstitutions.iterator();
    }

    @Override
    public Iterator<Substitution> match(StarQuery q) {
        throw new NotImplementedException();
    }

    @Override
    public Collection<Atom> getAtoms() {
        return rdfAtoms.stream().collect(Collectors.toSet());
    }

    private void setup() {
        dictionnary.createCodex();
        this.bPlusTree = new BPlusTree(3);

        List<int[]> listeTripletCODE = rdfAtoms.stream()
                .map(dictionnary::encodeTriplet)
                .collect(Collectors.toList());

        listeTripletCODE.stream().forEach(this.bPlusTree::insert);

        this.bPlusTree.printBPlusTree();
    }
}
