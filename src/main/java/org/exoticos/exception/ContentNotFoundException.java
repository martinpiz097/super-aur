package org.exoticos.exception;

public class ContentNotFoundException extends Exception {
    public ContentNotFoundException(String content) {
        super(content);
    }
}
