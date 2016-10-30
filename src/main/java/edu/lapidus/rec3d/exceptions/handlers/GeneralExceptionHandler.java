package edu.lapidus.rec3d.exceptions.handlers;

import org.apache.log4j.Logger;

/**
 * Created by Егор on 30.10.2016.
 */
public class GeneralExceptionHandler extends ExceptionHandler {
    private final static Logger logger = Logger.getLogger(GeneralExceptionHandler.class);

    @Override
    protected void handleException(Exception ex) {
        logger.error("Found unrecognized exception: " + ex.getMessage(), ex);
    }
}
