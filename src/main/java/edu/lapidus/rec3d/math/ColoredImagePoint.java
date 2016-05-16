package edu.lapidus.rec3d.math;

import java.awt.*;

/**
 * Created by Егор on 30.04.2016.
 */
public class ColoredImagePoint {
    private int x;
    private int y;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    private Color color;
    public ColoredImagePoint(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public ColoredImagePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ColoredImagePoint(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = new Color(color);
    }

    public double getDistanceTo(ColoredImagePoint p2) {
        int d1 = (x - p2.x) * (x - p2.x) + (y - p2.y) * (y - p2.y);
        return Math.sqrt(d1);
    }

    public double getColorDistance(ColoredImagePoint p2) {
        int r = color.getRed() - p2.color.getRed();
        r *= r;
        int g = color.getGreen() - p2.color.getGreen();
        g *= g;
        int b = color.getBlue() - p2.color.getBlue();
        b *= b;
        return Math.sqrt(r + g + b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColoredImagePoint that = (ColoredImagePoint) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        return color.equals(that.color);

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        if (color != null)
            result = 31 * result + color.hashCode();
        return result;
    }

    public String toString(){
        return x + " " + y + " ";
    }

    public String key() {
        return x + "_" + y;
    }

    public Point toSimplePoint() {
        return new Point(x, y);
    }
}
