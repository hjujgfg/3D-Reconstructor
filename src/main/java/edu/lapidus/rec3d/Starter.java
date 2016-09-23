package edu.lapidus.rec3d;

import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.utils.PairCorrespData;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.visualization.VRML.VRMLPointSetGenerator;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static edu.lapidus.rec3d.TwoImageCalculator.CorrespondenceLookupType.KMEANS_AND_CONVOLVE_SOURCE;

/**
 * Created by Егор on 09.04.2016.
 */
public class Starter {

    public static void main(String[] args) {
        Starter starter = new Starter(3, "sheep");
        starter.run();
    }

    private final static Logger logger = Logger.getLogger(Starter.class);

    private final static String IMAGE_LOCATION = "output/images/";
    private final static String CORRESPS_LOCATION = "output/correspondences/";
    private final static MatrixBuilderImpl matrixBuilder = new MatrixBuilderImpl();
    DoubleMatrix[] k;
    DoubleMatrix[] r;
    String[] images;
    String[] corresps;
    int numOfImages;

    DoubleMatrix testRotation;

    public Starter(int numberOfImages, String name) {
        numOfImages = numberOfImages;
        k = new DoubleMatrix[numberOfImages];
        r = new DoubleMatrix[numberOfImages];
        images = new String[numberOfImages];
        corresps = new String[numberOfImages - 1];
        for (int i = 0; i < numberOfImages; i ++) {
            images[i] = IMAGE_LOCATION + name + i + ".png";
            if (i < numberOfImages - 1)
                corresps[i] = CORRESPS_LOCATION + name + i + ".csv";
            k[i] = initK();
        }
        initRs(r);
    }

    public void run() {
        Set<PairCorrespData> results = new HashSet<PairCorrespData>();
        for (int i = 0; i < numOfImages - 1; i++ ) {
            logger.info("Starting images: " + images[i] + "    " + images[i + 1] + "\n Correspondences: " + corresps[i] + "\n" );
            double scaleFactor = 1;
            /*if (i == 0)
                scaleFactor = 5;*/
            TwoImageCalculator calculator = new TwoImageCalculator(k[i], k[i + 1], r[i], r[i + 1], images[i], images[i + 1], corresps[i], KMEANS_AND_CONVOLVE_SOURCE, scaleFactor);
            results.addAll(calculator.run().values());
            testRotation = calculator.getR2();
        }
        VRMLPointSetGenerator generator = new VRMLPointSetGenerator(results, VRMLPointSetGenerator.State.MULTIPLE);
        generator.buildPointSet();
    }

    private void addPoints(Set<PairCorrespData> to, Set<PairCorrespData> from) {

    }

    private DoubleMatrix initK() {
        //return matrixBuilder.createCalibrationMatrix(692, 519, 400, 300);
        return matrixBuilder.createCalibrationMatrix(692, 519, 400, 300);
    }

    private DoubleMatrix initR(double angle, int axis) {
        return matrixBuilder.createRotationMatrix(angle, axis);
    }
    //TODO it's a temporal method
    private void initRs(DoubleMatrix[] rs) {
        /*rs[0] = initR(0, MatrixBuilder.Y_AXIS);
        rs[1] = initR(-20, MatrixBuilder.Y_AXIS).multiplyBy(initR(-7, MatrixBuilder.X_AXIS));
        rs[2] = initR(-20, MatrixBuilder.Y_AXIS).multiplyBy(initR(-7, MatrixBuilder.X_AXIS));
        rs[3] = initR(-20, MatrixBuilder.Y_AXIS).multiplyBy(initR(-7, MatrixBuilder.X_AXIS));*/
        //rs[2] = rs[1].multiplyBy(rs[2]);
        /*rs[3] = initR(-60, MatrixBuilder.Y_AXIS);
        rs[4] = initR(-80, MatrixBuilder.Y_AXIS);
        rs[5] = initR(-100, MatrixBuilder.Y_AXIS);*/
        rs[0] = matrixBuilder.createRotationMatrixFull(0, 0, 0);
        rs[1] = matrixBuilder.createRotationMatrixFull(0, -12, 0);
        rs[2] = matrixBuilder.createRotationMatrixFull(0, -22, 0);
        //rs[3] = matrixBuilder.createRotationMatrixFull(0, -30, 0);
        //rs[3] = matrixBuilder.createRotationMatrixFull(0, -40, 0);
        testRotation = matrixBuilder.createRotationMatrixFull(0,0,0);
    }
}
