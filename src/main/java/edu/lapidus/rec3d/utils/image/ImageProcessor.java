package edu.lapidus.rec3d.utils.image;

import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Map;

/**
 * Created by Егор on 21.11.2015.
 */
public class ImageProcessor {
    final static Logger logger = Logger.getLogger(ImageProcessor.class);

    final static String STORAGE_DIR = "resources/res/";

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

    public void createDepthMap(Map<String, PairCorrespData> points) {
        File f = new File(STORAGE_DIR + "depth.png");
        logger.info("Building depth image");
        BufferedImage map = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        for (Map.Entry<String, PairCorrespData> entry : points.entrySet()) {
            String [] coords = entry.getKey().split("_");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            double z = entry.getValue().getZ() * 1000000;
            int i = (int)z;
            if (i < -382)
                i = -382;
            if (i > 382) {
                i = 382;
            }

            map.setRGB(x, y, calcColorX(z).getRGB());
        }
        try {
            ImageIO.write(map, "png", f);
            logger.info("Saving success");
        } catch (IOException e) {
            logger.error("Error saving image ", e);
        }
    }

    private Color calcColor(int depth) {
        int red = 0, green = 0, blue = 0 ;
        if (depth > -383 && depth < -127) {
            red = depth + 383;
        } else if (depth > -128 && depth < 127) {
            green = depth + 127;
        } else if (depth > 127 && depth < 383) {
            blue = depth - 128;
        }
        return new Color(red, green, blue);
    }

    private Color calcColor(double depth) {
        int x = (int)depth;
        //x += 700;
        //x *= 100000;
        if (x <= 0)
            x = 0;
        int r = x % 256;
        int tmp = x / 256;
        int g = tmp % 256;
        tmp = tmp / 256;
        int b = tmp % 256;
        return new Color(r, g, b);
    }

    private Color calcColorX(double depth) {
        int x = (int) depth;
        x += 320;
        if (x < 0) {
            x = 0;
        }
        int r = x % 256;
        x -= r;
        x /= 10;
        int g = x % 256;
        x -= g;
        x /= 10;
        int b = x % 256;
        return new Color(r,g,b);

    }

    public void saveImage(BufferedImage img, String fileName) {
        try {
            ImageIO.write(img, "png", new File(fileName));
            logger.info("Saved image " + fileName);
        } catch (IOException e) {
            logger.error("Error saving image", e);
        }
    }
}
