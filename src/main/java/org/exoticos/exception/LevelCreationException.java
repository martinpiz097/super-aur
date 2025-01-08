package org.exoticos.exception;

public class LevelCreationException extends RuntimeException {
    public LevelCreationException(int level, boolean exists) {
        super(exists
                ? "The level " + level + " already exists!"
                : "The level " + level + " to be created is huge compared to the highest level");
    }
}
