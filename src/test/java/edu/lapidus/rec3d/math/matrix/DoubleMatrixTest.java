package edu.lapidus.rec3d.math.matrix;

import edu.lapidus.rec3d.math.Correspondence;
import edu.lapidus.rec3d.math.vector.Vector;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Егор on 11.02.2016.
 */
public class DoubleMatrixTest {
    private final static Logger logger = Logger.getLogger(DoubleMatrixTest.class);
    MatrixBuilderImpl matrixBuilder;
    @Test
    public void testSolveHomogeneous() {
       DoubleMatrix tst = matrixBuilder.createAMatrix(new Correspondence("Data/points.csv").getInititalCorrespondences());
        Vector v = tst.solveHomogeneous();
        logger.debug(v.toString());
    }

    @Before
    public void setUp() throws Exception {
        matrixBuilder = new MatrixBuilderImpl();
    }
}