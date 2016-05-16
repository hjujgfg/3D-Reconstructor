package edu.lapidus.rec3d.math.matrix;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Егор on 27.01.2016.
 */
public class ColorMatrix implements Matrix {
    private int[][] inner;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private int height, width;
    private final static Logger logger = Logger.getLogger(ColorMatrix.class);

    public ColorMatrix(BufferedImage image) {
        logger.info("Started building color matrix");
        height = image.getHeight();
        width = image.getWidth();
        inner = new int[height][];
        for (int i = 0; i < height; i ++) {
            inner[i] = new int[width];
            for (int j = 0; j < width; j ++) {
                inner[i][j] = image.getRGB(j, i);
            }
        }
        logger.info("Ccreated Color Matrix with dimmensions: " + width + ":" + height);
    }
    //TODO think how to implement another way to cerate this matrix
    //when we do not need the whole image, but some region
    public ColorMatrix(int n) {}

    public void transpose() {
        int nHeight = width;
        int nWidth = height;
        int [][] res = new int[nHeight][];
        for (int j = 0; j < width; j ++) {
            res[j] = new int[height];
            for (int i = 0; i < height; i ++) {
                res[j][i] = inner[i][j];
            }
        }
        inner = res;
        width = nWidth;
        height = nHeight;
    }

    /**
     * Returns integer representation of color
     * @param x - x coordinate, it should be inside width
     * @param y - y coordinate, it should be inside height
     */
    public int getRGB(int x, int y) {
        return inner[y][x];
    }
    /**
     * Returns integer representation of color
     * @param x - x coordinate, it should be inside width
     * @param y - y coordinate, it should be inside height
     */
    public Color getColor(int x, int y) {
        return new Color(inner[y][x]);
    }
    public void setColor(int x, int y, Color color) {
        inner[y][x] = color.getRGB();
    }

    public void removeBackground() {
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                Color c = this.getColor(x, y);
                //TODO remove this shit
                if ( ( c.getGreen() > c.getRed() && c.getGreen() > c.getBlue() ) || y > 440) {
                    this.setColor(x, y, Color.GREEN);
                }
            }
        }
    }
}
