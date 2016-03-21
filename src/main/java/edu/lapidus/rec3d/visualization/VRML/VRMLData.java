package edu.lapidus.rec3d.visualization.VRML;

import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.Point3D;
import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Егор on 05.03.2016.
 */
public class VRMLData {
    private static final Logger logger = Logger.getLogger(VRMLData.class);

    public VRMLData() {
        vertices = new ArrayList<Point3D>();
        triangles = new ArrayList<IndexedTriangle>();
    }

    List<Point3D> vertices;
    List<IndexedTriangle> triangles;

    public void addPoint( double x, double y, double z) {
        vertices.add(new Point3D(x, y, z));
    }

    public void addTriangle(int i1, int i2, int i3) {
        triangles.add(new IndexedTriangle(i1, i2, i3));
    }

    public void saveWrl(){
        try {
            //FileOutputStream fos = new FileOutputStream();
            logger.info("Writing results");
            FileWriter fw = new FileWriter(new File("resources/res/result.wrl"));
            fw.write("Shape {\n" +
                    "\tappearance Appearance{\n" +
                    "\t\tmaterial Material { \n" +
                    "\t\t\tdiffuseColor     1 0 0 #simple red\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n" +
                    "\tgeometry IndexedFaceSet {\n" +
                    "\t\tsolid FALSE\n"+
                    "\t\tcoord Coordinate {\n" +
                    "                    point [");
            for (Point3D p : vertices) {
                fw.write(p.getX() + " " + p.getY() + " " + p.getZ() + ",\n");
            }
            fw.write("]\n" +
                    "                }\n" +
                    "\t\tcoordIndex [");
            for(IndexedTriangle t : triangles) {
                fw.write(t.i1 + ", " + t.i2 + ", " + t.i3 + ", -1,\n");
            }
            fw.write("]\n" +
                    "\t\t\t\n" +
                    "\t}\n" +
                    "}");
            fw.flush();
            fw.close();
            logger.info("Done writing results");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("Error writing result \n", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing result \n", e);
        }
    }

    class IndexedTriangle {
        int i1;
        int i2;
        int i3;
        IndexedTriangle(int i1, int i2, int i3){

            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
        }
    }

}
