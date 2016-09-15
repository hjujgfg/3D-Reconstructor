package edu.lapidus.rec3d.exceptions;

/**
 * Created by egor.lapidus on 01/09/16.
 */
public class FileLoadingException extends Exception {
    public FileLoadingException () {
        super();
    }

    public FileLoadingException (String message, Throwable cause) {
        super(message, cause);
    }

    public FileLoadingException (Throwable cause) {
        super (cause);
    }
}
