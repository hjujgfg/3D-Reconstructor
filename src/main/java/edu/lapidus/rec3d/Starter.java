package edu.lapidus.rec3d;

import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.matrix.Matrix;
import edu.lapidus.rec3d.utils.PairCorrespData;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;
import edu.lapidus.rec3d.visualization.VRML.VRMLPointSetGenerator;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Егор on 09.04.2016.
 */
public class Starter {

    public static void main(String[] args) {
        Starter starter = new Starter(3, "sheep");
        starter.run();
    }

    private final static Logger logger = Logger.getLogger(Starter.class);

    private final static String IMAGE_LOCATION = "resources/images/";
    private final static String CORRESPS_LOCATION = "resources/correspondences/";
    private final static MatrixBuilder matrixBuilder = new MatrixBuilderImpl();
    DoubleMatrix[] k;
    DoubleMatrix[] r;
    String[] images;
    String[] corresps;
    int numOfImages;

    public Starter(int numberOfImges, String name) {
        numOfImages = numberOfImges;
        k = new DoubleMatrix[numberOfImges];
        r = new DoubleMatrix[numberOfImges];
        images = new String[numberOfImges];
        corresps = new String[numberOfImges - 1];
        for (int i = 0; i < numberOfImges; i ++) {
            images[i] = IMAGE_LOCATION + name + i + ".png";
            if (i < numberOfImges - 1)
                corresps[i] = CORRESPS_LOCATION + name + i + ".csv";
            k[i] = initK();
        }
        initRs(r);
    }

    public void run() {
        Set<PairCorrespData> results = new HashSet<PairCorrespData>();
        for (int i = 0; i < numOfImages - 1; i++ ) {
            logger.info("Starting images: " + images[i] + "    " + images[i + 1] + "\n Correspondences: " + corresps[i] + "\n" );
            TwoImageCalculator calculator = new TwoImageCalculator(k[i], k[i + 1], r[i], r[i+1], images[i], images[i + 1], corresps[i]);
            results.addAll(calculator.run().values());
        }
        VRMLPointSetGenerator generator = new VRMLPointSetGenerator(results);
        generator.buildPointSet();
    }

    private void addPoints(Set<PairCorrespData> to, Set<PairCorrespData> from) {

    }

    private DoubleMatrix initK() {
        return matrixBuilder.createCalibrationMatrix(692, 692, 400, 300);
    }

    private DoubleMatrix initR(double angle, int axis) {
        return matrixBuilder.createRotationMatrix(angle, axis);
    }
    //TODO it's a temporal method
    private void initRs(DoubleMatrix[] rs) {
        rs[0] = initR(0, MatrixBuilder.Y_AXIS);
        rs[1] = initR(-30, MatrixBuilder.Y_AXIS).multiplyBy(initR(-7, MatrixBuilder.X_AXIS));
        rs[2] = initR(-50, MatrixBuilder.Y_AXIS);
        /*rs[3] = initR(-60, MatrixBuilder.Y_AXIS);
        rs[4] = initR(-80, MatrixBuilder.Y_AXIS);
        rs[5] = initR(-100, MatrixBuilder.Y_AXIS);*/
    }
}
