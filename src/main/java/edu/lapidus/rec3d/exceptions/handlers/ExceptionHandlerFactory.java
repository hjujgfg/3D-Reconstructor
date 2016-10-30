package edu.lapidus.rec3d.exceptions.handlers;

import edu.lapidus.rec3d.exceptions.DirectoryCreationException;
import edu.lapidus.rec3d.exceptions.FileLoadingException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Егор on 30.10.2016.
 */
public class ExceptionHandlerFactory {

    private final static Logger logger = Logger.getLogger(ExceptionHandlerFactory.class);
    private final static Map<String, ExceptionHandler> handlersMap = new HashMap<>();

    static {
        logger.info("Initiating Exception - Handler mapping");
        handlersMap.put(FileLoadingException.class.getSimpleName(), new FileExceptionHandler());
        handlersMap.put(DirectoryCreationException.class.getSimpleName(), new DirectoryExceptionHandler());
        handlersMap.put(GeneralExceptionHandler.class.getSimpleName(), new GeneralExceptionHandler());
    }

    public ExceptionHandler getInstance (Exception ex) {
        ExceptionHandler handler = handlersMap.get(ex.getClass().getSimpleName());
        if (handler != null) {
            return handler;
        } else {
            return handlersMap.get(GeneralExceptionHandler.class.getSimpleName());
        }
    }
}
