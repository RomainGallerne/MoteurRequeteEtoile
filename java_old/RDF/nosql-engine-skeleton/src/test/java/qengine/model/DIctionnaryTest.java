package test.java.qengine.model;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import main.java.qengine.exceptions.KeyNotFoundException;
import main.java.qengine.exceptions.ValueNotFoundException;
import main.java.qengine.model.Dictionnary;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.storage.RDFHexaStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link RDFHexaStore}.
 */
public class DIctionnaryTest {
    private static final Literal<String> SUBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("subject1");
    private static final Literal<String> PREDICATE_1 = SameObjectTermFactory.instance().createOrGetLiteral("predicate1");
    private static final Literal<String> OBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("object1");
    private static final Literal<String> SUBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("subject2");
    private static final Literal<String> OBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("object2");
    private static final Variable VAR_X = SameObjectTermFactory.instance().createOrGetVariable("varX");

    @Test
    public void testGetKey() {
        Dictionnary dictionnary = new Dictionnary();

        dictionnary.addTerm(SUBJECT_1);
        dictionnary.addTerm(SUBJECT_1);
        dictionnary.addTerm(PREDICATE_1);

        dictionnary.createCodex();

        assertEquals(0, dictionnary.getKey(SUBJECT_1), "SUBJECT_1 is supposed to be on first index");
    }

    @Test
    public void testGetVariableKey() {
        Dictionnary dictionnary = new Dictionnary();
        dictionnary.createCodex();

        assertEquals(-1, dictionnary.getKey(VAR_X));
    }

    @Test
    public void testGetInvalidKey() {
        Dictionnary dictionnary = new Dictionnary();

        dictionnary.addTerm(PREDICATE_1);

        dictionnary.createCodex();

        assertEquals(-1, dictionnary.getKey(SUBJECT_1));
    }

    @Test
    public void testGetValue() {
        Dictionnary dictionnary = new Dictionnary();

        dictionnary.addTerm(SUBJECT_1);
        dictionnary.addTerm(SUBJECT_1);
        dictionnary.addTerm(PREDICATE_1);

        dictionnary.createCodex();

        try {
            assertEquals(SUBJECT_1, dictionnary.getValue(0), "first value is supposed to be on SUBJECT_1");
        } catch (ValueNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetInvalidValue() {
        Dictionnary dictionnary = new Dictionnary();

        dictionnary.addTerm(PREDICATE_1);

        dictionnary.createCodex();

        assertThrows(ValueNotFoundException.class, () -> dictionnary.getValue(3));
    }

    @Test
    public void testEncodeTriplet() {
        Dictionnary dictionnary = new Dictionnary();

        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2); // RDFAtom(subject2, triple, object2)


        for(Term term: rdfAtom1.getTerms()) {
            dictionnary.addTerm(term);
        }

        for(Term term: rdfAtom2.getTerms()) {
            dictionnary.addTerm(term);
        }

        dictionnary.createCodex();

        int[] encodedTriplet1 = dictionnary.encodeTriplet(rdfAtom1);

        assertEquals(1, encodedTriplet1[0], "The first value should be 1");
        assertEquals(0, encodedTriplet1[1], "The second value should be 0");
        assertEquals(2, encodedTriplet1[2], "The third value should be 2");
    }

    @Test
    public void testDecodeTriplet() {
        Dictionnary dictionnary = new Dictionnary();

        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2); // RDFAtom(subject2, triple, object2)


        for(Term term: rdfAtom1.getTerms()) {
            dictionnary.addTerm(term);
        }

        for(Term term: rdfAtom2.getTerms()) {
            dictionnary.addTerm(term);
        }

        dictionnary.createCodex();

        RDFAtom decodedRDFAtom = null;
        try {
            decodedRDFAtom = dictionnary.decodeTriplet(new int[]{1,0,2});
        } catch (ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        assertEquals(decodedRDFAtom, rdfAtom1, "The RDFAtom is supposed to be rdfAtom1");
    }

    @Test
    public void toStringTest() {
        Dictionnary dictionnary = new Dictionnary();
        dictionnary.addTerm(SUBJECT_1);
        dictionnary.createCodex();

        assertEquals("subject1 : 1\n", dictionnary.toString(), "ToString is supposed to return 'term : key'");
    }
}
