package edu.lapidus.rec3d.exceptions.handlers;


import org.apache.log4j.Logger;

/**
 * Created by Егор on 30.10.2016.
 */
public class FileExceptionHandler extends ExceptionHandler {
    private final static Logger logger = Logger.getLogger(FileExceptionHandler.class);

    @Override
    protected void handleException(Exception ex) {
        logger.error("Handling FileLoadingException: ", ex);
        //TODO at this point we just quit the program however in future we should reattempt steps, where we failed
        System.exit(1);
    }
}
