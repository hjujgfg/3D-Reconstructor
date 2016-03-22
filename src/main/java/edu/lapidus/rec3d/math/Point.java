package edu.lapidus.rec3d.math;

/**
 * Created by Егор on 16.11.2015.
 */
public class Point {
    public double x, y;

    public Point (double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return x + ":" + y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
