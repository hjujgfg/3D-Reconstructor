package edu.lapidus.rec3d.depth;

import edu.lapidus.rec3d.machinelearning.kmeans.CorrespondenceHolder;
import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by Егор on 17.05.2016.
 */
public class CorrespondenceNormalizer {
    private List<Point> left, right;
    private List<Point> normalized1, normalized2;
    private DoubleMatrix T1;
    private DoubleMatrix T2;
    private DoubleMatrix fundamental;
    private static final Logger logger = Logger.getLogger(CorrespondenceNormalizer.class);

    private void initLists(int size) {
        logger.info("Initiating fundamental calculation");
        left = new ArrayList<>(size);
        right = new ArrayList<>(size);
    }

    public CorrespondenceNormalizer(Map<ColoredImagePoint, ColoredImagePoint> map) {
        initLists(map.size());
        correspsToTwoLists(map, left, right);
    }

    public CorrespondenceNormalizer(List<CorrespondenceHolder> holder) {
        initLists(holder.size());
        correspsToTwoLists(holder, left, right);
    }

    public CorrespondenceNormalizer(Point[][] points) {
        initLists(points.length);
        correspsToTwoLists(points, left, right);
    }

    public CorrespondenceNormalizer(List<CorrespondenceHolder> holder, Map<ColoredImagePoint, ColoredImagePoint> map) {
        initLists(holder.size() + map.size());
        correspsToTwoLists(holder, left, right);
        List<Point> t1 = new ArrayList<>(), t2 = new ArrayList<>();
        correspsToTwoLists(map, t1, t2);
        left.addAll(t1);
        right.addAll(t2);
    }

    private void correspsToTwoLists (Map<ColoredImagePoint, ColoredImagePoint> map, List<Point> first, List<Point> second) {
        for (Map.Entry<ColoredImagePoint, ColoredImagePoint> entry : map.entrySet()) {
            first.add(new Point(entry.getKey()));
            second.add(new Point(entry.getValue()));
        }
    }

    private void correspsToTwoLists (List<CorrespondenceHolder> list, List<Point> first, List<Point> second) {
        for (CorrespondenceHolder h : list) {
            first.add(new Point(h.getA()));
            second.add(new Point(h.getB()));
        }
    }

    private void correspsToTwoLists(Point[][] points, List<Point> first, List<Point> second) {
        for (Point[] pair : points) {
            first.add(pair[0]);
            second.add(pair[1]);
        }
    }

    public DoubleMatrix normalizeAndCalculateF() {
        normalize(left, right);
        calculateFundamental();
        return fundamental;
    }

    private void calculateFundamental() {
        logger.info("calculating normalized fundamental");
        DoubleMatrix A = createAMatrix(normalized1, normalized2);
        SingularValueDecomposition svd = A.SVD();
        double[] f = svd.getV().getColumn(svd.getV().getColumnDimension() - 1);
        double[][] ff = new double[3][];
        int counter = 0;
        for (int i = 0; i < 3; i ++) {
            ff[i] = new double[3];
            for (int j = 0; j < 3; j ++) {
                ff[i][j] = f[counter++];
            }
        }
        RealMatrix F = new Array2DRowRealMatrix(ff);
        SingularValueDecomposition fSvd = new SingularValueDecomposition(F);
        RealMatrix d = fSvd.getS();
        d.setEntry(2, 2, 0);
        RealMatrix u = fSvd.getU();
        RealMatrix v = fSvd.getVT();
        F = u.multiply(d).multiply(v);
        fundamental = T2.transpose().multiplyBy(new DoubleMatrix(F.getData())).multiplyBy(T1);
        logger.info("Calculated normalized fundamental: \n" + fundamental);
    }

    public void normalize(List<Point> first, List<Point> second) {
        logger.info("Normalizing correspondences");
        if (first.size() != second.size()) throw new IllegalArgumentException("Lists are not the same size");

        double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        for (int i = 0; i < first.size(); i ++) {
            x1 += first.get(i).x;
            y1 += first.get(i).y;
            x2 += second.get(i).x;
            y2 += second.get(i).y;
        }
        x1 /= first.size();
        y1 /= first.size();

        x2 /= second.size();
        y2 /= second.size();

        double s1 = calcS(left, x1, y1);
        double s2 = calcS(right, x2, y2);

        T1 = buildTMatrix(s1, x1, y1);
        T2 = buildTMatrix(s2, x2, y2);

        List<Point> p1 = new ArrayList<>(left.size());
        List<Point> p2 = new ArrayList<>(right.size());
        double[] pointContainer = new double[3];
        pointContainer[2] = 1;
        Vector temp = new Vector(pointContainer);
        for (int i = 0; i < first.size(); i ++) {
            temp.setDoubleAt(0, left.get(i).x);
            temp.setDoubleAt(1, left.get(i).y);
            Vector res = T1.postMultiply(temp);
            p1.add(new Point(res.get(0), res.get(1)));

            temp.setDoubleAt(0, right.get(i).x);
            temp.setDoubleAt(1, right.get(i).y);
            res = T2.postMultiply(temp);
            p2.add(new Point(res.get(0), res.get(1)));
        }
        normalized1 = p1;
        normalized2 = p2;
    }

    private DoubleMatrix createAMatrix(List<Point> l1, List<Point> l2) {
        double[][] A = new double[l1.size()][];
        for (int i = 0; i < l1.size(); i ++) {
            A[i] = new double[9];
            Point p1 = l1.get(i);
            Point p2 = l2.get(i);
            A[i][0] = p2.x * p1.x;
            A[i][1] = p2.x * p1.y;
            A[i][2] = p2.x;
            A[i][3] = p2.y * p1.x;
            A[i][4] = p2.y * p1.y;
            A[i][5] = p2.y;
            A[i][6] = p1.x;
            A[i][7] = p1.y;
            A[i][8] = 1;
        }
        return new DoubleMatrix(A);
    }

    private double calcS(List<Point> list, double xAvg, double yAvg) {
        double sum = 0;
        Point mean = new Point(xAvg, yAvg);
        for (Point p : list) {
            sum += p.getDistanceTo(mean);
        }
        return Math.sqrt(2) * list.size() / sum;
    }

    private DoubleMatrix buildTMatrix (double s, double xMean, double yMean) {
        DoubleMatrix res = new DoubleMatrix(3, 3);
        res.setAtPosition(0, 0, 1);
        res.setAtPosition(1, 1, 1);
        res.setAtPosition(0, 2, -xMean);
        res.setAtPosition(1, 2, -yMean);
        res.setAtPosition(2, 2, 1 / s);
        res.scale(s);
        return res;
    }

    public DoubleMatrix getFundamental() {
        return fundamental;
    }


    public static void main(String[] args) {
        Map<ColoredImagePoint, ColoredImagePoint> map = new HashMap<>(20);
        Random r = new Random();
        for (int i = 0; i < 20; i ++) {
            int x = r.nextInt(800);
            int y = r.nextInt(600);
            map.put(new ColoredImagePoint(x, y), new ColoredImagePoint(x + r.nextInt(10), y + r.nextInt(10)));
        }
        CorrespondenceNormalizer tst = new CorrespondenceNormalizer(map);
        DoubleMatrix f = tst.normalizeAndCalculateF();
        MatrixBuilderImpl mb = new MatrixBuilderImpl();
        DoubleMatrix A = mb.createAMatrix(map);
        SingularValueDecomposition svd = A.SVD();

    }
}
