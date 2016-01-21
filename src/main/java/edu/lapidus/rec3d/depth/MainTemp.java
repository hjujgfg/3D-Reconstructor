package edu.lapidus.rec3d.depth;

import org.apache.commons.math3.linear.*;
//import org.opencv.core.*;
//import org.opencv.core.Point;
import edu.lapidus.rec3d.math.Point;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Егор on 10.03.2015.
 */
public class MainTemp {

    final static int N_POINTS = 9;
    static int vertDisp;
    static int[] colors;
    static double minx = 1000, miny = 1000, minz = 1000;
    public int dpwidth = 2000;
    public int dpheight = 1700;
    ArrayList<double[]> depthPoints;
    ArrayList<int[]> scenePoints;
    public static int [] rowLengths;
    public static void main(String[] args) {

        System.out.println("Main initiated!");

        // Load the native library.
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        MainTemp m = new MainTemp();
        m.run();
        //TriangulationLayer tr = new TriangulationLayer(m.depthPoints);

    }


    public ArrayList<int[]> run() {
        System.out.println("\nProgram started");
        Point[][] points;
        double[][] matrA;

        try {
            ObjectInputStream fis = new ObjectInputStream(new FileInputStream(new File("Points.ser")));
            scenePoints = (ArrayList<int[]>) fis.readObject();

            System.out.println("Points found!");
            return scenePoints;
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

        //Correspondence points entered manually!!!
        points = fillPoints();
        //manual ends!!!
        matrA = createAmatrix(points);
        printMatrix(matrA);

        for (int i = 0; i < points.length; i++){
            System.out.println(points[i][0].toString() + " " + points[i][1].toString());
        }


        //calculate fundamental vector, I think
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

        ArrayList<int[]> correspondences = calcregionCorresp(700, 89, dpwidth, dpheight, img1, img2, 1, f);
        System.out.println("Corresps: " + correspondences.size());
        int sum = 0;
        for (int i = 0; i < rowLengths.length; i ++) {
            sum += rowLengths[i];
        }
        System.out.println("Corresps2: " + sum);
        //Visualization of found correspondences
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
        //it seems like i was not sure what i was doing here, that epipole is really a result of solving set of homogenious
        //equations of fundamental matrix yo 
        System.out.println("possible epipole!");
        for (int i = 0; i < epipole.length; i ++) {
            System.out.print(" " + epipole[i]);
        }
        //WTF is going on here? it seems like some attempt to calc epipole but it is not used anywhere.
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

        /*
        double[][] k1 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 639.5, 479.5);

        double[][] k2 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 639.5, 479.5);
        */

        /*
        double[][] k1 = createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 639.5, 479.5);

        double[][] k2 = createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 639.5, 479.5);
        */
        double[][] k1 = createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 1600, 1184);

        double[][] k2 = createCalibrationMatrix(1700.4641287642511, 1700.4641287642511, 1600, 1184);

        /*
        double[][] k1 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 1600, 1184);

        double[][] k2 = createCalibrationMatrix(954.4641287642511, 954.4641287642511, 1600, 1184);
        */

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

        depthPoints = calcDepthMapForRegion(homography, k1Inverse, epipoleC, correspondences, 1);

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



        try {

            boolean success = ImageIO.write(myResult, "png", new File("map.png"));
            System.out.println("saved success? "+success);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(maxDepth + " md " + minDepth);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("points.ser")));
            oos.writeObject(scenePoints);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scenePoints;

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

    //done
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

    //done
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

    //done
    /**
     * CAUTION this is not A matrix built from calibration and rotation matrices, but
     * it is a matrix of 9 learning points! (15) in paper yo
     * @param points
     * @return
     */
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

    //done
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

    //done
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

    //done
    static double[] multiplyAbyB(double[][] A, double[] B) {
        RealMatrix aa = new Array2DRowRealMatrix(A);
        RealVector bb = new ArrayRealVector(B);
        RealVector res = aa.preMultiply(bb);
        return res.toArray();
    }

    //done
    static double[] postMultyplyByVec(double[][] A, double[] v) {
        RealMatrix aa = new Array2DRowRealMatrix(A);
        RealVector vv = new ArrayRealVector(v);
        RealVector res = aa.operate(vv);
        return res.toArray();
    }

    /**
     * build correspondences between two images.
     * @return
     */
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
        rowLengths = new int[height];
        int[][] img1ColorMatrix = formMatrix(img1Colors, img1.getHeight(), img1.getWidth());
        int[][] img2ColorMatrix = formMatrix(img2Colors, img2.getHeight(), img2.getWidth());
        for (int i = yStart; i < yStart + height; i ++) {
            int [] tmp = new int[4];
            rowLengths[i - yStart] = 0;
            for (int j = xStart; j < xStart + width; j ++){
                try {
                    int[] res = calcSecondPoint(new double[]{j, i, 1}, f, img1ColorMatrix, img2ColorMatrix);
                    correspondences.add(new int[]{j, i, res[0], res[1]});
                    rowLengths[i - yStart] ++;
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

