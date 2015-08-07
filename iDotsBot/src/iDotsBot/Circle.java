package iDotsBot;

import java.awt.Color;

class Circle
{
  public int x, y, radius;
  public Color color;
  
  Circle(int _x, int _y, int _color)
  {
	  x = ++_x * 410/6/2;
	  y = ++_y * 410/6/2;
	  radius = 20;//28;
	  switch(_color)
	  {
	  	case -1  : color = Color.lightGray; break;
	  	case 0 : color = Color.darkGray; break;
	  	case 1 : color = Color.blue; break;
	  	case 2 : color = Color.red; break;
	  	case 3 : color = Color.green; break;
	  	case 4 : color = Color.yellow; break;
	  	case 5 : color = Color.pink; break;
	  } 
  }
}
