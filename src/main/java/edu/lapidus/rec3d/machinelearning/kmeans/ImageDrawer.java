package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Егор on 30.04.2016.
 */
public class ImageDrawer extends JPanel{
    private ImageProcessor imageProcessor;
    private BufferedImage image;
    private BufferedImage clusterized;
    //private BufferedImage bufferedImage;
    List<Centroid> centroids;
    List<List<ColoredImagePoint>> clusters;
    Kmeans kmeans;
    private final static String img0Path = "resources/images/sheep0.png";
    private final static String img1Path = "resources/images/sheep1.png";
    private static final Logger logger = Logger.getLogger(ImageDrawer.class);
    public ImageDrawer() {
        imageProcessor = new ImageProcessor();
        image = imageProcessor.loadImage(img0Path);

        /*image = new ColorMatrix(bufferedImage);
        image.removeBackground();*/
        centroids = new ArrayList<>();
        this.addMouseListener(new MyListener());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (clusterized != null) {
            g.drawImage(clusterized, 0, 0, null);
        } else {
            g.drawImage(image, 0, 0, null);
        }
        if (centroids != null) {
            for (ColoredImagePoint c : centroids) {
                g.setColor(Color.BLACK);
                g.drawLine(c.getX() - 5, c.getY() - 5, c.getX() + 5, c.getY() + 5);
                g.drawLine(c.getX() + 5, c.getY() - 5, c.getX() - 5, c.getY() + 5);
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    class MyListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getY() < 10 && e.getX() < 10) {
                kmeans = new Kmeans(centroids.size(), image, centroids);
                kmeans.runAlgorithm();
                centroids = kmeans.getCentroids();
                kmeans.saveToImage("firstCluster");
                List<Centroid> l1 = new ArrayList<>(centroids.size());
                l1.addAll(centroids.stream().map(c -> new Centroid(c.getX(), c.getY(), c.getColor())).collect(Collectors.toList()));
                logger.info("Starting second image");
                BufferedImage img2 = imageProcessor.loadImage(img1Path);
                Kmeans second = new Kmeans(centroids.size(), img2, centroids);
                second.runAlgorithm();
                second.saveToImage("secondCluster");
                saveCentroids(l1, second.getCentroids());
                ClusterComparator comparator = new ClusterComparator(image, img2, kmeans.getFinalClusters(), second.getFinalClusters());
                imageProcessor.saveCorrespsByKmeans(img0Path, img1Path, comparator.compareImages());
                imageProcessor.saveCorrClusters(img0Path, img1Path, l1, second.getCentroids());
                clusterized = kmeans.getClusterized();
            } else {
                centroids.add(new Centroid(e.getX(), e.getY(), image.getRGB(e.getX(), e.getY())));
                logger.info("added centroid: " + e.getX() + e.getY());
            }
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    private static void saveCentroids(List<Centroid> l1, List<Centroid> l2) {
        if (l1.size() != l2.size()) {
            logger.info("Error saving centroids... ");
            return;
        }
        File f = new File("Resources/clustering/centroids.txt");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < l1.size(); i ++) {
                ColoredImagePoint tmp  = l1.get(i);
                bw.write(tmp.getX() + " " + tmp.getY() + " " + tmp.getColor().getRGB() + " ");
                tmp = l2.get(i);
                bw.write(tmp.getX() + " " + tmp.getY() + " " + tmp.getColor().getRGB() + "\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
