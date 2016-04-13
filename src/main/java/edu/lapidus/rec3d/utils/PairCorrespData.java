package edu.lapidus.rec3d.utils;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Егор on 27.01.2016.
 */
public class PairCorrespData implements Serializable {

    int x1, y1, x2, y2;
    double X, Y, Z;
    double ro1 = 0, ro2 = 0;

    Color color;

    public String toString() {
        return x1 + " : " + y1 + "; "
                + x2 + " : " + y2 + "; "
                + X + " : " + Y + " : " + Z + "\n";
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public double getX() {
        return X;
    }

    public void setX(double x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double z) {
        Z = z;
    }

    public double getRo1() {
        return ro1;
    }

    public void setRo1(double ro1) {
        this.ro1 = ro1;
    }

    public double getRo2() {
        return ro2;
    }

    public void setRo2(double ro2) {
        this.ro2 = ro2;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PairCorrespData that = (PairCorrespData) o;

        if (x1 != that.x1) return false;
        if (y1 != that.y1) return false;
        if (x2 != that.x2) return false;
        if (y2 != that.y2) return false;
        if (x1 != that.x2 && y1 != that.y2) return false;
        if (x2 != that.x1 && y2 != that.y1) return false;
        if (Double.compare(that.X, X) != 0) return false;
        if (Double.compare(that.Y, Y) != 0) return false;
        return Double.compare(that.Z, Z) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(X);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(Y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(Z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + x1 + y1;
        result = 31 * result + x2 + y2;
        return result;
    }
}
