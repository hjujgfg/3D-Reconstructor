package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAccumulator;

/**
 * Created by Егор on 30.04.2016.
 */
public class Kmeans {
    public List<List<ColoredImagePoint>> getClusters() {
        return clusters;
    }

    public List<Centroid> getCentroids() {
        return centroids;
    }

    private List<List<ColoredImagePoint>> clusters;
    private List<Centroid> centroids;
    private ColorMatrix image;
    boolean stop;
    int iterationCounter = 0;

    private final static double THRESHOLD = 10;
    private final static double ITERATION_NUMBER = 30;
    private final static Logger logger = Logger.getLogger(Kmeans.class);

    public Kmeans(int numberOfClusters, ColorMatrix img, List<Centroid> initCentroids) {
        clusters = new ArrayList<>(numberOfClusters);
        for (int i = 0; i < numberOfClusters; i ++) {
            clusters.add(new ArrayList<>());
        }
        if (initCentroids != null) {
            centroids = initCentroids;
        } else
        //TODO implement random initialization
            centroids = new ArrayList<>(numberOfClusters);
        image = img;
        stop = false;
        logger.info("Inited kmeans");
    }

    public void runAlgorithm() {
        List<Centroid> prev;
        while (!stop && iterationCounter < ITERATION_NUMBER) {
            logger.info("Kmeans iteration: " + iterationCounter);
            prev = new ArrayList<>(centroids);
            runClusterization();
            updateCentroids();
            stop = compareCentroids(prev, centroids);
            iterationCounter ++;
        }
        logger.info("finished algorithm");
    }

    private boolean compareCentroids(List<Centroid> c1, List<Centroid> c2) {
        for (Centroid c : c1) {
            for (Centroid d : c2) {
                if (getDistance(c, d) > THRESHOLD) return false;
            }
        }
        return true;
    }


    private void runClusterization() {
        for (int x = 0; x < image.getWidth(); x ++) {
            for (int y = 0; y < image.getHeight(); y ++) {
                ColoredImagePoint temp = new ColoredImagePoint(x, y, image.getRGB(x, y));
                if (temp.getColor().equals(Color.GREEN)) continue;
                double minDist = Double.MAX_VALUE;
                int minIndex = 0;
                int counter = 0;
                for (ColoredImagePoint centroid : centroids) {
                    if (centroid.getColor().equals(Color.GREEN)) continue;
                    double currDist = getDistance(centroid, temp);
                    if (currDist < minDist) {
                        minDist = currDist;
                        minIndex = counter;
                    }
                    counter ++;
                }
                clusters.get(minIndex).add(temp);
            }
        }
    }

    private void updateCentroids() {
        centroids.clear();
        //centroids.addAll(clusters.stream().map(this::calcCentroid).collect(Collectors.toList()));
        for (List<ColoredImagePoint> cluster : clusters) {
            Centroid centroid = calcCentroid(cluster);
            centroid.cluster = cluster;
            centroids.add(centroid);
        }
    }

    private Centroid calcCentroid(List<ColoredImagePoint> cluster) {
        if (cluster.isEmpty()) {
            return new Centroid(0, 0, Color.GREEN);
        }
        double avgX = 0, avgY = 0, avgR = 0, avgB = 0, avgG = 0;
        int size = cluster.size();
        for (ColoredImagePoint p : cluster) {
            avgX += p.getX();
            avgY += p.getY();
            avgR += p.getColor().getRed();
            avgG += p.getColor().getGreen();
            avgB += p.getColor().getBlue();
        }
        int x = (int) ( avgX / size );
        int y = (int) ( avgY / size );
        int r = (int) (avgR / size);
        int g = (int) (avgG / size);
        int b = (int) (avgB / size);
        return new Centroid(x, y, new Color(r, g, b));
    }

    private double getDistance(ColoredImagePoint p1, ColoredImagePoint p2) {
        double pxDist = p1.getDistanceTo(p2);
        double colorMultiplier = (image.getWidth() + image.getHeight()) / 2 / 256;
        //logger.info("color multiplier: " + colorMultiplier);
        //logger.info("pxDist: " + pxDist + " colorDist: " + colorMultiplier * p1.getColorDistance(p2));
        double colorDist = p1.getColorDistance(p2) * colorMultiplier ;
        //double colorDist = 0;
        return pxDist + colorDist;
    }


}
