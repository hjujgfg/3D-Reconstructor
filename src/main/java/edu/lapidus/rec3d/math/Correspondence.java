package edu.lapidus.rec3d.math;

import org.apache.log4j.Logger;

/**
 * Created by Егор on 21.01.2016.
 */
public class Correspondence {
    private Point[][] inititalCorrespondences;
    final static Logger logger = Logger.getLogger(Correspondence.class);
    public Correspondence() {
        inititalCorrespondences = tempManualPoints();
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
        p[0][0] = new Point(605, 841);
        p[0][1] = new Point(673, 665);

        p[1] = new Point[2];
        p[1][0] = new Point(849, 837);
        p[1][1] = new Point(1041, 677);

        p[2] = new Point[2];
        p[2][0] = new Point(1633, 457);
        p[2][1] = new Point(1681, 245);

        p[3] = new Point[2];
        p[3][0] = new Point(1885, 497);
        p[3][1] = new Point(2141, 337);

        p[4] = new Point[2];
        p[4][0] = new Point(1741, 1149);
        p[4][1] = new Point(1869, 985);

        p[5] = new Point[2];
        p[5][0] = new Point(2025, 1625);
        p[5][1] = new Point(2173, 1497);

        p[6] = new Point[2];
        p[6][0] = new Point(1537, 2113);
        p[6][1] = new Point(1705, 2009);

        p[7] = new Point[2];
        p[7][0] = new Point(2153, 2145);
        p[7][1] = new Point(2369, 2097);

        p[8] = new Point[2];
        p[8][0] = new Point(409, 1969);
        p[8][1] = new Point(773, 1813);
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
}
