package edu.lapidus.rec3d.utils.interfaces;

import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.matrix.Matrix;
import edu.lapidus.rec3d.math.vector.Vector;

/**
 * Created by Егор on 16.11.2015.
 */
public interface MatrixBuilder {

    final int LEARNING_POINT_NUMBER = 16;

    public final int X_AXIS = 1, Y_AXIS = 2, Z_AXIS = 3;

    public DoubleMatrix createRotationMatrix(double angle, int axis);

    public DoubleMatrix createCalibrationMatrix(double ax, double ay, double px, double py);

    /**
     * Build A matrix which represents a set of homogeneous equations, which should be solved to
     * produce fundamental matrix in vector representation
     * @param correspondences - array of points correspondences[0] - Point at first image, [1] - at the second one
     * @return matrix of homogeneous equations
     */
    public DoubleMatrix createAMatrix(Point[][] correspondences);

    public Matrix buildFromVector(Vector doubleVector, int rows, int colls);

    public DoubleMatrix buildFundamental(DoubleMatrix A);

}
