package edu.lapidus.rec3d.exceptions.handlers;

import org.apache.log4j.Logger;

/**
 * Created by Егор on 30.10.2016.
 */
public class DirectoryExceptionHandler extends ExceptionHandler {
    private final static Logger logger = Logger.getLogger(DirectoryExceptionHandler.class);
    @Override
    protected void handleException(Exception ex) {
        logger.error("Handling DirectoryCreationException: ", ex);
        System.exit(1);
    }
}
