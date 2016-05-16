package edu.lapidus.rec3d.machinelearning.kmeans.forms;

import javax.swing.*;

/**
 * Created by Егор on 10.05.2016.
 */
public class KmeansCorrespondencePicker {
    private KmeansDoublePanel panel;
    private JPanel panel1;

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Yo");
        KmeansCorrespondencePicker picker = new KmeansCorrespondencePicker();
        jFrame.setContentPane(picker.panel);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
