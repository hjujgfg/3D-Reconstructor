package edu.lapidus.rec3d.utils.image;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.*;
import edu.lapidus.rec3d.math.vector.Vector;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.nio.Buffer;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 12.05.2016.
 */
public class ImageScanner {

    private static ImageProcessor processor = new ImageProcessor();
    private static final Logger logger = Logger.getLogger(ImageScanner.class);
    private final static String IMG1 = "output/images/sheep0.png";
    private final static String IMG2 = "output/images/sheep1.png";
    private final static int FILTER_SIZE = 15;
    private final static int WINDOW_SIDE = 20;
    private final static int CORRESPONDENCE_COUNT = 10;

    BufferedImage img1, img2;
    String img1Path, img2Path;

    Map<ColoredImagePoint, Double> det1, det2;

    Map<ColoredImagePoint, ColoredImagePoint> correspondences;
    List<BufferedImage> processed;


    public static void main(String[] args) {
        ImageScanner scanner = new ImageScanner(IMG1, IMG2);
        scanner.run();
    }

    public ImageScanner (String i1, String i2) {
        img1Path = i1;
        img2Path = i2;
        img1 = processor.loadImage(i1);
        img2 = processor.loadImage(i2);
        img1 = processor.removeGreen(img1);
        img2 = processor.removeGreen(img2);
        img1 = processor.toGrayScale(img1);
        img2 = processor.toGrayScale(img2);
        processor.saveImage(img1, "output/convolve/test.png");
        det1 = new HashMap<>(img1.getHeight() * img1.getWidth());
        det2 = new HashMap<>(img1.getHeight() * img1.getWidth());
    }

    public void run() {
        List<BufferedImage> images = new ArrayList<>(6);
        images.addAll(applyGaussianFilters(img1, FILTER_SIZE));
        images.addAll(applyGaussianFilters(img2, FILTER_SIZE));
        int i = 0;
        /*for (BufferedImage b : images) {
            processor.saveImage(b, "output/convolve/scanner" + i++ +".png");
        }*/
        /*det1 = evaluatePoints(images.subList(0, 3));
        det2 = evaluatePoints(images.subList(3, 6));*/

        //calculateDeterminants(images);
        /*
        saveToImg(det1, "afterGauss1");
        saveToImg(det2, "afterGauss2");*/

        det1 = evaluatePoints(images.subList(0, 3));
        correspondences = compareImages(images, mapToSortedList(det1));
        saveToCombined(correspondences);
        /*saveTopPoints(img1, det1, "topPoints1");
        saveTopPoints(img2, det2, "topPoints2");*/
    }

    private void saveTopPoints(BufferedImage img, Map<ColoredImagePoint, Double> map, String name) {
        List<Map.Entry<ColoredImagePoint, Double>> list = sortMap(map).subList(0, 200);
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        Graphics g = res.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.setColor(Color.BLACK);
        for (Map.Entry<ColoredImagePoint, Double> m : list) {
            int x = m.getKey().getX();
            int y = m.getKey().getY();
            g.drawLine(x-2, y-2, x+2, y+2);
            g.drawLine(x+2, y-2, x-2, y+2);
        }
        processor.saveImage(res, "output/convolve/" + name + ".png");
    }

    private void saveToCombined(Map<ColoredImagePoint, ColoredImagePoint> map) {
        BufferedImage combined = processor.buildCombined(img1Path, img2Path);
        Graphics g = combined.createGraphics();
        Random r = new Random();
        for (Map.Entry<ColoredImagePoint, ColoredImagePoint> entry : map.entrySet()) {
            ColoredImagePoint p1 = entry.getKey();
            ColoredImagePoint p2 = entry.getValue();
            g.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
            g.drawLine(p1.getX(), p1.getY(), p2.getX() + combined.getWidth()/2, p2.getY());
            g.drawOval(p1.getX() - 2, p1.getY() - 2, 4, 4);
            g.drawOval(p2.getX() - 2 + combined.getWidth() / 2, p2.getY() - 2, 4, 4);
        }
        processor.saveImage(combined, "output/convolve/corresps.png");
    }


    private void saveToImg(Map<String, Double> map, String name) {
        BufferedImage img = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img.getHeight(); y ++) {
            for (int x = 0; x < img.getWidth(); x ++) {
                int c = map.get(x + "_" + y).intValue();
                img.setRGB(x, y, new Color(c, c, c).getRGB());
            }
        }
        processor.saveImage(img, "output/convolve/" + name + ".png");
    }

    private void calculateDeterminants(List<BufferedImage> imgs) {
        int [] colors = new int[6];
        for (int y = 0; y < img1.getHeight(); y ++) {
            for (int x = 0; x < img1.getWidth(); x ++) {
                int i = 0;
                for (BufferedImage b : imgs) {
                    Color color = new Color(b.getRGB(x, y));
                    colors[i ++ ] = color.getRed();
                }
                double mult = 0.9;
                double d1 = colors[0] * colors[2] - (mult * colors[1]) * (mult * colors[1]);
                double d2 = colors[3] * colors[5] - (mult * colors[4]) * (mult * colors[4]);
                det1.put(new ColoredImagePoint(x, y), d1);
                det2.put(new ColoredImagePoint(x, y), d2);
            }
        }
    }

    private List<Map.Entry<ColoredImagePoint, Double>> sortMap(Map<ColoredImagePoint, Double> map) {
        List<Map.Entry<ColoredImagePoint, Double>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<ColoredImagePoint, Double>>() {
            @Override
            public int compare(Map.Entry<ColoredImagePoint, Double> o1, Map.Entry<ColoredImagePoint, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        return list;
    }

    private List<ColoredImagePoint> mapToSortedList(Map<ColoredImagePoint, Double> map) {
        List<Map.Entry<ColoredImagePoint, Double>> list = sortMap(map);
        List<ColoredImagePoint> results = new ArrayList<>(list.size());
        for (Map.Entry<ColoredImagePoint, Double> e : list) {
            results.add(e.getKey());
        }
        return results;
    }


    private List<BufferedImage> applyGaussianFilters(BufferedImage img, int size) {
        List<BufferedImage> res = new ArrayList<>(3);
        Kernel[] kernels = {KernelFactory.buildXXGaussianKernel(size), KernelFactory.buildXYGaussianKernel(size), KernelFactory.buildYYGaussianKernel(size)};
        for (int i = 0; i < 3; i++) {
            BufferedImage tmp = processor.applyKernel(img, kernels[i]);
            //processor.saveImage(tmp, "output/convolve/applied" + i + "" + FILTER_SIZE + ".png");
            res.add(tmp);
            /*NonMaxSuppression suppression = new NonMaxSuppression();
            int [] imgArr = processor.grayToIntArray(tmp);
            suppression.init(imgArr, img.getWidth(), img.getHeight());
            int[] resArr = suppression.process();
            res.add(processor.intArrToImg(resArr, img.getWidth(), img.getHeight()));*/
        }
        //only kernels
        /*res.add(processor.applyKernel(img, KernelFactory.buildXXGaussianKernel(size)));
        res.add(processor.applyKernel(img, KernelFactory.buildXYGaussianKernel(size)));
        res.add(processor.applyKernel(img, KernelFactory.buildYYGaussianKernel(size)));*/
        return res;
    }

    private int[] getRegion(BufferedImage img, int x, int y) {
        int startX = x - 4;
        int startY = y - 4;
        if (startX < 0) {
            startX = 0;
        }
        if (startY < 0) {
            startY = 0;
        }
        int endX = startX + 9;
        int endY = startY + 9;
        if (endX >= img.getWidth()) {
            endX = img.getWidth() - 1;
            startX = img.getWidth() - 1 - 9;
        }
        if (endY >= img.getHeight()) {
            endY = img.getHeight() - 1;
            startY = img.getHeight() - 1 - 9;
        }

        return null;
    }

    private Map<ColoredImagePoint, ColoredImagePoint> compareImages(List<BufferedImage> filtered, List<ColoredImagePoint> points) {
        int topPointCount = CORRESPONDENCE_COUNT > points.size() ? points.size() : CORRESPONDENCE_COUNT;
        Map<ColoredImagePoint, ColoredImagePoint> result = new HashMap<>();
        Random r = new Random(points.size());
        for (int i = 0; i < CORRESPONDENCE_COUNT ; i += 1) {
            logger.info("Processing point " + i + " out of " + topPointCount);
            ColoredImagePoint curr = points.get(100 + r.nextInt(points.size()/10));
            //ColoredImagePoint curr = points.get(i);
            List<Window> current = getWindow(filtered.subList(0, 3), curr.getX(), curr.getY());
            if (current == null) continue;
            double minDist = Double.MAX_VALUE;
            ColoredImagePoint candidate = new ColoredImagePoint(0, 0);
            for (int y = curr.getY() - 10; y < curr.getY() + 10; y ++) {
                if (y < 0 || y > filtered.get(0).getHeight() - WINDOW_SIDE) {
                    continue;
                }
                for (int x = curr.getX() - 50; x < curr.getX() + 50; x ++) {
                    if (x < 0 || x > filtered.get(0).getWidth() - WINDOW_SIDE) {
                        continue;
                    }
                    List<Window> second = getWindow(filtered.subList(3, 6), x, y);
                    if (second == null) continue;
                    double weight = compareWindowLists(current, second);
                    if (weight < minDist) {
                        minDist = weight;
                        candidate.setX(x);
                        candidate.setY(y);
                    }
                }
            }
            result.put(curr, candidate);
        }
        return result;
    }

    private List<Window> getWindow(List<BufferedImage> imgs, int x, int y) {
        if (x + WINDOW_SIDE >= imgs.get(0).getWidth() || y + WINDOW_SIDE >= imgs.get(0).getHeight()) return null;
        List<Window> res = new ArrayList<>(3);
        for (BufferedImage img : imgs) {
            Window w = new Window();
            for (int iy = y; iy < y + WINDOW_SIDE; iy ++) {
                for (int ix = x; ix < x + WINDOW_SIDE; ix ++) {
                    w.addColor(new Color(img.getRGB(ix, iy)).getRed());
                }
            }
            res.add(w);
        }
        return res;
    }

    private double compareWindowLists (List<Window> w1, List<Window> w2) {
        double res = 0;
        for (int i = 0; i < 3; i ++) {
            res += compareWindows(w1.get(i), w2.get(i));
        }
        return Math.sqrt(res);
    }

    private double compareWindows (Window w1, Window w2) {
        Vector v1 = new Vector(w1.colors);
        Vector v2 = new Vector(w2.colors);
        Vector res = v1.subtract(v2);
        return res.multiply(res);
    }


    private Map<ColoredImagePoint, Double> evaluatePoints(List<BufferedImage> filtered) {
        double dist;
        Map<ColoredImagePoint, Double> res = new HashMap<>();
        //TODO do we need this -200 ??? when we have normalization
        for(int y = 0; y < filtered.get(0).getHeight()/* - 200*/; y ++) {
            for (int x = 0; x < filtered.get(0).getWidth(); x ++) {
                dist = 1;
                if (new Color(filtered.get(0).getRGB(x, y)).getRed() == 0) continue;
                for (BufferedImage img : filtered) {
                    dist *= new Color(img.getRGB(x, y)).getRed();
                }
                res.put(new ColoredImagePoint(x, y), dist);
            }
        }
        return res;
    }

    public void initCorrespondenceChecker() {
        processed = new ArrayList<>(6);
        processed.addAll(applyGaussianFilters(img1, FILTER_SIZE));
        processed.addAll(applyGaussianFilters(img2, FILTER_SIZE));
    }

    public double comparePoints(int x1, int y1, int x2, int y2) {
        List<Window> w1 = getWindow(processed.subList(0, 3), x1, y1);
        List<Window> w2 = getWindow(processed.subList(3, 6), x2, y2);
        return compareWindowLists(w1, w2);
    }

    public Map<ColoredImagePoint, ColoredImagePoint> getCorrespondences() {
        return correspondences;
    }

    /*class Pair{
        Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int x, y;
    }*/

    private class Window {
        int [] colors = new int[WINDOW_SIDE * WINDOW_SIDE];
        int current = 0;

        void addColor(int val) {
            colors[current++] = val;
        }
    }
}
