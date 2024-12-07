package main.java.qengine.exceptions;

public class ValueNotFoundException extends Exception {
    public ValueNotFoundException(int index) {
        super("Key not found: " + index);
    }
}
