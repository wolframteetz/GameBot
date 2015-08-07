package sirTankiBot;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class sirTankiServer {

	static ServerSocket serverSocket = null;
//	final static int[] serverPorts = new int[] { 3972, 3973, 3974, 3975, 3976, 3977, 3978, 3979, 3780 };
	final static int[] serverPorts = new int[] { 3977, 3978, 3979, 3780 };
//	final static int[] serverPorts = new int[] { 3780 };
	static boolean sendImage = false;
	
	public static String[][] issueCommand ( String command ) throws IOException
	{
		String[][] results = new String[serverPorts.length-1][2];
		ArrayList<Socket> outSockets = new ArrayList<Socket>();
		ArrayList<PrintWriter> outWriter = new ArrayList<PrintWriter>();
		ArrayList<BufferedReader> inReader= new ArrayList<BufferedReader>();
		// Open connection
		for (int connectionNumber=0; connectionNumber<serverPorts.length-1; connectionNumber++) {
			Socket echoSocket = new Socket("127.0.0.1", serverPorts[connectionNumber]);
			outSockets.add(echoSocket);
			outWriter.add( new PrintWriter(echoSocket.getOutputStream(), true) );
			inReader.add( new BufferedReader(
					new InputStreamReader(echoSocket.getInputStream())
					));
		}
		// Issue command
		for (int connectionNumber=0; connectionNumber<serverPorts.length-1; connectionNumber++) {
			outWriter.get(connectionNumber).println(command); // issue command
		}
		// Read response
		for (int n=0; n<2; n++) { // read 2 lines to stderr (start and end of command)
			for (int connectionNumber=0; connectionNumber<serverPorts.length-1; connectionNumber++) {
				results[connectionNumber][n] = inReader.get(connectionNumber).readLine();
				System.err.println(results[connectionNumber][n]); // Echo response to stderr
			}
		}            	
		// Return response
		for (int connectionNumber=0; connectionNumber<serverPorts.length-1; connectionNumber++) {
			outSockets.get(connectionNumber).close();
		}

		return results;
	}
	
	public static void initShowThread() {
		Runnable helloRunnable = new Runnable() {
		    public void run() {
		    	if (sendImage) GreetingClient.main(new String[]{});
		    }
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);		
	}
	
	public static void main(String args[])
	{
		
		System.err.println("sirTankiBot starting");
		System.err.println("--------------------");		
		System.err.println("");		
		
		initShowThread();
		sirTankiBot.mainInit();		
		
		// Idle the robot
		try {
			Robot bot = new Robot();
			bot.setAutoDelay(50);
			bot.keyPress(KeyEvent.VK_8); // idle ON
			bot.keyRelease(KeyEvent.VK_8);
		} catch (AWTException e) {
		}
		
		System.err.println("sirTankiServer v1.3 ");
		System.err.println("--------------------");
		System.err.println("Available commands : reset, login, join, start, show, quit, register");
		Socket clientSocket = null;
		try
		{
			BufferedReader stdIn =
					new BufferedReader(
							new InputStreamReader(System.in));
			serverSocket = create(serverPorts);
			if (serverSocket.getLocalPort()==3780) { // Master
				// Server idles the robot
				try {
					Robot bot = new Robot();
					bot.keyPress(KeyEvent.VK_DIVIDE);
					bot.keyRelease(KeyEvent.VK_DIVIDE);	
				} catch (AWTException e) {
				}
				// Server doesn't listen (until now...)
				serverSocket.close();
				// Server DOES listen to images from clients
				Thread imageListeningThread = new GreetingServer(3775);
				imageListeningThread.start();
				
				String command;
				do {
					System.err.println("Command> ");
					command = stdIn.readLine();
					
					if (command.equalsIgnoreCase("x")) {
						Altbot bot;
						try {
							bot = new Altbot();
//							bot.keyPress(KeyEvent.VK_DIVIDE);
//							bot.keyRelease(KeyEvent.VK_DIVIDE);

							sysExec("/Applications/Google Chrome.app");

							{
								String[] getName = {System.getProperty("user.dir")+"/resetChrome.sh"};
								unixExecWait(getName); 
							}

							sysExec("/Applications/Google Chrome.app");
							bot.delay(4000);
							osaExec("tell application \"System Events\" to set position of first window of application process \"Google Chrome\" to {0, 0}");
							bot.delay(500);
							osaExec("tell application \"System Events\" to set size of first window of application process \"Google Chrome\" to {1000,800}");
							bot.delay(2000);
							bot.mouseMove(140, 80);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.keyPress(KeyEvent.VK_META);
							bot.keyPress(KeyEvent.VK_A);
							bot.keyRelease(KeyEvent.VK_META);
							bot.keyRelease(KeyEvent.VK_A);
							bot.type("http://tankionline.com/ru/#friend=QY35k2CMl3WkSnyxGEZIinGr5kcc16zL5rJP4cSEdhreIV10mcVpAnp1hQy5jzIC");
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(8000);
							bot.mouseMove(770, 270);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(8000);

							int off=10; // 0 for de 10 for RU
							while (bot.getPixelColor(340, 105).getGreen() < bot.getPixelColor(340, 105).getRed() + bot.getPixelColor(340, 105).getBlue())
							{
								// Get a name
								String[] getName = {System.getProperty("user.dir")+"/getName.sh"};
								String name = unixExecWait(getName).concat("_").concat(unixExecWait(getName));
								System.err.println(name);
								bot.delay(5000);
								bot.mouseMove(600, 370+off);
								bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
								bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
								bot.type(name);
								bot.delay(1000);
								String password = unixExecWait(getName).concat("_").concat(unixExecWait(getName));				
								password.concat(getRandomString());
								bot.delay(5000);
								bot.mouseMove(600, 410+off);
								bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
								bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
								bot.type(password);
								bot.delay(1000);
								bot.mouseMove(600, 450+off);
								bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
								bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
								bot.delay(1000);
								bot.type(password);
								bot.mouseMove(420-2*off, 490+off);
								bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
								bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
								bot.delay(1000);
								bot.mouseMove(620, 585-off);
								bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
								bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
								bot.delay(20000);
							}

							// WE SHOULD BE IN A BATTLE NOW ?!?					
							// Set size etc.
							System.err.println("Size battle");

							osaExec("tell application \"System Events\" to set size of first window of application process \"Google Chrome\" to {1200,840}");
							sysExec("/Applications/Google Chrome.app");
							for (int cx=0; cx<10; cx++)
							{
								bot.keyPress(KeyEvent.VK_ADD);
								bot.delay(50);
								bot.keyRelease(KeyEvent.VK_ADD);
								bot.delay(50);
							}
							for (int cx=0; cx<6; cx++)
							{  
								bot.keyPress(KeyEvent.VK_MINUS);
								bot.delay(50);
								bot.keyRelease(KeyEvent.VK_MINUS);
								bot.delay(50);
							}			

							// Equip twins
							bot.mouseMove(890, 105);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(200);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(15000);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(500);

							bot.delay(500);
							bot.mouseMove(1100, 800);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.mouseMove(700, 800);
							bot.delay(500);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseMove(900, 800);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseMove(850, 500);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.mouseMove(1187, 109);
							bot.delay(500);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);

							// GO! 


							int n = 177;
							while (bot.getPixelColor(32, 125).getRed()<200) // ranked up?
							{

								System.err.println("Check battle");
								// Check battle for too little players
								boolean littlePlayers = false;
								bot.keyPress(KeyEvent.VK_TAB);
								bot.delay(1000);
								if (
										(bot.getPixelColor(600, 540).getGreen() < bot.getPixelColor(600, 540).getRed() + bot.getPixelColor(600, 540).getBlue()) &&
										(bot.getPixelColor(600, 545).getGreen() < bot.getPixelColor(600, 545).getRed() + bot.getPixelColor(600, 545).getBlue()) &&
										(bot.getPixelColor(600, 550).getGreen() < bot.getPixelColor(600, 550).getRed() + bot.getPixelColor(600, 550).getBlue()) )
								{ 
									littlePlayers=true;
								}
								bot.keyRelease(KeyEvent.VK_TAB);
								bot.delay(50);						 
								if (littlePlayers) {
									bot.mouseMove(1187, 109);
									bot.delay(500);
									bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
									bot.delay(500);
									bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
									bot.delay(500);
									bot.keyPress(KeyEvent.VK_ENTER);
									bot.keyRelease(KeyEvent.VK_ENTER);
									bot.delay(8000);
									bot.keyPress(KeyEvent.VK_ENTER);
									bot.keyRelease(KeyEvent.VK_ENTER);
									bot.delay(500);
									boolean ninc = false;
									while (bot.getPixelColor(500, 700).getGreen() > bot.getPixelColor(500, 700).getRed() + bot.getPixelColor(500, 700).getBlue()) {
										bot.mouseMove(600, n);
										bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
										bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
										bot.delay(2500);
										bot.mouseMove(1111, 827);
										bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
										bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
										bot.delay(2500);
										if (n<277) n += 20; else n=177;
										ninc=true;
									}
									if (!ninc) {
										if (n<277) n += 20; else n=177;
									}
									bot.delay(8000);
									// Acknownledge any shit after battle join
									bot.keyPress(KeyEvent.VK_ENTER);
									bot.keyRelease(KeyEvent.VK_ENTER);
									bot.delay(500);
									if (bot.getPixelColor(17, 840).getGreen() > 100)  { // Chat open?
										bot.keyPress(KeyEvent.VK_ENTER);
										bot.keyRelease(KeyEvent.VK_ENTER);
										bot.delay(500);
									}
								} else {
									System.err.println("Play for a minute");

									//
									bot.keyPress(KeyEvent.VK_DIVIDE);
									bot.keyRelease(KeyEvent.VK_DIVIDE);
									bot.keyPress(KeyEvent.VK_7);
									bot.keyRelease(KeyEvent.VK_7);
									bot.keyPress(KeyEvent.VK_9);
									bot.keyRelease(KeyEvent.VK_9);
									bot.keyPress(KeyEvent.VK_H);
									bot.keyRelease(KeyEvent.VK_H);
									bot.keyPress(KeyEvent.VK_K);
									bot.keyRelease(KeyEvent.VK_K);
									bot.keyPress(KeyEvent.VK_DIVIDE);
									bot.keyRelease(KeyEvent.VK_DIVIDE);
									//
									try {
										Thread.sleep(60000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									//
									bot.keyPress(KeyEvent.VK_DIVIDE);
									bot.keyRelease(KeyEvent.VK_DIVIDE);
									bot.keyPress(KeyEvent.VK_H);
									bot.keyRelease(KeyEvent.VK_H);
									bot.keyPress(KeyEvent.VK_K);
									bot.keyRelease(KeyEvent.VK_K);
									bot.keyPress(KeyEvent.VK_DIVIDE);
									bot.keyRelease(KeyEvent.VK_DIVIDE);
									//
								}
							}

							System.err.println("RANKED UP - LOGOUT");						
							// Log out					
							bot.mouseMove(1187, 109);
							bot.delay(500);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(8000);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(500);
							bot.mouseMove(1187, 109);
							bot.delay(500);
							bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							bot.delay(500);
							bot.keyPress(KeyEvent.VK_ENTER);
							bot.keyRelease(KeyEvent.VK_ENTER);
							bot.delay(8000);

//							bot.keyPress(KeyEvent.VK_DIVIDE);
//							bot.keyRelease(KeyEvent.VK_DIVIDE);
							
						} catch (AWTException e1) {
							e1.printStackTrace();
						}
					} else {
						issueCommand("mag976 "+command);
					}
				} while (!command.equals("quit"));
				
				System.err.println("Closing client connections...");
			} else { 								// Client
				while (true) {
					//Service the connection
					System.err.println("Waiting for connection...");
					clientSocket = serverSocket.accept();
					ServiceClient(clientSocket);
				}
			}
		}
		catch (Exception ioe)
		{
			System.err.println("Error in SimpleWebServer: " + ioe);
		}
	}
	
	private static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 2) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	
	public static void ServiceClient_Reset(DataOutputStream outbound)
			throws IOException
	{
		outbound.writeBytes("reset       #"+serverSocket.getLocalPort()+"\n");
		try {
			Robot bot = new Robot();	
			bot.setAutoDelay(200);
			// Idle the robot
			bot.keyPress(KeyEvent.VK_8); // idle ON
			bot.keyRelease(KeyEvent.VK_8);
			// End all running TankiBots
//			bot.keyPress(KeyEvent.VK_V);
//			bot.delay(100);
//			bot.keyRelease(KeyEvent.VK_V);
//			bot.delay(100);
			// Login on server RU25 to reset logged in games on server RU23 and "killall flash player"
			String[] f = {System.getProperty("user.dir")+"/resetTanki.sh"};
			unixExecWait(f);		
		} catch (AWTException e) {
			e.printStackTrace();
			outbound.writeBytes("ERROR");
		}
		outbound.writeBytes("reset done  #"+serverSocket.getLocalPort()+"\n");
	}
	
	public static void ServiceClient_Login(DataOutputStream outbound)
			throws IOException
	{
		outbound.writeBytes("login       #"+serverSocket.getLocalPort()+"\n");
		boolean timeout = false;
		long startTime = System.currentTimeMillis();
		try {
			Robot bot = new Robot();	
			bot.setAutoDelay(200);
			bot.keyPress(KeyEvent.VK_8); // idle ON
			bot.keyRelease(KeyEvent.VK_8);
			// Launch Flash on RU23
			unixExec("/Users/sir/Desktop/TankiBot/startTanki.sh");
			bot.delay(3000); // Make sure flash player has opened the window
			while (!timeout && (bot.getPixelColor(62, 55).getRed() < 30)) // not yet loaded, still dark gray
			{
				// Careful. Pixel 117=Ready, Pixel=52=Popup visible! 
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				timeout = (System.currentTimeMillis()-startTime)>45000;
			}
		} catch (AWTException e) {
			e.printStackTrace();
			outbound.writeBytes("ERROR");
			timeout = true;
		}
		
		if (!timeout) {
			outbound.writeBytes("login    done  #"+serverSocket.getLocalPort()+"\n");
		} else {
			outbound.writeBytes("login    ERROR #"+serverSocket.getLocalPort()+"\n");
		}
	}

	public static void ServiceClient_JoinBattle(DataOutputStream outbound)
			throws IOException
	{
		outbound.writeBytes("join battle #"+serverSocket.getLocalPort()+"\n");
		boolean timeout = false;
		long startTime = System.currentTimeMillis();
		int joinBattleAttempts = 0;
		try {
			Robot bot = new Robot();	
			bot.setAutoDelay(200);
			bot.keyPress(KeyEvent.VK_8); // idle ON
			bot.keyRelease(KeyEvent.VK_8);
			// Set window position and size
			osaExec("tell application \"System Events\" to set position of first window of application process \"Flash Player\" to {0, 0}");
			bot.delay(200);
			osaExec("tell application \"System Events\" to set size of first window of application process \"Flash Player\" to {"+Board.winx+", "+Board.winy+"}");
			bot.delay(200);
			bot.mouseMove(30,80); // on rank icon
			// Activate window
			bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			// Accept any popups that might arise. If we are in battle, open and close chat twice.
			bot.keyPress(KeyEvent.VK_ENTER);
			bot.keyRelease(KeyEvent.VK_ENTER);
			bot.keyPress(KeyEvent.VK_ENTER);
			bot.keyRelease(KeyEvent.VK_ENTER);
			bot.keyPress(KeyEvent.VK_ENTER);
			bot.keyRelease(KeyEvent.VK_ENTER);
			bot.keyPress(KeyEvent.VK_ENTER);
			bot.keyRelease(KeyEvent.VK_ENTER);
			
			boolean join_success = false;
			do {
				// Find friends' battle y coordinate
				int y;
				for (y=120+4; y<860-120; y++) {
					int c = bot.getPixelColor(674, y).getGreen() +
							bot.getPixelColor(674, y-4).getGreen();
					if (c>400) break;
				}
				// Click on battle with friends
				bot.mouseMove(500, y);
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				bot.delay(500);
				bot.mouseMove(500, y);
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				bot.delay(500);
				// Click on join
				bot.mouseMove(1125, 885);
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				// Click on join
				bot.mouseMove(1125-250, 885); // Try other team
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				// Wait for join process to start
				bot.delay(500);
				// Check if join has worked
				int c;
				c = bot.getPixelColor(1100, 880).getRed();
				if (c<150) join_success = true;
				joinBattleAttempts++;
				timeout = (System.currentTimeMillis()-startTime)>45000;
			} while ( (!join_success) && (!timeout)); // 45 seconds max
			if (timeout) // TOO LATE
			{
				outbound.writeBytes("join     FAILED #"+serverSocket.getLocalPort()+" after attempt # "+joinBattleAttempts+"\n");
				return;
			}

			timeout = false;
			startTime = System.currentTimeMillis();
			// Wait for the battle to complete load (Randomly check 4 pixels in playing field to be non-dark-gray
			while (bot.getPixelColor(600, 400).getRed()<30 && bot.getPixelColor(620, 400).getGreen()<30 && 
				   bot.getPixelColor(580, 444).getRed()<30 && bot.getPixelColor(600, 444).getBlue()<30  &&
				   (!timeout) ) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				timeout = (System.currentTimeMillis()-startTime)>45000;
			}
		} catch (AWTException e) {
			outbound.writeBytes("ERROR");
			e.printStackTrace();
			timeout = true;
		}
		if (!timeout) {
			outbound.writeBytes("join     done   #"+serverSocket.getLocalPort()+" on attempt # "+joinBattleAttempts+"\n");
		} else {
			outbound.writeBytes("join     INCPL. #"+serverSocket.getLocalPort()+" on attempt # "+joinBattleAttempts+"\n");
		}
	}

	public static void ServiceClient_StartRobot(DataOutputStream outbound)
			throws IOException
	{
		outbound.writeBytes("start    robot #"+serverSocket.getLocalPort()+"\n");
		try {
			Robot bot = new Robot();	
			bot.setAutoDelay(200);
			// Start robot
			bot.keyPress(KeyEvent.VK_NUMPAD9); // Autostart
			bot.delay(16000);
			bot.keyPress(KeyEvent.VK_7); // idle OFF!
			bot.keyRelease(KeyEvent.VK_7);
		} catch (AWTException e) {
			outbound.writeBytes("ERROR");
			e.printStackTrace();
		}
		outbound.writeBytes("start    done  #"+serverSocket.getLocalPort()+"\n");
	}

	// Send an image of what's going on
	public static void ServiceClient_Show(DataOutputStream outbound)
			throws IOException
	{
		outbound.writeBytes("show     robot #"+serverSocket.getLocalPort()+"\n");
		GreetingClient.main(new String[]{});
		outbound.writeBytes("show     done  #"+serverSocket.getLocalPort()+"\n");
	}	

	public static void ServiceClient_Register(DataOutputStream outbound) throws IOException
	{
		outbound.writeBytes("register robot #"+serverSocket.getLocalPort()+"\n");

		outbound.writeBytes("register done  #"+serverSocket.getLocalPort()+"\n");
	}	

	public static void ServiceClient(Socket client)
			throws IOException
	{
		DataInputStream inbound = null;
		DataOutputStream outbound = null;
		inbound = new DataInputStream( client.getInputStream());
		outbound = new DataOutputStream( client.getOutputStream());
		
		String[] command = inbound.readLine().split("\\s");
		if (command[0].startsWith("mag976")) // magic word to execute anything
		{
			switch(command[1]) // java 7!
			{
			case "reset" :
				ServiceClient_Reset(outbound);
				break;
			case "login" :
				ServiceClient_Login(outbound);
				break;
			case "join" :
				ServiceClient_JoinBattle(outbound);
				break;
			case "start" :
				ServiceClient_StartRobot(outbound);
				break;
			case "show" :
				ServiceClient_Show(outbound);
				break;
			case "register" :
				ServiceClient_Register(outbound);
				break;
			case "showon" :
				sendImage = true;
				break;
			case "showoff" :
				sendImage = false;
				break;
			case "quit" :
				ServiceClient_Reset(outbound);
				// Clean up
				outbound.close();
				inbound.close();
				client.close();
				System.exit(0);
				break;
			}
		} else {
			outbound.writeBytes("unknown"+"\n");
			outbound.writeBytes("command"+"\n");
		}
		outbound.close();
		inbound.close();
	}

	public static ServerSocket create(int[] ports) throws IOException {
		for (int port : ports) {
			try {
				ServerSocket s = new ServerSocket(port); 
				System.err.println("Listening on port "+port);
				return s;
			} catch (IOException ex) {
				continue; // try next port
			}
		}

		// if the program gets here, no port in the range was found
		throw new IOException("No free port found");
	}

	/**
	 * Execute a desktop command (activate Tanki window e.g.)
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
	 * Execute a Unix command
	 * @param s Program to execute
	 */
	public static void unixExec(String cmd) {
		try {
			ProcessBuilder pb = new ProcessBuilder(
					cmd);
			Process p = pb.start();     // Start the process.
			//			p.waitFor();                // Wait for the process to finish.
			System.err.println("Script executed successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	/**
	 * Execute a Unix command and wait for it to complete before returning
	 * @param s Program to execute
	 */
	public static String unixExecWait(String[] cmd) {
		try {
			ProcessBuilder pb;
			switch(cmd.length) 
			{
			case 1:
				 pb = new ProcessBuilder( cmd[0] ); break;
			case 2:
				 pb = new ProcessBuilder( cmd[0], cmd[1] ); break;
			default:
				 pb = new ProcessBuilder( cmd[0], cmd[1], cmd[2] ); break;
			}
			Process p = pb.start();     // Start the process.
			p.waitFor(45,TimeUnit.SECONDS);
			//			p.waitFor();                // Wait for the process to finish.
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line=input.readLine();
			System.err.println("Script executed successfully");
            return line;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}	

	/**
	 * Execute a Mac Script (osascript) - position the Tanki window correctly etc.
	 * @param s Program to execute
	 */
	private static void osaExec(String s)
	{
		String[] cmd = { "osascript", "-e", s };
		String lsString;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getErrorStream()));
			while ((lsString = bufferedReader.readLine()) != null) {
				System.err.println(lsString);
			}    
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
