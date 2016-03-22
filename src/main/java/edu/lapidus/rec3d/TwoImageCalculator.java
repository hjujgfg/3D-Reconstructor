package edu.lapidus.rec3d;

import edu.lapidus.rec3d.depth.Homography;
import edu.lapidus.rec3d.depth.threaded.DepthRegionCalculator;
import edu.lapidus.rec3d.depth.threaded.EpipolarLineHolder;
import edu.lapidus.rec3d.depth.threaded.Lock;
import edu.lapidus.rec3d.math.Correspondence;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.PairCorrespData;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;

import edu.lapidus.rec3d.visualization.VRML.VRMLData;
import edu.lapidus.rec3d.visualization.VRML.VRMLPointSetGenerator;
import edu.lapidus.rec3d.visualization.VRML.VRMLTriangulator;
import edu.lapidus.rec3d.visualization.XYZformatter;
import org.apache.commons.math3.linear.*;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Егор on 21.11.2015.
 * This is going to be a main class to the program with
 */
public class TwoImageCalculator {
    public static void main(String ... args) {
        //DoubleMatrix k1 = matrixBuilder.createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 1600, 1184);
        //DoubleMatrix k2 = matrixBuilder.createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 1600, 1184);
        DoubleMatrix k1 = matrixBuilder.createCalibrationMatrix(425, 425, 400, 300);
        DoubleMatrix k2 = matrixBuilder.createCalibrationMatrix(425, 425, 400, 300);
        DoubleMatrix r1 = matrixBuilder.createRotationMatrix(0, MatrixBuilder.Y_AXIS);
        DoubleMatrix r2 = matrixBuilder.createRotationMatrix(15, MatrixBuilder.Y_AXIS);
        String img1 = "resources/cup1.png";
        String img2 = "resources/cup2.png";
        Vector c1 = new Vector(0.0, 0.0, 0.0);
        Vector c2 = new Vector(57., 0.0, 7.);
        TwoImageCalculator init = new TwoImageCalculator(k1, k2, r1, r2, c1, c2, img1, img2);
        init.init();
        Map<String, PairCorrespData> res = init.run();
        imageProcessor.createDepthMap(res);
    }


    final static Logger logger = Logger.getLogger(TwoImageCalculator.class);
    static MatrixBuilder matrixBuilder;
    Correspondence correspondence;
    static ImageProcessor imageProcessor;
    Homography homography;
    DoubleMatrix k1, k2, r1, r2;
    Vector c1, c2;

    ArrayList<EpipolarLineHolder> lines = new ArrayList<EpipolarLineHolder>();

    static {
        matrixBuilder = new MatrixBuilderImpl();
        imageProcessor = new ImageProcessor();
    }

    String img1Path;
    String img2Path;
    //TODO read it from properties
    private final static int THREAD_NUMBER = 10;

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
        results = new TreeMap<String, PairCorrespData>();
    }
    public Map<String, PairCorrespData> run () {
        DoubleMatrix Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
        DoubleMatrix fundamentalMatrix = (DoubleMatrix) matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);
        fundamentalMatrix.scale(-1);
        //DoubleMatrix fundamentalMatrix = matrixBuilder.buildFundamental(Amatrix);
        logger.info("Calculated fundamental matrix: " + fundamentalMatrix.toString());
        Vector epipole = calculateEpipoleFromFundamental(fundamentalMatrix);

        Map<String, PairCorrespData> result = new ConcurrentHashMap<String, PairCorrespData>();
        //TODO this is not good
        ColorMatrix[] images = loadImages();

        int linesPerThread = images[0].getHeight() / THREAD_NUMBER;
        Set<Lock> semaphore = new HashSet<Lock>(THREAD_NUMBER);
        int step = (images[0].getHeight() - 40) / THREAD_NUMBER;
        for (int i = 0; i < images[0].getHeight(); i += step) {
            int yStart = i;
            int yEnd = i + step;
            if (yEnd > images[0].getHeight()) {
                yEnd = images[0].getHeight();
            }
            //TODO REMOVE lines parameter!!!!
            Thread t = new Thread(new DepthRegionCalculator(homography, c1, c2, epipole, images[0], images[1], yStart, yEnd, fundamentalMatrix, result, semaphore, lines).setSkipNpoints(1));
            t.start();
        }
        try {
            Thread.sleep(1000);
            logger.info("Waiting a sec to init semaphore");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Waiting threads to finish");
        while (!semaphore.isEmpty()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                logger.error("Error waiting threads to finish", e);
            }
        }

        try {
            //FileOutputStream fos = new FileOutputStream();
            logger.info("Writing results");
            Map<String, PairCorrespData> sorted = new TreeMap<String, PairCorrespData>(result);
            FileWriter fw = new FileWriter(new File("resources/res/result.txt"));
            for (Map.Entry<String, PairCorrespData> entry : sorted.entrySet()) {
                fw.write(entry.getKey() + "      " + entry.getValue().toString());
            }
            fw.flush();
            fw.close();
            logger.info("Done writing results");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("Error writing result \n", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing result \n", e);
        }

        /*VRMLTriangulator triangulator = new VRMLTriangulator(result, images[0].getWidth(), images[1].getHeight());

        VRMLData vrml = triangulator.triangulate();

        vrml.saveWrl();*/

        /*XYZformatter xyz = new XYZformatter(result);

        xyz.saveXYZ();*/

        VRMLPointSetGenerator pointSet = new VRMLPointSetGenerator(result);

        pointSet.buildPointSet();

        imageProcessor.visualizeCorresps(result.values(), img1Path, img2Path, 20);

        imageProcessor.visualizeEpipolarLines(lines, img1Path, img2Path, 10);

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

    private Vector calculateEpipoleFromFundamental(DoubleMatrix fund) {
        SingularValueDecomposition svd = fund.SVD();
        RealMatrix v = svd.getV();
        Vector e = new Vector(v.getColumn(2));
        e = e.scalar(1/(e.get(2) * 1000));
        logger.info("epipole : " + e);
        //return new Vector(v.getColumn(v.getColumnDimension() - 1));
        return e;
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

}
