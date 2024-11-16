package main.java.qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
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

    public int[] dico_encodeTriplet(RDFAtom rdfAtom) {
        return this.dictionnary.encodeTriplet(rdfAtom);
    }

    public RDFAtom dico_decodeTriplet(int[] triplet_encode) {
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
            int[] atomEncoder = dico_encodeTriplet(atom);

            this.OPS.ajoutTriplet(permuteTriplet(atomEncoder, "OPS"));
            this.OSP.ajoutTriplet(permuteTriplet(atomEncoder, "OSP"));
            this.POS.ajoutTriplet(permuteTriplet(atomEncoder, "POS"));
            this.PSO.ajoutTriplet(permuteTriplet(atomEncoder, "PSO"));
            this.SOP.ajoutTriplet(permuteTriplet(atomEncoder, "SOP"));
            this.SPO.ajoutTriplet(permuteTriplet(atomEncoder, "SPO"));
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

        int s_code = dictionnary.getKey(s);
        int p_code = dictionnary.getKey(p);
        int o_code = dictionnary.getKey(o);

        boolean s_var = s.isLiteral();
        boolean p_var = p.isLiteral();
        boolean o_var = o.isLiteral();

        List<int[]> results;

        if (s_var && p_var && o_var) {
            //Vérifier si tuple est présent et le retourner sinon rien
            results = SPO.searchByThree(s_code, p_code, o_code);

        } else if (s_var && p_var) {
            //SP? ou PS?
            results = SPO.searchByTwo(s_code, p_code);
            //result = PSO.searchByTwo(p_code, s_code);

        } else if (s_var && o_var) {
            //SO? ou OS?
            results = SOP.searchByTwo(s_code, o_code);
            //result = OSP.searchByTwo(o_code, s_code);

        } else if (p_var && o_var) {
            //PO? ou OP?
            results = POS.searchByTwo(p_code, o_code);
            //result = OPS.searchByTwo(o_code, p_code);

        } else if (s_var) {
            System.out.println("3");
            //S??
            results = SPO.searchByOne(s_code);
            //result = SOP.searchByOne(s_code);

        } else if (p_var) {
            //P??
            results = PSO.searchByOne(p_code);
            //result = POS.searchByOne(p_code);

        } else if (o_var) {
            //O??
            results = OPS.searchByOne(o_code);
            //result = OSP.searchByOne(o_code);


        } else {
            //Retourner tous les tuples
            int[] array = {s_code, p_code, o_code};
            results = new ArrayList<>();
            results.add(array);
        }

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


    /**
     * @param q star query
     * @return an itérateur de subsitutions décrivrant les réponses à la requete
     */
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
    }
}
