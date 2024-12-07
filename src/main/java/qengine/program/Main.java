package qengine.program;

import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import main.java.qengine.exceptions.KeyNotFoundException;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.model.StarQuery;
import main.java.qengine.storage.RDFHexaStore;

import java.io.IOException;
import java.util.*;

import static main.java.qengine.program.Utils.*;

public final class Main {

	private static final String WORKING_DIR = "java/RDF/nosql-engine-skeleton/data/";
	private static final String SAMPLE_DATA_FILE = WORKING_DIR + "100K.nt";
	private static final String SAMPLE_QUERY_FILE = WORKING_DIR + "STAR_ALL_workload.queryset";
	private static final RDFHexaStore hexastore = new RDFHexaStore();


	public static void main(String[] args) throws IOException, KeyNotFoundException {
		System.out.println("\n=== Parsing RDF Data ===\n");
		List<RDFAtom> rdfAtoms = parseRDFData(SAMPLE_DATA_FILE);

		System.out.println("\n=== Building Dictionary ===");
		for (RDFAtom rdfAtom : rdfAtoms){
			hexastore.add_to_dico(rdfAtom.getTerms());
		}
		hexastore.dico_createCodex();
		System.out.println("Done.");

		System.out.println("\n=== Building Indexes ===");
		int rdfAtoms_size = rdfAtoms.size();
		for (int i=0; i < rdfAtoms_size; i++){
			hexastore.add(rdfAtoms.get(i));
			if(i%1000==0){
				System.out.print("█");
				//System.out.print(Integer.toString((int)((float)i/(float)rdfAtoms_size * 100.0)) + "% -> ");
			}

		}
		System.out.println("\nDone.");

		System.out.println("\n=== Match Atom ===");
		TermFactory termFactory = SameObjectTermFactory.instance();
		Variable var1 = termFactory.createOrGetVariable("?x0");
		//Variable var2 = termFactory.createOrGetVariable("?x1");
		Iterator<Substitution> match_result;

		RDFAtom first_atom = rdfAtoms.getFirst();

		RDFAtom match_object = new RDFAtom(first_atom.getTripleSubject(), first_atom.getTriplePredicate(), var1);
//		RDFAtom match_predicate = new RDFAtom(first_atom.getTripleSubject(), var1, first_atom.getTripleObject());
		RDFAtom match_subject = new RDFAtom(var1, first_atom.getTriplePredicate(), first_atom.getTripleObject());

//		RDFAtom match_predicate_object = new RDFAtom(first_atom.getTripleSubject(), var1, var2);
//		RDFAtom match_subject_predicate = new RDFAtom(var1, var2, first_atom.getTripleObject());
//		RDFAtom match_subject_object = new RDFAtom(var1, first_atom.getTriplePredicate(), var2);

		System.out.println("Atome de base : " + first_atom);
		System.out.println("Atome encodé : " + Arrays.toString(hexastore.dico_encodeTriplet(first_atom)) + "\n");

		System.out.println("Match object : " + first_atom);
		match_result = hexastore.match(first_atom);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------\n");

		System.out.println("Match object : " + match_object);
		match_result = hexastore.match(match_object);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------\n");

		System.out.println("Match object : " + match_subject);
		match_result = hexastore.match(match_subject);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------\n");

//		System.out.println("Match object : " + match_predicate_object.toString());
//		match_result = hexastore.match(match_predicate_object);
//		while(match_result.hasNext()){System.out.println(match_result.next());}
//		System.out.println("------------");


		System.out.println("\n=== Parsing Sample Queries ===\n");
		List<StarQuery> starQueries = parseStarQueries(SAMPLE_QUERY_FILE);

		System.out.println("\n=== Queries with Integraal ===\n");
		List<Set<Substitution>> integraalResults = executeWithIntegraal(rdfAtoms, starQueries, false);
		System.out.println("Done.");

		System.out.println("\n=== Queries with Hexastore ===\n");
		List<Set<Substitution>> hexastoreResults = executeWithHexastore(starQueries, hexastore, false);
		System.out.println("Done.");

		System.out.println("\n=== Correction et complétude ===\n");
		//Test de correction
		//Tous les éléments de la starQuery sont dans integraal
		Set<Set<Substitution>> integraalSet = new HashSet<>(integraalResults);
		Set<Set<Substitution>> hexastoreSet = new HashSet<>(hexastoreResults);
		if (integraalSet.containsAll(hexastoreSet)) {
			System.out.println("Matching Correct ✔");
		} else {
			System.out.println("Matching Incorrect");
			System.out.println(hexastoreResults);
		}

		//Test de complétude
		//Tous les éléments integral sont dans la starquerry
		if (hexastoreSet.containsAll(integraalSet)) {
			System.out.println("Matching Complet ✔");
		} else {
			System.out.println("Matching Incomplet");
			System.out.println(hexastoreResults);
		}
	}
}
