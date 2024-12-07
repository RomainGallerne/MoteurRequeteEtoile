package qengine.storage;

import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import qengine.exceptions.KeyNotFoundException;
import qengine.exceptions.ValueNotFoundException;
import qengine.model.*;

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
        Map<String, int[]> permutations = Map.of(
                "OPS", new int[]{2, 1, 0},
                "OSP", new int[]{2, 0, 1},
                "POS", new int[]{1, 2, 0},
                "PSO", new int[]{1, 0, 2},
                "SOP", new int[]{0, 2, 1}
        );

        int[] indices = permutations.getOrDefault(ordre, new int[]{0, 1, 2});
        return new int[]{triplet[indices[0]], triplet[indices[1]], triplet[indices[2]]};
    }

    /**
    * Créer le codex du dictionnaire.
    **/
    public void dico_createCodex() {
        this.dictionnary.createCodex();
    }

    public int[] dico_encodeTriplet(RDFAtom rdfAtom) {
        return this.dictionnary.encodeTriplet(rdfAtom);
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
        if (!rdfAtoms.add(atom)) {return false;}
        int[] atomEncoder = dico_encodeTriplet(atom);

        for (int i = 0; i < atomEncoder.length; i++) {
            if (atomEncoder[i] == -1){
                try {
                    throw new KeyNotFoundException(atom.getTerm(i));
                } catch (KeyNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        Map<String, Index> permutationMap = Map.of(
                "OPS", this.OPS,
                "OSP", this.OSP,
                "POS", this.POS,
                "PSO", this.PSO,
                "SOP", this.SOP
        );

        permutationMap.forEach((key, hexastore) ->
                hexastore.ajoutTriplet(permuteTriplet(atomEncoder, key))
        );
        this.SPO.ajoutTriplet(atomEncoder);

        return true;
    }

    /**
     * Retourne le nombre d'atomes dans le Store.
     *
     * @return le nombre d'atomes
     */
    @Override
    public long size() {
        return rdfAtoms.size();
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

        boolean s_var = s.isLiteral();
        boolean p_var = p.isLiteral();
        boolean o_var = o.isLiteral();

        int s_code = -1;
        int p_code = -1;
        int o_code = -1;

        if(s_var){
            s_code = dictionnary.getKey(s);
            if (s_code == -1) {return Collections.emptyIterator();}
        }
        if(p_var){
            p_code = dictionnary.getKey(p);
            if (p_code == -1) {return Collections.emptyIterator();}
        }
        if(o_var){
            o_code = dictionnary.getKey(o);
            if (o_code == -1) {return Collections.emptyIterator();}
        }

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
            //???
            int[] array = {s_code, p_code, o_code};
            results = new ArrayList<>();
            results.add(array);
        }

        Set<Substitution> uniqueSubstitutions = new HashSet<>();
        for (int[] result : results) {
            Substitution substitution = new SubstitutionImpl();

            for (int i = 0; i < result.length; i++) {
                if (atom.getTerms()[i] instanceof Variable) {
                    try {
                        substitution.add((Variable) atom.getTerms()[i], dictionnary.getValue(result[i]));
                    } catch (ValueNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            uniqueSubstitutions.add(substitution);
        }

        return uniqueSubstitutions.iterator();
    }


    /**
     * @param q star query
     * @return an itérateur de subsitutions décrivrant les réponses à la requete
     */
    @Override
    public Iterator<Substitution> match(StarQuery q) {
        List<RDFAtom> rdfAtoms = q.getRdfAtoms();

        Iterator<Substitution> matchingAtoms = match(rdfAtoms.getFirst());
        Set<Substitution> currentMatches = new HashSet<>();
        matchingAtoms.forEachRemaining(currentMatches::add);

        for (int i = 1; i < rdfAtoms.size(); i++) {
            RDFAtom rdfAtom = rdfAtoms.get(i);
            Iterator<Substitution> matchResult = match(rdfAtom);

            Set<Substitution> nextMatches = new HashSet<>();
            matchResult.forEachRemaining(nextMatches::add);
            currentMatches.retainAll(nextMatches);

            if (currentMatches.isEmpty()) {
                return Collections.emptyIterator();
            }
        }
        return currentMatches.iterator();
    }

    @Override
    public Collection<Atom> getAtoms() {
        return rdfAtoms.stream().collect(Collectors.toSet());
    }
}
