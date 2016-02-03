package edu.lapidus.rec3d.math.vector;

import java.util.Collections;

/**
 * Created by Егор on 20.11.2015.
 */
public class Vector {
    private double[] vec;

    public double[] getVec() {
        return vec;
    }

    public void setVec(double[] vec) {
        this.vec = vec;
    }

    public double byIndex(int index) {
        return vec[index];
    }

    public Vector(Double... args) {
        vec = new double[args.length];
        int i = 0;
        for (Double d : args) {
            vec[i] = d;
            i ++;
        }
    }

    public Vector(int[] values) {
        vec = new double[values.length];
        for (int i = 0; i < vec.length; i ++) {
            vec[i] = values[i];
        }
    }

    public Vector(double[] vec) {
        this.vec = vec;
    }

    public double get(int pos) {
        if (pos >= 0 && pos < vec.length) {
            return vec[pos];
        }
        return Double.NaN;
    }

    public int length(){
        return vec.length;
    }

    public Vector subtract(Vector v) {
        if (v.length() != vec.length) {
            return null;//TODO better throw exception
        }
        double [] res = new double[vec.length];
        for (int i = 0; i < vec.length; i ++) {
            res[i] = vec[i] - v.get(i);
        }
        return new Vector(res);
    }

    public Vector scalar(double scalar) {
        double[] res = new double[vec.length];
        for (int i = 0; i < vec.length; i ++) {
            res[i] = vec[i] * scalar;
        }
        return new Vector(res);
    }
}
