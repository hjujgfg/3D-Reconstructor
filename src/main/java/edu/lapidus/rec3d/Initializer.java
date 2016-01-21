package edu.lapidus.rec3d;

import com.sun.org.apache.xml.internal.security.Init;
import edu.lapidus.rec3d.math.Correspondence;
import edu.lapidus.rec3d.math.Point;
import edu.lapidus.rec3d.math.matrix.DoubleMatrix;
import edu.lapidus.rec3d.math.matrix.Matrix;
import edu.lapidus.rec3d.utils.helpers.MatrixBuilderImpl;
import edu.lapidus.rec3d.utils.image.ImageProcessor;
import edu.lapidus.rec3d.utils.interfaces.MatrixBuilder;

import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;

/**
 * Created by Егор on 21.11.2015.
 * This is going to be a main class to the program with
 */
public class Initializer {
    public static void main(String ... args) {
        Initializer init = new Initializer();
        init.init();
        init.run();
    }
    final static Logger logger = Logger.getLogger(Initializer.class);
    MatrixBuilder matrixBuilder;
    Correspondence correspondence;
    ImageProcessor imageProcessor;

    public void init() {
        logger.info("ENTERED INIT!!!");
        matrixBuilder = new MatrixBuilderImpl();
        correspondence = new Correspondence();
        imageProcessor = new ImageProcessor();
    }
    public void run () {
        DoubleMatrix Amatrix = matrixBuilder.createAMatrix(correspondence.getInititalCorrespondences());
        DoubleMatrix fundamentalMatrix = (DoubleMatrix) matrixBuilder.buildFromVector(Amatrix.solveHomogeneous(), 3, 3);
        logger.info("Calculated fundamental matrix: " + fundamentalMatrix.toString());
        BufferedImage firstImage = imageProcessor.loadImage("resources/flower1.png");
        BufferedImage secondImage = imageProcessor.loadImage("resources/flower2.png");

    }


}
