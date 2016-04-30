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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Егор on 30.04.2016.
 */
public class ImageDrawer extends JPanel{
    private ImageProcessor imageProcessor;
    private ColorMatrix image;
    private BufferedImage bufferedImage;
    List<Centroid> centroids;
    List<List<ColoredImagePoint>> clusters;
    Kmeans kmeans;
    private static final Logger logger = Logger.getLogger(ImageDrawer.class);
    public ImageDrawer() {
        imageProcessor = new ImageProcessor();
        bufferedImage = imageProcessor.loadImage("resources/images/sheep1.png");
        image = new ColorMatrix(bufferedImage);
        centroids = new ArrayList<>();
        this.addMouseListener(new MyListener());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bufferedImage, 0, 0, null);

        if (clusters != null) {
            for (List<ColoredImagePoint> cluster : clusters) {
                Random r = new Random();
                Color color = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
                g.setColor(color);
                for (ColoredImagePoint c : cluster) {
                    g.drawOval(c.getX(), c.getY(), 1, 1);
                }
            }
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
                clusters = kmeans.getClusters();
            }
            centroids.add(new Centroid(e.getX(), e.getY(), image.getColor(e.getX(), e.getY())));
            repaint();
            logger.info("added centroid: " + e.getX() + e.getY());
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
}
