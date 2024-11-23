package main.java.qengine.program;

import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
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
import main.java.qengine.model.*;
import main.java.qengine.storage.RDFHexaStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import main.java.qengine.parser.RDFAtomParser;
import main.java.qengine.parser.StarQuerySparQLParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Main {

	private static final String WORKING_DIR = "java/RDF/nosql-engine-skeleton/data/";
	private static final String SAMPLE_DATA_FILE = WORKING_DIR + "100K.nt";
	private static final String SAMPLE_QUERY_FILE = WORKING_DIR + "STAR_ALL_workload.queryset";
	private static final RDFHexaStore hexastore = new RDFHexaStore();


	public static void main(String[] args) throws IOException {
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
		Variable var2 = termFactory.createOrGetVariable("?x1");
		Iterator<Substitution> match_result;

		RDFAtom first_atom = rdfAtoms.getFirst();

		RDFAtom match_object = new RDFAtom(first_atom.getTripleSubject(), first_atom.getTriplePredicate(), var1);
		//RDFAtom match_predicate = new RDFAtom(first_atom.getTripleSubject(), var1, first_atom.getTripleObject());
		RDFAtom match_subject = new RDFAtom(var1, first_atom.getTriplePredicate(), first_atom.getTripleObject());

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

		System.out.println("Match object : " + match_subject.toString());
		match_result = hexastore.match(match_subject);
		while(match_result.hasNext()){System.out.println(match_result.next());}
		System.out.print("------------\n");

//		System.out.println("Match object : " + match_predicate_object.toString());
//		match_result = hexastore.match(match_predicate_object);
//		while(match_result.hasNext()){System.out.println(match_result.next());}
//		System.out.println("------------");


		System.out.println("\n=== Parsing Sample Queries ===\n");
		List<StarQuery> starQueries = parseSparQLQueries(SAMPLE_QUERY_FILE);

		System.out.println("\n=== Queries with Integraal ===\n");
		Set<Set<Substitution>> integraalResults = executeWithIntegraal(rdfAtoms, starQueries, false);
		System.out.println("Done.");

		System.out.println("\n=== Queries with Hexastore ===\n");
		Set<Set<Substitution>> hexastoreResults = executeWithHexastore(starQueries, false);
		System.out.println("Done.");

		System.out.println("\n=== Correction et complétude ===\n");
		//Test de correction
		//Tous les éléments de la starQuery sont dans integraal
		if (hexastoreResults.containsAll(integraalResults)) {
			System.out.println("Matching Correct ✔");
		} else {
			System.out.println("Matching Incorrect");
		}

		//Test de complétude
		//Tous les éléments de integraal sont dans la starquerry
		if (integraalResults.containsAll(hexastoreResults)) {
			System.out.println("Matching Complet ✔");
		} else {
			System.out.println("Matching Incomplet");
		}
	}

	private static Set<Set<Substitution>> executeWithIntegraal(List<RDFAtom> rdfAtoms, List<StarQuery> starQueries, boolean verbose) {
		// Préparer la fact base
		FactBase factBase = new SimpleInMemoryGraphStore();
		rdfAtoms.forEach(factBase::add);

		// Exécuter les requêtes
		Set<Set<Substitution>> results = new HashSet<>();

		for (StarQuery starQuery : starQueries) {
			//System.out.println("Star Query: " + starQuery + "\n");

			// Trouver les correspondances
			Set<Substitution> matches = new HashSet<>();
			executeStarQuery(starQuery, factBase).forEachRemaining(matches::add);
			results.add(matches);

			// Afficher les résultats
			if(verbose){matches.forEach(System.out::println);}
			if(verbose){System.out.println("------------\n");}
		}

		return results;
	}

	private static Set<Set<Substitution>> executeWithHexastore(List<StarQuery> starQueries, boolean verbose) {
		Set<Set<Substitution>> results = new HashSet<>();

		for (StarQuery starQuery : starQueries) {
			if(verbose){System.out.println("Star Query: " + starQuery + "\n");}

			// Trouver les correspondances
			Set<Substitution> matches = new HashSet<>();
			hexastore.match(starQuery).forEachRemaining(matches::add);
			results.add(matches);

			// Afficher les résultats
			if(verbose){matches.forEach(System.out::println);}
			if(verbose){System.out.println("------------\n");}
		}

		return results;
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
//				System.out.println("RDF Atom #" + (count) + ": " + atom);
				count ++;
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
//					System.out.println("Star Query #" + (++queryCount) + ":");
//					System.out.println("  Central Variable: " + starQuery.getCentralVariable().label());
//					System.out.println("  RDF Atoms:");
//					starQuery.getRdfAtoms().forEach(atom -> System.out.println("    " + atom));
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
	 * @return
	 */
	private static Iterator<Substitution> executeStarQuery(StarQuery starQuery, FactBase factBase) {
		FOQuery<FOFormulaConjunction> foQuery = starQuery.asFOQuery(); // Conversion en FOQuery
		FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance(); // Créer un évaluateur

		return evaluator.evaluate(foQuery, factBase);
	}
}
