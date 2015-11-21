package edu.lapidus.rec3d.math.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import edu.lapidus.rec3d.math.vector.Vector;

/**
 * Created by Егор on 16.11.2015.
 */
public class DoubleMatrix implements Matrix {
    double[][] internal;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (double[] d : internal) {
            for (double dd : d) {
                sb.append(dd + "");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public DoubleMatrix(double[][] matrix) {
        this.internal = matrix;
    }

    public void print() {
        System.out.println("Matrix:\n" + toString() + "----------------------------------\n");
    }

    public Vector getRow(int id) throws IllegalArgumentException {
        return new Vector(internal[id]);
    }

    public double[] getColumn(int id) throws IllegalArgumentException {
        double[] row = new double[internal.length];
        int i = 0;
        for (double[] d : internal) {
            row[i] = (d[id]);
            i++;
        }
        return row;
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
}
