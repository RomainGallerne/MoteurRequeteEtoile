package test.java.qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import main.java.qengine.model.StarQuery;
import org.apache.commons.lang3.NotImplementedException;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.storage.RDFHexaStore;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link RDFHexaStore}.
 */
public class RDFHexaStoreTest {
    private static final TermFactory termFactory = SameObjectTermFactory.instance();

    private static final Literal<String> SUBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("subject1");
    private static final Literal<String> PREDICATE_1 = SameObjectTermFactory.instance().createOrGetLiteral("predicate1");
    private static final Literal<String> OBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("object1");
    private static final Literal<String> SUBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("subject2");
    private static final Literal<String> PREDICATE_2 = SameObjectTermFactory.instance().createOrGetLiteral("predicate2");
    private static final Literal<String> OBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("object2");
    private static final Literal<String> PREDICATE_3 = SameObjectTermFactory.instance().createOrGetLiteral("predicate3");
    private static final Literal<String> OBJECT_3 = SameObjectTermFactory.instance().createOrGetLiteral("object3");
    private static final Variable VAR_X = SameObjectTermFactory.instance().createOrGetVariable("?x");
    private static final Variable VAR_Y = SameObjectTermFactory.instance().createOrGetVariable("?y");


    @Test
    public void testAddAllRDFAtoms() {
        RDFHexaStore store = new RDFHexaStore();

        // Version stream
        // Ajouter plusieurs RDFAtom
        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);

        Set<RDFAtom> rdfAtoms = Set.of(rdfAtom1, rdfAtom2);

        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());

        assertTrue(store.addAll(rdfAtoms.stream()), "Les RDFAtoms devraient être ajoutés avec succès.");

        // Vérifier que tous les atomes sont présents
        Collection<Atom> atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom ajouté.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom ajouté.");

        // Version collection
        store = new RDFHexaStore();
        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());

        assertTrue(store.addAll(rdfAtoms), "Les RDFAtoms devraient être ajoutés avec succès.");

        // Vérifier que tous les atomes sont présents
        atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom ajouté.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom ajouté.");
    }

    @Test
    public void testAddRDFAtom() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom atom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);

        store.add_to_dico(atom1.getTerms());

        assertTrue(store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)));
    }

    @Test
    public void testAddDuplicateAtom() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom atom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);

        store.add_to_dico(atom1.getTerms());
        store.add(atom1);

        assertFalse(store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)));
    }

    @Test
    public void testSize() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom atom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom atom2 = new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2);
        RDFAtom atom3 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_3);

        store.add_to_dico(atom1.getTerms());
        store.add_to_dico(atom2.getTerms());
        store.add_to_dico(atom3.getTerms());

        store.add(atom1); // RDFAtom(subject1, triple, object1)
        store.add(atom2); // RDFAtom(subject2, triple, object2)
        store.add(atom3); // RDFAtom(subject1, triple, object3)

        assertEquals(3, store.size(), "There should be three RDFAtoms in the store");
    }

    @Test
    public void testMatchAtomWithSOrders() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2);
        RDFAtom rdfAtom3 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_3);
        RDFAtom rdfAtom4 = new RDFAtom(SUBJECT_2, OBJECT_2, PREDICATE_2);
        RDFAtom rdfAtom5 = new RDFAtom(SUBJECT_1, OBJECT_1, PREDICATE_1);

        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());
        store.add_to_dico(rdfAtom3.getTerms());
        store.add_to_dico(rdfAtom4.getTerms());
        store.add_to_dico(rdfAtom5.getTerms());

        store.dico_createCodex();

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        store.add(rdfAtom3);
        store.add(rdfAtom4);
        store.add(rdfAtom5);

        // Case 1 - SPO
        RDFAtom matchingAtom = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X); // RDFAtom(subject1, predicate1, X)
        Iterator<Substitution> matchedAtoms = store.match(matchingAtom);
        List<Substitution> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        Substitution firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, OBJECT_1);
        Substitution secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, OBJECT_3);

        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);

        // Case 2 - SOP
        matchingAtom = new RDFAtom(SUBJECT_1, OBJECT_1, VAR_X);
        matchedAtoms = store.match(matchingAtom);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, PREDICATE_1);

        assertEquals(1, matchedList.size(), "There should be one matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchAtomWithOOrders() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom1 = new RDFAtom(OBJECT_1, PREDICATE_1, SUBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(OBJECT_2, PREDICATE_1, SUBJECT_2);
        RDFAtom rdfAtom3 = new RDFAtom(OBJECT_1, PREDICATE_1, SUBJECT_2);
        RDFAtom rdfAtom4 = new RDFAtom(OBJECT_2, SUBJECT_2, PREDICATE_2);
        RDFAtom rdfAtom5 = new RDFAtom(OBJECT_1, SUBJECT_1, PREDICATE_1);

        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());
        store.add_to_dico(rdfAtom3.getTerms());
        store.add_to_dico(rdfAtom4.getTerms());
        store.add_to_dico(rdfAtom5.getTerms());

        store.dico_createCodex();

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        store.add(rdfAtom3);
        store.add(rdfAtom4);
        store.add(rdfAtom5);

        // Case 1 - OPS
        RDFAtom matchingAtom = new RDFAtom(OBJECT_1, PREDICATE_1, VAR_X); // RDFAtom(subject1, predicate1, X)
        Iterator<Substitution> matchedAtoms = store.match(matchingAtom);
        List<Substitution> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        Substitution firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_1);
        Substitution secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, SUBJECT_2);

        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);

        // Case 2 - OSP
        matchingAtom = new RDFAtom(OBJECT_1, SUBJECT_1, VAR_X);
        matchedAtoms = store.match(matchingAtom);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, PREDICATE_1);

        assertEquals(1, matchedList.size(), "There should be one matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchAtomWithPOrders() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom1 = new RDFAtom(PREDICATE_1, OBJECT_1, SUBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(PREDICATE_2, OBJECT_2, SUBJECT_2);
        RDFAtom rdfAtom3 = new RDFAtom(PREDICATE_1, OBJECT_1, SUBJECT_2);
        RDFAtom rdfAtom4 = new RDFAtom(PREDICATE_2, SUBJECT_2, OBJECT_2);
        RDFAtom rdfAtom5 = new RDFAtom(PREDICATE_1, SUBJECT_1, OBJECT_1);

        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());
        store.add_to_dico(rdfAtom3.getTerms());
        store.add_to_dico(rdfAtom4.getTerms());
        store.add_to_dico(rdfAtom5.getTerms());

        store.dico_createCodex();

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        store.add(rdfAtom3);
        store.add(rdfAtom4);
        store.add(rdfAtom5);

        // Case 1 - POS
        RDFAtom matchingAtom = new RDFAtom(PREDICATE_1, OBJECT_1, VAR_X); // RDFAtom(subject1, predicate1, X)
        Iterator<Substitution> matchedAtoms = store.match(matchingAtom);
        List<Substitution> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        Substitution firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_1);
        Substitution secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, SUBJECT_2);

        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);

        // Case 2 - PSO
        matchingAtom = new RDFAtom(PREDICATE_1, SUBJECT_1, VAR_X);
        matchedAtoms = store.match(matchingAtom);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, OBJECT_1);

        assertEquals(1, matchedList.size(), "There should be one matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchAtomWith2Vars() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(PREDICATE_1, SUBJECT_2, OBJECT_2);
        RDFAtom rdfAtom3 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_3);
        RDFAtom rdfAtom4 = new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_3);
        RDFAtom rdfAtom5 = new RDFAtom(OBJECT_3, PREDICATE_1, SUBJECT_1);


        store.add_to_dico(rdfAtom1.getTerms()); // RDFAtom(subject1, triple, object1)
        store.add_to_dico(rdfAtom2.getTerms()); // RDFAtom(subject2, triple, object2)
        store.add_to_dico(rdfAtom3.getTerms()); // RDFAtom(subject1, triple, object3)
        store.add_to_dico(rdfAtom4.getTerms()); // RDFAtom(subject1, triple, object3)
        store.add_to_dico(rdfAtom5.getTerms()); // RDFAtom(subject1, triple, object3)

        store.dico_createCodex();

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        store.add(rdfAtom3);
        store.add(rdfAtom4);
        store.add(rdfAtom5);

        // Case 1
        RDFAtom matchingAtom = new RDFAtom(SUBJECT_1, VAR_X, VAR_Y);
        Iterator<Substitution> matchedAtoms = store.match(matchingAtom);
        List<Substitution> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        Substitution firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, PREDICATE_1);
        firstResult.add(VAR_Y, OBJECT_1);

        Substitution secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, PREDICATE_2);
        secondResult.add(VAR_Y, OBJECT_3);

        Substitution thirdResult = new SubstitutionImpl();
        thirdResult.add(VAR_X, PREDICATE_1);
        thirdResult.add(VAR_Y, OBJECT_3);


        assertEquals(3, matchedList.size(), "There should be three matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
        assertTrue(matchedList.contains(thirdResult), "Missing substitution: " + thirdResult);

        // Case 2
        matchingAtom = new RDFAtom(PREDICATE_1, VAR_X, VAR_Y);
        matchedAtoms = store.match(matchingAtom);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_2);
        firstResult.add(VAR_Y, OBJECT_2);


        assertEquals(1, matchedList.size(), "There should be three matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);


        // Case 3
        matchingAtom = new RDFAtom(OBJECT_3, VAR_X, VAR_Y);
        matchedAtoms = store.match(matchingAtom);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, PREDICATE_1);
        firstResult.add(VAR_Y, SUBJECT_1);


        assertEquals(1, matchedList.size(), "There should be three matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);

    }

    @Test
    public void testMatchStarQuery() {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom1 = new RDFAtom(PREDICATE_1, OBJECT_1, SUBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(PREDICATE_2, OBJECT_2, SUBJECT_2);
        RDFAtom rdfAtom3 = new RDFAtom(PREDICATE_1, OBJECT_1, SUBJECT_2);
        RDFAtom rdfAtom4 = new RDFAtom(PREDICATE_2, SUBJECT_2, OBJECT_2);
        RDFAtom rdfAtom5 = new RDFAtom(PREDICATE_1, SUBJECT_1, OBJECT_1);

        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());
        store.add_to_dico(rdfAtom3.getTerms());
        store.add_to_dico(rdfAtom4.getTerms());
        store.add_to_dico(rdfAtom5.getTerms());

        store.dico_createCodex();

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        store.add(rdfAtom3);
        store.add(rdfAtom4);
        store.add(rdfAtom5);

        Variable centralVariable = (Variable) termFactory.createOrGetVariable("?x");
        Term predicate1 = termFactory.createOrGetLiteral("predicate1");
        Term object1 = termFactory.createOrGetLiteral("object1");

        Term predicate2 = termFactory.createOrGetLiteral("predicate2");
        Term object2 = termFactory.createOrGetLiteral("object2");

        RDFAtom atom1 = new RDFAtom(centralVariable, predicate1, object1);
        RDFAtom atom2 = new RDFAtom(centralVariable, predicate2, object2);

        List<RDFAtom> rdfAtoms = List.of(atom1, atom2);
        Collection<Variable> answerVariables = List.of(centralVariable);

        StarQuery query = new StarQuery("Requête étoile valide", rdfAtoms, answerVariables);


        store.match(query);


    }

    public static void main(String[] args) {
        RDFHexaStore store = new RDFHexaStore();
        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2);
        RDFAtom rdfAtom3 = new RDFAtom(SUBJECT_1, PREDICATE_3, OBJECT_3);

        store.add_to_dico(rdfAtom1.getTerms());
        store.add_to_dico(rdfAtom2.getTerms());
        store.add_to_dico(rdfAtom3.getTerms());

        store.dico_createCodex();

        store.add(rdfAtom1);
        store.add(rdfAtom2);
        store.add(rdfAtom3);

        RDFAtom atom1 = new RDFAtom(VAR_X, PREDICATE_1, OBJECT_1);
        RDFAtom atom2 = new RDFAtom(VAR_X, PREDICATE_2, OBJECT_2);
        RDFAtom atom3 = new RDFAtom(VAR_X, PREDICATE_3, OBJECT_3);

        System.out.println(rdfAtom1);
        System.out.println(atom1);

        List<RDFAtom> rdfAtoms = List.of(atom1,atom2,atom3);
        Collection<Variable> answerVariables = List.of(VAR_X);

        StarQuery query = new StarQuery("Requête étoile valide", rdfAtoms, answerVariables);


        Iterator<Substitution> match_result2 = store.match(atom1);
        while(match_result2.hasNext()){System.out.println(match_result2.next());}


        System.out.println(query.toString());

        System.out.println("test");

        Iterator<Substitution> match_result = store.match(query);

        while(match_result.hasNext()){System.out.println(match_result.next());}
    }

}
