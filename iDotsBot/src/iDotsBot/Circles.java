package iDotsBot;

import java.awt.Graphics2D;
import java.util.ArrayList;

class Circles
{
  
  ArrayList<Circle> circles;

  public Circles()
  {
    circles = new ArrayList<Circle>();
    for (int i=0; i<6; i++)
    circles.add(new Circle(i,i,i));
  }

  public void draw(Graphics2D g) // draw must be called by paintComponent of the panel
  {
    for (Circle c : circles) {
    	g.setColor(c.color);
    	g.setBackground(c.color);
    	g.fillOval(c.x, c.y, c.radius, c.radius);
    }
  }
  
  public void clear() {
	    circles = new ArrayList<Circle>();
  }
  
  public void addCircle(Circle c)
  {
	  circles.add(c);
  }
  
}