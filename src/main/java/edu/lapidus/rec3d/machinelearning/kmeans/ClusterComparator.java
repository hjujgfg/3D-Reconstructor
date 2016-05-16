package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.image.ImageScanner;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 03.05.2016.
 */
public class ClusterComparator {
    public ClusterComparator(BufferedImage m1, BufferedImage m2,
                             List<Cluster> clusters1,
                             List<Cluster> clusters2,
                             Map<String, Integer> clusterMap) {
        this.m1 = new ColorMatrix(m1);
        this.m2 = new ColorMatrix(m2);
        this.clusters1 = clusters1;
        this.clusters2 = clusters2;
        this.clusterMap = clusterMap;
    }

    private ColorMatrix m1, m2;
    List<Cluster> clusters1, clusters2;
    Map<String, Integer> clusterMap;
    private final static Logger logger = Logger.getLogger(ClusterComparator.class);
    private final static Object LOCK_OBJ = new Object();
    private static final int COLOR_REGION_SIZE = 10;

    public List<CorrespondenceHolder> getRandomCorrespondences() {
        logger.info("Started comparing clusters");
        List<CorrespondenceHolder> bestOfTheBest = new ArrayList<>();
        List<CorrespondenceHolder> best = new ArrayList<>(20);
        for (int i = 0; i < clusters1.size(); i ++) {
            logger.info("processing " + i + " cluster");
            if (clusters1.get(i) == null || clusters2.get(i) == null
                    || clusters1.get(i).isEmpty() || clusters2.get(i).isEmpty()) continue;
            best.clear();
            for (int j = 0; j < 20; j ++) {
                best.add(compareClusters(clusters1.get(i), clusters2.get(i)));
            }
            bestOfTheBest.add(calcAverage(best));
            /*bestOfTheBest.add(compareClusters(clusters1.get(i), clusters2.get(i)));*/
        }
        Collections.sort(bestOfTheBest);
        return bestOfTheBest.subList(0, bestOfTheBest.size() / 2);
    }

    private CorrespondenceHolder calcAverage(List<CorrespondenceHolder> list) {
        ColoredImagePoint res1 = new ColoredImagePoint(0,0, list.get(0).getA().getColor());
        ColoredImagePoint res2 = new ColoredImagePoint(0,0, list.get(0).getB().getColor());
        double x1 = 0, y1 = 0, x2 = 0, y2 = 0, distance = 0;
        for (CorrespondenceHolder p : list) {
            x1 += p.getA().getX();
            y1 += p.getA().getY();
            x2 += p.getB().getX();
            y2 += p.getB().getY();
            distance += p.getDistance();
        }
        res1.setX((int) (x1 / list.size()));
        res1.setY((int) (y1 / list.size()));
        res2.setX((int) (x2 / list.size()));
        res2.setY((int) (y2 / list.size()));
        return new CorrespondenceHolder(res1, res2, distance / list.size());
    }

    public ColoredImagePoint getSpecificCorrespondence(ColoredImagePoint p1) {

        int clusterIndex = clusterMap.get(p1.key());
        if (clusterIndex == -1)
            throw new IllegalArgumentException("Point lies in background: " + p1.getX() + " : " + p1.getY());

        if (clusters1.get(clusterIndex) == null || clusters2.get(clusterIndex) == null
                || clusters1.get(clusterIndex).isEmpty() || clusters2.get(clusterIndex).isEmpty())
            throw new IllegalArgumentException("wrong cluster index");
        List<ColoredImagePoint> c2 = clusters2.get(clusterIndex);
        double min = Double.MAX_VALUE;
        ColoredImagePoint res = null;
        for (ColoredImagePoint p2 : c2){
            double currentDist = comparePoints(p1, p2);
            if (currentDist < min) {
                min = currentDist;
                res = p2;
            }
        }
        return res;
    }

    /**
     * finds list of similar points in descending order
     * @param c1
     * @param c2
     * @return
     */
    private CorrespondenceHolder compareClusters(Cluster c1, Cluster c2) {
        double min = Double.MAX_VALUE;
        ColoredImagePoint min1 = null;
        ColoredImagePoint min2 = null;
        //for (ColoredImagePoint p1 : c1) {
        Random r = new Random();
        //ColoredImagePoint p1 = c1.get(r.nextInt(c1.size()));
        ColoredImagePoint p1 = c1.getNextClosest();
            for (ColoredImagePoint p2 : c2) {
                double temp = comparePoints(p1, p2);
                if (temp < min) {
                    min1 = p1;
                    min2 = p2;
                    min = temp;
                }
            }
        //}
        return new CorrespondenceHolder(min1, min2, min);
    }

    private double comparePoints (ColoredImagePoint p1, ColoredImagePoint p2) {
        double res = 0;
        List<Color> l1 = getColorRegion(p1, m1);
        List<Color> l2 = getColorRegion(p2, m2);
        double dist = p1.getY() - p2.getY();
        for (int i = 0; i < l1.size(); i ++) {
            //res += Math.sqrt( evaluateColors(l1.get(i), l2.get(i)))
                    /*+ 0.9 * (p1.getY() - p2.getY()) * (p1.getY() - p2.getY())
                    + 0.3 * (p1.getX() - p2.getX()) * (p1.getX() - p2.getX()))*/ ;
            res += evaluateColors(l1.get(i), l2.get(i)) + dist * dist * 3;
        }
        //logger.info("Color dist: " + res + " yDist: " + dist*dist + " l1size: " + l1.size());
        //res += dist * dist * 10;

        return Math.sqrt(res) / l1.size();
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
        //Math.sqrt
        return r1 * r1 + g1 * g1 + b1 * b1;
    }
}
