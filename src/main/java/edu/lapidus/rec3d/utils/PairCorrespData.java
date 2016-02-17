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

}
