package main.java.qengine.model;

import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.impl.ConstantImpl;
import fr.boreal.model.logicalElements.impl.identityObjects.IdentityLiteralImpl;
import main.java.qengine.parser.RDFAtomParser;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class Dictionnary {
    private LinkedHashMap<Term, Integer> dictionary = new LinkedHashMap<>();

    public void addTerm(Term term) {
        dictionary.put(term, dictionary.getOrDefault(term, 0) + 1);
    }

    /// Organise les terms en fonction de leur fréquence par ordre décroissant.
    ///
    /// Créé l'ordre pour accéder plus rapidement à un élément selon sa récurrence
    public void createCodex() {
        List<Map.Entry<Term, Integer>> entries = new ArrayList<>(dictionary.entrySet());

        entries.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

        dictionary = new LinkedHashMap<>();
        for (Map.Entry<Term, Integer> entry : entries) {
            dictionary.put(entry.getKey(), entry.getValue());
        }
    }

    /// Donne la clé dans le dictionnaire depuis un terme.
    ///
    /// Accès max en O(n) mais à calculer précisément
    private int getKey(Term term) {
        return IntStream.range(0, new ArrayList<>(dictionary.keySet()).size())
                .filter(i -> new ArrayList<>(dictionary.keySet()).get(i).equals(term))
                .findFirst()
                .orElse(-1);
    }

    /// Donne la valeur depuis une clé.
    ///
    /// Accès instantanée -> O(1)
    private Term getValue(int index) {
        return (Term) dictionary.keySet().toArray()[index];
    }

    /// Encode un triplet RDF par ses clés
    ///
    /// Accès max en O(n) mais à calculer précisément
    public int[] encodeTriplet(RDFAtom triplet) {
        return Arrays.stream(triplet.getTerms())
                .mapToInt(this::getKey)
                .toArray();
    }

    /// Décode un triplet de clé et renvoie un triplet RDF
    ///
    /// Accès en 3*O(1) = O(1)
    public RDFAtom decodeTriplet(int[] triplet) {
        return new RDFAtom(getValue(triplet[0]), getValue(triplet[1]), getValue(triplet[2]));
    }

    /// tester le modèle objet Dictionnaire avec un fichier exemple
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
            System.out.println(rdfAtom);
            for (Term term : rdfAtom.getTerms()) {
                dictionnary.addTerm(term);
            }
        }

        dictionnary.createCodex();

        System.out.println("==================================");
        System.out.println("try getValue(2) \nexpected: Charlie\nresult: "+dictionnary.getValue(2));
        System.out.println();
        System.out.println("try getKey(\"estAmis\") \nexpected: 6\nresult: "+dictionnary.getKey(new IdentityLiteralImpl("http://example.org/estAmis")));
        System.out.println("==================================");

        System.out.println("Encoded RDF Atoms:\n");
        rdfAtoms.stream()
                .map(dictionnary::encodeTriplet)
                .forEach(encodedTriplet -> System.out.println(Arrays.toString(encodedTriplet)));
    }
}