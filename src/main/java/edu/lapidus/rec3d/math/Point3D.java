package edu.lapidus.rec3d.math;

/**
 * Created by Егор on 05.03.2016.
 */
public class Point3D {
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    double x;
    double y;
    double z;

    public Point3D(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }
}
