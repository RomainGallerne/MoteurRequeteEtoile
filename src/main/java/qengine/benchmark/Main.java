package qengine.benchmark;

import fr.boreal.model.logicalElements.api.Substitution;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.storage.RDFHexaStore;
import qengine_PLEV.storage.RDFHexaStore_PLEV;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

import static qengine.program.Utils.*;
import static qengine_PLEV.benchmark.ConcurrentBenchmark.buildRDFStore;
import static qengine_PLEV.benchmark.ConcurrentBenchmark.executeWithConcurrent;

public class Main {
    private static final RDFHexaStore hexastore = new RDFHexaStore();

    public Main(String FILE_PATH, String DATA_PATH, boolean uniformisation) throws IOException {
        // Parsing Data
        List<RDFAtom> rdf_data = parseRDFData(DATA_PATH);
        buildIndexesAndDictionary(rdf_data);

        // Parsing Queries
        List<StarQuery> starQueries = parseStarQueries(FILE_PATH);
        System.out.println("[INFO] Requêtes parsés.");
        starQueries= filterDuplicates(starQueries);
        System.out.println("[INFO] Taille après doublons supprimés : " + starQueries.size());

        // Exécuter les requêtes
        List<Set<Substitution>> integraalResults = executeWithIntegraal(rdf_data, starQueries, false);
        System.out.println("[INFO] Comptage du nombre de réponse par requêtes terminé.");

        // Définition de l'hexastore concurrent
        RDFHexaStore_PLEV concurrentstore = buildRDFStore(rdf_data);

        if(uniformisation) {
            // Uniformisation des résultats
            integraalResults = uniformizeList(starQueries, integraalResults);
            System.out.println("[INFO] Taille après uniformisation du nombre de résultats : " + starQueries.size());
        }

        Map<Integer, Integer> subSetSizes = countSubsetSizes(integraalResults);

        SwingUtilities.invokeLater(() -> {
            HistogramFrame frame = new HistogramFrame(subSetSizes);
            frame.setVisible(true);
        });

        //Libération d'espace
        integraalResults = null;

        Set<Set<Substitution>> integraalSet = runBenchmark_integraal(starQueries, rdf_data);
        Set<Set<Substitution>> hexastoreSet = runBenchmark_hexastore(starQueries);
        runBenchmark_concurrent(starQueries, concurrentstore);

        //Test de correction
        if (integraalSet.containsAll(hexastoreSet)) {
            System.out.println("Matching Correct ✔");
        } else {
            System.out.println("Matching Incorrect");
        }

        //Test de complétude
        if (hexastoreSet.containsAll(integraalSet)) {
            System.out.println("Matching Complet ✔");
        } else {
            System.out.println("Matching Incomplet");
        }
    }

    private Set<Set<Substitution>> runBenchmark_hexastore(List<StarQuery> starQueries) {
        int twentyPercentCount = (int) Math.ceil(starQueries.size() * 0.2);
        List<StarQuery> initialQueries = starQueries.subList(0, twentyPercentCount);
        List<StarQuery> remainingQueries = starQueries.subList(twentyPercentCount, starQueries.size());

        System.gc();
        executeWithHexastore(initialQueries, hexastore, false);
        ExecutionTimer.TimerReport report;

        Runtime runtime = Runtime.getRuntime();
        ExecutionTimer timer = new ExecutionTimer();
        timer.start();

        // Execution des 80% de requêtes restantes
        List<Set<Substitution>> remainingResults = executeWithHexastore(remainingQueries, hexastore, false);

        report = timer.stop();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();;
        System.out.println("[BENCHMARK HEXASTORE] : ");
        System.out.println("Mémoire utilisée : " + usedMemory / (1024 * 1024) + " MB");
        System.out.println(report);

        return new HashSet<>(remainingResults);
    }

    private Set<Set<Substitution>> runBenchmark_integraal(List<StarQuery> starQueries, List<RDFAtom> rdf_data){
        // Execution des 20% de requête pour chauffer la JVM
        int twentyPercentCount = (int) Math.ceil(starQueries.size() * 0.2);
        List<StarQuery> initialQueries = starQueries.subList(0, twentyPercentCount);
        List<StarQuery> remainingQueries = starQueries.subList(twentyPercentCount, starQueries.size());

        System.gc();
        executeWithIntegraal(rdf_data, initialQueries, false);

        Runtime runtime = Runtime.getRuntime();

        ExecutionTimer timer = new ExecutionTimer();
        timer.start();

        // Execution des 80% de requêtes restantes
        List<Set<Substitution>> remainingResults = executeWithIntegraal(rdf_data, remainingQueries, false);

        ExecutionTimer.TimerReport report = timer.stop();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();;
        System.out.println("[BENCHMARK INTEGRAAL] : ");
        System.out.println("Mémoire utilisée : " + usedMemory / (1024 * 1024) + " MB");
        System.out.println(report);

        return new HashSet<>(remainingResults);
    }

    private void runBenchmark_concurrent(List<StarQuery> starQueries, RDFHexaStore_PLEV concurrentstore) {
        int twentyPercentCount = (int) Math.ceil(starQueries.size() * 0.2);
        List<StarQuery> initialQueries = starQueries.subList(0, twentyPercentCount);
        List<StarQuery> remainingQueries = starQueries.subList(twentyPercentCount, starQueries.size());

        System.gc();
        executeWithConcurrent(initialQueries, concurrentstore);
        ExecutionTimer.TimerReport report;

        Runtime runtime = Runtime.getRuntime();
        ExecutionTimer timer = new ExecutionTimer();
        timer.start();

        // Execution des 80% de requêtes restantes
        executeWithConcurrent(remainingQueries, concurrentstore);

        report = timer.stop();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();;
        System.out.println("[BENCHMARK CONCURRENT] : ");
        System.out.println("Mémoire utilisée : " + usedMemory / (1024 * 1024) + " MB");
        System.out.println(report);
    }


    private void buildIndexesAndDictionary(List<RDFAtom> rdf_data){
        for (RDFAtom rdfAtom : rdf_data){hexastore.add_to_dico(rdfAtom.getTerms());}
        System.out.println("[INFO] Dictionnaire de données construit.");
        hexastore.dico_createCodex();
        System.out.println("[INFO] Codex créé.");
        hexastore.addAll(rdf_data, false); //Seul SPO est utilisé pour les starQuery, on ne construit donc que celui-ci
        System.out.println("[INFO] Index de données construit.");
    }

    public static List<Set<Substitution>> uniformizeList(List<StarQuery> starQueries, List<Set<Substitution>> hexastoreResults) {
        // Vérification : les deux listes doivent avoir la même taille
        if (starQueries.size() != hexastoreResults.size()) {
            throw new IllegalArgumentException("[ERROR] Les listes starQueries et hexastoreResults doivent avoir la même taille.");
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
    private static List<StarQuery> filterDuplicates(List<StarQuery> queries) {
        Set<StarQuery> uniqueQueries = new HashSet<>(queries);
        return new ArrayList<>(uniqueQueries);
    }

    public static void main(String[] args) throws IOException {
        args = new String[3];
        args[0] = "data/merged.queryset";
        args[1] = "data/2M.nt";
        args[2] = "true";

        if(args.length < 2) {
            System.out.println("Merci de fournir au moins deux arguments:");
            System.out.println("1 - Le chemin vers le fichier des query");
            System.out.println("2 - Le chemin vers le fichier des data");
        } else {
            if(args[0].contains(".queryset") && args[1].contains(".nt")) {
                if(args.length > 2 && args[2].equals("true")) {
                    new Main(args[0],args[1], true);
                } else {
                    new Main(args[0],args[1], false);
                }
            } else{
                System.out.println("Fichiers mal formatés");
            }
        }

        // new Main("merged_1M.queryset","500K.nt");
    }

}
