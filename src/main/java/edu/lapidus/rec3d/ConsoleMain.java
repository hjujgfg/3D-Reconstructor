package edu.lapidus.rec3d;

import edu.lapidus.rec3d.utils.image.ImageProcessor;

/**
 * Created by egor.lapidus on 03/09/16.
 */
public class ConsoleMain {
    private enum OPTIONS {
        ti,
        si,
        con,
        clu,
        pp,
        br,
        help
    }

    public static void main (String [] args) {
        ConsoleMain cm = new ConsoleMain();
        cm.parseParams(args);
    }

    public void parseParams(String ... args) {
        if (args.length == 0) {
            showHelp();
            return;
        }
        OPTIONS option = OPTIONS.valueOf(args[0]);
        switch (option) {
            case ti: handleTwoImages(args); break;
            case si: handleSeveral(args); break;
            case br: {
                if (args.length != 2) {
                    showHelp();
                    return;
                }
                handleBulkResize(args[1]);
                break;
            }
            case help: showHelp(); break;
        }
    }

    private void showHelp() {
        System.out.print("Usage:\njava -jar 3d_reconstructor.jar [option] \nOptions:\n" +
        "\tti - run two images\n" +
        "\tsi - run several images\n" +
        "\tcon - run convolution test\n" +
        "\tclu - run clusterization\n" +
        "\tpp - run point picker\n" +
        "\tbr - run bulk resize of the images\n" +
        "\thelp - for help");
    }

    private void handleTwoImages(String ... args) {
        TwoImageCalculator.main(args);
    }

    private void handleSeveral(String ... args) {
        Starter.main(args);
    }

    private void handleConvolve() {

    }

    private void handleCluster() {

    }

    private void handlePointPicker() {

    }

    private void handleBulkResize(String name) {
        ImageProcessor.bulkResizeImages(name, 800, 600);
    }
}
