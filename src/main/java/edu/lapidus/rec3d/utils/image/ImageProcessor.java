package edu.lapidus.rec3d.utils.image;

import edu.lapidus.rec3d.depth.threaded.EpipolarLineHolder;
import edu.lapidus.rec3d.machinelearning.kmeans.Centroid;
import edu.lapidus.rec3d.machinelearning.kmeans.CorrespondenceHolder;
import edu.lapidus.rec3d.math.ColoredImagePoint;
import edu.lapidus.rec3d.math.matrix.ColorMatrix;
import edu.lapidus.rec3d.utils.PairCorrespData;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Created by Егор on 21.11.2015.
 */
public class ImageProcessor {
    final static Logger logger = Logger.getLogger(ImageProcessor.class);

    final static String STORAGE_DIR = "resources/res/";

    public BufferedImage loadImage(String path) {
        logger.info("Started loading " + path);
        File f = new File(path);
        BufferedImage img = null;
        try {
            img = ImageIO.read(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("error loading " + path + "\n" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("error loading " + path + "\n" + e.toString());
        }
        logger.info("Successfully loaded " + path);
        return img;
    }

    public void createDepthMap(Map<String, PairCorrespData> points) {
        File f = new File(STORAGE_DIR + "depth.png");
        logger.info("Building depth image");
        BufferedImage map = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        for (Map.Entry<String, PairCorrespData> entry : points.entrySet()) {
            String [] coords = entry.getKey().split("_");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            double z = entry.getValue().getZ() * 1000000;
            int i = (int)z;
            if (i < -382)
                i = -382;
            if (i > 382) {
                i = 382;
            }

            map.setRGB(x, y, calcColorX(z).getRGB());
        }
        try {
            ImageIO.write(map, "png", f);
            logger.info("Saving success");
        } catch (IOException e) {
            logger.error("Error saving image ", e);
        }
    }

    private Color calcColor(int depth) {
        int red = 0, green = 0, blue = 0 ;
        if (depth > -383 && depth < -127) {
            red = depth + 383;
        } else if (depth > -128 && depth < 127) {
            green = depth + 127;
        } else if (depth > 127 && depth < 383) {
            blue = depth - 128;
        }
        return new Color(red, green, blue);
    }

    private Color calcColor(double depth) {
        int x = (int)depth;
        //x += 700;
        //x *= 100000;
        if (x <= 0)
            x = 0;
        int r = x % 256;
        int tmp = x / 256;
        int g = tmp % 256;
        tmp = tmp / 256;
        int b = tmp % 256;
        return new Color(r, g, b);
    }

    private Color calcColorX(double depth) {
        int x = (int) depth;
        x += 320;
        if (x < 0) {
            x = 0;
        }
        int r = x % 256;
        x -= r;
        x /= 10;
        int g = x % 256;
        x -= g;
        x /= 10;
        int b = x % 256;
        return new Color(r,g,b);

    }

    public void saveImage(BufferedImage img, String fileName) {
        try {
            ImageIO.write(img, "png", new File(fileName));
            logger.info("Saved image " + fileName);
        } catch (IOException e) {
            logger.error("Error saving image", e);
        }
    }

    public void visualizeCorresps(Collection<PairCorrespData> points, String i1, String i2, int numOfPoints) {
        logger.info("Saving correspondences");
        BufferedImage combined = buildCombined(i1, i2);
        Graphics g = combined.getGraphics();
        ArrayList<PairCorrespData> inner = new ArrayList(points);
        Random r = new Random();
        for (int i = 0; i < numOfPoints; i ++) {
            int index = r.nextInt(inner.size());
            PairCorrespData p = inner.get(index);
            int x1 = p.getX1();
            int y1 = p.getY1();
            int x2 = p.getX2() + combined.getWidth() / 2;
            int y2 = p.getY2();
            g.setColor(new Color(r.nextInt()));
            g.drawLine(x1, y1, x2, y2);
        }
        saveImage(combined, "resources/res/corresps.png");
    }

    public void visualizeEpipolarLines(List<EpipolarLineHolder> lines, String i1, String i2, int numOfLines) {
        BufferedImage combined = buildCombined(i1, i2);
        Graphics g = combined.createGraphics();
        Random r = new Random();
        int w = combined.getWidth() / 2;
        for (int i = 0; i < numOfLines; i ++) {
            int ind = r.nextInt(lines.size());
            EpipolarLineHolder e = lines.get(ind);
            int[] f = e.getFirstPoint();
            int[] s = e.getSecondPoint();
            s[0] += w;
            g.setColor(new Color(r.nextInt()));
            g.drawOval(f[0], f[1], 3, 10);
            g.drawOval(s[0], s[1], 3, 10);
            double[] coefficients = e.getCoefficients();

            for (int[] p : e.getLine()) {
                g.drawOval(p[0] + w, p[1], 1, 1);
            }

            /*int[] l = e.getLine().get(0);
            g.drawOval(l[0] + w, l[1], 10, 3);*/

            /*int x1 = w;
            int y1 = (int)((-1 * ( coefficients[2] + coefficients[0] * x1 )) / coefficients[1]);

            int x2 = combined.getWidth() - 1;
            int y2 = (int)((-1 * ( coefficients[2] + coefficients[0] * x2 )) / coefficients[1]);

            g.drawLine(x1, y1, x2, y2);*/
        }
        saveImage(combined, "resources/res/epipoles.png");
    }

    public BufferedImage buildCombined(String i1, String i2) {
        BufferedImage img1 = loadImage(i1);
        BufferedImage img2 = loadImage(i2);
        BufferedImage combined = new BufferedImage(img1.getWidth() + img2.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.createGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, img1.getWidth(), 0, null);
        return combined;
    }

    public BufferedImage subtract(BufferedImage a, BufferedImage bb, int threshold) {
        if (a.getHeight() != bb.getHeight() || a.getWidth() != bb.getWidth()) {
            throw new IllegalArgumentException("Dimmensions does not correspond");
        }
        BufferedImage res = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < a.getWidth(); x ++) {
            for (int y = 0; y < a.getHeight(); y ++) {
                Color c1 = new Color(a.getRGB(x, y));
                Color c2 = new Color(bb.getRGB(x, y));
                int r = c1.getRed() - c2.getRed();
                int g = c1.getGreen() - c2.getGreen();
                int b = c1.getBlue() - c2.getBlue();
                if (r < threshold && b < threshold && g < threshold) {
                    res.setRGB(x, y, Color.GREEN.getRGB());
                } else {
                    res.setRGB(x, y, c2.getRGB());
                }
            }
        }
        return res;
    }
    //TODO govnokod
    private static int counter = 0;
    public static void bulkResizeImages(String name, int newWidth, int newHeight) {
        String dir = "resources/images/"+name +"/";
        counter = 0;
        try {
            Files.walk(Paths.get(dir)).forEach(filePath -> {
                try {
                    if (filePath.toString().endsWith(".jpg")) {
                        BufferedImage originalImage = ImageIO.read(filePath.toFile());
                        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                        BufferedImage img = resizeImage(originalImage, type, newWidth, newHeight);
                        ImageIO.write(img, "png", new File(dir + "res/" + name + counter + ".png"));
                        counter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeImage(BufferedImage original, int type, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    private void changeBackGround(String sourcePath) {
        BufferedImage source = loadImage(sourcePath);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < source.getWidth(); x ++) {
            for (int y = 0; y < source.getHeight(); y ++) {
                Color s = new Color(source.getRGB(x, y));
                int r = s.getRed();
                int g = s.getGreen();
                int b = s.getBlue();
                Color res;
                if (r == g && g == b) {
                    res = new Color(255, 255, 255);
                } else {
                    res = s;
                }
                result.setRGB(x, y, res.getRGB());
            }
        }
        saveImage(result, "no_background.png");
    }

    public void saveCorrClusters(String i1, String i2, List<Centroid> c1, List<Centroid> c2) {
        BufferedImage combined = buildCombined(i1, i2);
        Graphics g = combined.createGraphics();
        for (int i = 0; i < c1.size(); i ++) {
            Centroid a = c1.get(i);
            Centroid b = c2.get(i);
            g.drawLine(a.getX(), a.getY(), b.getX() + combined.getWidth() / 2, b.getY());
        }
        saveImage(combined, "resources/clustering/combined.png");
    }

    public void saveCorrespsByKmeans(String i1, String i2, List<CorrespondenceHolder> corresps) {
        BufferedImage combined = buildCombined(i1, i2);
        Graphics g = combined.createGraphics();
        Random r = new Random();
        for (CorrespondenceHolder pp : corresps) {
            if (pp.getDistance() > 11) continue;
            ColoredImagePoint p1 = pp.get(0);
            ColoredImagePoint p2 = pp.get(1);
            Color c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
            g.setColor(c);
            g.drawOval(p1.getX() - 2, p1.getY() - 2, 4, 4);
            g.drawOval(p2.getX() + (combined.getWidth() / 2) - 2, p2.getY() - 2, 4, 4);
            g.drawLine(p1.getX(), p1.getY(), p2.getX() + (combined.getWidth() / 2) , p2.getY());
        }
        saveImage(combined, "resources/clustering/combined2.png");
    }

    public BufferedImage toGrayScale(BufferedImage source) {
        BufferedImage res = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = res.createGraphics();
        g.drawImage(source, 0, 0, null);
        return res;
    }

    public BufferedImage applyKernel(BufferedImage img, Kernel k) {
        logger.info("Applying Kernel");
        ConvolveOp op = new ConvolveOp(k);
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        return op.filter(img, res);
    }

    public BufferedImage removeGreen(BufferedImage img) {
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        for (int y = 0; y < img.getHeight(); y ++) {
            for (int x = 0; x < img.getWidth(); x ++) {
                Color c = new Color(img.getRGB(x, y));
                int cVal = c.getRGB();
                if (c.getGreen() > c.getBlue() && c.getGreen() > c.getRed()) {
                    res.setRGB(x, y, Color.GREEN.getRGB());
                } else {
                    res.setRGB(x, y, cVal);
                }
             }
        }
        return res;
    }

    public int[] grayToIntArray(BufferedImage img) {
        int[] res = new int[img.getHeight() * img.getWidth()];
        int i = 0;
        for (int y = 0; y < img.getHeight();  y ++) {
            for (int x = 0; x < img.getWidth(); x ++) {
                res [i ++] = new Color(img.getRGB(x,y)).getRed();
            }
        }
        return res;
    }

    public BufferedImage intArrToImg(int[] arr, int width, int height) {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int counter = 0;
        for (int y = 0; y < height; y ++) {
            for (int x= 0; x < width; x ++) {
                res.setRGB(x, y, arr[counter]);//new Color(arr[counter], arr[counter], arr[counter]).getRGB());
                counter ++;
            }
        }
        return res;
    }

    public BufferedImage normalize(BufferedImage img, int top) {
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        BufferedImage noBack = removeGreen(img);
        int topPixelY = 0;
        boolean exit = false;
        for (int y = 0; y < img.getHeight() && !exit; y ++) {
            for (int x = 0; x < img.getWidth(); x ++) {
                if (! new Color(noBack.getRGB(x, y)).equals(Color.GREEN)) {
                    topPixelY = y;
                    exit = true;
                    break;
                }
            }
        }
        Graphics g = res.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, res.getWidth(), res.getHeight());
        g.drawImage(img, 0, top - topPixelY, null);
        return res;
    }

    public static void main(String [] args) {
        //bulkResizeImages("sheep_holder", 800, 600);
        /*ImageProcessor p = new ImageProcessor();
        for (int i = 0; i < 3; i ++) {
            BufferedImage tst = p.loadImage("resources/images/sheep" + i + ".png");
            tst = p.removeGreen(tst);
            tst = p.toGrayScale(tst);
            p.saveImage(p.applyKernel(tst, KernelFactory.buildYYGaussianKernel(9)), "resources/convolve/xy9" + i + ".png");
            p.saveImage(p.applyKernel(tst, KernelFactory.buildYYGaussianKernel(15)), "resources/convolve/xy15" + i + ".png");
            p.saveImage(p.applyKernel(tst, KernelFactory.buildYYGaussianKernel(21)), "resources/convolve/xy21" + i + ".png");
        }*/
        ImageProcessor p = new ImageProcessor();
        for (int i = 0; i < 5; i ++) {
            BufferedImage res = p.loadImage("resources/images/sheep/res/sheep" + i + ".png");
            p.saveImage(p.normalize(res, 100), "resources/images/sheep/normalized/sheep" + i +".png");
        }
    }


}
