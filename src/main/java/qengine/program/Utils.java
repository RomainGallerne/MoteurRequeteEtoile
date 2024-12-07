package qengine.program;

import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.api.Query;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.model.StarQuery;
import main.java.qengine.parser.RDFAtomParser;
import main.java.qengine.parser.StarQuerySparQLParser;
import main.java.qengine.storage.RDFHexaStore;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Utils {

    /**
     * Parse et affiche le contenu d'un fichier RDF.
     *
     * @param rdfFilePath Chemin vers le fichier RDF à parser
     * @return Liste des RDFAtoms parsés
     */
    public static List<RDFAtom> parseRDFData(String rdfFilePath) throws IOException {
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
    public static List<StarQuery> parseStarQueries(String queryFilePath) throws IOException {
        List<StarQuery> starQueries = new ArrayList<>();

        Query query = null;
        try (StarQuerySparQLParser queryParser = new StarQuerySparQLParser(queryFilePath)) {

            while (queryParser.hasNext()) {
                query = queryParser.next();
                if (query instanceof StarQuery starQuery) {
                    starQueries.add(starQuery);
                } else {
                    System.err.println("Requête inconnue ignorée.");
                }
            }
            System.out.println("[INFO] Total StarQueries parsed: " + starQueries.size());
        } catch (Exception e) {
            System.out.println("[ERREUR] Requête mal formée");
            System.out.println(query);
        }
        return starQueries;
    }

    /**
     * Parse et affiche le contenu d'un fichier de requêtes SparQL.
     *
     * @param queryFilePath Chemin vers le fichier de requêtes SparQL
     * @return Liste des Queries parsées
     */
    public static List<Query> parseQueries(String queryFilePath) throws IOException {
        List<Query> queries = new ArrayList<>();

        Query query = null;
        try (StarQuerySparQLParser queryParser = new StarQuerySparQLParser(queryFilePath)) {

            while (queryParser.hasNext()) {
                query = queryParser.next();
                queries.add(query);  // Stocker la requête dans la collection
            }
            System.out.println("[INFO] Total Queries parsed: " + queries.size());
        } catch (Exception e) {
            System.out.println("[ERREUR] Requête invalide");
            System.out.println(query);
        }
        return queries;
    }

    /**
     * Exécute une requête en étoile sur le store et affiche les résultats.
     *
     * @param starQuery La requête à exécuter
     * @param factBase  Le store contenant les atomes
     * @return Itérateur de substituions correspondant à la variable de la requête.
     */
    public static Iterator<Substitution> executeStarQuery(StarQuery starQuery, FactBase factBase) {
        FOQuery<FOFormulaConjunction> foQuery = starQuery.asFOQuery(); // Conversion en FOQuery
        FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance(); // Créer un évaluateur

        return evaluator.evaluate(foQuery, factBase);
    }

    /**
     * Exécute une série de requêtes (StarQuery) sur une base de faits RDF à l'aide du moteur Integraal,
     * et retourne les résultats sous forme de substitutions.
     *
     * @param rdfAtoms une liste d'objets RDFAtom représentant les faits RDF à ajouter à la base de faits.
     * @param starQueries une liste d'objets StarQuery représentant les requêtes à exécuter.
     * @param verbose un booléen indiquant si les résultats doivent être affichés dans la console.
     *                Si true, chaque correspondance trouvée et une séparation visuelle entre les requêtes sont affichées.
     * @return un ensemble d'ensembles de substitutions, où chaque sous-ensemble correspond aux résultats
     *         d'une requête dans 'starQueries'.
     */
    public static List<Set<Substitution>> executeWithIntegraal(List<RDFAtom> rdfAtoms, List<StarQuery> starQueries, boolean verbose) {
        // Préparer la fact base
        FactBase factBase = new SimpleInMemoryGraphStore();
        rdfAtoms.forEach(factBase::add);

        // Exécuter les requêtes
        List<Set<Substitution>> results = new ArrayList<>();

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

    /**
     * Exécute une série de requêtes (StarQuery) sur une base RDF (Hexastore),
     * et retourne les résultats sous forme de substitutions.
     *
     * Cette méthode interroge un 'RDFHexaStore'
     * pour trouver les correspondances pour chaque requête spécifiée.
     *
     * @param starQueries une liste d'objets StarQuery représentant les requêtes à exécuter.
     * @param hexastore une instance de RDFHexaStore contenant les faits RDF sur lesquels les requêtes
     *                  doivent être exécutées.
     * @param verbose un booléen indiquant si les résultats doivent être affichés dans la console.
     *                Si true, chaque correspondance trouvée et une séparation visuelle entre les requêtes sont affichées.
     * @return un ensemble d'ensembles de substitutions, où chaque sous-ensemble correspond aux résultats
     *         d'une requête dans `starQueries`.
     */
    public static List<Set<Substitution>> executeWithHexastore(List<StarQuery> starQueries, RDFHexaStore hexastore, boolean verbose) {
        List<Set<Substitution>> results = new ArrayList<>();

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
}
