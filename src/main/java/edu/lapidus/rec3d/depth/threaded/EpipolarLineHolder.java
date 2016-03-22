package edu.lapidus.rec3d.depth.threaded;

import java.util.ArrayList;

/**
 * Created by Егор on 22.03.2016.
 */
public class EpipolarLineHolder {
    int [] firstPoint;


    ArrayList<int[]> line;


    int [] secondPoint;


    double [] coefficients;

    public EpipolarLineHolder(int[] firstPoint, double[] coeffs) {
        this.firstPoint = firstPoint;
        this.coefficients = coeffs;
        line = new ArrayList<int[]>();
    }

    public void addLinePoint(int x, int y) {
        line.add(new int[] {x, y});
    }

    public void setSecondPoint(int x, int y) {
        secondPoint = new int[2];
        secondPoint[0] = x;
        secondPoint[1] = y;
    }

    public int[] getFirstPoint() {
        return firstPoint;
    }

    public void setFirstPoint(int[] firstPoint) {
        this.firstPoint = firstPoint;
    }


    public ArrayList<int[]> getLine() {
        return line;
    }

    public int[] getSecondPoint() {
        return secondPoint;
    }

    public double[] getCoefficients() {
        return coefficients;
    }
}
