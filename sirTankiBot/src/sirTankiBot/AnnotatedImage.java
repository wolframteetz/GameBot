package sirTankiBot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * <br>
 * Game robot, version for Tanki Online (Flash browser tank game) <br>
 * <br>
 * License URL : http://www.gnu.org/licenses/agpl-3.0.de.html <br>
 * <br>
 * See wolframteetz.wordpress.com for further information <br>
 * <br>
 * @author Wolfram Teetz &#60;wolframteetz&#64;gmail.com\&#62; <br>
 * @version 1.2.28 <br>
 * @see <br>
 * 		Category : Robot <br> 
 *      License : GNU AGPL v3 <br>
 *      Project : sirTankiBot <br>
 */

// Screen capture -> Thread?
// Include "fireOnTargetAhead flag"

/**
 * A JPanel that displays the current game window together with statistics (fps, delay): <br>
 * and annotated features = points of interest.: <br>
 * Open/close the window from the bot by pressing "0" <br>
 * There are two types of features : <br>
 * x Terrain features : When casting a ray from the tank to the border, a set of pixels belong to terrain class "A", <br> 
 *   and not to terrain class "B" followed by a group of pixels for which the opposite is true. <br>
 *   These terrain features can be detected/auto-generated by running the bot and pressing "O" when the mouse cursor<br>
 *   is just like 10 pixels below a terrain type a/terrain type b - border. The border should be horizontal. This will<br>
 *   auto-generate some detection code that can be cut and pasted to BattleConfig.<br>
 *   <br>
 *   These terrain features are displayed as little colored squares. Press "U" and "I" to cycle backwards/forwards<br>
 *   through the available terrain classes.<br>
 * x Target features : <br>
 *   Target features are a) the closest enemy tank "name" and b) three pixels belonging to the enemy tank, that is in shooting<br>
 *   range of the weapon and closest to "above" the player tank = in direct line of fire.<br>
 *   These three pixels represent the leftmost and rightmost point of the recognized title and the center of the two points.<br>
 *   The gun is aimed towards this center point.<br>
 */
public class AnnotatedImage extends JPanel {

	private static final long serialVersionUID = -3601917438595449228L;
	
	/** Holds the captured image of the tanki flash window */
	public BufferedImage image; 
	public JFrame f;
	/** FPS String */
	private String fps="";
	/** Status String (multi-line) */
	private List<String> statusString = new ArrayList<String>();
	/** Window is visible or not */
	private boolean visibleStatus = false;
	/** Colors for the terrain classes. 0-5 are standard terrain, 6 is enemy closest, 7 is enemy ahead */
	private static final int[] annotateColorTable = new int[]{ 
		((0xFF)<<16), // 0->5
		((0xFF)<<16)+((0xFF)<<8), 
		((0xFF)<<8), ((0xFF)<<8)+((0xFF)), 
		((0xFF)), ((0xFF)<<16)+((0xFF)), 
		((0xFF)<<16)+((0xFF)<<8)+((0xFF)),  
		((0xFF)<<16)+((0x80)<<8)+((0xFF)), // 5
		((0x40)<<16)+((0xFF)<<8)+((0x40)), // 6 : enemy closest
		((0xFF)<<16)+((0x40)<<8)+((0x40)), // 7 : enemy ahead
		((0x60)<<16)+((0xFF)<<8)+((0x60)), // 8 : THE enemy closest
		((0xFF)<<16)+((0x60)<<8)+((0x60)) // 9 : THE enemy ahead
	};
	
	/** Annotate point x,y with color c */
	public void annotate(int x,int y,int c)
	{
		for (int acx=-2; acx<=2; acx++) {
			for (int acy=-2; acy<=2; acy++) {
				image.setRGB( (int)Math.round(x)+acx,(int)Math.round(y)+acy, annotateColorTable[c] ); // Annotate
			}								
		}

	}

	/** Create a new, lasting image of a BufferedImage (Deep copy/clone) */
	private static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	/** Default constructor */
	public AnnotatedImage() 
	{ 
		super(); 
	} 

	/** Make a copy of the image submitted and store the copy, paint the FPS on the copy. Ready to annotate. */
	public void setImage(BufferedImage _image) 
	{ 
		{
			// Copy image
			image = deepCopy(_image);
			// Draw Text
			int x,y;
			Graphics2D g2d = image.createGraphics();
			g2d.setPaint(Color.white);
			g2d.setFont(new Font("Courier", Font.BOLD, 10));
			FontMetrics fm = g2d.getFontMetrics();
			// Draw FPS
			x = image.getWidth() - fm.stringWidth(fps) - 5;
			y = image.getHeight()-3*fm.getHeight();
			g2d.drawString(fps, x, y);
			// Draw Status String
			int n=1;
			x=10;
//			for (String s:statusString) x = fm.stringWidth(s)>x?fm.stringWidth(s):x;
//			x = image.getWidth() - x - 5;
			for (String s:statusString) {
				y = image.getHeight()-( (n+4)*fm.getHeight());
				g2d.drawString(s, x, y);
				n++;
			}
			g2d.dispose();	    

			f.setSize(image.getWidth(), image.getHeight());	    
		}
	} 

	/** Set fps */
	public void setFPS(String _fps){
		fps = _fps;
	}

	/** Set statusString */
	public void setStatusString(List<String> _statusString){
		statusString = _statusString;
	}

	/** Toggle window visibility */
	public void toggleVisible()
	{
		visibleStatus = !visibleStatus;
		f.setVisible(visibleStatus);
	}

	/** Default paint - paint the image only */
	public void paintComponent(Graphics g) 
	{
		if (image != null) {
			g.drawImage(image, 0, 0, null); 
		}
	} 

	/** Create a new frame with the image (setup) */
	public void newFrame(int posx) 
	{ 
		f = new JFrame("Annotated Tanki Frame");
		f.setBounds(posx, 0, 100, 100);
		f.add(this); 
		f.setVisible(visibleStatus);
	} 
}
