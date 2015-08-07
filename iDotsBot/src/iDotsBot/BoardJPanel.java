package iDotsBot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class BoardJPanel extends JPanel {

    private Image img;
    Circles circles = new Circles();  //added this

    public BoardJPanel(Image img, Circles _cList) {
        this.img = img;
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);

        circles = _cList;
//        circles = new Circles(); //added this
    }

    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
        if (circles!=null) 
        	circles.draw((Graphics2D)g); //added this
    }
}