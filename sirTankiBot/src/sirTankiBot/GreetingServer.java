package sirTankiBot;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GreetingServer extends Thread
{
	private ServerSocket serverSocket;
	Socket server;
	JFrame frame = new JFrame();
	LinkedList<JLabel> Labels = new LinkedList<JLabel>();

	public GreetingServer(int port) throws IOException, SQLException, ClassNotFoundException, Exception
	{
		serverSocket = new ServerSocket(port);
		//serverSocket.setSoTimeout(5000);
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setVisible(false);
	}

	public void run()
	{
		while(true)
		{ 
			try
			{
				server = serverSocket.accept();
				BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
				while (Labels.size()>=8) Labels.removeFirst();
				frame.getContentPane().removeAll();
				Labels.offerLast(new JLabel(new ImageIcon(img)));
				for (int lcx=0; lcx<Labels.size(); lcx++) frame.getContentPane().add(Labels.get(lcx));
				frame.setBounds(0, 500, 4*650, 2*450);
				//frame.pack();
				frame.setVisible(true);                  
			}
			catch(SocketTimeoutException st)
			{
				System.out.println("Socket timed out!");
				break;
			}
			catch(IOException e)
			{
				e.printStackTrace();
				break;
			}
			catch(Exception ex)
			{
				System.out.println(ex);
			}
		}
	}

	public static void main(String [] args) throws IOException, SQLException, ClassNotFoundException, Exception
	{
		Thread t = new GreetingServer(3775);
		t.start();
	}
}

