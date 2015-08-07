package sirTankiAutoJoin;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * A little tool for Tanki Online that
 * a) Puts the "Flash player" window in which Tanki runs to a specified position and size
 * b) Works under Windows and Mac os
 * and
 * c) Allows the user to select a battle and click "Join Red" or "Join Blue"
 * The tool will then click for the user until the battle is joined.
 * 
 * License
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 <wolframteetz@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class sirTankiAutoJoin {

	static final int winx=1024;
	static final int winy=768;
	static JFrame frame;
	static JButton redButton;
	static JButton blueButton;
	static JButton idleButton;
	static JButton pauseButton;
	static JButton quitButton;
	static JButton setupButton;
	
	public static void main (String[] args){  
		
		
		frame = new JFrame("Tanki AutoJoin");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ImageIcon backImage = new ImageIcon("background.png");
		JLabel background=new JLabel(backImage);
		frame.add(background);
		background.setLayout(new FlowLayout());

//		JPanel panel = new JPanel();
//		background.add(panel);

		JLabel label; 
//		label = new JLabel("                                                   ");
//		background.add(label); label.setForeground(Color.white);
		label = new JLabel("Only works with \"Flash Player 16\"");
		background.add(label); label.setForeground(Color.lightGray);
		label = new JLabel("Press <esc> to cancel joining a battle");
		background.add(label); label.setForeground(Color.lightGray);
		
		setupButton = new JButton("Setup Tanki Window");
		background.add(setupButton);
		setupButton.addActionListener (new SetupWindow()); 

		quitButton = new JButton("Quit");
		background.add(quitButton);
		quitButton.addActionListener (new quit()); 

		redButton = new JButton("Join Red");
		background.add(redButton);
		redButton.addActionListener (new JoinRed());

		label = new JLabel("            ");
		background.add(label); label.setForeground(Color.white);

		blueButton = new JButton("Join Blue");
		background.add(blueButton);
		blueButton.addActionListener (new JoinBlue()); 

//		label = new JLabel("                                                   ");
//		background.add(label); label.setForeground(Color.white);
//
		idleButton = new JButton("Idle");
		background.add(idleButton);
		idleButton.addActionListener (new Idle()); 

		pauseButton = new JButton("pause");
		background.add(pauseButton);
		pauseButton.addActionListener (new Pause()); 
		
		frame.setBounds(1024, 0, backImage.getIconWidth(), backImage.getIconHeight()+20);
	}
	
	static class quit implements ActionListener {        
		public void actionPerformed (ActionEvent e) {     
			System.exit(0);
		}
	}   

	static class JoinRed implements ActionListener {        
		public void actionPerformed (ActionEvent e) {     
			disableButtons();
			Robot bot;
			try {
				bot = new Robot();
				bot.setAutoDelay(100);
				bot.mouseMove(750, 760);
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				// Window active, mouse positioned
				while ( (bot.getPixelColor(750, 760).getBlue()) > 50 ) // bright gray still
				{
					bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				}
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			enableButtons();
		}
	}   
	
	static class JoinBlue implements ActionListener {        
		public void actionPerformed (ActionEvent e) {     
			disableButtons();
			Robot bot;
			try {
				bot = new Robot();
				bot.setAutoDelay(100);
				bot.mouseMove(950, 760);
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				// Window active, mouse positioned
				while ( (bot.getPixelColor(950, 760).getBlue()) > 50 ) // bright gray still
				{
					bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				}
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			enableButtons();
		}
	}
	
	static int d(int a, int b)
	{
		return a<b?(b-a):(a-b); 
	}


	static class Pause implements ActionListener {        
		public void actionPerformed (ActionEvent e) {     
			disableButtons();
			Robot bot;
			try {
				bot = new Robot();
				bot.setAutoDelay(200);
				while ( (bot.getPixelColor(522, 50).getBlue()) > 140 ) // bright gray still
				{
					bot.mouseMove(400, 400);
					bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					// 24, 39, 255
					int crep=0;
					int yel=0;
					int xmin=400; int ymin=250; int xmax=600; int ymax=550;
					BufferedImage bi = bot.createScreenCapture(new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin));
					long t = System.currentTimeMillis();
					int[] pixel;	
					for (int y = 0; y < bi.getHeight(); y+=1) {
					    for (int x = 0; x < bi.getWidth(); x+=1) {
					        pixel = bi.getRaster().getPixel(x, y, new int[3]);
							if (d(pixel[0],207)+d(pixel[1],192)+d(pixel[2],16)<2) {
								yel++;
							}
							if (
									(d(pixel[0],24)+d(pixel[1],39)+d(pixel[2],255)<2) ||
									(d(pixel[0],188)+d(pixel[1],26)+d(pixel[2],18)<8) ||
									(d(pixel[0],198)+d(pixel[1],27)+d(pixel[2],18)<8) ||
									(d(pixel[0],203)+d(pixel[1],26)+d(pixel[2],18)<8) ||
									(d(pixel[0],211)+d(pixel[1],26)+d(pixel[2],18)<8) 
							   )
							{
								crep++;
							}
					    }
					}
					System.err.println("t = "+(System.currentTimeMillis()-t)+" pixels = " + crep + " yel = "+yel);
					if (crep>0)
					{
						if (yel<5) {
							bot.keyRelease(KeyEvent.VK_SPACE);
							bot.delay(2000);
						} 
						bot.keyPress(KeyEvent.VK_SPACE);
					} else {
						bot.keyRelease(KeyEvent.VK_SPACE);
					}
					/*try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}*/
				}
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			enableButtons();
		}
	}
	
	static class Idle implements ActionListener {        
		public void actionPerformed (ActionEvent e) {     
			disableButtons();
			Robot bot;
			try {
				bot = new Robot();
				bot.setAutoDelay(200);
				while ( (bot.getPixelColor(522, 50).getBlue()) > 140 ) // bright gray still
				{
					bot.mouseMove(400, 400);
					bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					// 24, 39, 255
					int crep=0;
					int yel=0;
					int xmin=400; int ymin=250; int xmax=600; int ymax=550;
					BufferedImage bi = bot.createScreenCapture(new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin));
					long t = System.currentTimeMillis();
					int[] pixel;	
					for (int y = 0; y < bi.getHeight(); y+=1) {
					    for (int x = 0; x < bi.getWidth(); x+=1) {
					        pixel = bi.getRaster().getPixel(x, y, new int[3]);
							if (d(pixel[0],207)+d(pixel[1],192)+d(pixel[2],16)<2) {
								yel++;
							}
							if (
									(d(pixel[0],24)+d(pixel[1],39)+d(pixel[2],255)<2) ||
									(d(pixel[0],188)+d(pixel[1],26)+d(pixel[2],18)<8) ||
									(d(pixel[0],198)+d(pixel[1],27)+d(pixel[2],18)<8) ||
									(d(pixel[0],203)+d(pixel[1],26)+d(pixel[2],18)<8) ||
									(d(pixel[0],211)+d(pixel[1],26)+d(pixel[2],18)<8) 
							   )
							{
								crep++;
							}
					    }
					}
					System.err.println("t = "+(System.currentTimeMillis()-t)+" pixels = " + crep + " yel = "+yel);
					if (crep>0)
					{
						if (yel<5) {
							bot.keyRelease(KeyEvent.VK_SPACE);
							bot.delay(2000);
						} 
						bot.keyPress(KeyEvent.VK_SPACE);
					} else {
						bot.keyRelease(KeyEvent.VK_SPACE);
					}
					/*try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}*/
				}
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			enableButtons();
		}
	}

	static class SetupWindow implements ActionListener {        
		public void actionPerformed (ActionEvent e) {     
			disableButtons();
			System.err.println("Set window");
			//		osaExec("tell application \"System Events\" to activate the first window of application process \"Flash Player\"");
			if (System.getProperty("os.name").startsWith("Windows")) {
				winExec();
			} else { // Mac OS
				// sudo sqlite3 /Library/Application\ Support/com.apple.TCC/TCC.db "INSERT or REPLACE INTO access VALUES('kTCCServiceAccessibility','/usr/bin/osascript',1,1,1,NULL)"
				sysExec("/Applications/Flash Player 16.app");
				osaExec("tell application \"System Events\" to set position of first window of application process \"Flash Player\" to {0, 0}");
				osaExec("tell application \"System Events\" to set size of first window of application process \"Flash Player\" to {"+winx+", "+winy+"}");
			}
			enableButtons();
		}
	}

	private static void disableButtons() 
	{
		setupButton.setEnabled(false);
		blueButton.setEnabled(false);
		idleButton.setEnabled(false);
		pauseButton.setEnabled(false);
		redButton.setEnabled(false);
		frame.repaint();
	}

	private static void enableButtons() 
	{
		setupButton.setEnabled(true);
		blueButton.setEnabled(true);
		idleButton.setEnabled(true);
		pauseButton.setEnabled(true);
		redButton.setEnabled(true);
		frame.repaint();
	}

	/**
	 * Execute a Mac Script (osascript) - position the Tanki window correctly etc.
	 * @param s Program to execute
	 */
	public static void osaExec(String s)
	{
		String[] cmd = { "osascript", "-e", s };
		String lsString;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getErrorStream()));
			while ((lsString = bufferedReader.readLine()) != null) {
				System.out.println(lsString);
			}    
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Execure a desktop command (activate Tanki window e.g.)
	 * @param s Program to execute
	 */
	public static void sysExec(String s)
	{
		try {
			Desktop.getDesktop().open(new File(s));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Execute a windows command (e.g. position/activate Tanki window)
	 */
	private static void winExec()
	{
		String[] cmd = { "cmdow", "\"Adobe Flash Player 16\"", "/res","/mov","0","0","/siz",""+winx,""+winy,"/act"};
		String lsString;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getErrorStream()));
			while ((lsString = bufferedReader.readLine()) != null) {
				System.out.println(lsString);
			}    
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
