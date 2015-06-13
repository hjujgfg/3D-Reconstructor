package rec3d.depth.triangulation;

import java.util.ArrayList;

/**
 * Created by Егор on 13.06.2015.
 */
public class TriangulationLayer {
    private ArrayList<Triangle> triangles;

    public TriangulationLayer(ArrayList<double[]> depthPoints) {
        System.out.println("Triangulation started");
        ArrayList<Point> points = new ArrayList<Point>();
        for (double [] d : depthPoints) {
            points.add(new Point(d[0], d[1], d[2]));
        }
        triangles = Delaunay.randomizedIncremental(points);
        System.out.println("Triangulation complete");
    }

    public ArrayList<Triangle> getTriangles() {
        return triangles;
    }
}
