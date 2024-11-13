package main.java.qengine.storage;

import fr.boreal.model.logicalElements.api.*;
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
    private ArrayList<RDFAtom> rdfAtoms = new ArrayList<>();
    private BPlusTree bPlusTree;

    @Override
    public boolean add(RDFAtom atom) {
        Arrays.stream(atom.getTerms())
                .forEach(dictionnary::addTerm);

        return rdfAtoms.add(atom);
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

        List<Substitution> substitutions = new ArrayList<>();
        for (int[] result : results) {
            Substitution substitution = new SubstitutionImpl();

            for (int i = 0; i < result.length; i++) {
                Variable variable = new VariableImpl("var" + i);
                Term term = new IdentityLiteralImpl(result[i]);
                substitution.add(variable, term);
            }

            substitutions.add(substitution);
        }

        return substitutions.iterator();
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
        BPlusTree bPlusTree = new BPlusTree(3);

        List<int[]> listeTripletCODE = rdfAtoms.stream()
                .map(dictionnary::encodeTriplet)
                .collect(Collectors.toList());

        listeTripletCODE.stream().forEach(bPlusTree::insert);
    }

    public static void main(String[] args) throws IOException {
        FileReader rdfFile = new FileReader("java/RDF/nosql-engine-skeleton/src/test/resources/sample_data2.nt");
        List<RDFAtom> rdfAtoms = new ArrayList<>();

        try (RDFAtomParser rdfAtomParser = new RDFAtomParser(rdfFile, RDFFormat.NTRIPLES)) {
            int count = 0;
            while (rdfAtomParser.hasNext()) {
                RDFAtom atom = rdfAtomParser.next();
                rdfAtoms.add(atom);  // Stocker l'atome dans la collection
                System.out.println("RDF Atom #" + (++count) + ": " + atom);
            }
            System.out.println("Total RDF Atoms parsed: " + count);
        }

        Dictionnary dictionnary = new Dictionnary();

        for (RDFAtom rdfAtom : rdfAtoms) {
            for (Term term : rdfAtom.getTerms()) {
                dictionnary.addTerm(term);
            }
        }

        dictionnary.createCodex();

        List<int[]> listeTripletCODE = rdfAtoms.stream()
                .map(dictionnary::encodeTriplet)
                .collect(Collectors.toList());

        BPlusTree bPlusTree = new BPlusTree(3);
        listeTripletCODE.stream().forEach(bPlusTree::insert);

        bPlusTree.printBPlusTree();

        System.out.println("Recherche avec préfixe (1,): ");
        List<int[]> results1 = bPlusTree.searchPrefix(new int[]{1});
        for (int[] result : results1) {
            System.out.println(Arrays.toString(result));
        }

        System.out.println("Recherche avec préfixe (1, 9): ");
        List<int[]> results2 = bPlusTree.searchPrefix(new int[]{1, 9});
        for (int[] result : results2) {
            System.out.println(Arrays.toString(result));
        }

        System.out.println("Recherche avec préfixe (2,): ");
        List<int[]> results3 = bPlusTree.searchPrefix(new int[]{2});
        for (int[] result : results3) {
            System.out.println(Arrays.toString(result));
        }
    }
}
