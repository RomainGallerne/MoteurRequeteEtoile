package main.java.qengine.model;

import java.util.ArrayList;
import java.util.List;

public class Indexes {
    private Index OPS;
    private Index OSP;
    private Index POS;
    private Index PSO;
    private Index SOP;
    private Index SPO;

    public Indexes() {
        this.OPS = null;
        this.OSP = null;
        this.POS = null;
        this.PSO = null;
        this.SOP = null;
        this.SPO = null;
    }

//    public void createIndexes(List<int[]> tripletsEncoder) {
//        this.OPS = new Index(permuteTriplets(tripletsEncoder, "OPS"));
//        this.OSP = new Index(permuteTriplets(tripletsEncoder, "OSP"));
//        this.POS = new Index(permuteTriplets(tripletsEncoder, "POS"));
//        this.PSO = new Index(permuteTriplets(tripletsEncoder, "PSO"));
//        this.SOP = new Index(permuteTriplets(tripletsEncoder, "SOP"));
//        this.SPO = new Index(permuteTriplets(tripletsEncoder, "SPO"));
//    }

    // Effectue les permutations nécessaires pour constuire l'index.
    // On suppose qu'on récupère un triplet au format SPO en entrée.
//    public static List<int[]> permuteTriplets(List<int[]> tripletsEncoder, String ordre) {
//        List<int[]> permuted = new ArrayList<>();
//
//        for (int[] triplet : tripletsEncoder) {
//            if (triplet.length == 3) {
//                int[] permutedTriplet;
//                switch(ordre){
//                    case "OPS" : permutedTriplet = new int[]{triplet[2], triplet[1], triplet[0]};
//                    case "OSP" : permutedTriplet = new int[]{triplet[2], triplet[0], triplet[1]};
//                    case "POS" : permutedTriplet = new int[]{triplet[1], triplet[2], triplet[0]};
//                    case "PSO" : permutedTriplet = new int[]{triplet[1], triplet[0], triplet[2]};
//                    case "SOP" : permutedTriplet = new int[]{triplet[0], triplet[2], triplet[1]};
//                    default : permutedTriplet = triplet;
//                }
//
//                permuted.add(permutedTriplet);
//            }
//        }
//
//        return permuted;
//    }

//    private Index getOPS(){return this.OPS;}
//    private Index getOSP(){return this.OSP;}
//    private Index getPOS(){return this.POS;}
//    private Index getPSO(){return this.PSO;}
//    private Index getSOP(){return this.SOP;}
//    private Index getSPO(){return this.SPO;}
}
