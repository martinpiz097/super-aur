package org.exoticos.exception;

public class LevelInfoException extends RuntimeException {
    public LevelInfoException(int level) {
        super("The level " + level + " not exists!");
    }
}
