package edu.lapidus.rec3d;

import edu.lapidus.rec3d.depth.Homography;
import edu.lapidus.rec3d.math.Correspondence;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.PairCorrespData;
import edu.lapidus.rec3d.utils.SerializedDataType;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.helpers.Serializer;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by Егор on 21.11.2015.
 * This is going to be a main class to the program with
 */
public class TwoImageCalculator {
    public static void main(String ... args) {
        DoubleMatrix k1 = matrixBuilder.createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 1600, 1184);
        DoubleMatrix k2 = matrixBuilder.createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 1600, 1184);
        DoubleMatrix r1 = matrixBuilder.createRotationMatrix(0, MatrixBuilder.Y_AXIS);
        DoubleMatrix r2 = matrixBuilder.createRotationMatrix(30, MatrixBuilder.Y_AXIS);
        String img1 = "resources/flower1.png";
        String img2 = "resources/flower2.png";
        Vector c1 = new Vector(0.0, 0.0, 0.0);
        Vector c2 = new Vector(57., 0.0, 7.);
        TwoImageCalculator init = new TwoImageCalculator(k1, k2, r1, r2, c1, c2, img1, img2);
        init.init();
        init.run();
    }


    final static Logger logger = Logger.getLogger(TwoImageCalculator.class);
    static MatrixBuilder matrixBuilder;
    Correspondence correspondence;
    static ImageProcessor imageProcessor;
    Homography homography;
    DoubleMatrix k1, k2, r1, r2;
    Vector c1, c2;

    static {
        matrixBuilder = new MatrixBuilderImpl();
        imageProcessor = new ImageProcessor();
    }

    public DoubleMatrix getK2() {
        return k2;
    }

    public void setK2(DoubleMatrix k2) {
        this.k2 = k2;
    }

    public DoubleMatrix getR1() {
        return r1;
    }

    public void setR1(DoubleMatrix r1) {
        this.r1 = r1;
    }

    public DoubleMatrix getR2() {
        return r2;
    }

    public void setR2(DoubleMatrix r2) {
        this.r2 = r2;
    }

    public DoubleMatrix getK1() {
        return k1;
    }

    public void setK1(DoubleMatrix k1) {
        this.k1 = k1;
    }

    public Homography getHomography() {
        return homography;
    }

    public void setHomography(Homography homography) {
        this.homography = homography;
    }

    public Vector getC1() {
        return c1;
    }

    public void setC1(Vector c1) {
        this.c1 = c1;
    }

    public Vector getC2() {
        return c2;
    }

    public void setC2(Vector c2) {
        this.c2 = c2;
    }

    String img1Path;
    String img2Path;
    //TODO read it from properties
    private final static int THREAD_NUMBER = 20;

    Map<String, PairCorrespData> results;

    ArrayList<Thread> depthComputers;

    public TwoImageCalculator(Homography homography) {
        this.homography = homography;
    }

    public TwoImageCalculator(DoubleMatrix k1, DoubleMatrix k2, DoubleMatrix r1, DoubleMatrix r2, Vector c1, Vector c2, String img1Path, String img2Path) {
        this.c1 = c1;
        this.c2 = c2;
        this.img1Path = img1Path;
        this.img2Path = img2Path;
        logger.info("Starting pair calculation");
        homography = new Homography(k1, k2, r1, r2);
        this.k1 = k1;
        this.k2 = k2;
        this.r1 = r1;
        this.r2 = r2;

    }

    public void init() {
        logger.info("ENTERED INIT!!!");
        //matrixBuilder = new MatrixBuilderImpl();
        correspondence = new Correspondence();
        //imageProcessor = new ImageProcessor();
        results = new HashMap<String, PairCorrespData>();
    }
    public Map<String, PairCorrespData> run () {
        DoubleMatrix Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
        DoubleMatrix fundamentalMatrix = (DoubleMatrix) matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);
        logger.info("Calculated fundamental matrix: " + fundamentalMatrix.toString());
        Vector epipole = calculateEpipole();

        Map<String, PairCorrespData> result = new HashMap<String, PairCorrespData>();
        //TODO this is not good
        ColorMatrix[] images = loadImages();

        int linesPerThread = images[0].getHeight() / THREAD_NUMBER;
        Set<Lock> semaphore = new HashSet<Lock>(THREAD_NUMBER);
        int step = (images[0].getHeight() - 400) / THREAD_NUMBER;
        for (int i = 200; i < images[0].getHeight() - 200; i += step) {
            int yStart = i;
            int yEnd = i + step;
            if (yEnd > images[0].getHeight() - 200) {
                yEnd = images[0].getHeight() - 200;
            }
            Thread t = new Thread(new DepthRegionCalculator(homography, epipole, images[0], images[1], yStart, yEnd, fundamentalMatrix, result, semaphore));
            t.start();
        }

        while (!semaphore.isEmpty()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                logger.error("Error waiting threads to finish", e);
            }
        }

        try {
            //FileOutputStream fos = new FileOutputStream();
            FileWriter fw = new FileWriter(new File("result.txt"));
            for (Map.Entry<String, PairCorrespData> entry : result.entrySet()) {
                fw.write(entry.getKey() + "      " + entry.getValue().toString());
            }
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("Error writing result \n", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing result \n", e);
        }


        return result;

        //TODO after that we should somehow start initiation of threads, each will calc it own portion of lines
        //each calculator should find second point, find ro1 TODO try to think - if ro1 is equal for every pair of points - calculate it only once
        //after it has found second point and ro1 it can find depth.
        //TODO PairCorrespData - think what it should contain
        //The idea here - make a some structure - HashMap, which will contain coordinates on the first image as key and
        //everything else as value. This is required for calculators to be able to dump their results into single place
        //TODO Idea to the future - we can have a Set or a Map with depth points as Key to understand, that we've already calculated this point.

    }

    /**
     * TODO replace code with loading a tuple of images received from camera, for now it will load them from disk
     * @return
     */
    private ColorMatrix[] loadImages() {
        ColorMatrix[] res = new ColorMatrix[2];
        BufferedImage firstImage = imageProcessor.loadImage(img1Path);
        BufferedImage secondImage = imageProcessor.loadImage(img2Path);
        res[0] = new ColorMatrix(firstImage);
        res[1] = new ColorMatrix(secondImage);
        return res;
    }
    //TODO think how to implement this one, and how to get C1 and C2!
    private Vector calculateEpipole() {
        RealMatrix K2 = new Array2DRowRealMatrix(k2.getData());
        RealMatrix R2 = new Array2DRowRealMatrix(r2.getData());
        RealVector C1 = new ArrayRealVector(c1.getVec());
        RealVector C2 = new ArrayRealVector(c2.getVec());

        R2 = R2.transpose();
        C1 = C1.subtract(C2);

        RealMatrix tmp1 = K2.multiply(R2);
        RealVector result = tmp1.operate(C1);
        return new Vector(result.toArray());
    }

    private class DepthRegionCalculator implements Runnable {
        Homography homography;
        private Vector epipole;
        ColorMatrix img1;
        ColorMatrix img2;
        //Here we put lines which this specific calculator should compute;
        //I mean there will be several threads, each calculating its own set of lines
        int yStart;
        int yEnd;
        private DoubleMatrix fundamental;
        private Map<String, PairCorrespData> container;
        private Set<Lock> semaphore;

        DepthRegionCalculator(Homography homography,
                              Vector epipole,
                              ColorMatrix img1,
                              ColorMatrix img2,
                              int yStart, int yEnd,
                              DoubleMatrix fundamental,
                              Map<String, PairCorrespData> container,
                              Set<Lock> semaphore) {
            this.homography = homography;
            this.epipole = epipole;
            this.img1 = img1;
            this.img2 = img2;
            this.yStart = yStart;
            this.yEnd = yEnd;
            this.fundamental = fundamental;
            this.container = container;
            this.semaphore = semaphore;
        }

        public void run() {
            logger.info("Started thread for lines: " + yStart + " - " + yEnd);
            Lock lock = new Lock(yStart);
            semaphore.add(lock);
            //TODO elaborate this
            for (int i = yStart; i < yEnd; i ++) {
                //TODO it is not good to put things like -200, -100 etc. !!!! rework!!!
                for (int j = 200; j < img1.getWidth() - 200; j ++) {
                    int[] firstPoint = {j, i, 1};
                    int[] secondPoint = calcSecondPoint(firstPoint);
                    Vector M = calcDepth(firstPoint, secondPoint);
                    PairCorrespData res = new PairCorrespData();
                    res.setX1(j);
                    res.setY1(i);
                    res.setX2(secondPoint[0]);
                    res.setY2(secondPoint[1]);
                    res.setX(M.get(0));
                    res.setY(M.get(1));
                    res.setZ(M.get(2));
                    container.put(j + "_" + i, res);
                }
            }
            semaphore.remove(lock);
            logger.info("Finished thread for lines: " + yStart + " - " + yEnd);
        }

        /**
         * this method should calculate second point, given coordinates of the first one
         * it uses m^T * F * m = 0 equation
         * @param firstPoint - coordinates of the point on the first image
         * @return coordinates of the point on the second image
         */
        private int[] calcSecondPoint(int[] firstPoint) {
            //TODO implement this
            Vector first = new Vector(firstPoint);
            Vector coefficients = fundamental.postMultiply(first);
            //TODO it would be nice to use Gradient Descent here to find the most similar point from the second image!!!
            //TODO also think how to bound this -100 - + 100 thing, in case our line is almost horizontal it may cause issues
            int [] result = new int[3];
            result[2] = 1;
            result[0] = Integer.MIN_VALUE;
            result[1] = Integer.MIN_VALUE;
            double minDiff = Double.MAX_VALUE;
            for (int x2 = firstPoint[0] - 100; x2 < firstPoint[0] + 100; x2++) {
                int y2 = (int)((( - coefficients.get(0) * x2 - coefficients.get(2)) / coefficients.get(1)));
                if (y2 > img2.getHeight()) {
                    y2 = img2.getHeight() - 1;
                } else if (y2 < 0) {
                    y2 = 0;
                }
                double tmp = evaluateSimilarity(firstPoint, new int[] {x2, y2});
                if (tmp < minDiff) {
                    minDiff = tmp;
                    result[0] = x2;
                    result[1] = y2;
                }
            }
            return result;
        }

        private double evaluateSimilarity(int[] point1, int[] point2) {
            Color [] firstSample = getColorRegion(img1, point1);
            Color [] secondSample = getColorRegion(img2, point2);
            double meanDiff = 0;
            for (int i = 0; i < firstSample.length; i ++) {
                meanDiff += comparePixels(firstSample[i], secondSample[i]);
            }
            return meanDiff / firstSample.length;
        }

        /**
         * Gives the sum of squares of distances of colors
         * At this point it returns array of colors to compare
         * @param first - first point
         * @param second - second point
         * @return total score
         */
        private double comparePixels(Color first, Color second) {
            int redDiff = first.getRed() - second.getRed();
            int greenDiff = first.getGreen() - second.getGreen();
            int blueDiff = first.getBlue() - second.getBlue();

            return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;
        }

        /**
         * Get colors of nearby pixels
         * @param img image to search at
         * @param point - coordinates [0] - x, [1] - y
         * @return array of colors.
         */
        private Color[] getColorRegion(ColorMatrix img, int[] point) {
            Color [] res = new Color[5];
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

        /**
         * Calcs M from two points
         * @param firstPoint
         * @param secondPoint
         * @return M-vector
         */
        private Vector calcDepth(int[] firstPoint, int[] secondPoint) {
            //A * m1
            Vector z = homography.postMultiply(new Vector(firstPoint));
            //k2*r2^t
            DoubleMatrix f = k2.multiplyBy(r2.transpose());
            //C1 - C2 according to formula
            Vector s = c1.subtract(c2);
            //K2 * R2^t * (C1 - C2)
            Vector c = f.postMultiply(s);
            //See copybook :O
            double ro1 = (c.get(1) - c.get(2)*secondPoint[1]) / (z.get(2) * secondPoint[1] - z.get(1));

            Vector M = k1.inverse().postMultiply(new Vector(firstPoint)).scalar(ro1);

            return M;
        }

    }

    private class Lock {
        int id;
        Lock(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Lock lock = (Lock) o;

            return id == lock.id;

        }

        @Override
        public int hashCode() {
            Integer i = id;
            return i.hashCode();
        }
    }


}
