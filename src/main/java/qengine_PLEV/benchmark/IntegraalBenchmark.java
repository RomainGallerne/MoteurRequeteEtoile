package qengine_PLEV.benchmark;

import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine_PLEV.parser.RDFAtomParser;
import qengine_PLEV.parser.StarQuerySparQLParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IntegraalBenchmark {

    private static final String DATA_DIR = "data/";
    private static final String DATA_FILE = DATA_DIR + "500K.nt";
    private static final String QUERYSET_DIR_100 = "watdiv-mini-projet-partie-2/testsuite/queries/100";
    private static final String QUERYSET_DIR_1000 = "watdiv-mini-projet-partie-2/testsuite/queries/1000";
    private static final String QUERYSET_DIR_10000 = "watdiv-mini-projet-partie-2/testsuite/queries/10000";

    public static void main(String[] args) throws IOException {
        List<RDFAtom> rdfAtoms = parseRDFData(DATA_FILE);

        FactBase factBase = new SimpleInMemoryGraphStore();
        for (RDFAtom atom : rdfAtoms) {
            factBase.add(atom);
        }

        System.out.println("Données RDF chargées dans Integraal. Début du benchmark...");

        Map<String, Map<String, Long>> results = executeGroupedQueries(QUERYSET_DIR_1000, factBase);
        saveResultsToFile(results);

        System.out.println("Benchmarks terminés. Résultats enregistrés dans le répertoire 'benchmark'.");
    }

    /**
     * Parse le contenu d'un fichier RDF.
     *
     * @param rdfFilePath Chemin vers le fichier RDF à parser
     * @return Liste des RDFAtoms parsés
     */
    private static List<RDFAtom> parseRDFData(String rdfFilePath) throws IOException {
        FileReader rdfFile = new FileReader(rdfFilePath);
        List<RDFAtom> rdfAtoms = new ArrayList<>();

        try (RDFAtomParser rdfAtomParser = new RDFAtomParser(rdfFile, RDFFormat.NTRIPLES)) {
            while (rdfAtomParser.hasNext()) {
                rdfAtoms.add(rdfAtomParser.next());
            }
        }
        return rdfAtoms;
    }

    /**
     * Exécute les requêtes par groupe (Q1, Q2, etc.) et mesure les temps d'exécution.
     *
     * @param querySetDir Chemin vers le répertoire contenant les fichiers queryset
     * @param factBase    Instance du FactBase Integraal
     * @return Une map contenant les résultats regroupés par catégorie
     */
    private static Map<String, Map<String, Long>> executeGroupedQueries(String querySetDir, FactBase factBase) {
        // Utilisation d'un TreeMap pour trier les catégories par ordre naturel (numérique)
        Map<String, Map<String, Long>> groupedResults = new TreeMap<>();
        File dir = new File(querySetDir);

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Le répertoire spécifié n'existe pas : " + querySetDir);
            return groupedResults;
        }

        File[] queryFiles = dir.listFiles((d, name) -> name.endsWith(".queryset"));
        if (queryFiles == null) {
            System.err.println("Aucun fichier queryset trouvé dans : " + querySetDir);
            return groupedResults;
        }

        for (File queryFile : queryFiles) {
            String fileName = queryFile.getName();
            // Extrait correctement la catégorie (ex. "Q1", "Q2", ...)
            String[] parts = fileName.split("_");
            String category = parts[0] + parts[1];

            groupedResults.putIfAbsent(category, new LinkedHashMap<>());
            long fileExecutionTime = executeQueryFile(queryFile, factBase);
            groupedResults.get(category).put(fileName, fileExecutionTime);
        }

        return groupedResults;
    }

    /**
     * Exécute toutes les requêtes d'un fichier et mesure le temps total avec Integraal.
     *
     * @param queryFile Fichier queryset à exécuter
     * @param factBase  Instance du FactBase Integraal
     * @return Temps total d'exécution pour ce fichier
     */
    private static long executeQueryFile(File queryFile, FactBase factBase) {
        long totalTime = 0;

        try (StarQuerySparQLParser parser = new StarQuerySparQLParser(queryFile.getAbsolutePath())) {
            while (parser.hasNext()) {
                StarQuery query = (StarQuery) parser.next();
                FOQuery<FOFormulaConjunction> foQuery = query.asFOQuery();

                FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance();
                long startTime = System.currentTimeMillis();
                evaluator.evaluate(foQuery, factBase); // Évaluation avec Integraal
                totalTime += (System.currentTimeMillis() - startTime);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du traitement du fichier : " + queryFile.getName());
        }

        return totalTime;
    }

    /**
     * Sauvegarde les résultats des benchmarks dans un fichier texte.
     *
     * @param results Map des résultats groupés par catégorie
     */
    private static void saveResultsToFile(Map<String, Map<String, Long>> results) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String outputPath = "benchmark/benchmark_integraal_" + timestamp + ".txt";
        File benchmarkDir = new File("benchmark");

        if (!benchmarkDir.exists()) {
            benchmarkDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(outputPath)) {
            // Écrire les informations de la machine
            writer.write("=== MACHINE ===\n");
            writer.write("\n");

            // Écrire les résultats des benchmarks
            for (Map.Entry<String, Map<String, Long>> entry : results.entrySet()) {
                String category = entry.getKey();
                Map<String, Long> fileResults = entry.getValue();

                long categoryTotalTime = fileResults.values().stream().mapToLong(Long::longValue).sum();

                writer.write("=== " + category + " ===\n");
                writer.write("TOTAL : " + categoryTotalTime + "ms\n");
                for (Map.Entry<String, Long> fileResult : fileResults.entrySet()) {
                    writer.write(fileResult.getKey() + " : " + fileResult.getValue() + "ms\n");
                }
                writer.write("\n");
            }
            System.out.println("Résultats sauvegardés dans : " + outputPath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier de benchmark : " + outputPath);
        }
    }
}
