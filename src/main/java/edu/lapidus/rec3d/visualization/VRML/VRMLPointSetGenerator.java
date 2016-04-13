package edu.lapidus.rec3d.visualization.VRML;

import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;

import java.awt.*;
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
    Set<PairCorrespData> points;
    private final static Logger logger = Logger.getLogger(VRMLPointSetGenerator.class);

    public VRMLPointSetGenerator(Map<String, PairCorrespData> points) {
        this.points = new HashSet<PairCorrespData>(points.values());
    }
    public VRMLPointSetGenerator(Set<PairCorrespData> res) {
        this.points = res;
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
                if (p.getZ() > -1 && p.getZ() < 1 && p.getX1() > 100 && p.getX1() < 700) {
                    //fw.write(p.getX() * 100000 + " " + p.getY() * 100000 + " " + p.getZ() * 100000 + ",\n");
                    sb2.append(p.getX1() + " " +  (p.getY1()) + " " + (p.getZ() * 600000) + ",\n");
                    sb1.append(p.getX() * 1000 + " " + (p.getY() * 1000) + " " + p.getZ() * 1000 + ",\n");
                    //fw.write(p.getX1() + " " + p.getY1() + " " + (p.getZ() * 100000) + ",\n");
                    Color c = p.getColor();
                    double r = c.getRed() / 256.;
                    double g = c.getGreen() / 256.;
                    double b = c.getBlue() / 256.;
                    color.append(r + " " + g + " " + b +",\n");
                }
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
            FileWriter fw = new FileWriter(new File("resources/res/pointSet3D.wrl"));
            fw.write(sb1.toString());
            fw.flush();
            fw.close();
            fw = new FileWriter(new File("resources/res/pointSetPlain.wrl"));
            fw.write(sb2.toString());
            fw.flush();
            fw.close();
            logger.info("Done saving pointset");
        } catch (IOException e) {
            logger.error("Error saving pointset", e);
        }

    }

    private void appendHeader(StringBuilder sb) {
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
