package edu.lapidus.rec3d.utils.helpers;

import edu.lapidus.rec3d.machinelearning.kmeans.CorrespondenceHolder;
import edu.lapidus.rec3d.machinelearning.kmeans.Kmeans;
import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.matrix.Matrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.log4j.Logger;

import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Егор on 16.11.2015.
 */
public class MatrixBuilderImpl{
    final static Logger logger = Logger.getLogger(MatrixBuilderImpl.class);

    public DoubleMatrix createRotationMatrixFull(double xAng, double yAng, double zAng){
        return createRotationMatrix(zAng, MatrixBuilder.Z_AXIS)
                .multiplyBy(createRotationMatrix(yAng, MatrixBuilder.Y_AXIS))
                .multiplyBy(createRotationMatrix(xAng, MatrixBuilder.X_AXIS));
    }

    public DoubleMatrix createRotationMatrix(double angle, int axis) {
        angle = Math.PI * angle / 180;
        double[][] res = new double[3][];
        for (int i = 0; i < 3; i ++) {
            res[i] = new double[3];
            for (int j = 0; j < 3; j++) {
                res[i][j] = 0;
            }
        }
        switch (axis) {
            //rotate around z
            case MatrixBuilder.Z_AXIS:
                res[0][0] = Math.cos(angle);
                res[1][0] = Math.sin(angle);
                res[0][1] = -Math.sin(angle);
                res[1][1] = Math.cos(angle);
                res[2][2] = 1;
                break;
            //rotate around y
            case MatrixBuilder.Y_AXIS:
                res[0][0] = Math.cos(angle);
                res[2][0] = -Math.sin(angle);
                res[0][2] = Math.sin(angle);
                res[2][2] = Math.cos(angle);
                res[1][1] = 1;
                break;
            case MatrixBuilder.X_AXIS:
                res[0][0] = 1;
                res[1][1] = Math.cos(angle);
                res[1][2] = - Math.sin(angle);
                res[2][1] = Math.sin(angle);
                res[2][2] = Math.cos(angle);
                break;
        }
        return new DoubleMatrix(res);
    }

    public DoubleMatrix createCalibrationMatrix(double ax, double ay, double px, double py) {
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
        return new DoubleMatrix(res);
    }

    /**
     * Uses an array of manual points to build a set of homogeneous equations to construct a fundamental matrix
     * @param points
     * @return
     */
    public DoubleMatrix createAMatrix(Point[][] points) {

        /*double [][] A = new double[MatrixBuilder.LEARNING_POINT_NUMBER][];
        for (int i = 0; i < MatrixBuilder.LEARNING_POINT_NUMBER; i ++) {*/
        double [][] A = new double[points.length][];
        for (int i = 0; i < A.length; i ++) {
            A[i] = new double[9];
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
        DoubleMatrix res = new DoubleMatrix(A);
        //logger.info("Amatrix created: \n" + res.toString());
        return res;
    }

    public DoubleMatrix createAMatrix(List<CorrespondenceHolder> points) {
        double [][] A = new double[points.size()][];
        int i = 0;
        for (CorrespondenceHolder c : points) {
            A[i] = new double[9];
            A[i][0] = c.getA().getX() * c.getB().getX();
            A[i][1] = c.getA().getY() * c.getB().getX();
            A[i][2] = c.getB().getX();
            A[i][3] = c.getA().getX() * c.getB().getY();
            A[i][4] = c.getA().getY() * c.getB().getY();
            A[i][5] = c.getB().getY();
            A[i][6] = c.getA().getX();
            A[i][7] = c.getA().getY();
            A[i][8] = 1;
            i ++;
        }
        return new DoubleMatrix(A);
    }

    public DoubleMatrix createAMatrix(Map<ColoredImagePoint, ColoredImagePoint> points) {
        double[][] A = new double[points.size()][];
        int i = 0;
        for (Map.Entry<ColoredImagePoint, ColoredImagePoint> p : points.entrySet()) {
            ColoredImagePoint p1 = p.getKey();
            ColoredImagePoint p2 = p.getValue();
            A[i] = new double[9];
            A[i][0] = p1.getX() * p2.getX();
            A[i][1] = p1.getY() * p2.getX();
            A[i][2] = p2.getX();
            A[i][3] = p1.getX() * p2.getY();
            A[i][4] = p1.getY() * p2.getY();
            A[i][5] = p2.getY();
            A[i][6] = p1.getX();
            A[i][7] = p1.getY();
            A[i][8] = 1;
            i ++;
        }
        return new DoubleMatrix(A);
    }

    public DoubleMatrix createAMatrix(Map<ColoredImagePoint, ColoredImagePoint> map, List<CorrespondenceHolder> list) {
        DoubleMatrix m1 = createAMatrix(map);
        DoubleMatrix m2 = createAMatrix(list);
        m1.rowsAppend(m2);
        return m1;
    }

    /*protected void scaleToRoot(List<ColoredImagePoint> list) {
        boolean stop = false;
        double factor = 1;
        double realMultiplier = 1;
        double twoRoot = 1.41421;
        double currentDistance = scaleList(list, factor);
        factor = 2;
        boolean changeFactor = true;
        while (true) {
            if (Math.abs(currentDistance - twoRoot) < 0.1) break;
            if (currentDistance < 2 && changeFactor)
                factor *= 2;
            realMultiplier = 1;
            if (currentDistance > twoRoot) {
                realMultiplier -= 1 / factor;
            } else {
                changeFactor = false;
                factor /= 2;
                realMultiplier += 1 / factor;
            }
            currentDistance = scaleList(list, realMultiplier);
        }
    }


    private final static ColoredImagePoint ORIGIN_POINT = new ColoredImagePoint(0, 0);
    private double scaleList(List<ColoredImagePoint> list, double factor) {
        int x, y;
        *//*int totalX = 0, totalY = 0;*//*
        double distance = 0;
        double currentDistance ;
        for (ColoredImagePoint p : list) {

            x = (int) (p.getX() * factor);
            y = (int) (p.getY() * factor);
            p.setX(x);
            p.setY(y);
            currentDistance = ORIGIN_POINT.getDistanceTo(p);
            distance += currentDistance * currentDistance;
            *//*totalX += x;
            totalY += y;*//*
        }
        *//*mean.setX(totalX / list.size());
        mean.setY(totalY / list.size());*//*
        return Math.sqrt(distance / list.size());
    }*/



    public DoubleMatrix buildFromVector(Vector doubleVector, int rows, int colls) {
        double[][] res = new double[rows][];
        int counter = 0;
        for (int i = 0; i < rows; i++) {
            res[i] = new double[colls];
            for (int j = 0; j < colls; j++) {
                res[i][j] = doubleVector.byIndex(counter);
                counter ++;
            }
        }
        return new DoubleMatrix(res);
    }

    public DoubleMatrix buildFundamental(DoubleMatrix A) {
        SingularValueDecomposition Asvd = A.SVD();
        RealMatrix v = Asvd.getV();
        Vector f = new Vector(v.getColumn(2));
        DoubleMatrix fund = buildFromVector(f, 3, 3);
        //todo this is shit implement real fundamental matrix calculation
        //this is atually the second part from matlab
        SingularValueDecomposition svd = fund.SVD();
        RealMatrix s = svd.getS();
        s.setEntry(2, 2, 0.);
        RealMatrix u = svd.getU();
        RealMatrix vt = svd.getVT();
        RealMatrix fundamental = u.multiply(s).multiply(vt);
        return new DoubleMatrix(fundamental.getData());
    }
}
