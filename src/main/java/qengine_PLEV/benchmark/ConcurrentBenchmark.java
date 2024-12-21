package qengine_PLEV.benchmark;

import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine_PLEV.storage.RDFHexaStore_PLEV;

import java.util.List;

public class ConcurrentBenchmark {

    public static RDFHexaStore_PLEV buildRDFStore(List<RDFAtom> rdfAtoms){
        RDFHexaStore_PLEV store = new RDFHexaStore_PLEV();
        store.addAll(rdfAtoms);
        return store;
    }

    public static void executeWithConcurrent(List<StarQuery> queries, RDFHexaStore_PLEV store) {
        for (StarQuery query : queries) {
            store.match(query);
        }
    }
}
