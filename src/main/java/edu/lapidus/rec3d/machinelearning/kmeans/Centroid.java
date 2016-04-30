package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 30.04.2016.
 */
public class Centroid extends ColoredImagePoint {

    List<ColoredImagePoint> cluster;

    public Centroid(int x, int y, Color color) {
        super(x, y, color);
    }

    public Centroid(int x, int y, int color) {
        super(x, y, color);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
