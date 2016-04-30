package edu.lapidus.rec3d.machinelearning.kmeans;

import javax.swing.*;

/**
 * Created by Егор on 30.04.2016.
 */
public class KmeansMain {
    private JPanel panel1;
    private ImageDrawer imageDrawer1;

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Main frame");
        KmeansMain mainFrame = new KmeansMain();
        jFrame.setContentPane(mainFrame.imageDrawer1);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
