package edu.lapidus.rec3d.depth;

import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by Егор on 15.01.2016.
 */
public class Homography {

    DoubleMatrix homography;

    DoubleMatrix k1;
    DoubleMatrix k2;
    DoubleMatrix r1;
    DoubleMatrix r2;

    public DoubleMatrix getR1() {
        return r1;
    }

    public void setR1(DoubleMatrix r1) {
        calculate();
        this.r1 = r1;
    }

    public DoubleMatrix getK1() {
        return k1;
    }

    public void setK1(DoubleMatrix k1) {
        calculate();
        this.k1 = k1;
    }

    public DoubleMatrix getK2() {
        return k2;
    }

    public void setK2(DoubleMatrix k2) {
        calculate();
        this.k2 = k2;
    }

    public DoubleMatrix getR2() {
        return r2;
    }

    public void setR2(DoubleMatrix r2) {
        calculate();
        this.r2 = r2;
    }

    public Homography (DoubleMatrix k1, DoubleMatrix k2, DoubleMatrix r1, DoubleMatrix r2) {
        this.k1 = k1;
        this.k2 = k2;
        this.r1 = r1;
        this.r2 = r2;

        calculate();
    }

    private void calculate() {
        RealMatrix res;
        RealMatrix K1 = new Array2DRowRealMatrix(k1.getData());
        RealMatrix K2 = new Array2DRowRealMatrix(k2.getData());
        RealMatrix R1 = new Array2DRowRealMatrix(r1.getData());
        RealMatrix R2 = new Array2DRowRealMatrix(r2.getData());

        K1 = MatrixUtils.inverse(K1);
        R2 = R2.transpose();

        RealMatrix tmp1 = K2.multiply(R2);
        RealMatrix tmp2 = tmp1.multiply(R1);
        res = tmp2.multiply(K1);
        homography = new DoubleMatrix(res.getData());
    }

}
