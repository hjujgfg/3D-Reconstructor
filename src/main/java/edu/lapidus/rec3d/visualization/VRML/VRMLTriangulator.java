package edu.lapidus.rec3d.visualization.VRML;

import edu.lapidus.rec3d.utils.PairCorrespData;

import java.util.Map;

/**
 * Created by Егор on 05.03.2016.
 */
public class VRMLTriangulator {
    private Map<String, PairCorrespData> points;
    private final int width;
    private final int height;

    VRMLData result = new VRMLData();

    public VRMLTriangulator(Map<String, PairCorrespData> points, int width, int height) {
        this.points = points;
        this.width = width;
        this.height = height;
    }

    public VRMLData triangulate() {
        for (int x = 0; x < width; x +=10) {
            for (int y = 0; y < height; y++) {
                PairCorrespData p = points.get(x + "_" + y);
                result.addPoint(p.getX() * 100, p.getY() * 100, p.getZ() * 100);
                if (x < width - 1 && y < height - 1) {
                    addTwoTriangles(x, y);
                }
            }
        }
        return result;
    }

    private void addTwoTriangles(int x, int y) {
        int i = x * width + y;
        int i1 = i + width;
        int i2 = i1 + 1;
        int i3 = i + 1;
        result.addTriangle(i, i1, i2);
        result.addTriangle(i, i2, i3);
    }
}
