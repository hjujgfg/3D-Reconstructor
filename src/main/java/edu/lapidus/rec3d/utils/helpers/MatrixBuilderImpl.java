package edu.lapidus.rec3d.utils.helpers;

import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.matrix.Matrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;
import org.apache.log4j.Logger;

/**
 * Created by Егор on 16.11.2015.
 */
public class MatrixBuilderImpl implements MatrixBuilder{
    final static Logger logger = Logger.getLogger(MatrixBuilder.class);

    public DoubleMatrix createRotationMatrix(double angle, int axis) {
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

        double [][] A = new double[MatrixBuilder.LEARNING_POINT_NUMBER][];
        for (int i = 0; i < MatrixBuilder.LEARNING_POINT_NUMBER; i ++) {
            A[i] = new double[MatrixBuilder.LEARNING_POINT_NUMBER];
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
        logger.info("Amatrix created: \n" + res.toString());
        return res;
    }

    public Matrix buildFromVector(Vector doubleVector, int rows, int colls) {
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
}
