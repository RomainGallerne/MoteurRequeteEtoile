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
    private static final RDFHexaStore hexastore = new RDFHexaStore();

    public QueryBenchmark(String FILE_PATH, String DATA_PATH, boolean uniformisation) throws IOException {
        String QUERY_FILE = FILE_PATH;
        String DATA_FILE = DATA_PATH;

        // Parsing Data
        List<RDFAtom> rdf_data = parseRDFData(DATA_FILE);
        buildIndexesAndDictionary(rdf_data);

        // Parsing Queries
        List<Query> queries = filterDuplicates(parseQueries(QUERY_FILE));
        System.out.println("[INFO] Taille après doublons supprimés : " + queries.size());

        List<StarQuery> starQueries = StarQueryExtractor(queries);
        System.out.println("[INFO] Taille après extraction des starQueries : " + starQueries.size());

        // Exécuter les requêtes
        List<Set<Substitution>> hexastoreResults = executeWithIntegraal(rdf_data, starQueries, false);

        if(uniformisation) {
            // Uniformisation des résultats
            hexastoreResults = uniformizeList(starQueries, hexastoreResults);
            System.out.println("[INFO] Taille après uniformisation du nombre de résultats : " + starQueries.size());
        }

        Map<Integer, Integer> subSetSizes = countSubsetSizes(hexastoreResults);

        SwingUtilities.invokeLater(() -> {
            HistogramFrame frame = new HistogramFrame(subSetSizes);
            frame.setVisible(true);
        });

        runBenchmark_hexastore(starQueries);
        runBenchmark_integraal(starQueries, rdf_data);
    }

    private void runBenchmark_hexastore(List<StarQuery> starQueries){
        // Execution des 20% de requête pour chauffer la JVM
        int twentyPercentCount = (int) Math.ceil(starQueries.size() * 0.2);
        List<StarQuery> initialQueries = starQueries.subList(0, twentyPercentCount);
        List<StarQuery> remainingQueries = starQueries.subList(twentyPercentCount, starQueries.size());

        List<Set<Substitution>> initialResults = executeWithHexastore(initialQueries, hexastore, false);

        // Instancier le timer
        ExecutionTimer timer = new ExecutionTimer();
        timer.start();

        // Execution des 80% de requêtes restantes
        List<Set<Substitution>> remainingResults = executeWithHexastore(remainingQueries, hexastore, false);

        ExecutionTimer.TimerReport report = timer.stop();
        System.out.println("[BENCHMARK HEXASTORE] : ");
        System.out.println(report);
    }

    private void runBenchmark_integraal(List<StarQuery> starQueries, List<RDFAtom> rdf_data){
        // Execution des 20% de requête pour chauffer la JVM
        int twentyPercentCount = (int) Math.ceil(starQueries.size() * 0.2);
        List<StarQuery> initialQueries = starQueries.subList(0, twentyPercentCount);
        List<StarQuery> remainingQueries = starQueries.subList(twentyPercentCount, starQueries.size());

        List<Set<Substitution>> initialResults = executeWithIntegraal(rdf_data, initialQueries, false);

        // Instancier le timer
        ExecutionTimer timer = new ExecutionTimer();
        timer.start();

        // Execution des 80% de requêtes restantes
        List<Set<Substitution>> remainingResults = executeWithIntegraal(rdf_data, remainingQueries, false);

        ExecutionTimer.TimerReport report = timer.stop();
        System.out.println("[BENCHMARK INTEGRAAL] : ");
        System.out.println(report);
    }


    private void buildIndexesAndDictionary(List<RDFAtom> rdf_data){
        for (RDFAtom rdfAtom : rdf_data){hexastore.add_to_dico(rdfAtom.getTerms());}
        hexastore.dico_createCodex();
        for (RDFAtom rdfData : rdf_data) {hexastore.add(rdfData);}
    }

    public static List<Set<Substitution>> uniformizeList(List<StarQuery> starQueries, List<Set<Substitution>> hexastoreResults) {
        // Vérification : les deux listes doivent avoir la même taille
        if (starQueries.size() != hexastoreResults.size()) {
            throw new IllegalArgumentException("Les listes starQueries et hexastoreResults doivent avoir la même taille.");
        }

        // Étape 1 : Grouper les sets par leur taille
        Map<Integer, List<Set<Substitution>>> sizeGroups = new HashMap<>();
        for (Set<Substitution> set : hexastoreResults) {
            int size = set.size();
            sizeGroups.computeIfAbsent(size, k -> new ArrayList<>()).add(set);
        }

        System.out.println("[INFO] Il y a "+sizeGroups.get(0).size()+" requêtes à 0 réponses.");

        // Étape 2 : Calculer la taille cible pour chaque groupe
        int totalSets = hexastoreResults.size();
        int targetSetsPerGroup = totalSets / sizeGroups.size();
        List<Map.Entry<Integer, List<Set<Substitution>>>> groupList = new ArrayList<>(sizeGroups.entrySet());

        // Étape 3 : Ajuster la liste pour avoir des groupes avec environ la même taille
        List<Set<Substitution>> result = new ArrayList<>();
        List<StarQuery> updatedStarQueries = new ArrayList<>();

        for (Map.Entry<Integer, List<Set<Substitution>>> entry : groupList) {
            List<Set<Substitution>> setsOfThisSize = entry.getValue();
            int currentSize = setsOfThisSize.size();

            // Calculer le nombre de sets à garder pour ce groupe
            int setsToKeep = Math.min(currentSize, (targetSetsPerGroup / 10));

            // Ajouter les sets sélectionnés à la liste résultat
            for (int i = 0; i < setsToKeep; i++) {
                result.add(setsOfThisSize.get(i));
                // Ajouter l'élément correspondant dans starQueries à la liste mise à jour
                updatedStarQueries.add(starQueries.get(hexastoreResults.indexOf(setsOfThisSize.get(i))));
            }
        }

        // Mettre à jour la liste starQueries pour refléter les changements
        starQueries.clear();
        starQueries.addAll(updatedStarQueries);

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
        System.out.println("[INFO] Il y a "+starQueries.size()+" requêtes en étoile soit "+(starQueries.size()/queries.size())*100.0+"% des requêtes.");
        return starQueries;
    }

    public static void main(String[] args) throws IOException {
        // args[0] = FILE_PATH
        // args[1] = DATA_PATH

        System.out.println(args[0]);
        System.out.println(args[1]);
        if(args.length < 2) {
            System.out.println("Merci de fournir au moins deux argmuments:");
            System.out.println("1 - Le chemin vers le fichier des query");
            System.out.println("2 - Le chemin vers le fichier des data");
        } else {
            if(args[0].contains(".queryset") && args[1].contains(".nt")) {
                if(args.length > 2) {
                    if(args[2].equals("true")) {
                        new QueryBenchmark(args[0],args[1], true);
                    }
                } else {
                    new QueryBenchmark(args[0],args[1], false);
                }
            } else{
                System.out.println("toto");
            }
        }

        // new QueryBenchmark("merged_1M.queryset","500K.nt");
    }

}
