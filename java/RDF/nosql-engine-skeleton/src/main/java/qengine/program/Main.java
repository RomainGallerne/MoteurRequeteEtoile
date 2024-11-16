package main.java.qengine.program;

import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.query.api.Query;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import fr.lirmm.graphik.graal.api.core.VariableGenerator;
import main.java.qengine.model.*;
import main.java.qengine.storage.RDFHexaStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import main.java.qengine.parser.RDFAtomParser;
import main.java.qengine.parser.StarQuerySparQLParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public final class Main {

	private static final String WORKING_DIR = "java/RDF/nosql-engine-skeleton/data/";
	private static final String SAMPLE_DATA_FILE = WORKING_DIR + "sample_data.nt";
	private static final String SAMPLE_QUERY_FILE = WORKING_DIR + "sample_query.queryset";
	private static final RDFHexaStore hexastore = new RDFHexaStore();


	public static void main(String[] args) throws IOException {
		System.out.println("\n=== Parsing RDF Data ===\n");
		List<RDFAtom> rdfAtoms = parseRDFData(SAMPLE_DATA_FILE);

		System.out.println("\n=== Building Dictionary ===\n");
		for (RDFAtom rdfAtom : rdfAtoms){
			hexastore.add_to_dico(rdfAtom.getTerms());
		}
		hexastore.dico_createCodex();

		System.out.println("\n=== Building Indexes ===\n");
		for (RDFAtom rdfAtom : rdfAtoms){
			hexastore.add(rdfAtom);
		}

		System.out.println("\n=== Match Atom ===\n");
		TermFactory termFactory = SameObjectTermFactory.instance();
		Variable var1 = termFactory.createOrGetVariable("?x0");
		Variable var2 = termFactory.createOrGetVariable("?x1");
		Iterator<Substitution> match_result;

		RDFAtom first_atom = rdfAtoms.getFirst();

		RDFAtom match_object = new RDFAtom(first_atom.getTripleSubject(), first_atom.getTriplePredicate(), var1);
		//RDFAtom match_predicate = new RDFAtom(first_atom.getTripleSubject(), var1, first_atom.getTripleObject());
		//RDFAtom match_subject = new RDFAtom(var1, first_atom.getTriplePredicate(), first_atom.getTripleObject());

		RDFAtom match_predicate_object = new RDFAtom(first_atom.getTripleSubject(), var1, var2);
		//RDFAtom match_subject_predicate = new RDFAtom(var1, var2, first_atom.getTripleObject());
		//RDFAtom match_subject_object = new RDFAtom(var1, first_atom.getTriplePredicate(), var2);

		System.out.println("Atome de base : " + first_atom.toString());
		System.out.println("Atome encodé : " + Arrays.toString(hexastore.dico_encodeTriplet(first_atom)) + "\n");

		System.out.println("Match object : " + first_atom.toString());
		match_result = hexastore.match(first_atom);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------\n");

		System.out.println("Match object : " + match_object.toString());
		match_result = hexastore.match(match_object);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------\n");

		System.out.println("Match object : " + match_predicate_object.toString());
		match_result = hexastore.match(match_predicate_object);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------");







//		System.out.println("\n=== Parsing Sample Queries ===");
//		List<StarQuery> starQueries = parseSparQLQueries(SAMPLE_QUERY_FILE);

//		System.out.println("\n=== Executing the queries with Integraal ===");
//		FactBase factBase = new SimpleInMemoryGraphStore();
//		for (RDFAtom atom : rdfAtoms) {
//			factBase.add(atom);  // Stocker chaque RDFAtom dans le store
//		}

//		// Exécuter les requêtes sur le store
//		for (StarQuery starQuery : starQueries) {
//			executeStarQuery(starQuery, factBase);
//		}
	}

	/**
	* Parse et affiche le contenu d'un fichier RDF.
	*
	* @param rdfFilePath Chemin vers le fichier RDF à parser
	* @return Liste des RDFAtoms parsés
	*/
	private static List<RDFAtom> parseRDFData(String rdfFilePath) throws IOException {
		FileReader rdfFile = new FileReader(rdfFilePath);
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
		return rdfAtoms;
	}

	/**
	 * Parse et affiche le contenu d'un fichier de requêtes SparQL.
	 *
	 * @param queryFilePath Chemin vers le fichier de requêtes SparQL
	 * @return Liste des StarQueries parsées
	 */
	private static List<StarQuery> parseSparQLQueries(String queryFilePath) throws IOException {
		List<StarQuery> starQueries = new ArrayList<>();

		try (StarQuerySparQLParser queryParser = new StarQuerySparQLParser(queryFilePath)) {
			int queryCount = 0;

			while (queryParser.hasNext()) {
				Query query = queryParser.next();
				if (query instanceof StarQuery starQuery) {
					starQueries.add(starQuery);  // Stocker la requête dans la collection
					System.out.println("Star Query #" + (++queryCount) + ":");
					System.out.println("  Central Variable: " + starQuery.getCentralVariable().label());
					System.out.println("  RDF Atoms:");
					starQuery.getRdfAtoms().forEach(atom -> System.out.println("    " + atom));
				} else {
					System.err.println("Requête inconnue ignorée.");
				}
			}
			System.out.println("Total Queries parsed: " + starQueries.size());
		}
		return starQueries;
	}

	/**
	 * Exécute une requête en étoile sur le store et affiche les résultats.
	 *
	 * @param starQuery La requête à exécuter
	 * @param factBase  Le store contenant les atomes
	 */
	private static void executeStarQuery(StarQuery starQuery, FactBase factBase) {
		FOQuery<FOFormulaConjunction> foQuery = starQuery.asFOQuery(); // Conversion en FOQuery
		FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance(); // Créer un évaluateur
		Iterator<Substitution> queryResults = evaluator.evaluate(foQuery, factBase); // Évaluer la requête

		System.out.printf("Execution of  %s:%n", starQuery);
		System.out.println("Answers:");
		if (!queryResults.hasNext()) {
			System.out.println("No answer.");
		}
		while (queryResults.hasNext()) {
			Substitution result = queryResults.next();
			System.out.println(result); // Afficher chaque réponse
		}
		System.out.println();
	}
}
