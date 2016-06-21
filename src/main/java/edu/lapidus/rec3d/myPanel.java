package edu.lapidus.rec3d;

import edu.lapidus.rec3d.StaticsForTests.BigStatic;
import edu.lapidus.rec3d.depth.threaded.EpipolarLineHolder;
import edu.lapidus.rec3d.math.*;
import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Егор on 25.02.2016.
 */
public class MyPanel extends JPanel {
    private final static Logger logger = Logger.getLogger(MyPanel.class);
    private JFrame parent;

    public MyPanel() {
        imgProcessor = new ImageProcessor();
        img1 = imgProcessor.loadImage("resources/images/" + POINTS_NAME + COUNTER +".png");
        img2 = imgProcessor.loadImage("resources/images/" + POINTS_NAME + (COUNTER + 1) + ".png");
        combined = new BufferedImage(img1.getWidth() + img2.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.createGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, img1.getWidth(), 0, null);
        addMouseMotionListener(new MyMouseMoutionListener());
        if (state == 1) {
            addMouseListener(new MyMouseListener(this));
        } else {
            addMouseListener(new MyEpipMouseListener());
            i1 = new ColorMatrix(img1);
            i2 = new ColorMatrix(img2);
            MatrixBuilderImpl matrixBuilder = new MatrixBuilderImpl();
            Correspondence correspondence = new Correspondence(CORRESP_LOCATION);
            DoubleMatrix Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
            fundamentalMatrix = (DoubleMatrix) matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);
            fundamentalMatrix.scale(-1);
            Vector e = BigStatic.calculateEpipoleFromFundamental(fundamentalMatrix);

            logger.info("fundamental: " + fundamentalMatrix);
        }
    }

    public void setParent(JFrame f) {
        parent = f;
    }

    private final static int COUNTER = 0;

    int width, height;
    ImageProcessor imgProcessor;
    BufferedImage img1, img2;
    BufferedImage combined;
    Map<Point, Point> points = new HashMap<Point, Point>();
    Point start, end;
    Color penColor = new Color(5, 255, 0);
    static final String POINTS_NAME = "sheep";
    //I know
    public static int state = 1;
    boolean isValid;
    String mouse ="";
    ArrayList<int[]> left = new ArrayList<int[]>();
    ArrayList<int[]> right = new ArrayList<int[]>();
    ArrayList<Vector> coeffs = new ArrayList<Vector>();
    ArrayList<EpipolarLineHolder> lines = new ArrayList<EpipolarLineHolder>();
    ColorMatrix i1, i2;
    DoubleMatrix fundamentalMatrix;
    static final String CORRESP_LOCATION = "resources/correspondences/" + POINTS_NAME + COUNTER + ".csv";

    private class MyMouseListener implements MouseListener {
        MyPanel pn;

        MyMouseListener(MyPanel p) {
            pn = p;
        }
        private boolean in = false;
        public void mouseClicked(MouseEvent e) {

            if (e.getX() < 10 && e.getY() < 10) {
                savePointsToCsv(POINTS_NAME + COUNTER);
                imgProcessor.saveImage(combined, "resources/COMBINED.png");
                parent.dispose();
            }

            if (!in) {
                start = new Point(e.getX(), e.getY());
                in = true;
                logger.info("in");
            } else {
                end = new Point(e.getX(), e.getY());
                points.put(start, end);
                start = null;
                end = null;
                in = false;
                logger.debug("out");
            }
            repaint();
        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }
    }

    private class MyEpipMouseListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            if (e.getX() < 10 && e.getY() < 10) {
                //savePointsToCsv(POINTS_NAME);
                imgProcessor.saveImage(combined, "resources/COMBINED.png");
                parent.dispose();
            }
            int [] first = new int[] {e.getX(), e.getY(), 1};
            int [] second = BigStatic.calcSecondPointAlongX(first, fundamentalMatrix, i1, i2, coeffs, lines);
            left.add(first);
            second[0] += i1.getWidth();
            logger.info(first[0] + " : " + first[1] + "; " + second[0] + " : " + second[1]);
            right.add(second);
            isValid = false;
            Random r = new Random();
            repaint();
        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }
    }

    private class MyMouseMoutionListener implements MouseMotionListener {

        public void mouseDragged(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {
            mouse = e.getX() + " : " + e.getY();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g){

        super.paintComponent(g);
        g.drawImage(combined, 0, 0, null);
        if (isValid) {
            g.drawString(mouse, 10, 10);
            if (points != null) {
                g.drawString(points.size() + "", 10, 20);
            }
        }
        isValid = true;
        g.setColor(penColor);
        if (state == 1) {
            for (Map.Entry<Point, Point> entry : points.entrySet()) {
                g.drawLine((int) entry.getKey().x, (int) entry.getKey().y, (int) entry.getValue().x, (int) entry.getValue().y);
                /*Random r = new Random();
                g.setColor(new Color(r.nextInt(1000000)));*/
            }
            if (start != null) {
                g.drawLine((int) start.x - 5, (int) start.y - 5, (int) start.x + 5, (int) start.y + 5);
                g.drawLine((int) start.x + 5, (int) start.y - 5, (int) start.x - 5, (int) start.y + 5);
            }
            logger.debug("Painted s,thng");
        } else {
            int s = 5;
            for (int[] p : left) {
                g.drawLine(p[0] - s, p[1] - s, p[0] + s, p[1] + s);
                g.drawLine(p[0] + s, p[1] - s, p[0] - s, p[1] + s);
            }
            int i = 0;
            for (int[] p : right) {
                g.drawLine(p[0] - s, p[1] - s, p[0] + s, p[1] + s);
                g.drawLine(p[0] + s, p[1] - s, p[0] - s, p[1] + s);
                /*Vector c = coeffs.get(i);
                int x1 = i1.getWidth();
                int y1 = (int) (- c.get(2) / c.get(1));
                int x2 = i2.getWidth();
                int y2 = (int) ( (-c.get(2) - c.get(0) * x2) / c.get(1) );
                g.drawLine(x1, y1, x2 + i1.getWidth(), y2);
                i++;*/
            }
            for (EpipolarLineHolder h : lines) {
                for (int[] x : h.getLine()) {
                    g.drawOval(x[0] + i1.getWidth(), x[1], 1, 1);
                }
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(combined.getWidth(), combined.getHeight());
    }

    private void savePointsToCsv(String name) {
        File csv = new File("resources/correspondences/" + name + ".csv");
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csv)));
            for (Map.Entry<Point, Point> entry : points.entrySet()) {
                int x1 = (int) entry.getKey().x;
                int y1 = (int) entry.getKey().y;
                int x2 = ((int) entry.getValue().x) - img1.getWidth();
                int y2 = (int) entry.getValue().y;
                bw.write(x1 + "; " + y1 + "; " + x2 + "; " + y2 + "\n");
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
