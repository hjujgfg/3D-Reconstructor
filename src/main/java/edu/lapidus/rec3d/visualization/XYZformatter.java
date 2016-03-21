package edu.lapidus.rec3d.visualization;

import edu.lapidus.rec3d.math.Point3D;
import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Егор on 05.03.2016.
 */
public class XYZformatter {
    private final static Logger logger = Logger.getLogger(XYZformatter.class);
    private final Map<String, PairCorrespData> points;

    public XYZformatter(Map<String, PairCorrespData> points) {

        this.points = points;
    }

    public void saveXYZ() {
        try {
            //FileOutputStream fos = new FileOutputStream();
            logger.info("Writing results XYZ");
            FileWriter fw = new FileWriter(new File("resources/res/result.xyz"));
            for (Map.Entry<String, PairCorrespData> entry : points.entrySet()) {
                PairCorrespData p = entry.getValue();
                fw.write("C " + p.getX() * 10000 + " " + p.getY() * 10000 + " " + p.getZ() * 10000 + "\n");
            }
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
}
