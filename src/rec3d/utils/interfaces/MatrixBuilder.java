package rec3d.utils.interfaces;

import rec3d.math.Point;
import rec3d.math.matrix.DoubleMatrix;
import rec3d.math.matrix.Matrix;
import rec3d.math.matrix.PointMatrix;
import rec3d.math.vector.Vector;

import java.util.Map;

/**
 * Created by Егор on 16.11.2015.
 */
public interface MatrixBuilder {

    final int LEARNING_POINT_NUMBER = 9;

    public Matrix createRotationMatrix(double angle, int axis);

    public Matrix createCalibrationMatrix(double ax, double ay, double px, double py);

    /**
     * Build A matrix which represents a set of homogeneous equations, which should be solved to
     * produce fundamental matrix in vector representation
     * @param correspondences - array of points correspondences[0] - Point at first image, [1] - at the second one
     * @return matrix of homogeneous equations
     */
    public Matrix createAMatrix(Point[][] correspondences);

    public Matrix buildFromVector(Vector doubleVector);
}