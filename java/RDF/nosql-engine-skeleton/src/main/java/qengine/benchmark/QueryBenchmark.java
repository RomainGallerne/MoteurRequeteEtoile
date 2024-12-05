package main.java.qengine.benchmark;

import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.query.api.Query;
import main.java.qengine.model.RDFAtom;
import main.java.qengine.model.StarQuery;
import main.java.qengine.storage.RDFHexaStore;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

import static main.java.qengine.program.Utils.*;

public class QueryBenchmark {
    private static final String WORKING_DIR = "java/RDF/nosql-engine-skeleton/data/";
    private static final RDFHexaStore hexastore = new RDFHexaStore();

    public QueryBenchmark(String FILE_PATH, String DATA_PATH) throws IOException {
        String QUERY_FILE = WORKING_DIR + FILE_PATH;
        String DATA_FILE = WORKING_DIR + DATA_PATH;

        // Parsing Data
        List<RDFAtom> rdf_data = parseRDFData(DATA_FILE);
        buildIndexesAndDictionary(rdf_data);

        // Parsing Queries
        List<Query> queries = filterDuplicates(parseQueries(QUERY_FILE));
        List<StarQuery> starQueries = StarQueryExtractor(queries);

        // Execute queries
        List<Set<Substitution>> hexastoreResults = executeWithHexastore(starQueries, hexastore, false);

        hexastoreResults = uniformizeList(hexastoreResults);

        Map<Integer, Integer> subSetSizes = countSubsetSizes(hexastoreResults);

        SwingUtilities.invokeLater(() -> {
            HistogramFrame frame = new HistogramFrame(subSetSizes);
            frame.setVisible(true);
        });

        // Uniformiser les requêtes par nombre de réponses


        // Benchmarks
    }

    private void buildIndexesAndDictionary(List<RDFAtom> rdf_data){
        for (RDFAtom rdfAtom : rdf_data){hexastore.add_to_dico(rdfAtom.getTerms());}
        hexastore.dico_createCodex();
        for (RDFAtom rdfData : rdf_data) {hexastore.add(rdfData);}
    }

    public static List<Set<Substitution>> uniformizeList(List<Set<Substitution>> hexastoreResults) {
        // Étape 1 : Grouper les sets par leur taille
        Map<Integer, List<Set<Substitution>>> sizeGroups = new HashMap<>();
        for (Set<Substitution> set : hexastoreResults) {
            int size = set.size();
            sizeGroups.computeIfAbsent(size, k -> new ArrayList<>()).add(set);
        }

        // Étape 2 : Calculer la taille cible pour chaque groupe
        int totalSets = hexastoreResults.size();
        int targetSetsPerGroup = totalSets / sizeGroups.size();
        List<Map.Entry<Integer, List<Set<Substitution>>>> groupList = new ArrayList<>(sizeGroups.entrySet());

        // Étape 3 : Ajuster la liste pour avoir des groupes avec environ la même taille
        List<Set<Substitution>> result = new ArrayList<>();

        for (Map.Entry<Integer, List<Set<Substitution>>> entry : groupList) {
            List<Set<Substitution>> setsOfThisSize = entry.getValue();
            int currentSize = setsOfThisSize.size();

            // Calculer le nombre de sets à garder pour ce groupe
            int setsToKeep = Math.min(currentSize, (targetSetsPerGroup / 10) * 10);

            // Ajouter les sets sélectionnés à la liste résultat
            result.addAll(setsOfThisSize.subList(0, setsToKeep));
        }

        // Retourner la liste uniformisée
        return result;
    }

    /**
     * Compte le nombre de sous-ensembles dans un ensemble de résultats selon leur taille.
     *
     * @param hexastoreResults l'ensemble des résultats contenant des sous-ensembles de substitutions.
     *                         Si hexastoreResults est null, une IllegalArgumentException est levée.
     * @return une map où chaque clé est une taille de sous-ensemble, et la valeur correspond au
     *         nombre de sous-ensembles de cette taille.
     * @throws IllegalArgumentException si hexastoreResults est null.
     */
    public static Map<Integer, Integer> countSubsetSizes(List<Set<Substitution>> hexastoreResults) {
        if (hexastoreResults == null) {
            throw new IllegalArgumentException("hexastoreResults ne peut pas être null.");
        }

        Map<Integer, Integer> sizeDistribution = new HashMap<>();

        for (Set<Substitution> subset : hexastoreResults) {
            int size = subset.size(); // Taille du sous-ensemble
            sizeDistribution.put(size, sizeDistribution.getOrDefault(size, 0) + 1);
        }

        return sizeDistribution;
    }

    /**
     * Supprime les doublons d'une liste de requêtes (Query)
     *
     * @param queries la liste de Query contenant potentiellement des doublons.
     *         Si la liste est null, une exception IllegalArgumentException est levée.
     * @return une nouvelle liste contenant uniquement des objets Query uniques.
     */
    private static List<Query> filterDuplicates(List<Query> queries) {
        Set<Query> uniqueQueries = new HashSet<>(queries);
        return new ArrayList<>(uniqueQueries);
    }

    /**
     * Extraits les requêtes en étoile (StarQuery) d'une liste de requête (Query)
     *
     * @param queries la liste de Query contenant potentiellement des requêtes en étoile.
     *
     * @return une nouvelle liste contenant uniquement des objets StarQuery.
     */
    private List<StarQuery> StarQueryExtractor(List<Query> queries) {
        List<StarQuery> starQueries = new ArrayList<>();

        for (Query query : queries) {
            if (query instanceof StarQuery starQuery) {
                starQueries.add(starQuery);
            }
        }
        return starQueries;
    }

    public static void main(String[] args) throws IOException {
        new QueryBenchmark("STAR_ALL_workload.queryset","100K.nt");
    }

}
