package edu.lapidus.rec3d.machinelearning.kmeans;

import edu.lapidus.rec3d.math.ColoredImagePoint;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 14.05.2016.
 */
public class Cluster extends ArrayList<ColoredImagePoint> {
    private ColoredImagePoint centroid;
    private double[] centroidCoords = new double[5];
    TreeSet<IndexDistanceBinding> bindings = new TreeSet<IndexDistanceBinding>(bindingComparator);
    public Cluster() {
        super();
        centroid = new ColoredImagePoint(0,0, Color.BLACK);
        for(int i = 0; i< centroidCoords.length; i ++) {
            centroidCoords[i] = 0;
        }
    }

    public Cluster(int init) {
        super(init);
        centroid = new ColoredImagePoint(0,0, Color.BLACK);
        for(int i = 0; i< centroidCoords.length; i ++) {
            centroidCoords[i] = 0;
        }
    }
    public Cluster (List<ColoredImagePoint> cl, ColoredImagePoint centroid) {
        super(cl);
        this.centroid = centroid;
    }

    public Cluster (List<ColoredImagePoint> list) {
        super(list);
        calcCentroid();
    }

    public boolean add(ColoredImagePoint point){
        boolean res = super.add(point);
        centroidCoords[0] += point.getX();
        centroidCoords[1] += point.getY();
        centroidCoords[2] += point.getColor().getRed();
        centroidCoords[3] += point.getColor().getGreen();
        centroidCoords[4] += point.getColor().getBlue();
        centroid.setX((int) (centroidCoords[0] / size()));
        centroid.setY((int) (centroidCoords[1] / size()));
        int r = (int) (centroidCoords[2] / size());
        int g = (int) (centroidCoords[3] / size());
        int b = (int) (centroidCoords[4] / size());
        centroid.setColor(new Color(r, g, b));
        bindings.add(new IndexDistanceBinding(size() - 1, point.getDistanceTo(centroid)));
        return res;
    }

    public ColoredImagePoint getNextClosest() {
        int index = bindings.pollFirst().index;
        return get(index);
    }

    private void calcCentroid() {
        double x = 0, y = 0, r = 0, g = 0, b = 0;
        for (ColoredImagePoint c : this) {
            x += c.getX();
            y += c.getY();
            Color color = c.getColor();
            r += color.getRed();
            g += color.getGreen();
            b += color.getBlue();
        }
        x /= this.size();
        y /= this.size();
        r /= this.size();
        g /= this.size();
        b /= this.size();

        centroid = new ColoredImagePoint((int)x, (int)y, new Color((int)r, (int)g, (int)b));
    }

    private static Comparator<IndexDistanceBinding> bindingComparator = new Comparator<IndexDistanceBinding>() {
        @Override
        public int compare(IndexDistanceBinding o1, IndexDistanceBinding o2) {
            if (o1.distance == o2.distance) return 0;
            if (o1.distance > o2.distance) return -1;
            return 1;
        }
    };

    private class IndexDistanceBinding {
        int index;
        double distance;

        IndexDistanceBinding(int index, double distance) {
            this.index = index;
            this.distance = distance;
        }
    }
}
