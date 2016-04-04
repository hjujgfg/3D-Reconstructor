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

    public void buildPointSet() {
        logger.info("Saving pointset wrl");
        try {
            FileWriter fw = new FileWriter(new File("resources/res/pointSet.wrl"));
            fw.write("Shape {\n" +
                    "    appearance Appearance {\n" +
                    "        material Material {\n" +
                    "            emissiveColor 1.0 1.0 1.0\n" +
                    "        }\n" +
                    "    }\n" +
                    "    geometry PointSet {\n" +
                    "        coord Coordinate {\n" +
                    "            point [\n");
            StringBuilder color = new StringBuilder();
            color.append("color Color {\n" +
                    "                    color [\n");
            for (PairCorrespData p : points) {
                if (p.getZ() > -1 && p.getZ() < 1 && p.getX1() > 150 && p.getX1() < 650) {
                    //fw.write(p.getX() * 100000 + " " + p.getY() * 100000 + " " + p.getZ() * 100000 + ",\n");
                    //fw.write(p.getX1() + " " + p.getY1() + " " + (p.getZ() * 20000) + ",\n");
                    fw.write(p.getX() + " " + p.getY() + " " + p.getZ() + ",\n");
                    //fw.write(p.getX1() + " " + p.getY1() + " " + (p.getZ() * 100000) + ",\n");
                    Color c = p.getColor();
                    double r = c.getRed() / 256.;
                    double g = c.getGreen() / 256.;
                    double b = c.getBlue() / 256.;
                    color.append(r + " " + g + " " + b +",\n");
                }
            }
            fw.write("]\n" +
                    "}\n");
            color.append("\t]\n" +
                    " \t}\n");
            fw.write(color.toString());
            fw.write("    }\n" +
                    "}");
            fw.flush();
            fw.close();
            logger.info("Done saving pointset");
        } catch (IOException e) {
            logger.error("Error saving pointset", e);
        }

    }
}
