package edu.lapidus.rec3d.utils.helpers;

import edu.lapidus.rec3d.exceptions.DirectoryCreationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        File f = new File (IMAGES_DIR);
        if (f.exists()) {
            try {
                Files.walk(Paths.get(IMAGES_DIR), 2).forEach(filepath -> {
                    if (filepath.toString().endsWith(".jpg") || filepath.toString().endsWith(".png")) {
                        logger.info("Removing: " + filepath.toString());
                        filepath.toFile().delete();
                    }
                });
            } catch (IOException e) {
                throw new DirectoryCreationException("Error deleting old images", e);
            }
        }
        createDirsInner(innerDir);
        createDirsInner(CONVOLVE_OUT);
        createDirsInner(CLUSTERING_OUT);
        createDirsInner(THREE_D_OUT);
    }

    private void createDirsInner(String name) throws DirectoryCreationException {
        File f = new File(name);
        boolean success = true;
        if (!f.exists()) {
            success = f.mkdirs();
        }
        if (!success) {
            throw new DirectoryCreationException("Unable to create directory structure for " + name);
        }
    }

    public void copyFile(String from, String to) throws IOException {
        logger.info("Copying: from " + from + "; to " + to);
        Files.copy(Paths.get(from), Paths.get(to).resolve(from), REPLACE_EXISTING);
    }

}
