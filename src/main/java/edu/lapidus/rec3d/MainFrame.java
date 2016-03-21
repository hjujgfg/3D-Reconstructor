package edu.lapidus.rec3d;

import javax.swing.*;

/**
 * Created by Егор on 25.02.2016.
 */
public class MainFrame {
    private JPanel jPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        MainFrame m = new MainFrame();
        //m.myPanel = new MyPanel();
        //m.myPanel.setParent(frame);
        frame.setContentPane(m.myPanel);
        m.myPanel.setParent(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private MyPanel myPanel;
}
