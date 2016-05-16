package edu.lapidus.rec3d.utils.image;

import java.awt.image.BufferedImage;

/**
 * Created by Егор on 15.05.2016.
 */
public class NonMaxSuppression {
    static int[] input;
    static int[] output;
    int progress;
    int width;
    int height;
    int convolveX[] = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
    int convolveY[] = {-1, -1, -1, 0, 0, 0, 1, 1, 1};
    int templateSize = 3;
    public void nonmax() {
        progress=0;
    }

    public void init(int[] inputIn, int widthIn, int heightIn) {
        width=widthIn;
        height=heightIn;
        input = inputIn;
        output = new int[width*height];

    }
    public int[] process() {
        progress=0;

        // first convolute image with x and y templates
        int diffx[] = new int[width*height];
        int diffy[] = new int[width*height];
        int mag[] = new int[width*height];

        int valx, valy;

        for(int x = templateSize / 2; x < width - (templateSize / 2); x++) {
            for(int y= templateSize / 2; y < height- (templateSize / 2); y++) {
                valx = 0;
                valy = 0;
                for(int x1 = 0; x1 < templateSize; x1++) {
                    for(int y1 = 0; y1 < templateSize; y1++) {
                        int pos = (y1 * templateSize + x1);
                        int imPos = (x + (x1 - (templateSize / 2))) + ((y + (y1 - (templateSize / 2))) * width);

                        valx +=((input[imPos]&0xff) * convolveX[pos]);
                        valy +=((input[imPos]&0xff) * convolveY[pos]);
                    }
                }
                diffx[x + (y * width)] = valx;
                diffy[x + (y * width)] = valy;
                mag[x + (y * width)] = (int)(Math.sqrt((valx * valx) + (valy * valy)));
            }
        }

        for(int x = 1; x < width - 1; x++) {
            progress++;
            for(int y = 1 ; y < height - 1; y++) {
                int dx, dy;

                if(diffx[x + (y * width)] > 0) dx = 1;
                else dx = -1;

                if(diffy[x + (y * width)] > 0) dy = 1;
                else dy = -1;

                int a1, a2, b1, b2, A, B, point, val;
                if(Math.abs(diffx[x + (y * width)]) > Math.abs(diffy[x + (y * width)]))
                {
                    a1 = mag[(x+dx) + ((y) * width)];
                    a2 = mag[(x+dx) + ((y-dy) * width)];
                    b1 = mag[(x-dx) + ((y) * width)];
                    b2 = mag[(x-dx) + ((y+dy) * width)];
                    A = (Math.abs(diffx[x + (y * width)]) - Math.abs(diffy[x + (y * width)]))*a1 + Math.abs(diffy[x + (y * width)])*a2;
                    B = (Math.abs(diffx[x + (y * width)]) - Math.abs(diffy[x + (y * width)]))*b1 + Math.abs(diffy[x + (y * width)])*b2;
                    point = mag[x + (y * width)] * Math.abs(diffx[x + (y * width)]);
                    if(point >= A && point > B) {
                        val = Math.abs(diffx[x + (y * width)]);
                        output[x + (y * width)] = 0xff000000 | ((int)(val) << 16 | (int)(val ) << 8 | (int)(val));
                    }
                    else {
                        val = 0;
                        output[x + (y * width)] = 0xff000000 | ((int)(val) << 16 | (int)(val ) << 8 | (int)(val));
                    }
                }
                else
                {
                    a1 = mag[(x) + ((y-dy) * width)];
                    a2 = mag[(x+dx) + ((y-dy) * width)];
                    b1 = mag[(x) + ((y+dy) * width)];
                    b2 = mag[(x-dx) + ((y+dy) * width)];
                    A = (Math.abs(diffy[x + (y * width)]) - Math.abs(diffx[x + (y * width)]))*a1 + Math.abs(diffx[x + (y * width)])*a2;
                    B = (Math.abs(diffy[x + (y * width)]) - Math.abs(diffx[x + (y * width)]))*b1 + Math.abs(diffx[x + (y * width)])*b2;
                    point = mag[x + (y * width)] * Math.abs(diffy[x + (y * width)]);
                    if(point >= A && point > B) {
                        val = Math.abs(diffy[x + (y * width)]);
                        output[x + (y * width)] = 0xff000000 | ((int)(val) << 16 | (int)(val ) << 8 | (int)(val));
                    }
                    else {
                        val = 0;
                        output[x + (y * width)] = 0xff000000 | ((int)(val) << 16 | (int)(val ) << 8 | (int)(val));
                    }
                }
            }
        }

        return output;
    }

    public int getProgress() {
        return progress;
    }

    public static void main(String[] args) {
        ImageProcessor p = new ImageProcessor();
        for (int i = 0; i < 3; i ++) {
            BufferedImage img1 = p.loadImage("resources/images/sheep"+i+".png");
            img1 = p.removeGreen(img1);
            img1 = p.toGrayScale(img1);
            img1 = p.applyKernel(img1, KernelFactory.buildYYGaussianKernel(21));
            int [] input = p.grayToIntArray(img1);
            NonMaxSuppression suppression = new NonMaxSuppression();
            suppression.init(input, img1.getWidth(), img1.getHeight());
            int[] res = suppression.process();
            BufferedImage result = p.intArrToImg(res, img1.getWidth(), img1.getHeight());
            p.saveImage(result, "resources/convolve/suppression"+i+".png");
        }




    }
}
