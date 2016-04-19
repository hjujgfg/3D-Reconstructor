package edu.lapidus.rec3d.StaticsForTests;

import edu.lapidus.rec3d.depth.threaded.EpipolarLineHolder;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Егор on 04.04.2016.
 */
public class BigStatic {
    private final static Logger logger = Logger.getLogger(BigStatic.class);
    //sheep
    /*private final static int SECOND_POINT_LOOKUP_WIDTH = 240;
    private final static int SECOND_POINT_SHIFT = 20;

    private final static int COLOR_REGION_RADIUS = 10;
    private final static int HEIGHT_DIFF_LIM = 20;
    private final static double HEIGHT_DIFF_WEIGHT = 5;
    private final static double WIDTH_DIFF_WEIGHT = 10;*/
    //cup
    /*private final static int SECOND_POINT_LOOKUP_WIDTH = 240;
    private final static int SECOND_POINT_SHIFT = 80;

    private final static int COLOR_REGION_RADIUS = 20;
    private final static int HEIGHT_DIFF_LIM = 60;
    private final static double HEIGHT_DIFF_WEIGHT = 5;
    private final static double WIDTH_DIFF_WEIGHT = 5;*/
    private final static int SECOND_POINT_LOOKUP_WIDTH = 120;
    private final static int SECOND_POINT_SHIFT = -10;

    private final static int COLOR_REGION_RADIUS = 20;
    private final static int HEIGHT_DIFF_LIM = 10;//5
    private final static int WIDTH_DIFF_LIM = 3;
    private final static double HEIGHT_DIFF_WEIGHT = 300;
    private final static double WIDTH_DIFF_WEIGHT = 3;


    public static Vector calculateEpipoleFromFundamental(DoubleMatrix fund) {
        SingularValueDecomposition svd = fund.transpose().SVD();
        RealMatrix v = svd.getV();
        Vector e = new Vector(v.getColumn(2));
        e = e.scalar(1/(e.get(2) * 1000));
        logger.info("epipole : " + e);
        //return new Vector(v.getColumn(v.getColumnDimension() - 1));
        return e;
    }

    public static int[] calcSecondPointAlongX(int[] firstPoint, DoubleMatrix fundamental, ColorMatrix img1, ColorMatrix img2, ArrayList<Vector> coeffs, ArrayList<EpipolarLineHolder> lines) {
        Vector first = new Vector(firstPoint);
        Vector coefficients = fundamental.postMultiply(first);
        coeffs.add(coefficients);
        //TODO it would be nice to use Gradient Descent here to find the most similar point from the second image!!!
        //TODO also think how to bound this -100 - + 100 thing, in case our line is almost horizontal it may cause issues
        int[] result = new int[3];
        result[2] = 1;
        result[0] = Integer.MIN_VALUE;
        result[1] = Integer.MIN_VALUE;
        double minDiff = Double.MAX_VALUE;
        int widthClass = 0;
        /*if (firstPoint[0] >= 180 && firstPoint[0] < 510 ) {
            widthClass = 50;
        } else {
            widthClass = -20;
        }*/
        int startX = firstPoint[0] + SECOND_POINT_SHIFT - widthClass - (SECOND_POINT_LOOKUP_WIDTH / 2),
                endX = firstPoint[0] + SECOND_POINT_SHIFT - widthClass + (SECOND_POINT_LOOKUP_WIDTH / 2);
        if (startX < 0) {
            startX = 0;
            endX = SECOND_POINT_LOOKUP_WIDTH;
        } else if (endX >= img1.getWidth()) {
            endX = img1.getWidth();
            startX = img1.getWidth() - SECOND_POINT_LOOKUP_WIDTH - 1;
        }
        //TODO this is only for debugging, eats much resources
        EpipolarLineHolder TMP = new EpipolarLineHolder(firstPoint, coefficients.getVec());
        for (int x2 = startX; x2 < endX - 4; x2 += 4) {
            int y2 = (int)( ( - coefficients.get(2) - coefficients.get(0) * x2 )  / coefficients.get(1) );

            if (y2 < 0)
                y2 = 0;
            if (y2 >= img2.getHeight())
                y2 = img2.getHeight() - 1;
            TMP.addLinePoint(x2, y2);
            double tmp = 1000;
            try {
                tmp = evaluateSimilarity(firstPoint, new int[]{x2, y2}, img1, img2);
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error(String.format("Index out of bounds: %d : %d; %d : %d", firstPoint[0], firstPoint[1], x2, y2));
            }
            if (tmp < minDiff) {
                minDiff = tmp;
                result[0] = x2;
                result[1] = y2;
            }
        }
        TMP.setSecondPoint(result[0], result[1]);
        lines.add(TMP);
        return result;
    }
    private static double evaluateSimilarity(int[] point1, int[] point2, ColorMatrix img1, ColorMatrix img2) {
        Color[] firstSample = getColorRegionX(img1, point1);
        Color[] secondSample = getColorRegionX(img2, point2);
        double meanDiff = 0;
        for (int i = 0; i < firstSample.length; i++) {
            meanDiff += comparePixels(firstSample[i], secondSample[i]);
        }

        int heightDiff = point1[1] - point2[1];
        heightDiff = (heightDiff - HEIGHT_DIFF_LIM) * (heightDiff - HEIGHT_DIFF_LIM);
        int widthDiff = point1[0] - point2[0] - WIDTH_DIFF_LIM; // it was -30
        widthDiff *= widthDiff * WIDTH_DIFF_WEIGHT;
        logger.info("Mean diff: " + meanDiff + " height diff: " + heightDiff * HEIGHT_DIFF_WEIGHT + " width: " + widthDiff);
        meanDiff += heightDiff * HEIGHT_DIFF_WEIGHT + widthDiff;
        logger.info("TOTAL DIFF: " + meanDiff);
        return meanDiff / (2 * (firstSample.length + 1));
    }

    private static Color[] getColorRegionX(ColorMatrix img, int[] point) {
        Color[] res = new Color[COLOR_REGION_RADIUS * 4];
        for (int i = 1, j = 0; i <= COLOR_REGION_RADIUS; i ++, j += 4) {
            //TODO GOVNOKOD INITIATED
            //Here we take colors from image in cross pattern
            Color c = null;
            try {
                c = img.getColor(point[0] - i, point[1]);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j] = c;
            try {
                c = img.getColor(point[0], point[1] - i);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j + 1] = c;
            try {
                c = img.getColor(point[0] + i, point[1]);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j + 2] = c;
            try {
                c = img.getColor(point[0], point[1] + i);
            } catch (IndexOutOfBoundsException e) {
                c = Color.magenta;
            }
            res[j + 3] = c;
        }
        return res;
    }

    private static double comparePixels(Color first, Color second) {
        int redDiff = first.getRed() - second.getRed();
        int greenDiff = first.getGreen() - second.getGreen();
        int blueDiff = first.getBlue() - second.getBlue();

        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;
    }
}
