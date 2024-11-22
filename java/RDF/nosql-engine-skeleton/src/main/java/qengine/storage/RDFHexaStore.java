package main.java.qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import main.java.qengine.exceptions.KeyNotFoundException;
import main.java.qengine.exceptions.ValueNotFoundException;
import main.java.qengine.model.*;
import org.apache.commons.lang3.NotImplementedException;

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

    private Index OPS;
    private Index OSP;
    private Index POS;
    private Index PSO;
    private Index SOP;
    private Index SPO;

    public RDFHexaStore() {
        this.OPS = new Index();
        this.OSP = new Index();
        this.POS = new Index();
        this.PSO = new Index();
        this.SOP = new Index();
        this.SPO = new Index();
    }

    // Effectue les permutations nécessaires pour constuire l'index.
    // On suppose qu'on récupère un triplet au format SPO en entrée.
    public static int[] permuteTriplet(int[] triplet, String ordre) {

        return switch (ordre) {
            case "OPS" -> new int[]{triplet[2], triplet[1], triplet[0]};
            case "OSP" -> new int[]{triplet[2], triplet[0], triplet[1]};
            case "POS" -> new int[]{triplet[1], triplet[2], triplet[0]};
            case "PSO" -> new int[]{triplet[1], triplet[0], triplet[2]};
            case "SOP" -> new int[]{triplet[0], triplet[2], triplet[1]};
            default -> triplet;
        };
    }

    /**
    * Créer le codex du dictionnaire.
    **/
    public void dico_createCodex() {
        this.dictionnary.createCodex();
    }

    public int[] dico_encodeTriplet(RDFAtom rdfAtom) throws KeyNotFoundException {
        return this.dictionnary.encodeTriplet(rdfAtom);
    }

    public RDFAtom dico_decodeTriplet(int[] triplet_encode) throws ValueNotFoundException{
        return this.dictionnary.decodeTriplet(triplet_encode);
    }

    public void add_to_dico(Term[] terms){
        Arrays.stream(terms).forEach(dictionnary::addTerm);
    }

    /**
     * Ajoute un RDFAtom dans le store.
     *
     * @param atom le RDFAtom à ajouter
     * @return true si le RDFAtom a été ajouté avec succès, false s'il est déjà présent
     */
    @Override
    public boolean add(RDFAtom atom) {
        boolean res = rdfAtoms.add(atom);
        if(res) {
            try {
                int[] atomEncoder = dico_encodeTriplet(atom);

                this.OPS.ajoutTriplet(permuteTriplet(atomEncoder, "OPS"));
                this.OSP.ajoutTriplet(permuteTriplet(atomEncoder, "OSP"));
                this.POS.ajoutTriplet(permuteTriplet(atomEncoder, "POS"));
                this.PSO.ajoutTriplet(permuteTriplet(atomEncoder, "PSO"));
                this.SOP.ajoutTriplet(permuteTriplet(atomEncoder, "SOP"));
                this.SPO.ajoutTriplet(atomEncoder);
            } catch (KeyNotFoundException e) {
                e.printStackTrace();
                res = false;
            }
        }

        return res;
    }

    /**
     * Retourne le nombre d'atomes dans le Store.
     *
     * @return le nombre d'atomes
     */
    @Override
    public long size() {
        return SPO.size();
    }

    /**
     * @param atom RDFAtom
     * @return un itérateur de substitutions correspondant aux match des atomes
     *          (i.e., sur quels termes s'envoient les variables)
     */
    @Override
    public Iterator<Substitution> match(RDFAtom atom) {
        Term s = atom.getTripleSubject();
        Term p = atom.getTriplePredicate();
        Term o = atom.getTripleObject();

        int s_code = 0;
        int p_code = 0;
        int o_code = 0;
        try {
            s_code = dictionnary.getKey(s);
            p_code = dictionnary.getKey(p);
            o_code = dictionnary.getKey(o);
        } catch (Exception e) {

        }

        boolean s_var = s.isLiteral();
        boolean p_var = p.isLiteral();
        boolean o_var = o.isLiteral();

        List<int[]> results = new ArrayList<>();
        List<int[]> ordered_results = new ArrayList<>();

        if (s_var && p_var && o_var) {
            //SPO
            for (int [] result : SPO.searchByThree(s_code, p_code, o_code)) {results.add(permuteTriplet(result, "SPO"));}
        } else if (s_var && p_var) {
            //SP? ou PS?
            for (int [] result : SPO.searchByTwo(s_code, p_code)) {results.add(permuteTriplet(result, "SPO"));}
            //for (int [] result : PSO.searchByTwo(p_code, s_code)) {results.add(permuteTriplet(result, "PSO"));}
        } else if (s_var && o_var) {
            //SO? ou OS?
            for (int [] result : SOP.searchByTwo(s_code, o_code)) {results.add(permuteTriplet(result, "SOP"));}
            //for (int [] result : OSP.searchByTwo(o_code, s_code)) {results.add(permuteTriplet(result, "POS"));}
        } else if (p_var && o_var) {
            //PO? ou OP?
            for (int [] result : POS.searchByTwo(p_code, o_code)) {results.add(permuteTriplet(result, "OSP"));}
            //for (int [] result : OPS.searchByTwo(o_code, p_code)) {results.add(permuteTriplet(result, "OPS"));}
        } else if (s_var) {
            //S??
            for (int [] result : SPO.searchByOne(s_code)){results.add(permuteTriplet(result, "SPO"));}
            //for (int [] result : SOP.searchByOne(s_code)) {results.add(permuteTriplet(result, "SOP"));}
        } else if (p_var) {
            //P??
            for (int [] result : PSO.searchByOne(p_code)) {results.add(permuteTriplet(result, "PSO"));}
            //for (int [] result : POS.searchByOne(p_code)) {results.add(permuteTriplet(result, "OSP"));}
        } else if (o_var) {
            //O??
            for (int [] result : OPS.searchByOne(o_code)) {results.add(permuteTriplet(result, "OPS"));}
            //for (int [] result : OSP.searchByOne(o_code)) {results.add(permuteTriplet(result, "POS"));}
        } else {
            //Retourner tous les tuples
            int[] array = {s_code, p_code, o_code};
            results = new ArrayList<>();
            results.add(array);
        }

        Set<Substitution> uniqueSubstitutions = new HashSet<>();
        try {
            for (int[] result : results) {
                Substitution substitution = new SubstitutionImpl();

                for (int i = 0; i < result.length; i++) {
                    if (atom.getTerms()[i] instanceof Variable) {
                        substitution.add((Variable) atom.getTerms()[i], dictionnary.getValue(result[i]));
                    }
                }
                uniqueSubstitutions.add(substitution);
            }
        } catch (ValueNotFoundException e) { e.printStackTrace(); }

        return uniqueSubstitutions.iterator();
    }


    /**
     * @param q star query
     * @return an itérateur de subsitutions décrivrant les réponses à la requete
     */
    @Override
    public Iterator<Substitution> match(StarQuery q) {
        List<RDFAtom> rdfAtoms = q.getRdfAtoms();

        // Initialisation avec les correspondances du premier RDFAtom
        Iterator<Substitution> matchingAtoms = match(rdfAtoms.getFirst());
        while(matchingAtoms.hasNext()){System.out.println("matchingAtoms : "+matchingAtoms.next());}
        Set<Substitution> currentMatches = new HashSet<>();
        matchingAtoms.forEachRemaining(currentMatches::add);

        // Parcourir les autres RDFAtom
        for (int i = 1; i < rdfAtoms.size(); i++) {
            RDFAtom rdfAtom = rdfAtoms.get(i);
            Iterator<Substitution> matchResult = match(rdfAtom);
            while(matchingAtoms.hasNext()){System.out.println("matchResult : "+matchResult.next());}

            // Convertir matchResult en un ensemble pour une intersection rapide
            Set<Substitution> nextMatches = new HashSet<>();
            matchResult.forEachRemaining(nextMatches::add);

            // Réaliser l'intersection directement
            currentMatches.retainAll(nextMatches);

            // Si aucune correspondance, sortir rapidement
            if (currentMatches.isEmpty()) {
                return Collections.emptyIterator();
            }
        }

        // Retourner l'itérateur des résultats finaux
        return currentMatches.iterator();
    }

    @Override
    public Collection<Atom> getAtoms() {
        return rdfAtoms.stream().collect(Collectors.toSet());
    }
}
