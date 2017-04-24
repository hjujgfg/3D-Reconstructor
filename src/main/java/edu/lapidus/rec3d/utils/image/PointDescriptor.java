package edu.lapidus.rec3d.utils.image;

import edu.lapidus.rec3d.math.vector.Vector;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by egor.lapidus on 03/01/2017.
 */
public class PointDescriptor {

    private final static ImageProcessor processor = new ImageProcessor();
    private final static Logger logger = Logger.getLogger(PointDescriptor.class);

    private int descriptorLength  = 128;

    private BufferedImage xGrad, yGrad;

    public PointDescriptor(BufferedImage original, int size) {
        xGrad = processor.applyKernel(processor.toGrayScale(original), KernelFactory.buildXSimpleGradientKernel());
        yGrad = processor.applyKernel(processor.toGrayScale(original), KernelFactory.buildYSimpleGradientKernel());
        descriptorLength = 128;
    }

    public Vector getGradientAtPoint(int x, int y) {
        double xg = new Color(xGrad.getRGB(x,y)).getRed();
        double yg = new Color(yGrad.getRGB(x,y)).getRed();
        return new Vector(xg, yg);
    }

}
