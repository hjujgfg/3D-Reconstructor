package edu.lapidus.rec3d.machinelearning.kmeans.forms;

import edu.lapidus.rec3d.exceptions.FileLoadingException;
import edu.lapidus.rec3d.machinelearning.kmeans.ClusterComparator;
import edu.lapidus.rec3d.machinelearning.kmeans.CorrespondenceHolder;
import edu.lapidus.rec3d.machinelearning.kmeans.Kmeans;
import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 10.05.2016.
 */
public class KmeansDoublePanel extends JPanel {
    private static final Logger logger = Logger.getLogger(KmeansDoublePanel.class);
    public KmeansDoublePanel() throws FileLoadingException {
        logger.info("Entered building panel");
        BufferedImage b1 = imageProcessor.loadImage(img0Path);
        BufferedImage b2 = imageProcessor.loadImage(img1Path);
        /*b1 = imageProcessor.removeGreen(b1);
        b2 = imageProcessor.removeGreen(b2);
        b1 = imageProcessor.toGrayScale(b1);
        b2 = imageProcessor.toGrayScale(b2);*/
        combined = imageProcessor.buildCombined(img0Path, img1Path);
        k1 = new Kmeans(NUM_OF_CLUSTERS, b1, null);
        k1.runAlgorithm();
        k1.saveToImage("Form1");
        k2 = new Kmeans(NUM_OF_CLUSTERS, b2, k1.getCentroids());
        k2.runAlgorithm();
        k2.saveToImage("Form2");
        comparator = new ClusterComparator(b1, b2, k1.getFinalClusters(), k2.getFinalClusters(), k1.getClusterMap());
        addMouseListener(new LocalMouseListener());
    }

    private final static String img0Path = "output/images/sheep0.png";
    private final static String img1Path = "output/images/sheep2.png";
    private final static int NUM_OF_CLUSTERS = 50;
    private ImageProcessor imageProcessor = new ImageProcessor();
    private BufferedImage combined;
    private Kmeans k1, k2;
    private List<CorrespondenceHolder> points = new ArrayList<>();

    private ClusterComparator comparator;
    int state = 1;

    private class LocalMouseListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
            int y = e.getY();
            int x = e.getX();
            int color = combined.getRGB(x, y);
            if (e.getX() >= combined.getWidth() / 2) {
                return;
            }
            if (e.getX() < 10 && e.getY() < 10) {
                state = 2;
                points = comparator.getRandomCorrespondences();
                imageProcessor.saveCorrClusters(img0Path, img1Path, k1.getCentroids(), k2.getCentroids());
                imageProcessor.saveCorrespsByKmeans(img0Path, img1Path, points);
                repaint();
                return;
            }
            logger.info("Looking for correspondence");
            ColoredImagePoint p = new ColoredImagePoint(e.getX(), e.getY(), color);
            ColoredImagePoint p2 = comparator.getSpecificCorrespondence(p);
            points.add(new CorrespondenceHolder(p, p2, 0.));
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

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(combined, 0,0, null);

        Color color = Color.BLACK;
        for (CorrespondenceHolder c : points) {
            g.setColor(color);
            ColoredImagePoint a = c.get(0);
            ColoredImagePoint b = c.get(1);
            g.drawLine(a.getX() - 2, a.getY(), a.getX() + 2, a.getY() + 2);
            g.drawLine(a.getX() - 2, a.getY() + 2, a.getX() - 2, a.getY() + 2);
            g.drawLine(b.getX() + (combined.getWidth()/2) - 2, b.getY(), b.getX() + (combined.getWidth()/2) + 2, b.getY() + 2);
            g.drawLine(b.getX() + (combined.getWidth()/2) - 2, b.getY() + 2, b.getX() + (combined.getWidth()/2) - 2, b.getY() + 2);
            g.drawLine(a.getX(), a.getY(), b.getX() + combined.getWidth() / 2, b.getY());
            if (state == 2) {
                color = color.brighter();
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(combined.getWidth(), combined.getHeight());
    }
}
