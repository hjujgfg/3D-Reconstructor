package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Егор on 03.05.2016.
 */
public class ClusterComparator {
    public ClusterComparator(BufferedImage m1, BufferedImage m2, List<List<ColoredImagePoint>> clusters1, List<List<ColoredImagePoint>> clusters2) {
        this.m1 = new ColorMatrix(m1);
        this.m2 = new ColorMatrix(m2);
        this.clusters1 = clusters1;
        this.clusters2 = clusters2;
    }

    private ColorMatrix m1, m2;
    List<List<ColoredImagePoint>> clusters1, clusters2;
    private final static Logger logger = Logger.getLogger(ClusterComparator.class);
    private final static Object LOCK_OBJ = new Object();
    private static final int COLOR_REGION_SIZE = 5;

    public List<List<ColoredImagePoint>> compareImages() {
        logger.info("Started comparing clusters");
        List<List<ColoredImagePoint>> bestOfTheBest = new ArrayList<>();
        for (int i = 0; i < clusters1.size(); i ++) {
            logger.info("processing " + i + " cluster");
            if (clusters1.get(i) == null || clusters2.get(i) == null
                    || clusters1.get(i).isEmpty() || clusters2.get(i).isEmpty()) continue;
            bestOfTheBest.add(compareClusters(clusters1.get(i), clusters2.get(i)));
        }
        return bestOfTheBest;
    }

    /**
     * finds list of similar points in descending order
     * @param c1
     * @param c2
     * @return
     */
    private List<ColoredImagePoint> compareClusters(List<ColoredImagePoint> c1, List<ColoredImagePoint> c2) {
        double min = Double.MAX_VALUE;
        ColoredImagePoint min1 = null;
        ColoredImagePoint min2 = null;
        //for (ColoredImagePoint p1 : c1) {
        Random r = new Random();
        ColoredImagePoint p1 = c1.get(r.nextInt(c1.size()));
            for (ColoredImagePoint p2 : c2) {
                double temp = comparePoints(p1, p2);
                if (temp < min) {
                    min1 = p1;
                    min2 = p2;
                    min = temp;
                }
            }
        //}
        return Arrays.asList(min1, min2);
    }

    private double comparePoints (ColoredImagePoint p1, ColoredImagePoint p2) {
        double res = 0;
        List<Color> l1 = getColorRegion(p1, m1);
        List<Color> l2 = getColorRegion(p2, m2);
        for (int i = 0; i < l1.size(); i ++) {
            res += evaluateColors(l1.get(i), l2.get(i));
        }
        return res / l1.size();
    }

    private List<Color> getColorRegion(ColoredImagePoint p, ColorMatrix m) {
        List<Color> res = new ArrayList<>();
        int px = p.getX();
        int py = p.getY();
        int startX = px - COLOR_REGION_SIZE;
        int startY = py - COLOR_REGION_SIZE;
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;
        int endX = startX + 2 * COLOR_REGION_SIZE;
        int endY = startY + 2 * COLOR_REGION_SIZE;
        if (endX >= m.getWidth()) {
            endX = m.getWidth() - 1;
            startX = m.getWidth() - 1 - 2 * COLOR_REGION_SIZE;
        }
        if (endY >= m.getHeight()) {
            endY = m.getHeight() - 1;
            startY = m.getHeight() - 1 - 2 * COLOR_REGION_SIZE;
        }
        for (int x = startX; x < endX; x += 2){
            for (int y = startY; y < endY; y += 2) {
                res.add(m.getColor(x, y));
            }
        }
        return res;
    }

    private double evaluateColors(Color c1, Color c2) {
        int r1 = c1.getRed(), r2 = c2.getRed(), g1 = c1.getGreen(), g2 = c2.getGreen(), b1 = c1.getBlue(), b2 = c2.getBlue();
        r1 -= r2;
        g1 -= g2;
        b1 -= b2;
        return Math.sqrt(r1 * r1 + g1 * g1 + b1 * b1);
    }
}
