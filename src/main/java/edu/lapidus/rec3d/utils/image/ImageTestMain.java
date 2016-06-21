package edu.lapidus.rec3d.utils.image;

import java.awt.image.BufferedImage;

/**
 * Created by Егор on 11.04.2016.
 */
public class ImageTestMain {
    public static void main(String[] args) {
        ImageProcessor processor = new ImageProcessor();
        String path1 = "output/images/sheep0.png";
        String path2 = "output/images/sheep1.png";
        BufferedImage b = processor.loadImage(path1);
        BufferedImage c = processor.loadImage(path2);
        BufferedImage res = processor.subtract(b, c, 5);
        processor.saveImage(res, "output/test.png");
    }
}
