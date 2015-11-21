package edu.lapidus.rec3d.math.matrix;

import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.vector.Vector;

import java.util.Map;

/**
 * This is a class for storing correspondences between twoo images.
 * Created by Егор on 16.11.2015.
 */
public class PointMatrix implements Matrix {

    Map<Point, Point> correspondences;


    public Vector preMultiply() {
        return null;
    }

    public Vector postMultiply() {
        return null;
    }
}
