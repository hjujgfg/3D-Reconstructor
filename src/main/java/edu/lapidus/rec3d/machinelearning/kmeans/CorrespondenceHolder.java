package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;

import java.util.ArrayList;

/**
 * Created by Егор on 09.05.2016.
 */
public class CorrespondenceHolder extends ArrayList<ColoredImagePoint> implements Comparable {

    private final double distance;
    public CorrespondenceHolder(ColoredImagePoint p1, ColoredImagePoint p2, double distance) {
        super(2);
        this.add(p1);
        this.add(p2);
        this.distance = distance;
    }
    public double getDistance() {
        return distance;
    }


    @Override
    public int compareTo(Object o) {
        if (o == null) throw new NullPointerException();
        if (! (o instanceof CorrespondenceHolder) ) throw new ClassCastException();
        CorrespondenceHolder oo = (CorrespondenceHolder)o;
        if (this.distance < oo.distance) return -1;
        if (this.distance == oo.distance) return 0;
        return 1;
    }

    public ColoredImagePoint getA() {
        return get(0);
    }

    public ColoredImagePoint getB() {
        return get(1);
    }

}
