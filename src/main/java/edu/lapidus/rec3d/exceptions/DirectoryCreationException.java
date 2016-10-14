package edu.lapidus.rec3d.exceptions;

/**
 * Created by egor.lapidus on 14/10/16.
 */
public class DirectoryCreationException extends Exception {
    public DirectoryCreationException(String cause) {
        super(cause);
    }
    public DirectoryCreationException(String cause, Throwable e) {
        super(cause, e);
    }
}
