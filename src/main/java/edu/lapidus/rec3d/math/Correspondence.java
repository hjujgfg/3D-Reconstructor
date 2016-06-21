package edu.lapidus.rec3d.math;

import org.apache.log4j.Logger;

import javax.jnlp.IntegrationService;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Егор on 21.01.2016.
 */
public class Correspondence {
    private Point[][] inititalCorrespondences;
    final static Logger logger = Logger.getLogger(Correspondence.class);
    public Correspondence(String name) {
        inititalCorrespondences = loadFromfile(name);
        logger.info("Correspondences created");
    }

    public Point[][] getInititalCorrespondences() {
        return inititalCorrespondences;
    }

    public void setInititalCorrespondences(Point[][] inititalCorrespondences) {
        this.inititalCorrespondences = inititalCorrespondences;
    }

    /**
     * TODO This method MUST be removed and initial correspondences should be received in some more convenient way!!!!
     * @return Matrix of tuples - 1t image point 2nd image point.
     */
    private Point[][] tempManualPoints() {
        Point[][] p = new Point[9][];
        p[0] = new Point[2];
        p[0][0] = new Point(247, 1017);
        p[0][1] = new Point(631, 913);

        p[1] = new Point[2];
        p[1][0] = new Point(617, 905);
        p[1][1] = new Point(683, 715);

        p[2] = new Point[2];
        p[2][0] = new Point(1583, 1279);
        p[2][1] = new Point(1513, 1059);

        p[3] = new Point[2];
        p[3][0] = new Point(1817, 963);
        p[3][1] = new Point(2061, 827);

        p[4] = new Point[2];
        p[4][0] = new Point(419, 1987);
        p[4][1] = new Point(767, 1821);

        p[5] = new Point[2];
        p[5][0] = new Point(1829, 1925);
        p[5][1] = new Point(2011, 1835);

        p[6] = new Point[2];
        p[6][0] = new Point(2033, 1613);
        p[6][1] = new Point(2177, 1495);

        p[7] = new Point[2];
        p[7][0] = new Point(2589, 1818);
        p[7][1] = new Point(2925, 1777);

        p[8] = new Point[2];
        p[8][0] = new Point(1444, 2087);
        p[8][1] = new Point(1637, 1975);
        /*
        vertDisp = 0;
        for (int i = 0; i < 9; i ++) {
            System.out.println("point x dist n: " + i + " : " + (p[i][1].x - p[i][0].x));
            System.out.println("point y dist n: " + i + " : " + (p[i][1].y - p[i][0].y));
            if (Math.abs(p[i][1].y - p[i][0].y) > Math.abs(vertDisp))
                vertDisp =(int)(p[i][1].y - p[i][0].y);
        }
        System.out.println("vertical dispersy: " + vertDisp);
        */
        return p;
    }

    private Point[][] loadFromfile(String path) {
        ArrayList<Point[]> prePoints = new ArrayList<Point[]>();
        logger.info("Loading correspondences: " + path);
        //File f = new File("output/correspondences/" + name + ".csv");
        File f = new File(path);
        String line;
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while((line = br.readLine()) != null) {
                String [] items = line.split(";");
                Point first = new Point(Integer.parseInt(items[0].trim()), Integer.parseInt(items[1].trim()));
                Point second = new Point(Integer.parseInt(items[2].trim()), Integer.parseInt(items[3].trim()));
                prePoints.add(new Point[]{first, second});
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prePoints.toArray(new Point[prePoints.size()][]);
    }
}
