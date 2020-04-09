package main;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends JFrame {

    public Main() {
        setVisible(true);
        setResizable(true);
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        validate();
        handleClick();
    }

    public static void main(String[] args) {
        new Main();
    }

    public void handleClick(){
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                System.out.println(e.getLocationOnScreen());
            }
        });
    }

    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        drawMap(g);
    }

    private void drawMap(Graphics g){
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3,3));
//        for(int x = 100; x < 600; x += 100) {
//            for(int y = 100; y < 600; y += 100) {
//                g.drawRect(x,y,100,100);
//                new Field(x, y, 100, 100).drawField(g);
//            }
//        }
    }


}
