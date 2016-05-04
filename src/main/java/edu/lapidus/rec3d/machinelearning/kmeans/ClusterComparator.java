package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;

import java.util.List;

/**
 * Created by Егор on 03.05.2016.
 */
public class ClusterComparator {
    private List<ColoredImagePoint> cluster1;
    private List<ColoredImagePoint> cluster2;
    private ColorMatrix m1, m2;

    public ClusterComparator(List<ColoredImagePoint> cluster1, List<ColoredImagePoint> cluster2, ColorMatrix m1, ColorMatrix m2) {
        this.cluster1 = cluster1;
        this.cluster2 = cluster2;
        this.m1 = m1;
        this.m2 = m2;
    }




}
