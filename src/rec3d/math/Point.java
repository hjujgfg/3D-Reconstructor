package rec3d.math;

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
}
