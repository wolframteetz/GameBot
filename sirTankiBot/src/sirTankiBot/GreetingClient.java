package sirTankiBot;

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

public class GreetingClient
{
	public static void main(String [] args)
	{
		String serverName = "localhost";
		int port = 3775;
		try
		{
			Socket client = new Socket(serverName, port);
			Robot bot;
			bot = new Robot();


			BufferedImage before = bot.createScreenCapture(new Rectangle(0, 50, 600*2, 440*2));
			double factor = 0.5;
			int w = (int)(before.getWidth()*factor);
			int h = (int)(before.getHeight()*factor);
			BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(factor,factor);
			AffineTransformOp scaleOp = 
			   new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			bimg = scaleOp.filter(before, bimg);
			
//			Image img = bot.createScreenCapture(new Rectangle(0, 50, 2500/4*2, 550*2)).getScaledInstance(2500/4, 500, Image.SCALE_SMOOTH);
//			BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
//		    Graphics g = bimg.createGraphics();
//		    g.drawImage(img, 0, 0, null);
//		    g.dispose();		    

//			BufferedImage bimg = bot.createScreenCapture(new Rectangle(0, 50, 2500/4, 550));
		    
			ImageIO.write(bimg,"JPG",client.getOutputStream());
			client.close();
		} catch(IOException | AWTException e) {
			e.printStackTrace();
		}
	}
}
