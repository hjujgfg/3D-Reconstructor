import org.apache.commons.math3.linear.*;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.calib3d.Calib3d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Егор on 10.03.2015.
 */
public class MainTemp {

    final static int N_POINTS = 9;
    static int vertDisp;
    static int[] colors;
    static double minx = 1000, miny = 1000, minz = 1000;

    public static void main(String[] args) {
        System.out.println("Hello, OpenCV");

        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new MainTemp().run();
    }


    public void run() {
        System.out.println("\nRunning DetectFaceDemo");
        Point[][] points = new Point[N_POINTS][];
        double[][] matrA = new double[3][];
        boolean pointsExist = false;
        try {
            ObjectInputStream fis = new ObjectInputStream(new FileInputStream(new File("Points.ser")));
            matrA = (double[][])fis.readObject();
            pointsExist = true;
            System.out.println("Points found!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Create a face detector from the cascade file in the resources
        // directory.
        //CascadeClassifier faceDetector = new CascadeClassifier(getClass().getResource("/lbpcascade_frontalface.xml").getPath());
        //if (!pointsExist) {
        if (true){
            /*File cmlFile = new File("resources\\lbpcascade_frontalface.xml");
            //CascadeClassifier faceDetector = new CascadeClassifier(cmlFile.getPath());
            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SURF);

            //Mat image = Highgui.imread(getClass().getResource("/lena1.png").getPath());
            File img = new File("resources\\book1.png");
            Mat image = Highgui.imread(img.getPath());

            File img1 = new File("resources\\book2.png");
            Mat image1 = Highgui.imread(img1.getPath());

            *//*File img2 = new File("resources\\coffee3.png");
            Mat image2 = Highgui.imread(img2.getPath());*//*

            MatOfKeyPoint keyPoints = new MatOfKeyPoint();
            MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
            //MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();

            featureDetector.detect(image, keyPoints);
            featureDetector.detect(image1, keyPoints1);
            //featureDetector.detect(image2, keyPoints2);

            System.out.println("kp: " + keyPoints.toString());
            System.out.println("kp1: " + keyPoints1.toString());
            //System.out.println("kp2: " + keyPoints2.toString());

            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);


        List<Mat> images = new ArrayList<Mat>();
        images.add(image);
        images.add(image1);

        List<MatOfKeyPoint> keyPointsList = new ArrayList<MatOfKeyPoint>();
        keyPointsList.add(keyPoints);
        keyPointsList.add(keyPoints1);*//*
            Mat descriptor = new Mat();
            Mat descriptor1 = new Mat();
            //Mat descriptor2 = new Mat();
            extractor.compute(image, keyPoints, descriptor);
            extractor.compute(image1, keyPoints1, descriptor1);
            //extractor.compute(image2, keyPoints2, descriptor2);

            System.out.println("desc: " + descriptor.toString());
            System.out.println("desc1: " + descriptor1.toString());
            //System.out.println("desc2: " + descriptor2.toString());

            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
            MatOfDMatch matches = new MatOfDMatch();
            Mat mask = new MatOfByte();
            matcher.match(descriptor1, descriptor, matches, mask);


            double max_dist = 0;
            double min_dist = 100;
            DMatch[] matchesArray = matches.toArray();

            System.out.println("All matches number: " + matchesArray.length);
            //-- Quick calculation of max and min distances between keypoints
            //for (int i = 0; i < descriptor.rows(); i++) {
            for (int i = 0; i < matchesArray.length; i++) {
                double dist = matchesArray[i].distance;
                if (dist < min_dist) min_dist = dist;
                if (dist > max_dist) max_dist = dist;
            }

            System.out.println("min dist: " + min_dist);
            System.out.println("max dist: " + max_dist);

            List<DMatch> goodMatches = new ArrayList<DMatch>();
            //MatOfDMatch goodMatches = new MatOfDMatch();
            //for (int i = 0; i < descriptor.rows(); i++) {
            for (int i = 0; i < matchesArray.length; i++) {
                if (matchesArray[i].distance <= 2 * min_dist) {
                    goodMatches.add(matchesArray[i]);
                }
            }

            KeyPoint[] kp = keyPoints.toArray();
            KeyPoint[] kp1 = keyPoints1.toArray();
            System.out.println("numof kp = " + kp.length);
            System.out.println("numof kp1 = " + kp1.length);
            KeyPoint[] matchesQuery = new KeyPoint[goodMatches.size()];
            KeyPoint[] matchesTrain = new KeyPoint[goodMatches.size()];
            int k = 0;

            int fundPointIndexer = 0;

            System.out.println("Min dist: " + min_dist + " threshold: " + 1.2 * min_dist);

            for (DMatch d : goodMatches) {

                System.out.println("match " + k + " train: " + d.trainIdx + " query: " + d.queryIdx + " distance: " + d.distance);
                matchesQuery[k] = kp1[d.queryIdx];
                matchesTrain[k] = kp[d.trainIdx];

                if (Math.abs(matchesQuery[k].pt.y - matchesTrain[k].pt.y) <= 50 && fundPointIndexer < N_POINTS) {
                    points[fundPointIndexer] = new Point[2];
                    points[fundPointIndexer][1] = matchesQuery[k].pt;
                    points[fundPointIndexer][0] = matchesTrain[k].pt;
                    System.out.println(kp[d.trainIdx].toString());
                    System.out.println(kp1[d.queryIdx].toString());
                    fundPointIndexer++;
                }


                k++;
            }


            MatOfDMatch finalMatches = new MatOfDMatch();
            finalMatches.fromList(goodMatches);

            System.out.println("Good matches number: " + goodMatches.size());
            Mat outImage = new Mat();
            Features2d.drawMatches(image1, keyPoints1, image, keyPoints, finalMatches, outImage, Scalar.all(-1), new Scalar(1, 1, 0),
                    (MatOfByte) mask, Features2d.NOT_DRAW_SINGLE_POINTS);
            String outImageFileName = "matchedFeatures.png";
            Highgui.imwrite(outImageFileName, outImage);
            */


            //manual points!!!
            points = fillPoints();
            //manual ends!!!
            matrA = createAmatrix(points);
            printMatrix(matrA);

            for (int i = 0; i < points.length; i++){
                System.out.println(points[i][0].toString() + " " + points[i][1].toString());
            }

            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("points.ser")));
                oos.writeObject(matrA);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        double[] matrx = solveHomogeneous(matrA);

        System.out.println("f-vector");

        for (int i = 0; i < matrx.length; i ++) {
            System.out.print(matrx[i] + " ");
        }

        double[][] f = formMatrix(matrx);
        printMatrix(f);


        double[] testVec = multiplyAbyB(f, new double[]{2248, 750, 1});
        System.out.println("Test vector");
        for (int i = 0; i < testVec.length; i ++)
            System.out.print(testVec[i] + " ");
        System.out.println();


        BufferedImage img1 = loadImg("resources/flower1.png");
        BufferedImage img2 = loadImg("resources/flower2.png");

        ArrayList<int[]> correspondences = calcregionCorresp(700, 89, 2000, 1700, img1, img2, 1, f);
        System.out.println("Corresps: " + correspondences.size());

        BufferedImage res = joinBufferedImage(img1, img2);

        Graphics2D grph = (Graphics2D)res.getGraphics();
        Random random = new Random();
        for (int i = 0; i < correspondences.size(); i +=687) {
            int [] tmp = correspondences.get(i);
            grph.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            grph.drawLine(tmp[0], tmp[1], img1.getWidth() + tmp[2], tmp[3]);
        }

        try {

            boolean success = ImageIO.write(res, "png", new File("joined.png"));
            System.out.println("saved success? "+success);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RealMatrix tst = new Array2DRowRealMatrix(f);
        RealMatrix transposed = tst.transpose();
        double [] epipole = solveHomogeneous(transposed.getData());

        System.out.println("possible epipole!");
        for (int i = 0; i < epipole.length; i ++) {
            System.out.print(" " + epipole[i]);
        }
        double scale = 1/ epipole[2];
        epipole[0] *= scale;
        epipole[1] *= scale;
        epipole[2] *= scale;
        for (int i = 0; i < epipole.length; i ++) {
            System.out.print(" " + epipole[i]);
        }

        System.out.println();


        double[][] r1 = createRotationMatrix(0, 2);

        double[][] r2 = createRotationMatrix(15, 2);

       /*double[][] k1 = createCalibrationMatrix(2798, 2748, 1600, 1184);

        double[][] k2 = createCalibrationMatrix(2798, 2748, 1600, 1184);*/

        /*double[][] k1 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 639.5, 479.5);

        double[][] k2 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 639.5, 479.5);*/

        double[][] k1 = createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 639.5, 479.5);

        double[][] k2 = createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 639.5, 479.5);

        /*double[][] k1 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 1600, 1184);

        double[][] k2 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 1600, 1184);*/

        double[] test = postMultyplyByVec(k1, new double[] {20, 100, 200});

        System.out.println("Testetste!!!!!!!!!!!!!!");
        for (double d : test) {
            System.out.println(" " + d / test[2]);
        }
        System.out.println("Testetste!!!!!!!!!!!!!!");

        RealMatrix rm = new Array2DRowRealMatrix(k1, true);

        rm = MatrixUtils.inverse(rm);

        double[][] k1Inverse = rm.getData();

        double[][] homography = calculateInfiniteHomography(k1, k2, r1, r2);

        double[] c1 = new double[] {0, 0, 0};

        double[] c2 = new double[] {57, 0, 7};

        double[] epipoleC = calculateEpipole(k2, r2, c1, c2);

        System.out.println("new epipole");
        for (int i = 0; i < epipoleC.length; i ++) {
            System.out.print(" " + epipoleC[i]);
        }
        System.out.println();

        ArrayList<double[]> depthPoints = calcDepthMapForRegion(homography, k1Inverse, epipoleC, correspondences, 1);
        System.out.println(minx + " mins " + miny + " size: " +depthPoints.size());
        //normalize(depthPoints);
        ArrayList<int[]> scenePoints = calcImagePoints(depthPoints, k1);
        System.out.println("Depth Points!!!");
        /*for(int[] p : scenePoints) {
            for (int i = 0; i < p.length; i ++) {
                System.out.print(" " + p[i]);
            }
            System.out.println();
        }*/
        System.out.println(totalX + " " + totalY);

        BufferedImage myResult = new BufferedImage((int)totalX + 1, (int) totalY + 1 , BufferedImage.TYPE_INT_ARGB);
        for (int[] d : scenePoints) {
            Graphics2D graphics2D = (Graphics2D)myResult.createGraphics();
            //int color = Math.abs(100 * d[2] / 20);
            int color = Math.abs(d[2]);
            if (color < 256 && color >= 0) {
                graphics2D.setColor(new Color(color, color, color));
            } else {
                graphics2D.setColor(new Color(255, 255, 255));
            }
            int x = d[0];
            int y = d[1];

            //System.out.print(x+ ":" + y +"; ");
            if (x < 0 || y < 0) continue;
            graphics2D.drawLine(x, y, x, y + 1);
        }


        /*for (double[] d : depthPoints) {
            Graphics2D graphics2D = myResult.createGraphics();
            //int color = Math.abs(100 * d[2] / 20);
            int color = (int)Math.abs(d[2]) + 80;
            if (color < 256 && color >= 0) {
                graphics2D.setColor(new Color(color, color, color));
            } else {
                graphics2D.setColor(new Color(255, 255, 255));
            }
            int x = (int)d[0];
            int y = (int)d[1];

            //System.out.print(x+ ":" + y +"; ");
            if (x < 0 || y < 0) continue;
            graphics2D.drawLine(x, y, x, y + 1);
        }*/
        try {

            boolean success = ImageIO.write(myResult, "png", new File("map.png"));
            System.out.println("saved success? "+success);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(maxDepth + " md " + minDepth);
        /*for (int [] a : correspondences) {
            System.out.print("img1: " + a[0] + ":" + a[1] + " img2: " + a[2] + ":" + a[3]);
        }*/

        /*colors = img2.getRGB(0,0, img2.getWidth(), img2.getHeight(), null, 0, img2.getWidth());
        int[][] secondColorMatrix = formMatrix(colors, img2.getHeight(), img2.getWidth());
        int[] firstColorRow = img1.getRGB(0, 0, img1.getWidth(), img1.getHeight(), null, 0, img1.getWidth());
        int[][] firstColorMatrix = formMatrix(firstColorRow, img1.getHeight(), img1.getWidth());
        calcSecondPoint(new double[] {1833, 1408, 1}, f, firstColorMatrix, secondColorMatrix);*/

        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        /*MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);


        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }

        // Save the visualized detection.
        String filename = "faceDetection.png";
        System.out.println(String.format("Writing %s", filename));
        Highgui.imwrite(filename, image);*/
    }


    public static double[] calculateEpipole(double[][] k2, double[][] r2, double[] c1, double[] c2) {
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

    public static double[][] calculateInfiniteHomography(double[][] k1, double[][] k2, double[][] r1, double[][] r2){
        RealMatrix homography = new Array2DRowRealMatrix();
        RealMatrix K1 = new Array2DRowRealMatrix(k1);
        RealMatrix K2 = new Array2DRowRealMatrix(k2);
        RealMatrix R1 = new Array2DRowRealMatrix(r1);
        RealMatrix R2 = new Array2DRowRealMatrix(r2);

        K1 = MatrixUtils.inverse(K1);
        R2 = R2.transpose();

        RealMatrix tmp1 = K2.multiply(R2);
        RealMatrix tmp2 = tmp1.multiply(R1);
        homography = tmp2.multiply(K1);

        return homography.getData();
    }

    public static double[][] createRotationMatrix(double angle, int axis) {
        double[][] res = new double[3][];
        for (int i = 0; i < 3; i ++) {
            res[i] = new double[3];
            for (int j = 0; j < 3; j++) {
                res[i][j] = 0;
            }
        }
        switch (axis) {
            case 3:
                res[0][0] = Math.cos(angle);
                res[1][0] = Math.sin(angle);
                res[0][1] = -Math.sin(angle);
                res[1][1] = Math.cos(angle);
                res[2][2] = 1;
                break;
            case 2:
                res[0][0] = Math.cos(angle);
                res[2][0] = -Math.sin(angle);
                res[0][2] = Math.sin(angle);
                res[2][2] = Math.cos(angle);
                res[1][1] = 1;
        }
        return res;
    }

    public static double[][] createCalibrationMatrix(double ax, double ay, double px, double py) {
        double[][] res = new double[3][];
        for (int i = 0; i < 3; i ++) {
            res[i] = new double[3];
            for (int j = 0; j < 3; j++) {
                res[i][j] = 0;
            }
        }
        res[0][0] = ax;
        res[1][1] = ay;
        res[0][2] = px;
        res[1][2] = py;
        res[2][2] = 1;
        return res;
    }

    public static BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        //do some calculate first
        int offset  = 5;
        int wid = img1.getWidth()+img2.getWidth()+offset;
        int height = Math.max(img1.getHeight(),img2.getHeight())+offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth()+offset, 0);
        g2.dispose();
        return newImage;
    }

    static double[][] createAmatrix(Point[][] points) {
        double [][] A = new double[N_POINTS][];
        for (int i = 0; i < N_POINTS; i ++) {
            A[i] = new double[N_POINTS];
            A[i][0] = (int)(points[i][0].x * points[i][1].x);
            A[i][1] = (int)(points[i][0].y * points[i][1].x);
            A[i][2] = (int)(1 * points[i][1].x);
            A[i][3] = (int)(points[i][0].x * points[i][1].y);
            A[i][4] = (int)(points[i][0].y * points[i][1].y);
            A[i][5] = (int)(1 * points[i][1].y);
            A[i][6] = (int)(points[i][0].x * 1);
            A[i][7] = (int)(points[i][0].y * 1);
            A[i][8] = 1;
        }
        return A;
    }

    static void printMatrix(double[][] matr) {

        System.out.println("matrix: " + matr.length + " x " + matr[0].length);
        for (int i = 0; i < matr.length; i ++) {
            System.out.println();
            for (int j = 0; j < matr.length; j ++) {
                System.out.print(matr[i][j] + " ");
            }
        }
        System.out.println();
    }

    static double[] solveMatrix(double[][] matr) {
        RealMatrix coeffs = new Array2DRowRealMatrix(matr, false);
        DecompositionSolver solver = new SingularValueDecomposition(coeffs).getSolver();
        double[] vec = new double[N_POINTS];
        for (int i = 0; i < N_POINTS; i ++) {
            vec[i] = 0;
        }
        RealVector constants = new ArrayRealVector(vec);
        RealVector result = solver.solve(constants);
        return result.toArray();
    }

    static double[] solveHomogeneous(double[][] matr) {
        RealMatrix M = new Array2DRowRealMatrix(matr, false);
        SingularValueDecomposition SVD = new SingularValueDecomposition(M);
        RealMatrix V = SVD.getV();

        return V.getColumn(V.getColumnDimension() - 1);
        //return V.getColumn(0);
    }

    static double[][] formMatrix(double[] vec) {
        double[][] matrix = new double[3][];
        matrix[0] = new double[3];
        matrix[1] = new double[3];
        matrix[2] = new double[3];
        int k = 0;
        for (int i = 0; i < 3; i ++) {
            for (int j = 0; j < 3; j ++) {
                matrix[j][i] = vec[k];
                k ++;
            }
        }
        return matrix;
    }

    static int[][] formMatrix(int[] vec, int height, int width) {
        int [][] res = new int[height][];
        int k = 0;
        for (int i = 0; i < height; i ++) {
            res [i] = new int[width];
            for (int j = 0; j < width; j ++) {
                res[i][j] = vec[k];
                k ++;
            }
        }
        return res;
    }

    static double[] multiplyAbyB(double[][] A, double[] B) {
        RealMatrix aa = new Array2DRowRealMatrix(A);
        RealVector bb = new ArrayRealVector(B);
        RealVector res = aa.preMultiply(bb);
        return res.toArray();
    }

    static double[] postMultyplyByVec(double[][] A, double[] v) {
        RealMatrix aa = new Array2DRowRealMatrix(A);
        RealVector vv = new ArrayRealVector(v);
        RealVector res = aa.operate(vv);
        return res.toArray();
    }

    static Point[][] fillPoints() {
        Point[][] p = new Point[9][];
        p[0] = new Point[2];
        p[0][0] = new Point(605, 841);
        p[0][1] = new Point(673, 665);

        p[1] = new Point[2];
        p[1][0] = new Point(849, 837);
        p[1][1] = new Point(1041, 677);

        p[2] = new Point[2];
        p[2][0] = new Point(1633, 457);
        p[2][1] = new Point(1681, 245);

        p[3] = new Point[2];
        p[3][0] = new Point(1885, 497);
        p[3][1] = new Point(2141, 337);

        p[4] = new Point[2];
        p[4][0] = new Point(1741, 1149);
        p[4][1] = new Point(1869, 985);

        p[5] = new Point[2];
        p[5][0] = new Point(2025, 1625);
        p[5][1] = new Point(2173, 1497);

        p[6] = new Point[2];
        p[6][0] = new Point(1537, 2113);
        p[6][1] = new Point(1705, 2009);

        p[7] = new Point[2];
        p[7][0] = new Point(2153, 2145);
        p[7][1] = new Point(2369, 2097);

        p[8] = new Point[2];
        p[8][0] = new Point(409, 1969);
        p[8][1] = new Point(773, 1813);
        System.out.println("_-____----__-__--_--_-");
        vertDisp = 0;
        for (int i = 0; i < 9; i ++) {
            System.out.println("point x dist n: " + i + " : " + (p[i][1].x - p[i][0].x));
            System.out.println("point y dist n: " + i + " : " + (p[i][1].y - p[i][0].y));
            if (Math.abs(p[i][1].y - p[i][0].y) > Math.abs(vertDisp))
                vertDisp =(int)(p[i][1].y - p[i][0].y);
        }
        System.out.println("vertical dispersy: " + vertDisp);
        return p;
    }

    static BufferedImage loadImg(String path) {
        File f = new File(path);
        BufferedImage img = null;
        try {
            img = ImageIO.read(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    static int[] calcSecondPoint(double[] first, double[][] F, int[][] img1Color, int[][] colorsTwo) {
        double[] coeffs = multiplyAbyB(F, first);
        //int intColor = img1.getRGB((int)first[0], (int)first[1]);
        int intColor = img1Color[(int)first[1]][(int)first[0]];
        Color currentColor = new Color(intColor);
        /*System.out.println((int)first[0] + " : " + (int)first[1] + " : " + intColor);
        System.out.println(currentColor.toString());*/

        int yStart = (int)first[1];
        int yEnd = yStart + vertDisp;

        int xStart = (int) ((-coeffs[2] - (coeffs[1] * yStart))/coeffs[0]);
        int xEnd = (int) ((-coeffs[2] - (coeffs[1] * yEnd))/coeffs[0]);

        /*System.out.println("second point calculation");
        System.out.println("xstrt: " + xStart + " ystart: " + yStart);
        System.out.println("xend: " + xEnd + " yend: " + yEnd);*/


        int step;
        if (yStart > yEnd)
            step = -1;
        else
            step = 1;
        ArrayList<Double> distances = new ArrayList<Double>();
        //System.out.println("Color distances!");
        for (int i = yStart; i != yEnd; i += step) {
            int x = (int) ((-coeffs[2] - (coeffs[1] * i))/coeffs[0]);
            //try {
                Color tmp = new Color(colorsTwo[i][x]);
                double distance = Math.sqrt(
                        (currentColor.getRed() - tmp.getRed()) * (currentColor.getRed() - tmp.getRed()) +
                                (currentColor.getBlue() - tmp.getBlue()) * (currentColor.getBlue() - tmp.getBlue()) +
                                (currentColor.getGreen() - tmp.getGreen()) * (currentColor.getGreen() - tmp.getGreen()));
                distances.add(distance);
                //System.out.println(x + ":" + i + " dist: " + distance);
            /*} catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }*/

        }

        double min = distances.get(0);
        int oneMoreCounter = yStart;
        int i = 0;
        for (double d : distances) {
            if (d < min) {
                min = d;
                oneMoreCounter = yStart + (i * step);
            }
            i ++;
        }
        int[] res = new int[3];
        res [1] = oneMoreCounter;
        res [0] = (int) ((-coeffs[2] - (coeffs[1] * res[1]))/coeffs[0]);
        res [2] = 1;
        //System.out.println("point: " + res[0] + ":" + res[1]);
        return res;
    }

    static ArrayList<int[]> calcregionCorresp(int xStart, int yStart, int width, int height, BufferedImage img1, BufferedImage img2, int accuracy, double[][] f) {
        //int[][] correspondences = new int [(int)height/accuracy][];
        ArrayList<int[]> correspondences = new ArrayList<int[]>();
        int[] img1Colors = img1.getRGB(0,0, img1.getWidth(), img1.getHeight(), null, 0, img1.getWidth());
        int[] img2Colors = img2.getRGB(0,0, img2.getWidth(), img2.getHeight(), null, 0, img2.getWidth());

        int[][] img1ColorMatrix = formMatrix(img1Colors, img1.getHeight(), img1.getWidth());
        int[][] img2ColorMatrix = formMatrix(img2Colors, img2.getHeight(), img2.getWidth());
        for (int i = yStart; i < yStart + height; i ++) {
            int [] tmp = new int[4];
            for (int j = xStart; j < xStart + width; j ++){
                try {
                    int[] res = calcSecondPoint(new double[]{j, i, 1}, f, img1ColorMatrix, img2ColorMatrix);
                    correspondences.add(new int[]{j, i, res[0], res[1]});
                }catch (ArrayIndexOutOfBoundsException e ) {
                    continue;
                }
            }
        }
        return correspondences;
    }

    /***
     *
     * @param homography
     * @param corresp - in inner format
     * @param epipole
     * @return [0] - ro1, [1] - ro2;
     */
    static double calcRo1(double[][] homography, int[] corresp, double[] epipole) {
        double [] a = postMultyplyByVec(homography, new double[] {corresp[0], corresp[1], 1});
        double ro1 = (epipole[1] - epipole[2] * corresp[3])
                / (a[2] * corresp[3] - a[1]);
        double ro2 = ro1 * a[2] + epipole[2];
        //return new double[] {ro1, ro2};
        //System.out.println(ro1 + " q " + ro2);
        return ro1;
    }

    static double[] calcScenePoint(double ro1, double[][] K1Inverse, int[] corresp) {
        double [] vec = postMultyplyByVec(K1Inverse, new double[] {corresp[0], corresp[1], 1});
        //int [] res = new int[vec.length];
        for (int i = 0; i < vec.length; i ++) {
            vec[i] *= ro1;
            //res[i] = (int)vec[i];
        }
        return vec;
    }

    static ArrayList<double[]> calcDepthMapForRegion(double[][] homography, double[][] k1Inverse, double[] epipole, ArrayList<int[]> corresps, int ratio) {
        ArrayList<double[]> points = new ArrayList<double[]>();
        for (int i = 0; i < corresps.size(); i += ratio) {
            double ro1 = calcRo1(homography, corresps.get(i), epipole);
            double[] tmp = calcScenePoint(ro1, k1Inverse, corresps.get(i));
            points.add(tmp);
            if (minx > tmp[0])
                minx = tmp[0];
            if (miny > tmp[1])
                miny = tmp[1];
        }
        return points;
    }
    static double totalX = 0, totalY = 0, maxDepth = 0, minDepth = 5000;
    static void normalize(ArrayList<double[]> depthPoints) {
        for (double[] d : depthPoints) {

            d[0] *= 100;
            d[1] *= 100;

            d[0] += Math.abs(minx);
            d[1] += Math.abs(miny);

            d[2] = Math.abs(d[2]) * 10;
            System.out.println(d[0] + " " + d[1] + " "  + d[2]);
            if (Math.abs(d[0]) > totalX)
                totalX = Math.abs(d[0]);
            if (Math.abs(d[1]) > totalY)
                totalY = Math.abs(d[1]);
        }
    }

    static ArrayList<int[]> calcImagePoints(ArrayList<double[]> depthPoints, double[][] k1) {
        ArrayList<int[]> points = new ArrayList<int[]>();
        for (double[] d : depthPoints) {
            int[] tmp = new int[3];
            double[] imgp = postMultyplyByVec(k1, d);
            tmp[0] = (int) (imgp[0] / imgp[2]);
            tmp[1] = (int) (imgp[1] / imgp[2]);
            tmp[2] = (int) (imgp[2] * 10);

            //System.out.println(imgp[0] + " " + imgp[1] + " " + imgp[2]);
            points.add(tmp);
            if (totalX < tmp[0]) totalX = tmp[0];
            if (totalY < tmp[1]) totalY = tmp[1];
            if (maxDepth < Math.abs(tmp[2])) maxDepth =  Math.abs(tmp[2]);
            if (minDepth > Math.abs(tmp[2])) minDepth =  Math.abs(tmp[2]);
        }
        return points;
    }
}
