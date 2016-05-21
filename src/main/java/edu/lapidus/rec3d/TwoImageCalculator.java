package edu.lapidus.rec3d;

import edu.lapidus.rec3d.depth.CorrespondenceNormalizer;
import edu.lapidus.rec3d.depth.Homography;
import edu.lapidus.rec3d.depth.threaded.DepthRegionCalculator;
import edu.lapidus.rec3d.depth.threaded.EpipolarLineHolder;
import edu.lapidus.rec3d.depth.threaded.Lock;
import edu.lapidus.rec3d.machinelearning.kmeans.ClusterComparator;
import edu.lapidus.rec3d.machinelearning.kmeans.CorrespondenceHolder;
import edu.lapidus.rec3d.machinelearning.kmeans.Kmeans;
import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.Correspondence;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.PairCorrespData;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.image.ImageScanner;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;

import edu.lapidus.rec3d.visualization.VRML.VRMLPointSetGenerator;
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
        //sheep01
        DoubleMatrix k1 = matrixBuilder.createCalibrationMatrix(692, 519, 400, 300);
        DoubleMatrix k2 = matrixBuilder.createCalibrationMatrix(692, 519, 400, 300);
        /*DoubleMatrix k1 = matrixBuilder.createCalibrationMatrix(1000, 700, 400, 300);
        DoubleMatrix k2 = matrixBuilder.createCalibrationMatrix(1000, 700, 400, 300);*/
        DoubleMatrix r1 = matrixBuilder.createRotationMatrix(0, MatrixBuilder.Y_AXIS);
        DoubleMatrix r2 = matrixBuilder.createRotationMatrix(-9, MatrixBuilder.Y_AXIS);
        r2 = r2.multiplyBy(matrixBuilder.createRotationMatrix(-0.9, MatrixBuilder.X_AXIS));

        String img1 = "resources/images/sheep0.png";
        String img2 = "resources/images/sheep1.png";
        /*Vector c1 = new Vector(0.0, 0.0, 0.0);
        Vector c2 = new Vector(57., 0.0, 7.);*/
        //TwoImageCalculator init = new TwoImageCalculator(k1, k2, r1, r2, img1, img2, "resources/kMeansCorrespondences/sheep0.csv", 1);
        TwoImageCalculator init = new TwoImageCalculator(k1, k2, r1, r2, img1, img2, null, TwoImageCalculator.KMEANS_AND_CONVOLVE_SOURCE, 1);
        Map<String, PairCorrespData> res = init.run();
        VRMLPointSetGenerator generator = new VRMLPointSetGenerator(res, VRMLPointSetGenerator.State.SINGLE);
        generator.buildPointSet();
        //imageProcessor.createDepthMap(res);
    }

    final static int FILE_CORRESPS_SOURCE = 1;
    final static int KMEANS_CORREPS_SOURCE = 2;
    final static int CONVOLVE_CORRESPS_SOURCE = 3;
    final static int KMEANS_AND_CONVOLVE_SOURCE = 4;

    final static Logger logger = Logger.getLogger(TwoImageCalculator.class);
    static MatrixBuilderImpl matrixBuilder;
    Correspondence correspondence;
    static ImageProcessor imageProcessor;
    Homography homography;
    CorrespondenceNormalizer normalizer;
    DoubleMatrix k1, k2, r1, r2;
    Vector epipole;

    ArrayList<EpipolarLineHolder> lines = new ArrayList<EpipolarLineHolder>();

    static {
        matrixBuilder = new MatrixBuilderImpl();
        imageProcessor = new ImageProcessor();
    }

    private String img1Path;
    private String img2Path;
    private BufferedImage firstImage;
    private BufferedImage secondImage;
    private double modelScaleFactor;

    private List<CorrespondenceHolder> kMeansCorrespondences;
    private Map<ColoredImagePoint, ColoredImagePoint> convolveCorrespondences;
    private String fileCorrespondencesPath;
    private DoubleMatrix Amatrix;

    //TODO read it from properties
    private final static int THREAD_NUMBER = 20;
    private final static int NUMBER_OF_CLUSTERS = 50;

    ArrayList<Thread> depthComputers;

    public TwoImageCalculator(Homography homography) {
        this.homography = homography;
    }

    public TwoImageCalculator(DoubleMatrix k1, DoubleMatrix k2, DoubleMatrix r1, DoubleMatrix r2, String img1Path, String img2Path, String correspsFile, int correspondenceType, double modelScaleFactor) {
        this.img1Path = img1Path;
        this.img2Path = img2Path;
        firstImage = imageProcessor.loadImage(img1Path);
        secondImage = imageProcessor.loadImage(img2Path);
        fileCorrespondencesPath = correspsFile;
        buildAMatrix(correspondenceType);
        this.modelScaleFactor = modelScaleFactor;
        logger.info("Starting pair calculation");
        homography = new Homography(k1, k2, r1, r2);
        this.k1 = k1;
        this.k2 = k2;
        this.r1 = r1;
        this.r2 = r2;
    }

    private void buildAMatrix(int correspondenceSource) throws IllegalArgumentException {
        switch (correspondenceSource) {
            case FILE_CORRESPS_SOURCE:
                logger.info("Starting loading correspondences from file: " + fileCorrespondencesPath);
                buildFileCorrespondences();
                Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
                break;
            case KMEANS_CORREPS_SOURCE:
                logger.info("Started building correspondences by Kmeans");
                buildKmeansCorrespondences();
                Amatrix = matrixBuilder.createAMatrix(kMeansCorrespondences);
                normalizer = new CorrespondenceNormalizer(kMeansCorrespondences);
                break;
            case CONVOLVE_CORRESPS_SOURCE:
                logger.info("Started building correspondences by convolve");
                buildConvolveCorrespondences();
                Amatrix = matrixBuilder.createAMatrix(convolveCorrespondences);
                normalizer = new CorrespondenceNormalizer(convolveCorrespondences);
                break;
            case KMEANS_AND_CONVOLVE_SOURCE:
                logger.info("!!!Starting both kmeans and convolve!!!");
                buildKmeansAndConvolveCorrespondences();
                Amatrix = matrixBuilder.createAMatrix(convolveCorrespondences, kMeansCorrespondences);
                normalizer = new CorrespondenceNormalizer(kMeansCorrespondences, convolveCorrespondences);
                break;
        }
    }

    private void buildKmeansCorrespondences() {
        Kmeans kmeans1 = new Kmeans(NUMBER_OF_CLUSTERS, imageProcessor.loadImage(img1Path), null);
        kmeans1.runAlgorithm();
        Kmeans kmeans2 = new Kmeans(NUMBER_OF_CLUSTERS, imageProcessor.loadImage(img2Path), kmeans1.getCentroids());
        kmeans2.runAlgorithm();
        ClusterComparator comparator = new ClusterComparator(firstImage, secondImage, kmeans1.getFinalClusters(), kmeans2.getFinalClusters(), kmeans1.getClusterMap());
        kMeansCorrespondences = comparator.getRandomCorrespondences();
        imageProcessor.saveCorrespsByKmeans(img1Path, img2Path, kMeansCorrespondences);
        kmeans1.saveToImage("TwoImg1");
        kmeans2.saveToImage("TwoImg2");
        imageProcessor.saveCorrClusters(img1Path, img2Path, kmeans1.getCentroids(), kmeans2.getCentroids());
    }

    private void buildConvolveCorrespondences() {
        ImageScanner scanner = new ImageScanner(img1Path, img2Path);
        scanner.run();
        convolveCorrespondences = scanner.getCorrespondences();
    }

    private void buildFileCorrespondences() {
        if (fileCorrespondencesPath == null || fileCorrespondencesPath.isEmpty())
            throw new IllegalArgumentException("Correspondence path is empty or null, but was specified as source");
        correspondence = new Correspondence(fileCorrespondencesPath);
    }

    private void buildKmeansAndConvolveCorrespondences() {
        buildConvolveCorrespondences();
        buildKmeansCorrespondences();
    }

    /*public void init() {
        logger.info("ENTERED INIT!!!");
        //matrixBuilder = new MatrixBuilderImpl();

        //imageProcessor = new ImageProcessor();

    }*/
    public Map<String, PairCorrespData> run () {

        DoubleMatrix fundamentalMatrix2 = matrixBuilder.buildFundamental(Amatrix);
        logger.info("Found fundamental without normalization: " + fundamentalMatrix2);
        //DoubleMatrix fundamentalMatrix2 = matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);

        //fundamentalMatrix2.scale(-1);

        DoubleMatrix fundamentalMatrix = normalizer.normalizeAndCalculateF();

        //DoubleMatrix fundamentalMatrix = matrixBuilder.buildFundamental(Amatrix);
        logger.info("Calculated fundamental matrix: " + fundamentalMatrix.toString());
        boolean success = calculateEpipoleFromFundamental(fundamentalMatrix);
        //success = false;
        if (!success){
            logger.info("attempting another epipole");
            calculateEpipoleFromFundamental(fundamentalMatrix2);
            //TODO we calculate EPIPOLE from one matrix, BUT later use another FUNDAMENTAL - fixed not tested
            fundamentalMatrix = fundamentalMatrix2;
        }

        Map<String, PairCorrespData> result = new ConcurrentHashMap<String, PairCorrespData>();

        ColorMatrix[] images = loadImages();

        for (ColorMatrix c : images) {
            c.removeBackground();
        }

        ImageScanner scanner = new ImageScanner(img1Path, img2Path);
        scanner.initCorrespondenceChecker();
        int linesPerThread = images[0].getHeight() / THREAD_NUMBER;
        Set<Lock> semaphore = new HashSet<Lock>(THREAD_NUMBER);
        int step = (images[0].getHeight()) / THREAD_NUMBER;
        for (int i = 0; i < images[0].getHeight(); i += step) {
            int yStart = i;
            int yEnd = i + step;
            if (yEnd > images[0].getHeight()) {
                yEnd = images[0].getHeight();
            }
            //TODO REMOVE lines parameter!!!!
            Thread t = new Thread(new DepthRegionCalculator(homography,
                    epipole,
                    images[0],
                    images[1],
                    yStart,
                    yEnd,
                    fundamentalMatrix,
                    result,
                    semaphore,
                    modelScaleFactor, lines, scanner).setSkipNpoints(1));
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

        /*VRMLPointSetGenerator pointSet = new VRMLPointSetGenerator(result);

        pointSet.buildPointSet();*/

        imageProcessor.visualizeCorresps(result.values(), img1Path, img2Path, 20);

        imageProcessor.visualizeEpipolarLines(lines, img1Path, img2Path, 20);

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
        res[0] = new ColorMatrix(firstImage);
        res[1] = new ColorMatrix(secondImage);
        return res;
    }
    //TODO think how to implement this one, and how to get C1 and C2!
    //This should be unnecessary
/*    private Vector calculateEpipole() {
        RealMatrix K2 = new Array2DRowRealMatrix(k2.getData());
        RealMatrix R2 = new Array2DRowRealMatrix(r2.getData());
        RealVector C1 = new ArrayRealVector(c1.getVec());
        RealVector C2 = new ArrayRealVector(c2.getVec());

        R2 = R2.transpose();
        C1 = C1.subtract(C2);

        RealMatrix tmp1 = K2.multiply(R2);
        RealVector result = tmp1.operate(C1);
        return new Vector(result.toArray());
    }*/

    private boolean calculateEpipoleFromFundamental(DoubleMatrix fund) {
        //SingularValueDecomposition svd = fund.transpose().SVD();
        SingularValueDecomposition svd = fund.SVD();
        RealMatrix v = svd.getV();
        Vector e = new Vector(v.getColumn(2));
        e = e.scalar( 1 / (e.get(2)  ));
        logger.info("epipole : " + e);
        boolean correctEpipole = true;
        for (double d : e.getVec()) {
            if (!Double.isFinite(d)) {
                logger.error("Incorrect epipole!!!");
                correctEpipole = false;
                break;
            }
        }
        epipole = e;
        //return new Vector(v.getColumn(v.getColumnDimension() - 1));
        return correctEpipole;
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
}
