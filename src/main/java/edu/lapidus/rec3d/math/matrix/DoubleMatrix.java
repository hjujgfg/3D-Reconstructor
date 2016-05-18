package edu.lapidus.rec3d.math.matrix;

import edu.lapidus.rec3d.machinelearning.kmeans.CorrespondenceHolder;
import org.apache.commons.math3.linear.*;
import edu.lapidus.rec3d.math.vector.Vector;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Егор on 16.11.2015.
 */
public class DoubleMatrix implements Matrix, Serializable {
    protected double[][] internal;
    private final static Logger logger = Logger.getLogger(DoubleMatrix.class);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (double[] d : internal) {
            for (double dd : d) {
                sb.append(String.format("%.4f ", dd));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    //TODO check why Homography does not compile without this constructor
    protected DoubleMatrix() {}

    public DoubleMatrix(int rows, int columns){
        internal = new double[rows][];
        for (int i = 0; i < rows; i ++) {
            internal[i] = new double[columns];
            for (int j = 0; j < columns; j ++) {
                internal[i][j] = 0;
            }
        }
    }

    public DoubleMatrix(double[][] matrix) {
        this.internal = matrix;
    }

    public DoubleMatrix(List<CorrespondenceHolder> init) {
        internal = new double[init.size()][];

    }

    public void print() {
        logger.info("\nMatrix:\n" + toString() + "----------------------------------\n");
    }

    public Vector getRow(int id) throws IllegalArgumentException {
        return new Vector(internal[id]);
    }

    public Vector getColumn(int id) throws IllegalArgumentException {
        double[] row = new double[internal.length];
        int i = 0;
        for (double[] d : internal) {
            row[i] = (d[id]);
            i++;
        }
        return new Vector(row);
    }

    public Vector preMultiply(Vector vector) {
        RealMatrix aa = new Array2DRowRealMatrix(internal);
        RealVector bb = new ArrayRealVector(vector.getVec());
        RealVector res = aa.preMultiply(bb);
        return new Vector(res.toArray());
    }

    public Vector postMultiply(Vector vector) {
        RealMatrix aa = new Array2DRowRealMatrix(internal);
        RealVector vv = new ArrayRealVector(vector.getVec());
        RealVector res = aa.operate(vv);
        return new Vector(res.toArray());
    }

    public DoubleMatrix transpose(){
        RealMatrix m = new Array2DRowRealMatrix(internal);
        return new DoubleMatrix(m.transpose().getData());
    }

    public double[][] getData() {
        return internal;
    }

    /**
     * Solves a homogeneous set of equasions represented as this matrix
     * TODO add checks if the operation can be performed!
     * TODO understand WHAT happens in SVD
     * @return
     */
    public Vector solveHomogeneous() {
        //logger.info("Solving homogeneous on matrix: " + toString());
        RealMatrix M = new Array2DRowRealMatrix(this.getData(), false);
        SingularValueDecomposition SVD = new SingularValueDecomposition(M);
        RealMatrix V = SVD.getV();
        Vector v = new Vector(V.getColumn(V.getColumnDimension() - 1));
        //logger.info("Solved with resulting vector: " + v.toString());
        return v;
    }

    public SingularValueDecomposition SVD() {
        //logger.info("Performing SVD over matrix: " + toString());
        RealMatrix M = new Array2DRowRealMatrix(internal);
        return new SingularValueDecomposition(M);
    }

    public DoubleMatrix inverse() {
        //logger.info("Inversing matrix: " + toString());
        RealMatrix matrix = new Array2DRowRealMatrix(internal);
        matrix = MatrixUtils.inverse(matrix);
        DoubleMatrix res = new DoubleMatrix(matrix.getData());
        //logger.info("Inversed matrix: " + res.toString());
        return res;
    }

    public DoubleMatrix multiplyBy(DoubleMatrix mtrx) {
        RealMatrix r1 = new Array2DRowRealMatrix(internal);
        RealMatrix r2 = new Array2DRowRealMatrix(mtrx.getData());
        RealMatrix res = r1.multiply(r2);
        return new DoubleMatrix(res.getData());
    }

    public void scale(double scalar) {
        RealMatrix r = new Array2DRowRealMatrix(internal);
        r = r.scalarMultiply(scalar);
        internal = r.getData();
    }

    public void rowsAppend (DoubleMatrix a) {
        double [][] res = new double[internal.length + a.internal.length][];
        System.arraycopy(internal, 0, res, 0, internal.length);
        System.arraycopy(a.internal, 0, res, internal.length, a.internal.length);
        internal = res;
     }

    public void setAtPosition(int row, int column, double value) {
        internal[row][column] = value;
    }
}
