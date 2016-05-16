package edu.lapidus.rec3d.depth.threaded;

import edu.lapidus.rec3d.TwoImageCalculator;
import edu.lapidus.rec3d.depth.Homography;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 17.02.2016.
 */
public class DepthRegionCalculator implements Runnable {
    private final static Logger logger = Logger.getLogger(DepthRegionCalculator.class);
    private final static Object LOCK_OBJ = new Object();

    private final static int SECOND_POINT_LOOKUP_WIDTH = 120;
    private final static int SECOND_POINT_SHIFT = -20;

    private final static int COLOR_REGION_RADIUS = 10;
    //sheep0 - 1
    /*private final static int HEIGHT_DIFF_LIM = 3;//5
    private final static int WIDTH_DIFF_LIM = 30;
    private final static double HEIGHT_DIFF_WEIGHT = 500;
    private final static double WIDTH_DIFF_WEIGHT = 30;*/
    /*private final static int SECOND_POINT_LOOKUP_WIDTH = 240;
    private final static int SECOND_POINT_SHIFT = -40;

    private final static int COLOR_REGION_RADIUS = 5;
    private final static int HEIGHT_DIFF_LIM = 30;
    private final static double HEIGHT_DIFF_WEIGHT = 3;
    private final static double WIDTH_DIFF_WEIGHT = 50;*/
    //crane
    private final static int HEIGHT_DIFF_LIM = 5;//5
    private final static int WIDTH_DIFF_LIM = 3;
    private final static double HEIGHT_DIFF_WEIGHT = 500;
    private final static double WIDTH_DIFF_WEIGHT = 3;
    Homography homography;
    private Vector epipole;
    ColorMatrix img1;
    ColorMatrix img2;

    ArrayList<EpipolarLineHolder> lines;
    //Here we put lines which this specific calculator should compute;
    //I mean there will be several threads, each calculating its own set of lines
    int yStart;
    int yEnd;
    double modelScaleFactor;
    private DoubleMatrix fundamental;
    private Map<String, PairCorrespData> container;
    private Set<Lock> semaphore;

    private DoubleMatrix k1, k2, r1, r2;
    //private Vector c1, c2;
    //Number of points to skip when iterating over the images, like we process every Nth point
    private int skipNpoints = 1;

    public DepthRegionCalculator(Homography homography,
                                 Vector epipole,
                                 ColorMatrix img1,
                                 ColorMatrix img2,
                                 int yStart, int yEnd,
                                 DoubleMatrix fundamental,
                                 Map<String, PairCorrespData> container,
                                 Set<Lock> semaphore,
                                 double modelScaleFactor,
                                 ArrayList<EpipolarLineHolder> lines) {
        this.homography = homography;
        this.epipole = epipole;
        this.img1 = img1;
        this.img2 = img2;
        this.yStart = yStart;
        this.yEnd = yEnd;
        this.fundamental = fundamental;
        this.container = container;
        this.semaphore = semaphore;
        this.modelScaleFactor = modelScaleFactor;
        this.lines = lines;
        k1 = homography.getK1();
        k2 = homography.getK2();
        r1 = homography.getR1();
        r2 = homography.getR2();

    }

    public void run() {
        logger.info("Started thread for lines: " + yStart + " - " + yEnd);
        Lock lock = new Lock(yStart);
        synchronized (LOCK_OBJ) {
            semaphore.add(lock);
        }
        //TODO elaborate this
        for (int i = yStart; i < yEnd; i++) {
            for (int j = 0; j < img1.getWidth(); j += skipNpoints) {
                int[] firstPoint = {j, i, 1};
                if (img1.getColor(j, i).equals(Color.GREEN)) continue;

                int[] secondPoint = calcSecondPointAlongX(firstPoint);
                if (img2.getColor(secondPoint[0], secondPoint[1]).equals(Color.GREEN)) continue;
                Vector M = calcMetricDepthXXX(firstPoint, secondPoint);
                PairCorrespData res = new PairCorrespData();
                res.setX1(j);
                res.setY1(i);
                res.setX2(secondPoint[0]);
                res.setY2(secondPoint[1]);
                res.setX(M.get(0) * modelScaleFactor);
                res.setY(M.get(1) * modelScaleFactor);
                res.setZ(M.get(2) * modelScaleFactor);
                res.setColor(img2.getColor(secondPoint[0], secondPoint[1]));
                synchronized (LOCK_OBJ) {
                    container.put(j + "_" + i, res);
                }
            }
            /*if (i % 10 == 0) {
                logger.info("Thread #" + lock.getId() + " Processed up to line " + i);
            }*/
        }
        synchronized (LOCK_OBJ) {
            semaphore.remove(lock);
        }
        logger.info("#" + lock.getId() + " Finished thread for lines: " + yStart + " - " + yEnd + " Threads left: " + semaphore.size());
    }

    /**
     * this method should calculate second point, given coordinates of the first one
     * it uses m2^T * F * m1 = 0 equation
     *
     * @param firstPoint - coordinates of the point on the first image
     * @return coordinates of the point on the second image
     */
    private int[] calcSecondPoint(int[] firstPoint) {
        //TODO implement this
        Vector first = new Vector(firstPoint);
        Vector coefficients = fundamental.postMultiply(first);
        //TODO it would be nice to use Gradient Descent here to find the most similar point from the second image!!!
        //TODO also think how to bound this -100 - + 100 thing, in case our line is almost horizontal it may cause issues
        int[] result = new int[3];
        result[2] = 1;
        result[0] = Integer.MIN_VALUE;
        result[1] = Integer.MIN_VALUE;
        double minDiff = Double.MAX_VALUE;
        //for (int x2 = firstPoint[0] - 100; x2 < firstPoint[0] + 100; x2++) {
        int startY = firstPoint[1] - SECOND_POINT_LOOKUP_WIDTH / 2, endY = firstPoint[1] + SECOND_POINT_LOOKUP_WIDTH / 2;
        if (startY < 0) {
            startY = 0;
            endY = SECOND_POINT_LOOKUP_WIDTH;
        } else if (endY >= img1.getHeight()) {
            endY = img1.getHeight() - 1;
            startY = img1.getHeight() - SECOND_POINT_LOOKUP_WIDTH - 1;
        }
        for (int y2 = startY; y2 < endY; y2 ++) {
            /*int y2 = (int) (((-coefficients.get(0) * x2 - coefficients.get(2)) / coefficients.get(1)));
            if (y2 >= img2.getHeight()) {
                y2 = img2.getHeight() - 1;
            } else if (y2 < 0) {
                y2 = 0;
            }*/
            int x2 = (int) ( - ( (coefficients.get(1) * y2 + coefficients.get(2)) / coefficients.get(0) ) );
            if (x2 < 0) {
                x2 = 0;
            } else if (x2 >= img2.getHeight()) {
                x2 = img2.getHeight()-1;
            }
            double tmp = 1000;
            try {
                tmp = evaluateSimilarity(firstPoint, new int[]{x2, y2});
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error(String.format("Index out of bounds: %d : %d; %d : %d", firstPoint[0], firstPoint[1], x2, y2));
            }
            if (tmp < minDiff) {
                minDiff = tmp;
                result[0] = x2;
                result[1] = y2;
            }
        }
        return result;
    }

    private int[] calcSecondPointAlongX(int[] firstPoint) {
        Vector first = new Vector(firstPoint);
        Vector coefficients = fundamental.postMultiply(first);
        //TODO it would be nice to use Gradient Descent here to find the most similar point from the second image!!!
        //TODO also think how to bound this -100 - + 100 thing, in case our line is almost horizontal it may cause issues
        int[] result = new int[3];
        result[2] = 1;
        result[0] = Integer.MIN_VALUE;
        result[1] = Integer.MIN_VALUE;
        double minDiff = Double.MAX_VALUE;
        int widthClass = 0;
        /*if (firstPoint[0] >= 180 && firstPoint[0] < 510 ) {
            widthClass = 50;
        } else {
            widthClass = -20;
        }*/
        int startX = firstPoint[0] + SECOND_POINT_SHIFT - widthClass - (SECOND_POINT_LOOKUP_WIDTH / 2),
                endX = firstPoint[0] + SECOND_POINT_SHIFT - widthClass + (SECOND_POINT_LOOKUP_WIDTH / 2);
        if (startX < 0) {
            startX = 0;
            endX = SECOND_POINT_LOOKUP_WIDTH;
        } else if (endX >= img1.getWidth()) {
            endX = img1.getWidth();
            startX = img1.getWidth() - SECOND_POINT_LOOKUP_WIDTH - 1;
        }
        //TODO this is only for debugging, eats much resources
        EpipolarLineHolder TMP = new EpipolarLineHolder(firstPoint, coefficients.getVec());
        for (int x2 = startX; x2 < endX - 4; x2 += 4) {
            int y2 = (int)( ( - coefficients.get(2) - coefficients.get(0) * x2 )  / coefficients.get(1) );

            if (y2 < 0)
                y2 = 0;
            if (y2 >= img2.getHeight())
                y2 = img2.getHeight() - 1;
            TMP.addLinePoint(x2, y2);
            double tmp = 1000;
            try {
                tmp = evaluateSimilarityX(firstPoint, new int[]{x2, y2});
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error(String.format("Index out of bounds: %d : %d; %d : %d", firstPoint[0], firstPoint[1], x2, y2));
            }
            if (tmp < minDiff) {
                minDiff = tmp;
                result[0] = x2;
                result[1] = y2;
            }
        }
        TMP.setSecondPoint(result[0], result[1]);
        synchronized (LOCK_OBJ) {
            lines.add(TMP);
        }
        return result;
    }

    private double evaluateSimilarityX(int[] point1, int[] point2) {
        List<Color> firstSample = getColorRegionXX(img1, point1);
        List<Color> secondSample = getColorRegionXX(img2, point2);
        double meanDiff = 0;
        int xDiff = xDiff = (point1[0] - point2[0]) * (point1[0] - point2[0]);
        int yDiff = yDiff = (point1[1] - point2[1]) * (point1[1] - point2[1]);
        for (int i = 0; i < firstSample.size(); i ++) {
            meanDiff += comparePixels(firstSample.get(i), secondSample.get(i));
        }
        return Math.sqrt(meanDiff + xDiff + yDiff);

    }

    private double evaluateSimilarity(int[] point1, int[] point2) {
        Color[] firstSample = getColorRegionX(img1, point1);
        Color[] secondSample = getColorRegionX(img2, point2);
        double meanDiff = 0;
        for (int i = 0; i < firstSample.length; i++) {
            meanDiff += comparePixels(firstSample[i], secondSample[i]);
        }

        int heightDiff = point1[1] - point2[1];
        heightDiff = (heightDiff - HEIGHT_DIFF_LIM) * (heightDiff - HEIGHT_DIFF_LIM);
        int widthDiff = point1[0] - point2[0] - WIDTH_DIFF_LIM;; //TODO was - 30
        widthDiff *= widthDiff * WIDTH_DIFF_WEIGHT;
        //logger.info("Mean diff: " + meanDiff + " height diff: " + heightDiff);
        meanDiff += heightDiff * HEIGHT_DIFF_WEIGHT + widthDiff;
        return Math.sqrt(meanDiff) / (2 * (firstSample.length + 1));
    }

    /**
     * Gives the sum of squares of distances of colors
     * At this point it returns array of colors to compare
     *
     * @param first  - first point
     * @param second - second point
     * @return total score
     */
    private double comparePixels(Color first, Color second) {
        int redDiff = first.getRed() - second.getRed();
        int greenDiff = first.getGreen() - second.getGreen();
        int blueDiff = first.getBlue() - second.getBlue();

        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;
    }

    //private double compareRegions(Color[] )

    /**
     * Get colors of nearby pixels
     *
     * @param img   image to search at
     * @param point - coordinates [0] - x, [1] - y
     * @return array of colors.
     */
    private Color[] getColorRegion(ColorMatrix img, int[] point) {
        Color[] res = new Color[5];
        /*TODO think how to handle corners
        if (point[0] < 2)
            res[1] = img.getRGB(point[0], point[1]);
        if (point[1] < 2)
            res[4]*/
        int xLeft, xRight, yUp, yDown;
        if (point[0] - 1 < 0) {
            xLeft = point[0];
        } else {
            xLeft = point[0] - 1;
        }
        if (point[0] + 1 >= img.getWidth()) {
            xRight = point[0];
        } else {
            xRight = point[0] + 1;
        }
        if (point[1] - 1 < 0) {
            yUp = point[1];
        } else {
            yUp = point[1] - 1;
        }
        if (point[1] + 1 >= img.getHeight()) {
            yDown = point[1];
        } else {
            yDown = point[1] + 1;
        }
        res[0] = img.getColor(point[0], point[1]);
        res[1] = img.getColor(xLeft, yUp);
        res[2] = img.getColor(xRight, yUp);
        res[3] = img.getColor(xRight, yDown);
        res[4] = img.getColor(xLeft, yDown);
        return res;
    }

    private Color[] getColorRegionX(ColorMatrix img, int[] point) {
        Color[] res = new Color[COLOR_REGION_RADIUS * 4];
        for (int i = 1, j = 0; i <= COLOR_REGION_RADIUS; i ++, j += 4) {
            //TODO GOVNOKOD INITIATED
            //Here we take colors from image in cross pattern
            Color c = null;
            try {
                c = img.getColor(point[0] - i, point[1]);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j] = c;
            try {
                c = img.getColor(point[0], point[1] - i);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j + 1] = c;
            try {
                c = img.getColor(point[0] + i, point[1]);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j + 2] = c;
            try {
                c = img.getColor(point[0], point[1] + i);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j + 3] = c;
        }
        return res;
    }

    private List<Color> getColorRegionXX(ColorMatrix m, int[] p) {
        List<Color> res = new ArrayList<>();
        int px = p[0];
        int py = p[1];
        int startX = px - COLOR_REGION_RADIUS;
        int startY = py - COLOR_REGION_RADIUS;
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;
        int endX = startX + 2 * COLOR_REGION_RADIUS;
        int endY = startY + 2 * COLOR_REGION_RADIUS;
        if (endX >= m.getWidth()) {
            endX = m.getWidth() - 1;
            startX = m.getWidth() - 1 - 2 * COLOR_REGION_RADIUS;
        }
        if (endY >= m.getHeight()) {
            endY = m.getHeight() - 1;
            startY = m.getHeight() - 1 - 2 * COLOR_REGION_RADIUS;
        }
        for (int x = startX; x < endX; x += 2){
            for (int y = startY; y < endY; y += 2) {
                res.add(m.getColor(x, y));
            }
        }
        return res;
    }

    /**
     * Calcs M from two points
     * HOPEFULLY WE WON'T NEED THIS
     * @param firstPoint
     * @param secondPoint
     * @return M-vector
     */
    /*private Vector calcDepth(int[] firstPoint, int[] secondPoint) {
        //A * m1
        Vector z = homography.postMultiply(new Vector(firstPoint));
        //k2*r2^t
        DoubleMatrix f = k2.multiplyBy(r2.transpose());
        //C1 - C2 according to formula
        Vector s = c1.subtract(c2);
        //K2 * R2^t * (C1 - C2)
        Vector c = f.postMultiply(s);
        //See copybook :O
        double ro1 = (c.get(1) - c.get(2) * secondPoint[1]) / (z.get(2) * secondPoint[1] - z.get(1));

        Vector M = k1.inverse().postMultiply(new Vector(firstPoint)).scalar(ro1);

        return M;
    }*/

    private Vector calcMetricDepth(int [] firstPoint, int[] secondPoint) {
        Vector A = homography.postMultiply(new Vector(firstPoint));
        //TODO What should we do if we have 0 at secondPoints[1] - we will divide by zero
        if (secondPoint[1] == 0) {
            secondPoint[1] = 1;
        }
        double ro1 = ( (epipole.get(1) / secondPoint[1]) - epipole.get(2) ) / ( 1 - ( A.get(1) / secondPoint[1] ) );

        Vector M = k1.inverse().postMultiply(new Vector(firstPoint)).scalar(ro1);

        return M;
    }

    private Vector calcMetricDepthX(int[] firstPoint, int[] secondPoint) {
        DoubleMatrix a = k2.multiplyBy(r2.transpose()).multiplyBy(r1);
        a = a.inverse();
        Vector b = a.postMultiply(new Vector(secondPoint));
        Vector c = k1.inverse().postMultiply(new Vector(firstPoint));
        Vector d = a.postMultiply(epipole);

        double ro1 = ( d.get(0) * b.get(1) - d.get(1) * b.get(0) ) / ( c.get(1) * b.get(0) + c.get(0) * b.get(1) );

        double ro2 = ( d.get(1) + ro1 * c.get(1) ) / b.get(1);

        Vector M = k1.inverse().postMultiply(new Vector(firstPoint)).scalar(ro1);

        Vector M1 = a.postMultiply(new Vector(secondPoint)).scalar(ro2).subtract(a.postMultiply(epipole));
        //Vector M = a.postMultiply(new Vector(secondPoint)).scalar(ro2).subtract(epipole);
        return M;

    }

    private Vector calcMetricDepthXX(int[] firstPoint, int [] secondPoint) {
        Vector m1 = new Vector(firstPoint);
        Vector m2 = new Vector(secondPoint);
        Vector b = homography.postMultiply(m1);
        double ro1 = ( epipole.get(1) * m2.get(0) - epipole.get(0) * m2.get(1) ) / ( b.get(0) * m2.get(1) - b.get(1) * m2.get(0));

        Vector M = k1.inverse().postMultiply(m1).scalar(ro1);
        return M;
    }

    private Vector calcMetricDepthXXX(int [] firstPoint, int[] secondPoint) {
        Vector m1 = new Vector(firstPoint);
        Vector m2 = new Vector(secondPoint);
        Vector a = homography.postMultiply(m1);
        double ro1 = ( epipole.get(1) - ( epipole.get(0) * m2.get(1) / m2.get(0) ) ) / ( (a.get(0) * m2.get(1) / m2.get(0)) - a.get(1) );

        Vector M = k1.inverse().postMultiply(m1).scalar(ro1);
        return M;
    }

    public DepthRegionCalculator setSkipNpoints(int skipNpoints) {
        this.skipNpoints = skipNpoints;
        return this;
    }
}
