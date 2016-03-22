package edu.lapidus.rec3d;

import edu.lapidus.rec3d.math.*;
import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
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
        img1 = imgProcessor.loadImage("resources/cup1.png");
        img2 = imgProcessor.loadImage("resources/cup2.png");
        combined = new BufferedImage(img1.getWidth() + img2.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.createGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, img1.getWidth(), 0, null);
        imgProcessor.saveImage(combined, "resources/COMBINED.png");
        addMouseListener(new MyMouseListener(this));
    }

    public void setParent(JFrame f) {
        parent = f;
    }

    int width, height;
    ImageProcessor imgProcessor;
    BufferedImage img1, img2;
    BufferedImage combined;
    Map<Point, Point> points = new HashMap<Point, Point>();
    Point start, end;


    class MyMouseListener implements MouseListener {
        MyPanel pn;

        MyMouseListener(MyPanel p) {
            pn = p;
        }
        private boolean in = false;
        public void mouseClicked(MouseEvent e) {

            if (e.getX() < 10 && e.getY() < 10) {
                savePointsToCsv();
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

    @Override
    protected void paintComponent(Graphics g){

        super.paintComponent(g);
        g.drawImage(combined, 0, 0, null);
        for(Map.Entry<Point, Point> entry : points.entrySet()) {
            g.drawLine((int)entry.getKey().x, (int)entry.getKey().y, (int)entry.getValue().x, (int)entry.getValue().y);
            Random r = new Random();
            g.setColor(new Color(r.nextInt(1000000)));
        }
        if (start != null) {
            g.drawString("X", (int) start.x, (int) start.y);
        }
        logger.debug("Painted s,thng");
    }

    public Dimension getPreferredSize() {
        return new Dimension(combined.getWidth(), combined.getHeight());
    }

    private void savePointsToCsv() {
        File csv = new File("resources/points.csv");
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
