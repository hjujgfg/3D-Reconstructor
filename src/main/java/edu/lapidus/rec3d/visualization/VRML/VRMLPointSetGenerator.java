package edu.lapidus.rec3d.visualization.VRML;

import edu.lapidus.rec3d.utils.PairCorrespData;
import edu.lapidus.rec3d.utils.helpers.DirectoryHelper;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Егор on 21.03.2016.
 */
public class VRMLPointSetGenerator {

    private final static String MULTIPLE_NAME = "pointSet3D_Mult";
    private final static String SINGLE_NAME = "pointSet3D_Single";
    Set<PairCorrespData> points;
    private final static Logger logger = Logger.getLogger(VRMLPointSetGenerator.class);
    public enum State { SINGLE, MULTIPLE}



    State state;

    public VRMLPointSetGenerator(Map<String, PairCorrespData> points, State state) {
        this.points = new HashSet<PairCorrespData>(points.values());
        this.state = state;
    }
    public VRMLPointSetGenerator(Set<PairCorrespData> res, State state) {
        this.points = res;
        this.state = state;
    }

    public void buildPointSet() {
        logger.info("Saving pointset wrl");
        try {

            StringBuilder sb1 = new StringBuilder();
            appendHeader(sb1);
            StringBuilder sb2 = new StringBuilder();
            appendHeader(sb2);
            StringBuilder color = new StringBuilder();
            color.append("color Color {\n" +
                    "                    color [\n");
            for (PairCorrespData p : points) {
                //if (p.getZ() > -0.1 && p.getZ() < 0.1 && p.getX1() > 100 && p.getX1() < 700) {
                //if (p.getZ() > -0.1 && p.getZ() < 0.1 && p.getX1() > 100 && p.getX1() < 700) {
                if (Double.isNaN(p.getY()) || Double.isNaN(p.getX()) || Double.isNaN(p.getZ())) {
                    continue;
                }

                //if (p.getY() != Double.NaN && p.getX() != Double.NaN && p.getZ() != Double.NaN) {
                    //fw.write(p.getX() * 100000 + " " + p.getY() * 100000 + " " + p.getZ() * 100000 + ",\n");
                    sb2.append(p.getX1()).append(" ").append(p.getY1()).append(" ").append(p.getZ() * 5).append(",\n");
                    sb1.append(p.getX() * 1000).append(" ").append(p.getY() * 1000).append(" ").append(p.getZ() * 1000).append(",\n");
                    //fw.write(p.getX1() + " " + p.getY1() + " " + (p.getZ() * 100000) + ",\n");
                    Color c = p.getColor();
                    double r = c.getRed() / 256.;
                    double g = c.getGreen() / 256.;
                    double b = c.getBlue() / 256.;
                    color.append(r).append(" ").append(g).append(" ").append(b).append(",\n");
                //}
            }
            sb1.append("]\n" +
                    "}\n");
            sb2.append("]\n" +
                    "}\n");
            color.append("\t]\n" +
                    " \t}\n");
            sb1.append(color.toString());
            sb2.append(color.toString());
            sb1.append("    }\n" +
                    "}");
            sb2.append("    }\n" +
                    "}");
            StringBuilder nameBuilder = new StringBuilder(DirectoryHelper.THREE_D_OUT);
            if (state == State.MULTIPLE) {
                nameBuilder.append(MULTIPLE_NAME);
            } else {
                nameBuilder.append(SINGLE_NAME);
            }
            nameBuilder.append(".wrl");
            FileWriter fw = new FileWriter(new File(nameBuilder.toString()));
            fw.write(sb1.toString());
            fw.flush();
            fw.close();
            fw = new FileWriter(new File(DirectoryHelper.POINTS_SET_PLAIN));
            fw.write(sb2.toString());
            fw.flush();
            fw.close();
            logger.info("Done saving pointset");
        } catch (IOException e) {
            logger.error("Error saving pointset", e);
        }

    }

    private void appendHeader(StringBuilder sb) {
        sb.append("#VRML V2.0 utf8\n");
        sb.append("Shape {\n" +
                "    appearance Appearance {\n" +
                "        material Material {\n" +
                "            emissiveColor 1.0 1.0 1.0\n" +
                "        }\n" +
                "    }\n" +
                "    geometry PointSet {\n" +
                "        coord Coordinate {\n" +
                "            point [\n");
    }
}
