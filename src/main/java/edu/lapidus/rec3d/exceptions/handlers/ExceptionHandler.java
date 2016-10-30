package edu.lapidus.rec3d.exceptions.handlers;

import edu.lapidus.rec3d.exceptions.FileLoadingException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Егор on 30.10.2016.
 * Class is designed to be a base class for different handlers
 */
public abstract class ExceptionHandler {
    private final static Logger logger = Logger.getLogger(ExceptionHandler.class);
    private final static ExceptionHandlerFactory factory = new ExceptionHandlerFactory();
    protected abstract void handleException (Exception ex);

    public static void handle (Exception ex) {
        factory.getInstance(ex).handleException(ex);
    }

    public static void handle (String message, Exception ex) {
        logger.error(message);
        factory.getInstance(ex).handleException(ex);
    }
}
