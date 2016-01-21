package edu.lapidus.rec3d.utils.image;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Егор on 21.11.2015.
 */
public class ImageProcessor {
    final static Logger logger = Logger.getLogger(ImageProcessor.class);

    public BufferedImage loadImage(String path) {
        logger.info("Started loading " + path);
        File f = new File(path);
        BufferedImage img = null;
        try {
            img = ImageIO.read(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("error loading " + path + "\n" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("error loading " + path + "\n" + e.toString());
        }
        logger.info("Successfully loaded " + path);
        return img;
    }
}
