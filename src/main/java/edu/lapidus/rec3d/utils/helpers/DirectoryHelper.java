package edu.lapidus.rec3d.utils.helpers;

import edu.lapidus.rec3d.exceptions.DirectoryCreationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by egor.lapidus on 14/10/16.
 */
public class DirectoryHelper {

    private final static Logger logger = Logger.getLogger(DirectoryHelper.class);

    public static final String INPUT_DIR = "input/";
    public static final String OUTPUT_DIR = "output/";

    public static final String IMAGES_DIR = "input/images/";
    public static final String CONVOLVE_OUT = "output/convolve/";
    public static final String CLUSTERING_OUT = "output/clustering/";
    public static final String THREE_D_OUT = "output/3D/";

    public String getImagesFolder() {
        return IMAGES_DIR;
    }

    public String getImagesDir(String name) {
        return IMAGES_DIR + name + "/";
    }

    public void createDirs(String model) throws DirectoryCreationException{
        String innerDir = getImagesDir(model) + "res/";
        createDirsInner(innerDir);
        createDirsInner(CONVOLVE_OUT);
        createDirsInner(CLUSTERING_OUT);
        createDirsInner(THREE_D_OUT);
    }

    private void createDirsInner(String name) throws DirectoryCreationException {
        boolean success = new File(name).mkdirs();
        if (!success) {
            throw new DirectoryCreationException("Unable to create directory structure for input");
        }
    }

    public void copyFile(String from, String to) throws IOException {
        Files.copy(Paths.get(from), Paths.get(to), REPLACE_EXISTING);
    }

}
