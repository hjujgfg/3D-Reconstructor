package rec3d.utils.interfaces;

import rec3d.math.matrix.DoubleMatrix;
import rec3d.math.matrix.Matrix;
import rec3d.math.vector.DoubleVector;

/**
 * Created by Егор on 16.11.2015.
 */
public interface MatrixBuilder {
    public Matrix createRotationMatrix(double angle, int axis);

    public Matrix createCalibrationMatrix(double ax, double ay, double px, double py);

    public Matrix createAMatrix(DoubleMatrix points);

    public Matrix buildFromVector(DoubleVector doubleVector);
}
