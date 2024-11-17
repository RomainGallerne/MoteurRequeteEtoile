package main.java.qengine.model;

import java.util.*;

import static java.lang.System.exit;

public class Index {
    private final Map<Integer, Map<Integer, List<Integer>>> index;

    public Index() {
        this.index = new HashMap<>();
    }

    // Méthode pour ajouter un triplet
    public void ajoutTriplet(int[] triplet) {
        index
                .computeIfAbsent(triplet[0], k -> new HashMap<>()) // Si le sujet (triplet[0]) n'existe pas, on le crée
                .computeIfAbsent(triplet[1], k -> new ArrayList<>()) // Si le prédicat (triplet[1]) n'existe pas, on le crée
                .add(triplet[2]); // On ajoute l'objet (triplet[2])
    }

    // Méthode pour rechercher des triplets pour les trois éléments données
    public List<int[]> searchByThree(Integer first_element, Integer second_element, Integer third_element) {
        Map<Integer, List<Integer>> first_elem_match =  this.index.getOrDefault(first_element, Collections.emptyMap());
        List<Integer> second_elem_match =  first_elem_match.getOrDefault(second_element, Collections.emptyList());

        List<int[]> return_list = new ArrayList<>(Collections.emptyList());
        for (Integer elem : second_elem_match){
            if(Objects.equals(third_element, elem)) {
                return_list.add(new int[]{first_element, second_element, third_element});
            }
        }
        return return_list;
    }

    // Méthode pour rechercher des variables (?x) pour deux éléments donnés
    public List<int[]> searchByTwo(Integer first_element, Integer second_element) {
        List<Integer> third_elem_match =  this.index.getOrDefault(first_element, Collections.emptyMap())
                .getOrDefault(second_element, Collections.emptyList());

        List<int[]> return_list = new ArrayList<>(Collections.emptyList());
        for (Integer third_element : third_elem_match){
            return_list.add(new int[]{first_element, second_element, third_element});
        }
        return return_list;
    }

    // Méthode pour rechercher des variables (?x, ?y) pour un élément donné
    public List<int[]> searchByOne(Integer first_element) {
        Map<Integer, List<Integer>> map_match =  this.index.getOrDefault(first_element, Collections.emptyMap());

        List<int[]> return_list = new ArrayList<>(Collections.emptyList());

        for (Integer second_element :  map_match.keySet()){
            for (Integer third_element : map_match.get(second_element)){
                return_list.add(new int[]{first_element, second_element, third_element});
            }
        }

        return return_list;
    }
}
