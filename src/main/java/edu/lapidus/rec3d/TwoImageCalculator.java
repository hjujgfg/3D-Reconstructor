package edu.lapidus.rec3d;

import edu.lapidus.rec3d.depth.Homography;
import edu.lapidus.rec3d.math.Correspondence;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Егор on 21.11.2015.
 * This is going to be a main class to the program with
 */
public class TwoImageCalculator {
    public static void main(String ... args) {
        TwoImageCalculator init = new TwoImageCalculator();
        init.init();
        init.run();
    }


    final static Logger logger = Logger.getLogger(TwoImageCalculator.class);
    MatrixBuilder matrixBuilder;
    Correspondence correspondence;
    ImageProcessor imageProcessor;
    Homography homography;
    DoubleMatrix k1, k2, r1, r2;
    String img1Path;
    String img2Path;

    ArrayList<Thread> depthComputers;

    public TwoImageCalculator(Homography homography) {
        this.homography = homography;
    }

    public TwoImageCalculator(DoubleMatrix k1, DoubleMatrix k2, DoubleMatrix r1, DoubleMatrix r2, String img1Path, String img2Path) {
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
        matrixBuilder = new MatrixBuilderImpl();
        correspondence = new Correspondence();
        imageProcessor = new ImageProcessor();
    }
    public void run () {
        DoubleMatrix Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
        DoubleMatrix fundamentalMatrix = (DoubleMatrix) matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);
        logger.info("Calculated fundamental matrix: " + fundamentalMatrix.toString());
        Vector epipole = calculateEpipole();
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
        RealMatrix K2 = new Array2DRowRealMatrix(k2);
        RealMatrix R2 = new Array2DRowRealMatrix(r2);
        RealVector C1 = new ArrayRealVector(c1);
        RealVector C2 = new ArrayRealVector(c2);

        R2 = R2.transpose();
        C1 = C1.subtract(C2);

        RealMatrix tmp1 = K2.multiply(R2);
        RealVector result = tmp1.operate(C1);
        return result.toArray();
    }

    private class DepthRegionCalculator implements Runnable {
        Homography homography;
        ColorMatrix img1;
        ColorMatrix img2;
        //Here we put lines which this specific calculator should compute;
        //I mean there will be several threads, each calculating its own set of lines
        int yStart;
        int yEnd;

        DepthRegionCalculator(Homography homography, ColorMatrix img1, ColorMatrix img2, int yStart, int yEnd) {
            this.homography = homography;
            this.img1 = img1;
            this.img2 = img2;
            this.yStart = yStart;
            this.yEnd = yEnd;
        }

        public void run() {

        }
    }


}
