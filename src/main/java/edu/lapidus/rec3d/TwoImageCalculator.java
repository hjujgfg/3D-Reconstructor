package edu.lapidus.rec3d;

import edu.lapidus.rec3d.depth.CorrespondenceNormalizer;
import edu.lapidus.rec3d.depth.Homography;
import edu.lapidus.rec3d.depth.threaded.DepthRegionCalculator;
import edu.lapidus.rec3d.depth.threaded.EpipolarLineHolder;
import edu.lapidus.rec3d.depth.threaded.Lock;
import edu.lapidus.rec3d.exceptions.FileLoadingException;
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

import static java.lang.Math.PI;
import static java.lang.Math.max;

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
        /*DoubleMatrix k1 = matrixBuilder.createCalibrationMatrix(700, 500, 400, 300);
        DoubleMatrix k2 = matrixBuilder.createCalibrationMatrix(700, 500, 400, 300);*/
        DoubleMatrix r1 = matrixBuilder.createRotationMatrix(0, MatrixBuilder.Z_AXIS)
                .multiplyBy(matrixBuilder.createRotationMatrix(0, MatrixBuilder.Y_AXIS)
                .multiplyBy(matrixBuilder.createRotationMatrix(0, MatrixBuilder.X_AXIS)));
        DoubleMatrix r2 = matrixBuilder.createRotationMatrix(0, MatrixBuilder.Z_AXIS)
                .multiplyBy(matrixBuilder.createRotationMatrix(-9, MatrixBuilder.Y_AXIS))
                .multiplyBy(matrixBuilder.createRotationMatrix(0, MatrixBuilder.X_AXIS));

        String img1 = "input/images/" + args[1];
        String img2 = "input/images/" + args[2];
        /*Vector c1 = new Vector(0.0, 0.0, 0.0);
        Vector c2 = new Vector(57., 0.0, 7.);*/
        //TwoImageCalculator init = new TwoImageCalculator(k1, k2, r1, r2, img1, img2, "output/kMeansCorrespondences/sheep0.csv", 1);
        TwoImageCalculator init = new TwoImageCalculator(k1, k2, r1, r2, img1, img2, "input/correspondences/sheep0.csv", CorrespondenceLookupType.CONVOLVE_CORRESPS_SOURCE, 1);
        Map<String, PairCorrespData> res = init.run();
        VRMLPointSetGenerator generator = new VRMLPointSetGenerator(res, VRMLPointSetGenerator.State.SINGLE);
        generator.buildPointSet();
        //imageProcessor.createDepthMap(res);
    }

    public enum CorrespondenceLookupType {
        FILE_CORRESPS_SOURCE,
        KMEANS_CORRESPS_SOURCE,
        CONVOLVE_CORRESPS_SOURCE,
        KMEANS_AND_CONVOLVE_SOURCE
    }

    private final static Logger logger = Logger.getLogger(TwoImageCalculator.class);
    private static MatrixBuilderImpl matrixBuilder;
    private Correspondence correspondence;
    private static ImageProcessor imageProcessor;
    private Homography homography;
    private DoubleMatrix fundamentalMatrix;
    private CorrespondenceNormalizer normalizer;
    private DoubleMatrix k1, k2, r1, r2;
    private Vector epipole;

    private final static double[] NormalizedModelShift = new double[] {0, 100, 0};
    private final static double NormalizedModelHeight = 100;
    private PairCorrespData maximalPair = new PairCorrespData();
    private PairCorrespData minimumPair = new PairCorrespData();

    private ArrayList<EpipolarLineHolder> lines = new ArrayList<EpipolarLineHolder>();

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
    private List<CorrespondenceHolder> convolveCorrespondences;
    private String fileCorrespondencesPath;
    private DoubleMatrix Amatrix;

    //TODO read it from properties
    private final static int THREAD_NUMBER = 20;
    private final static int NUMBER_OF_CLUSTERS = 50;

    ArrayList<Thread> depthComputers;

    public TwoImageCalculator(Homography homography) {
        this.homography = homography;
    }

    /**
     * Public constructor
     * @param k1 - calibration matrix for the first image
     * @param k2 - calibration matrix for the second image
     * @param r1 - rotation matrix of the first image
     * @param r2 - rotation matrix of the second image, if it is null then an attempt to extract it
     *           from Fundamental will be performed
     * @param img1Path - path to the first image location
     * @param img2Path - path to the second image location
     * @param correspsFile - path to location of the file containing user picked correspondences
     * @param correspondenceType - type of the correspondence lookup method
     * @param modelScaleFactor - scale factor - multiplier to change size of the onstructed model
     */
    public TwoImageCalculator(DoubleMatrix k1, DoubleMatrix k2, DoubleMatrix r1, DoubleMatrix r2, String img1Path, String img2Path, String correspsFile, CorrespondenceLookupType correspondenceType, double modelScaleFactor) {
        this.img1Path = img1Path;
        this.img2Path = img2Path;
        this.k1 = k1;
        this.k2 = k2;
        this.r1 = r1;
        this.r2 = r2;
        try {
            firstImage = imageProcessor.loadImage(img1Path);
            secondImage = imageProcessor.loadImage(img2Path);
        } catch (FileLoadingException ex) {
            logger.error("can't open files", ex);
            System.exit(1);
        }
        fileCorrespondencesPath = correspsFile;
        buildAMatrix(correspondenceType);
        buildFundamental();
        if (r2 == null) {
            this.r2 = extractRotationMatrix(buildEssentialMatrix());
        }
        logger.info("R1: " + this.r1);
        getRotationAngles(this.r1);
        logger.info("R2: " + this.r2);
        getRotationAngles(this.r2);
        this.modelScaleFactor = modelScaleFactor;
        logger.info("Starting pair calculation");
        homography = new Homography(this.k1, this.k2, this.r1, this.r2);
        maximalPair.setX1(firstImage.getWidth());
        maximalPair.setX2(secondImage.getWidth());
        maximalPair.setY1(firstImage.getHeight());
        maximalPair.setY2(secondImage.getHeight());
        maximalPair.setX(0);
        maximalPair.setY(0);
        maximalPair.setZ(0);
        minimumPair.setX1(0);
        minimumPair.setX2(0);
        minimumPair.setY1(0);
        minimumPair.setY2(0);
        minimumPair.setX(0);
        minimumPair.setY(0);
        minimumPair.setZ(0);
    }

    /**
     * Calculates Fundamental matrix
     */
    private void buildFundamental() {
        DoubleMatrix fundamentalMatrix2 = matrixBuilder.buildFundamental(Amatrix);
        logger.info("Found fundamental without normalization: " + fundamentalMatrix2);
        //DoubleMatrix fundamentalMatrix2 = matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);

        //fundamentalMatrix2.scale(-1);

        fundamentalMatrix = normalizer.normalizeAndCalculateF();

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
    }

    /**
     * Builds special matrix required for creation of the Fundamental matrix
     * @param correspondenceSource - source for correspondences: whether correspondences should be computed via algorithms
     *                             or they are given as user picked located in special file
     * @throws IllegalArgumentException
     */
    private void buildAMatrix(CorrespondenceLookupType correspondenceSource) throws IllegalArgumentException {
        switch (correspondenceSource) {
            case FILE_CORRESPS_SOURCE:
                logger.info("Starting loading correspondences from file: " + fileCorrespondencesPath);
                try {
                    buildFileCorrespondences();
                } catch (FileLoadingException e) {
                    logger.error("Error loading file correspondences, exiting!!!", e);
                    System.exit(1);
                }
                Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
                normalizer = new CorrespondenceNormalizer(correspondence.getInititalCorrespondences());
                break;
            case KMEANS_CORRESPS_SOURCE:
                logger.info("Started building correspondences by Kmeans");
                try {
                    buildKmeansCorrespondences();
                } catch (FileLoadingException e) {
                    logger.error("Error loading Kmeans correspondences, exiting!!!", e);
                    System.exit(1);
                }
                Amatrix = matrixBuilder.createAMatrix(kMeansCorrespondences);
                normalizer = new CorrespondenceNormalizer(kMeansCorrespondences);
                break;
            case CONVOLVE_CORRESPS_SOURCE:
                logger.info("Started building correspondences by convolve");
                try {
                    buildConvolveCorrespondences();
                } catch (FileLoadingException e) {
                    logger.error("Error loading Kmeans correspondences, exiting!!!", e);
                    System.exit(1);
                }
                Amatrix = matrixBuilder.createAMatrix(convolveCorrespondences);
                normalizer = new CorrespondenceNormalizer(convolveCorrespondences);
                break;
            case KMEANS_AND_CONVOLVE_SOURCE:
                logger.info("!!!Starting both kmeans and convolve!!!");
                try {
                    buildKmeansAndConvolveCorrespondences();
                } catch (FileLoadingException e) {
                    logger.error("Error loading Kmeans correspondences, exiting!!!", e);
                    System.exit(1);
                }
                Amatrix = matrixBuilder.createAMatrix(convolveCorrespondences, kMeansCorrespondences);
                normalizer = new CorrespondenceNormalizer(kMeansCorrespondences, convolveCorrespondences);
                break;
        }
    }

    /**
     * Activates Kmeans clustering and correspondence lookup
     * @throws FileLoadingException - if unable to open one or both images
     */
    private void buildKmeansCorrespondences() throws FileLoadingException {
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

    /**
     * Activates convolution correspondence lookup
     * @throws FileLoadingException - if unable to open one or both images
     */
    private void buildConvolveCorrespondences() throws FileLoadingException {
        ImageScanner scanner = new ImageScanner(img1Path, img2Path);
        scanner.run();
        convolveCorrespondences = scanner.getCorrespondences();
    }

    /**
     * Loads correspondences specified in correspondence file
     * @throws FileLoadingException - if nuable to open correspondence file
     */
    private void buildFileCorrespondences() throws FileLoadingException {
        if (fileCorrespondencesPath == null || fileCorrespondencesPath.isEmpty())
            throw new IllegalArgumentException("Correspondence path is empty or null, but was specified as source");
        correspondence = new Correspondence(fileCorrespondencesPath);
    }

    /**
     * Starts convolve and clustering correspondences lookup
     * @throws FileLoadingException - if one or both images can not be opened
     */
    private void buildKmeansAndConvolveCorrespondences() throws FileLoadingException {
        buildConvolveCorrespondences();
        buildKmeansCorrespondences();
    }

    /**
     * Computes Essential matrix from the Fundamental one
     * @return
     */
    private DoubleMatrix buildEssentialMatrix() {
        DoubleMatrix E = k2.transpose().multiplyBy(fundamentalMatrix).multiplyBy(k1);
        logger.info("Built essential matrix: " + E);
        return E;
    }

    /**
     * Calculates R2 from fundamental matrix
     * @param essential
     * @return R2
     */
    private DoubleMatrix extractRotationMatrix(DoubleMatrix essential) {
        double[][] w = new double[3][];
        for (int i = 0; i < 3; i ++) {
            w[i] = new double[3];
            for (int j = 0; j < 3; j ++) {
                w[i][j] = 0;
            }
        }
        w[0][1] = -1;
        w[1][0] = 1;
        w[2][2] = 1;
        DoubleMatrix W = new DoubleMatrix(w).transpose();
        SingularValueDecomposition esvd = essential.SVD();
        logger.info(new DoubleMatrix(esvd.getS().getData()));
        DoubleMatrix u = new DoubleMatrix(esvd.getU().getData());
        DoubleMatrix vt = new DoubleMatrix(esvd.getVT().getData());
        DoubleMatrix rotationM = u.multiplyBy(W).multiplyBy(vt);
        logger.info("Calculated rotation matrix: " + rotationM);
        //http://nghiaho.com/?page_id=846
        return rotationM;
    }

    /**
     * Converts rotation matrix to angles around three axises
     * @param rotationM - rotaion matrix
     * @return array of 3 elements, 1 - x axis, 2 - y, 3 - z
     */
    public double[] getRotationAngles(DoubleMatrix rotationM) {
        double x = Math.atan2(rotationM.get(2,1), rotationM.get(2,2));
        double y = Math.atan2(-rotationM.get(2,0), Math.sqrt(rotationM.get(2,1) * rotationM.get(2,1) + rotationM.get(2,2) * rotationM.get(2,2)));
        double z = Math.atan2(rotationM.get(1,0), rotationM.get(0,0));
        x = (x >= 0 ? x : (2*PI + x)) * 360 / (2*PI);
        y = (y >= 0 ? y : (2*PI + y)) * 360 / (2*PI);
        z = (z >= 0 ? z : (2*PI + z)) * 360 / (2*PI);
        logger.info("R angles: x = " + x + " y = " + y + " z = " + z);
        double[] res = new double[] {x, y, z};
        return res;
    }

    /*public void init() {
        logger.info("ENTERED INIT!!!");
        //matrixBuilder = new MatrixBuilderImpl();

        //imageProcessor = new ImageProcessor();

    }*/

    /**
     * Starts the reconstruction algorithms
     * @return Map \<String, PairCorrespData\>; String key is constructed as x_y
     * where x and y are coordinates of the calculated 3D point on the first image,
     * value is a complex object, which contains coordinates of the 3D point on both images and 3D point itself
     */
    public Map<String, PairCorrespData> run () {

        Map<String, PairCorrespData> result = new ConcurrentHashMap<>();

        ColorMatrix[] images = loadImages();

        for (ColorMatrix c : images) {
            c.removeBackground();
        }

        ImageScanner scanner = null;
        try {
            scanner = new ImageScanner(img1Path, img2Path);
        } catch (FileLoadingException e) {
            logger.error("Error starting image scanner: file exception", e);
        }
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
            FileWriter fw = new FileWriter(new File("output/res/result.txt"));
            for (Map.Entry<String, PairCorrespData> entry : sorted.entrySet()) {
                fw.write(entry.getKey() + "      " + entry.getValue().toString());
                PairCorrespData temp = entry.getValue();
                if (temp.getY1() < maximalPair.getY1() && temp.getY2() < maximalPair.getY2()) {
                    maximalPair = temp;
                }
                if (temp.getY1() > minimumPair.getY1() && temp.getY2() > minimumPair.getY2()) {
                    minimumPair = temp;
                }
            }
            fw.flush();
            fw.close();
            logger.info("Done writing results");
            logger.info("Maximal point: " + maximalPair + "\nMinimum point: " + minimumPair);
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
        //normalizeResult(result);
        return result;

        //TODO after that we should somehow start initiation of threads, each will calc it own portion of lines
        //each calculator should find second point, find ro1 TODO try to think - if ro1 is equal for every pair of points - calculate it only once
        //after it has found second point and ro1 it can find depth.
        //TODO PairCorrespData - think what it should contain
        //The idea here - make a some structure - HashMap, which will contain coordinates on the first image as key and
        //everything else as value. This is required for calculators to be able to dump their results into single place
        //TODO Idea to the future - we can have a Set or a Map with depth points as Key to understand, that we've already calculated this point.

    }

    private void normalizeResult (Map<String, PairCorrespData> map) {
        logger.info("Starting results normalization");
        double height = maximalPair.getY() - minimumPair.getY();
        double scale = NormalizedModelHeight / height;
        double xShift = maximalPair.getX() - NormalizedModelShift[0];
        double yShift = maximalPair.getY() - NormalizedModelShift[1];
        double zShift = maximalPair.getZ() - NormalizedModelShift[2];
        for (Map.Entry<String, PairCorrespData> entry : map.entrySet()) {
            PairCorrespData temp = entry.getValue();
            temp.setX(temp.getX() * scale - xShift);
            temp.setY(temp.getY() * scale - yShift);
            temp.setZ(temp.getZ() * scale - zShift);
        }
        logger.info("Finished results normalization");
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

    /**
     * Calculates vector representing Epipole from the Fundamental matrix
     * @param fund - fundamental matrix
     * @return true if the Epipole is formally correct (it does not have infinite values), false otherwise
     */
    private boolean calculateEpipoleFromFundamental(DoubleMatrix fund) {
        //SingularValueDecomposition svd = fund.transpose().SVD();
        SingularValueDecomposition svd = fund.SVD();
        RealMatrix v = svd.getU();
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
