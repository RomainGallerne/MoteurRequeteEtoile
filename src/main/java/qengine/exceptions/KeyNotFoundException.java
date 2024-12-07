package qengine.exceptions;

import fr.boreal.model.logicalElements.api.Term;

public class KeyNotFoundException extends Exception {
    public KeyNotFoundException(Term term) {
        super("Key not found: " + term);
    }
}
