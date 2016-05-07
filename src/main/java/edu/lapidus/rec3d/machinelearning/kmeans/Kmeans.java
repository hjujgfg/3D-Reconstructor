package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import org.apache.log4j.Logger;
import sun.util.PreHashedMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.stream.Collectors;

/**
 * Created by Егор on 30.04.2016.
 */
public class Kmeans {


    public List<Centroid> getCentroids() {
        return centroids;
    }

    //private List<List<ColoredImagePoint>> finalClusters;
    private Map<String, Integer> clusterMap;
    private List<Centroid> centroids;
    private ColorMatrix image;
    boolean stop;
    int iterationCounter = 0;
    int [][] centroidSums;
    int [] clusterPointCounts;
    ColoredImagePoint temporalPoint;

    private List<List<ColoredImagePoint>> finalClusters;
    private final static double THRESHOLD = 3;
    private final static double ITERATION_NUMBER = 200;
    private final static int CLUSTERS_NUMBER = 50;
    private final static Logger logger = Logger.getLogger(Kmeans.class);

    public Kmeans(int numberOfClusters, BufferedImage img, List<Centroid> initCentroids) {
        /*clusters = new ArrayList<>(numberOfClusters);
        for (int i = 0; i < numberOfClusters; i ++) {
            clusters.add(new ArrayList<>());
        }*/
        image = new ColorMatrix(img);
        image.removeBackground();
        if (initCentroids != null && initCentroids.size() > 0) {
            centroids = initCentroids;
        } else {
            randomizeCentroids();
        }

        stop = false;
        centroidSums = new int[centroids.size()][];
        clusterPointCounts = new int[centroids.size()];
        clusterMap = new HashMap<String, Integer>(image.getHeight() * img.getWidth());
        temporalPoint = new ColoredImagePoint(0,0,124);
        logger.info("Inited kmeans");
    }

    private void randomizeCentroids() {
        centroids = new ArrayList<>(CLUSTERS_NUMBER);
        for (int i = 0; i < CLUSTERS_NUMBER; i ++) {
            Random r = new Random();
            int x = r.nextInt(image.getWidth());
            int y = r.nextInt(image.getHeight());
            Color c = image.getColor(x, y);
            centroids.add(new Centroid(x, y, c));
        }
    }

    public void runAlgorithm() {
        while (!stop && iterationCounter < ITERATION_NUMBER) {
            logger.info("Kmeans iteration: " + iterationCounter);
            /*prev = new ArrayList<>(centroids.size());
            prev.addAll(centroids.stream().map(c -> new Centroid(c.getX(), c.getY(), c.getColor())).collect(Collectors.toList()));*/
            runClusterization();
            stop = updateCentroids();
            //stop = compareCentroids(prev, centroids);
            iterationCounter ++;
        }
        buildList();
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
        /*clusters.clear();
        clusters = new ArrayList<>(centroids.size());
        for (int i = 0; i < centroids.size(); i ++) {
            clusters.add(new ArrayList<>());
        }*/
        for (int x = 0; x < image.getWidth(); x ++) {
            for (int y = 0; y < image.getHeight(); y ++) {
                temporalPoint.setX(x);
                temporalPoint.setY(y);
                temporalPoint.setColor(image.getColor(x, y));
                if (temporalPoint.getColor().equals(Color.GREEN)) {
                    clusterMap.put(x + "_" + y, -1);
                    continue;
                }
                double minDist = Double.MAX_VALUE;
                int minIndex = 0;
                int counter = 0;
                for (Centroid centroid : centroids) {
                    if (centroid.getColor().equals(Color.GREEN)) continue;
                    double currDist = getDistance(centroid, temporalPoint);
                    if (currDist < minDist) {
                        minDist = currDist;
                        minIndex = counter;
                    }
                    counter ++;
                }
                clusterMap.put(x + "_" + y, minIndex);
            }
        }
    }

    private boolean updateCentroids() {
        /*centroids.clear();
        centroids.addAll(clusters.stream().map(this::calcCentroid).collect(Collectors.toList()));*/
        /*for (List<ColoredImagePoint> cluster : clusters) {
            Centroid centroid = calcCentroid(cluster);
            centroid.cluster = cluster;
            centroids.add(centroid);
        }*/
        boolean res = true;
        for (int i = 0; i < CLUSTERS_NUMBER; i ++) {
            clusterPointCounts[i] = 0;
        }
        for (int x = 0; x < image.getWidth(); x ++) {
            for (int y = 0; y < image.getHeight(); y ++) {
                int c = clusterMap.get(x + "_" + y);
                if (c == -1) continue;
                if (centroidSums[c] == null) {
                    centroidSums[c] = new int[]{0, 0, 0, 0, 0};
                }
                Color color = image.getColor(x, y);
                centroidSums[c][0] += x;
                centroidSums[c][1] += y;
                centroidSums[c][2] += color.getRed();
                centroidSums[c][3] += color.getGreen();
                centroidSums[c][4] += color.getBlue();
                clusterPointCounts[c] ++;
            }
        }

        for (int i = 0; i < CLUSTERS_NUMBER; i ++) {
            if (clusterPointCounts[i] == 0 && centroidSums[i] == null) {
                centroidSums[i] = new int[5];
            }
            for (int j = 0; j < 5; j ++) {
                if (clusterPointCounts[i] != 0) {
                    centroidSums[i][j] /= clusterPointCounts[i];
                } else {
                    centroidSums[i][j] = 0;
                }
            }
            Centroid centroid = centroids.get(i);
            Color cc = centroid.getColor();
            if ( ( (centroid.getX() - centroidSums[i][0]) *  (centroid.getX() - centroidSums[i][0]) > THRESHOLD * THRESHOLD)
                    || ( (centroid.getY() - centroidSums[i][1]) *  (centroid.getY() - centroidSums[i][1]) > THRESHOLD * THRESHOLD)/*
                    || ( (cc.getRed() - centroidSums[i][2]) *  (cc.getRed() - centroidSums[i][2]) > THRESHOLD * THRESHOLD)
                    || ( (cc.getGreen() - centroidSums[i][3]) *  (cc.getGreen() - centroidSums[i][3]) > THRESHOLD * THRESHOLD)
                    || ( (cc.getBlue() - centroidSums[i][2]) *  (cc.getBlue() - centroidSums[i][2]) > THRESHOLD * THRESHOLD)*/) {
                res = false;
            }
            centroids.get(i).setX(centroidSums[i][0]);
            centroids.get(i).setY(centroidSums[i][1]);
            Color c = new Color(centroidSums[i][2], centroidSums[i][3], centroidSums[i][4]);
            centroids.get(i).setColor(c);
        }
        return res;
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
        double colorMultiplier = (image.getWidth() + image.getHeight()) / 2. / 128.;
        //logger.info("color multiplier: " + colorMultiplier);
        //logger.info("pxDist: " + pxDist + " colorDist: " + colorMultiplier * p1.getColorDistance(p2));
        double colorDist = p1.getColorDistance(p2) * colorMultiplier ;
        //double colorDist = 0;
        return pxDist + colorDist;
    }

    public BufferedImage getClusterized() {
        int rStep = 250 / centroids.size();
        int gStep = 250 / (centroids.size() * 2);
        int bStep = 250 / (centroids.size() * 3);
        if (clusterMap == null || centroids == null) return null;
        int r = 0, gr = 0, b = 0;
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < image.getWidth(); x ++) {
            for (int y = 0; y < image.getHeight(); y ++) {
                int cluster = clusterMap.get(x + "_" + y);
                Color color;
                if (cluster == -1) color = Color.GREEN;
                else {
                    color = new Color(rStep * cluster, gStep * cluster, bStep * cluster);
                }
                img.setRGB(x, y, color.getRGB());
            }
        }
        return img;
    }

    private void buildList() {
        finalClusters = new ArrayList<>(CLUSTERS_NUMBER);
        for (int i = 0; i < CLUSTERS_NUMBER; i ++) {
            finalClusters.add(new ArrayList<>());
        }
        for (int x = 0; x < image.getWidth(); x ++) {
            for (int y = 0; y < image.getHeight(); y ++) {
                int cl = clusterMap.get(x + "_" + y);
                if (cl == -1) continue;
                finalClusters.get(cl).add(new ColoredImagePoint(x, y, image.getColor(x, y)));
            }
        }
    }

    public void saveToImage(String name) {

        /*for (List<ColoredImagePoint> cluster : clusters) {
            *//*Random random = new Random();
            int r = random.nextInt(256);
            int gr = random.nextInt(256);
            int b = random.nextInt(256);*//*
            r += rStep;
            gr += gStep;
            b += bStep;
            Color c = new Color(r, gr, b);
            for (ColoredImagePoint p : cluster) {
                img.setRGB(p.getX(), p.getY(), c.getRGB());
            }
        }
        for (ColoredImagePoint x : centroids) {
            g.drawOval(x.getX(), x.getY(), 5, 5);
        }*/
        BufferedImage img = getClusterized();
        ImageProcessor processor = new ImageProcessor();
        processor.saveImage(img, "resources/clustering/" + name + ".png");
    }


    public List<List<ColoredImagePoint>> getFinalClusters() {
        return finalClusters;
    }

}
