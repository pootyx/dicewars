package main;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {
        setVisible(true);
        setResizable(true);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        validate();
    }

    public static void main(String[] args) {
        new Main();

    }

    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawRect(100,100,100,100);
        g.drawLine(100, 100, 200, 200);
    }


}
