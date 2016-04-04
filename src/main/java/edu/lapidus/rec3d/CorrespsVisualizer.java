package edu.lapidus.rec3d;

import javax.swing.*;

/**
 * Created by Егор on 04.04.2016.
 */
public class CorrespsVisualizer {
    private MyPanel myPanel1;
    private JPanel panel1;
    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        MyPanel.state = 2;
        CorrespsVisualizer m = new CorrespsVisualizer();
        //m.myPanel = new MyPanel();
        //m.myPanel.setParent(frame);
        frame.setContentPane(m.myPanel1);
        m.myPanel1.setParent(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
