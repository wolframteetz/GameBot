package sirTankiBot;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import sirTankiBot.BattleConfig.MapName;
import sirTankiBot.BattleConfig.MapTime;
import sirTankiBot.BattleConfig.TankType;
import sirTankiBot.BattleConfig.ViewType;
import sirTankiBot.BattleConfig.WeaponType;

/**
 * <br>
 * Game robot, version for Tanki Online (Flash browser tank game) <br>
 * <br>
 * License URL : http://www.gnu.org/licenses/agpl-3.0.de.html <br>
 * <br>
 * This program <br>
 * - Captures and processes the screen where the game is displayed  <br>
 * - Extracts the player color  <br>
 * - Extracts enemy positions on screen. This is currently done by detecting the "text writing" of the player name above the tank and determining <br>
 *     whether it is of enemy color. TODO: Find the CENTER of the writing (a little to the left is the tank...). <br>
 * -   Has an auto-fire mode that automatically fires if there is an enemy detected straight ahead <br>
 * -   Has an auto-targeting mode that automatically turns the turret towards the nearest enemy <br>
 * -   Has a greedy-follow mode that automatically drives and turns towards an enemy ahead <br>
 * - Finds "nearest" terrain features along a set to rays projected from the tank center. <br>
 *   Terrain features are points, in which a terrain class "a" changes into a terrain class "b". <br>
 *   Terrain classes are defined in BattleConfig. They are set by giving the min and max RGB values of each terrain pattern. <br> 
 *   See class BattleConfig for details. <br>
 * -   Has a record function that records the currently detected terrain features into a List <br>
 * -   Has a save/load function for that list <br>
 * -   Has a autopilot function, that, based on the currently detected terrain "replays" the user-recorded action when this terrain is met. <br>
 * -   Has a "autogenerate terrain feature" function, that prints a "Terrain detection line in java" to the console. Position the Mouse with <br>
 *     the tip to the lower right of the terrain change and press "O" to print a terrain detection line to the console. This can be cut and  <br>
 *     pasted to the BattleConfig Class to extend the terrain detection <br>
 *     <br>
 *     NOTE: <br>
 *     Using this software VIOLATES THE STANDARD USAGE AGREEMENT and may result in your ACCOUNT BEING BLOCKED OR BANNED. <br>
 *     <br>
 *     == IT IS ILLEGAL TO USE THIS SOFTWARE ON TANKIONLINE.COM WITHOUT PREVIOUSLY CONTACTING TANKIONLINE AND GETTING A USAGE EXCEPTION == <br>
 *     <br>
 *     You need to WRITE TO TANKIONLINE and get their AGREEMENT to use this software PRIOR TO LAUNCHING IT! <br>
 *     <br>     
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES <br>
 *     WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF <br>
 *     MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR <br>
 *     ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES <br>
 *     WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN <br>
 *     ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF <br>
 *     OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. <br>
 *      <br>
 *     This software uses JNATIVEHOOOK (https://code.google.com/p/jnativehook/) to capture user interaction with Tanki for recording it. <br>
 *     JNATIVEHOOK is licensed under the GNU Lesser General Public License <br>
 *     Keep this in mind when packaging this code into a JAR. <br>
 * <br>     
 * See <a href="http://wolframteetz.wordpress.com">http://wolframteetz.wordpress.com</a> for further information <br>
 * <br>
 * @author Wolfram Teetz &#60;wolframteetz&#64;gmail.com\&#62; <br>
 * @version 1.2.28 <br>
 * @see <br>
 * 		Category : Robot <br> 
 *      License : GNU AGPL v3 <br>
 *      Project : sirTankiBot <br>
 */

public class sirTankiBot implements NativeKeyListener {

	// General
											/** Java robot (Keypresses, capture screen)  */
	Robot bot;								
											/** The representation of the tanki board */
	Board board;							
											/** Battle configuration (Which tank, map, weapon is used, which terrain types are there) */
	BattleConfig battleConfig;				
											/** Keys that are currently held pressed, 0=w, 1=a, 2=s, 3=d, 4=y, 5=x, 6=space */
	boolean[] keysDown;						

	//	Keypress related
											/** Keydelay is the delay the program waits after each keypress not to flood the buffer. Use this or lazy keypressing, then set this to 0 */
	int keydelay=0;
											/** keyThreadDelay is the frequency in milliseconds keys are pressed from the key press buffer. Use this or keydelay with direct keypresses. */
	int keyThreadDelay=100;
											/** If true, just idle the robot */
	boolean idleMode = false;		
											/** If true, do not process user keypresses except "NUMPAD /" = Enable keyboard */
	boolean keyboardOffMode = false;		
											/** Smooth firing cycles */
	final int smoothFiringCycles = 20;						
											/** Smooth firing counter, will fire until multiple frames (count) do not contain target */
	int smoothFiring;						
											/** The key press service (every keyThreadDelay ms) has encountered an empty queue. Ready to keypress immediately. */
	Boolean keyPressQueueEmpty = true;		
	int keyPressQueueEmptyCounter = 0;		
	int fireCounter = 0;		
											/** Key press service */
	ScheduledExecutorService keyPressExec;  
											/** Execute key press runnable */
	Runnable keyPressRunnable; 				
											/** Queue of keys to be pressed. The bot will wait for this queue to be empty
											 ** before or after screen capturing and analyzing. 
											 ** Advantage of emptying before capture : The delay between capture and new keypresses is minimized. 
											 **   The action is more appropriate because is is closer to the "real current state"
											 ** Advantage of emptying after the capture : Higher framerate. The capture processing is done while keys are still pressed
											 ** Current solution : Get the queue down to max. 1 keypress before starting the capture (that means max. 2*keydelay forced capture time) */
	LinkedList<Integer> keyPressQueue = new LinkedList<Integer>();
											/** System time at last automatic keypress */	
	long lastKeypressSystemMillis = 0;		
											/** Recording/Terrain recognition related */
	boolean recording=false;				
											/** Currently recording terrain-based driving  */
	boolean auto_on=false;					
											/** Fire directly, do not write to keyboard buffer... */
	boolean fireWithoutKeyboardBuffer = true; 
											/** The turret is currently turned. Do not drive based on terrain, first center the turret! 
											 *  All known statuses. Will exectute the k-d-nearest neighbour on "auto-exec" mode */
	LinkedList<Status> statusList = new LinkedList<Status>();	
											/** The k-d-tree of the statusList. Can be used as a faster variant of brute force
											 *  nearest neighbour search for the above. Currently, there is a bug ::TODO:: in the method.
											 */
	KDTree<Status> statusTree;									
											/** Look ahead of the status. e.g. 2 means take 2 frames ahead with the current keyboard input. 
											 * Increase this value to turn "earlier" or for systems with great lag on playback. */
	int statusLookAhead = 1;
											/** Last status (status at the last capture round, one full capture and processing frame back). On recording, 
											 *		the current key response to this scenario (one round ago) will be recorded, since at playback time
											 * 		there is a delay of capture to recognize
											 * A big improvement would be to save the delay and play back keypreqsses after the same delay.
											 * Even better, recognize that keypresses happen e.g. 200ms after features appear and execute then
											 * 		example : 200ms, after tank is ahead and reaction was turn right, turn left and circle enemy */
	LinkedList<Status> statusCopy = new LinkedList<Status>();	 
											/** Timer that blocks movement for some time if the tank is firing */
	TimerTask fireOnlyTimerTask = null;
											/** Timer that stops the left turret turn when turning goal is reached */
	TimerTask leftTimerTask = null;
											/** Timer that stops the right turret turn when turning goal is reached */
	TimerTask rightTimerTask = null;
	
	/**
	 * Initialize the Battle configuration and the board.
	 * Call start later to start a robot on on the board.
	 */
	public sirTankiBot() {
		try {
			bot = new Robot();

			battleConfig = new BattleConfig(
					ViewType.LowView,
					TankType.WaspM3,													// MODIFY ME AS NECCESSARY
					WeaponType.IsidaM3,													// MODIFY ME AS NECCESSARY
					MapName.Insel,														// MODIFY ME AS NECCESSARY
					MapTime.Sommer														// MODIFY ME AS NECCESSARY
					);
			smoothFiring=smoothFiringCycles;

			board = new Board(bot, battleConfig);
			board.annotatedImage.newFrame(board.winx); // DISPLAY ANNOTATION FRAME
			keysDown = new boolean[8];
			for (int lcx=0; lcx<keysDown.length; lcx++) keysDown[lcx] = false;
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the robot. This function never returns. 
	 */
	public void start()
	{
		bot.setAutoDelay(5);

		startKeyboardThread();

		TimerTask mainTask =new java.util.TimerTask() {
			@Override
			public void run() {
				while (true)
				{
					try {
						if (idleMode) {
							Thread.sleep(1000);
						} else {
							if (auto_on)
							{
								automaticAction();
								Thread.sleep(2);	// TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST
							} else {
								updateStatus();
								Thread.sleep(2);	// TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}; 
		new java.util.Timer().schedule(mainTask,1); 
	}
	
	/**
	 * Start the keyboard thread. This thread will work down the list of "to-do-keypresses" every few milliseconds
	 * not to overflow the buffer or to loose some.
	 */
	private void startKeyboardThread() 
	{
		keyPressExec = Executors.newSingleThreadScheduledExecutor();
		keyPressRunnable = new Runnable() {
			@Override
			public void run() {
//				System.err.println("Run");
				synchronized(this) {
				try {
						// Something to do?
					synchronized (keyPressQueue) {

						if ( ((keyPressQueueEmptyCounter > 5000) || (fireCounter>2000)) && board.status.autofire) // autofire is on and 100s nothing?!? Destruct
						{
							System.err.println("Doin' nothin' so : Lazy firin'");
							keyPressQueue.add(100);	// self destruction
							keyPressQueue.add(101);
							keyPressQueueEmptyCounter = 0;
							fireCounter=0;
						}

						boolean executedKeypress=false;
						while ( (!keyPressQueue.isEmpty()) && (!executedKeypress) ) {
							// Execute
							int i = keyPressQueue.pop();
							if (i==100) {
								bot.keyPress(KeyEvent.VK_DELETE);
								executedKeypress=true;
							} else if (i==101) {
								bot.keyRelease(KeyEvent.VK_DELETE);
								executedKeypress=true;
							} else if (i>=10) {
								executedKeypress=keyUp(i-10);
							} else {
								executedKeypress=keyDown(i);
							}
						}

						if (keyPressQueue.isEmpty()) {
							keyPressQueueEmpty = true;
							keyPressQueueEmptyCounter++;
							this.notify();
							return;
						}
						
					}
				} catch (Exception e) {
					System.err.println("-------------");
					System.err.println(".oO FATAL Oo.");
					System.err.println("-------------");
					System.err.println("keyPressQueue           : "+keyPressQueue);
					System.err.println("keyPressQueue==null     : "+keyPressQueue==null);
					System.err.println("keyPressQueue.size()    : "+keyPressQueue.size());
					System.err.println("keyPressQueue.isEmpty() : "+keyPressQueue.isEmpty());
					System.err.println("-------------");
					e.printStackTrace();					
				}
			}	
			}	
		};
		keyPressExec.scheduleAtFixedRate(keyPressRunnable, 0, keyThreadDelay, TimeUnit.MILLISECONDS); // 25ms per keypress min
	}

	/**
	 * Push down a key as soon as possible. Writes the key in the keyboard thread queue.
	 * @param nr Key number (0-6)
	 */
	private void lazyKeyDown(int nr)
	{
		synchronized (keyPressQueue) {
//		{
			keyPressQueueEmpty = false;
			if (nr != 6)
				keyPressQueue.offerLast(nr);  // Push keypress in queue
			else {
				keyPressQueue.offerFirst(nr); // Push "Fire!" first in queue
			}
		}
	}

	/**
	 * Release a key as soon as possible. Writes the key in the keyboard thread queue.
	 * @param nr Key number (0-6)
	 */
	private void lazyKeyUp(int nr)
	{
		synchronized (keyPressQueue) {
//		{
			keyPressQueueEmpty = false;
			keyPressQueue.offerLast(nr+10);   // Push key release in queue
		}
	}

	/**
	 * Execute a Mac Script (osascript) - position the Tanki window correctly etc.
	 * @param s Program to execute
	 */
	private void osaExec(String s)
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
	private void sysExec(String s)
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
	private void winExec()
	{
		String[] cmd = { "cmdow", "\"Adobe Flash Player 16\"", "/res","/mov","0","0","/siz",""+board.winx,""+board.winy,"/act"};
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
	 * Main start, then loop eternally.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		mainInit();
		System.err.println("Welcome to SirTankiBot v1.0\n");
		System.err.println("");
		System.err.println("-- Automated killing features --");
		System.err.println("U/I = Last/next track feature in display window");
		System.err.println("L = Toggle target tracking");
		System.err.println("K = Toggle automatic firing");
		System.err.println("J = Toggle target chasing");
		System.err.println("");
		System.err.println("-- Automated driving features --");
		System.err.println("R = Toggle track recording");
		System.err.println("6 = Save tracks");
		System.err.println("9 = Load tracks");
		System.err.println("0 = Display tracking window");
		System.err.println("H = Toggle track based automatic moving");
		System.err.println("");
		System.err.println("-- General features --");
		System.err.println("Numpad / = Turn TankiBot keyboard capture on/off");
		System.err.println("Numpad * = Initialize Tanki Window");
		System.err.println("Numpad 8 = Increase keypress delay (type slower)");
		System.err.println("Numpad 2 = Increase keypress delay (type faster)");
		System.err.println("Numpad 4 = Increase record delay (more lookahead for slower playback)");
		System.err.println("Numpad 2 = Decrease record delay (less lookahead for faster playback)");
		System.err.println("Numpad 1 = Toggle FireWithoutKeyboardBuffer off/on (if you experience keyboard crashes)");
		System.err.println("Numpad 3 = Toggle Direct fire mode (Instead of turn 20° and fire turn until target ahead+fire)");
		System.err.println("Numpad 9 = Autostart in 10 seconds");
		System.err.println("");
		System.err.println("Try to increase the keypress delay if you experience problems.");
		System.err.println("");
		System.err.println("------------------------------");
		System.err.println("QUICKSTART : Press * 9 l k j h");
		System.err.println("------------------------------");
		System.err.println("");
		System.err.println("Press <esc> or <V> (globally w/o window focus!) to quit.");
		System.err.println("");
		
		while (true) {
			try {
				Thread.sleep(999999);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Main start. Register the keylogger, create an instance of the class, run start on it.
	 * @param args ignored
	 */
	public static void mainInit() {
		try {
			GlobalScreen.registerNativeHook();
			// Get the logger for "org.jnativehook" and set the level to off.
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}
		sirTankiBot stb = new sirTankiBot();
		GlobalScreen.addNativeKeyListener(stb);
		stb.start();
	}	

	/**
	 * Push key down immediately and wait for "keydelay" milliseconds.
	 * @param nr key number
	 */
	public boolean keyDown(int nr)
	{
		if (nr>7) return true; // this is a delay keypress
		if ((nr==6)&&(board.status.fireInterrupted)) return false; // no firing on recharging, nothing to do
		if (keysDown[nr]) return false; // nothing to do
		keysDown[nr] = true;
		smoothFiring=smoothFiringCycles;
		
		if (nr<6) fireCounter = 0; else fireCounter++;  		

		switch (nr) {
		case 0: bot.keyPress(KeyEvent.VK_W); if (keydelay>0) bot.delay(keydelay); break;
		case 1: bot.keyPress(KeyEvent.VK_A); if (keydelay>0) bot.delay(keydelay); break;
		case 2: bot.keyPress(KeyEvent.VK_S); if (keydelay>0) bot.delay(keydelay); break;
		case 3: bot.keyPress(KeyEvent.VK_D); if (keydelay>0) bot.delay(keydelay); break;
		case 4: bot.keyPress(KeyEvent.VK_COMMA); board.status.turretIsTurned = true; if (keydelay>0) bot.delay(keydelay); break;
		case 5: bot.keyPress(KeyEvent.VK_X); board.status.turretIsTurned = true; if (keydelay>0) bot.delay(keydelay); break;
		case 6: bot.keyPress(KeyEvent.VK_SPACE); keyPressQueueEmptyCounter=0; if (keydelay>0) bot.delay(keydelay); break;
		case 7: bot.keyPress(KeyEvent.VK_C); if (keydelay>0) bot.delay(keydelay);
		break;
		}
		return true;
	}

	/**
	 * Release key immediately and wait for "keydelay" millis/econds.
	 * @param nr key number
	 */
	public boolean keyUp(int nr)
	{
		if (nr>7) return true; // this is a delay keypress
		if ((nr==6)&&(board.status.fireInterrupted)) {
			board.status.fireInterrupted = false;
		}
		if (!keysDown[nr]) return false; // nothing to do
		keysDown[nr] = false;

		if (nr<6) board.status.tankMovementChangedMillis = System.currentTimeMillis();

		switch (nr) {
		case 0: bot.keyRelease(KeyEvent.VK_W); if (keydelay>0) bot.delay(keydelay); break;
		case 1: bot.keyRelease(KeyEvent.VK_A); if (keydelay>0) bot.delay(keydelay); break;
		case 2: bot.keyRelease(KeyEvent.VK_S); if (keydelay>0) bot.delay(keydelay); break;
		case 3: bot.keyRelease(KeyEvent.VK_D); if (keydelay>0) bot.delay(keydelay); break;
		case 4: bot.keyRelease(KeyEvent.VK_COMMA); if (keydelay>0) bot.delay(keydelay); break;
		case 5: bot.keyRelease(KeyEvent.VK_X); if (keydelay>0) bot.delay(keydelay); break;
		case 6: bot.keyRelease(KeyEvent.VK_SPACE); if (keydelay>0) bot.delay(keydelay); break;
		case 7: bot.keyRelease(KeyEvent.VK_C); if (keydelay>0) bot.delay(keydelay); break;
		}
		return true;
	}

	/**
	 * Force immediate release of all keys (e.g. on program exit)
	 */
	private void forceAllKeysUp() {
//		System.err.println("FORCE ALL KEYS UP");
		bot.keyRelease(KeyEvent.VK_W); bot.delay(keydelay+keyThreadDelay); 
		bot.keyRelease(KeyEvent.VK_A); bot.delay(keydelay+keyThreadDelay);  
		bot.keyRelease(KeyEvent.VK_S); bot.delay(keydelay+keyThreadDelay);  
		bot.keyRelease(KeyEvent.VK_D); bot.delay(keydelay+keyThreadDelay);  
		bot.keyRelease(KeyEvent.VK_COMMA); bot.delay(keydelay+keyThreadDelay); 
		bot.keyRelease(KeyEvent.VK_X); bot.delay(keydelay+keyThreadDelay); 
		bot.keyRelease(KeyEvent.VK_SPACE); bot.delay(keydelay+keyThreadDelay); 
		bot.keyRelease(KeyEvent.VK_C); bot.delay(keydelay+keyThreadDelay); 
		for (int cx=0; cx<7; cx++) keysDown[cx] = false;
	}

	/**
	 * Center the tank turret
	 */
	public void centerTurret()
	{
		{
			if (board.status.turretIsTurned) {
				System.err.println("Center turret"); // TODO
				board.status.turretIsTurned = false;
				lazyKeyUp(4); lazyKeyUp(5); // Safer!
				lazyKeyDown(7); lazyKeyUp(7);
			}
		}
	}

	/**
	 * Init the Tanki window. Put it to the right place, activate it, set the perspective, set the size.
	 * The user needs to manually set the graphics to constant parameters. I recommend all off.
	 * Automatic quality control will surely mess up the autodetection.
	 */
	public void initTanki()
	{
		System.err.println("Set window");
		//		osaExec("tell application \"System Events\" to activate the first window of application process \"Flash Player\"");
		if (System.getProperty("os.name").startsWith("Windows")) {
			winExec();
		} else { // Mac OS
			// sudo sqlite3 /Library/Application\ Support/com.apple.TCC/TCC.db "INSERT or REPLACE INTO access VALUES('kTCCServiceAccessibility','/usr/bin/osascript',1,1,1,NULL)"
			sysExec("/Applications/Flash Player 16.app");
			osaExec("tell application \"System Events\" to set position of first window of application process \"Flash Player\" to {0, 0}");
			osaExec("tell application \"System Events\" to set size of first window of application process \"Flash Player\" to {"+board.winx+", "+board.winy+"}");
		}

		System.err.println("Set viewport");
		int setupDelay=100;
		try {
			switch (battleConfig.view) {
			case FirstView:
				bot.keyPress(KeyEvent.VK_E);
				bot.delay(30*setupDelay);
				bot.keyRelease(KeyEvent.VK_E);
				bot.delay(keydelay);
				break;
			case LowView:
				bot.keyPress(KeyEvent.VK_E);
				bot.delay(30*setupDelay);
				bot.keyRelease(KeyEvent.VK_E);
				bot.delay(keydelay);
				bot.keyPress(KeyEvent.VK_Q);
				bot.delay(6*setupDelay);
				bot.keyRelease(KeyEvent.VK_Q);
				break;
			case NormView:
				bot.keyPress(KeyEvent.VK_Q);
				bot.delay(30*setupDelay);
				bot.keyRelease(KeyEvent.VK_Q);
				bot.delay(keydelay);
				bot.keyPress(KeyEvent.VK_E);
				bot.delay(13*setupDelay);
				bot.keyRelease(KeyEvent.VK_E);
				break;
			case TopView:
				bot.keyPress(KeyEvent.VK_Q);
				bot.delay(30*setupDelay);
				bot.keyRelease(KeyEvent.VK_Q);
				bot.delay(keydelay);
				break;
			default:
				System.err.println("VIEW NOT DECLARED");
				break;
			}

			System.err.println("Set view size");
			for (int cx=0; cx<10; cx++)
			{
				bot.keyPress(KeyEvent.VK_ADD);
				bot.delay(setupDelay);
				bot.keyRelease(KeyEvent.VK_ADD);
				bot.delay(setupDelay);
			}
			for (int cx=0; cx<6; cx++)
			{
				bot.keyPress(KeyEvent.VK_MINUS);
				bot.delay(setupDelay);
				bot.keyRelease(KeyEvent.VK_MINUS);
				bot.delay(setupDelay);
			}						
		} catch (Exception e) {
			System.err.println("ERROR : "+e.toString());
		}
	}

	/**
	 * React to a user keypress
	 * @param e User keypress
	 */
	@SuppressWarnings("unchecked")
	public void nativeKeyPressed(NativeKeyEvent e) {  	
		String st = NativeKeyEvent.getKeyText(e.getKeyCode());


		// Keyboard on/off
		if (st.compareTo("NumPad Divide")==0) {
			keyboardOffMode = !keyboardOffMode;
			System.err.println("Keyboard switched "+(keyboardOffMode?"off":"on"));
		}

		if (keyboardOffMode) return;

		if (st.compareTo("NumPad Multiply")==0) {
			initTanki();																// MODIFY ME AS NECCESSARY
			System.err.println("Initialized tanki");
		}

		if (st.compareTo("7")==0) {
			idleMode = false;
			System.err.println("Idle switched off");
		}

		if (st.compareTo("8")==0) {
			idleMode = true;
			System.err.println("Idle switched on");
		}

		if (st.compareTo("NumPad 8")==0) {
			keyPressExec.shutdown();
			keyPressExec = Executors.newSingleThreadScheduledExecutor();
			keyThreadDelay++;
			keyPressExec.scheduleAtFixedRate(keyPressRunnable, 0, keyThreadDelay, TimeUnit.MILLISECONDS); // 25ms per keypress min
			System.err.println("New keyboard delay : "+keyThreadDelay+"ms");
		}

		if (st.compareTo("NumPad 2")==0) {
			keyPressExec.shutdown();
			keyPressExec = Executors.newSingleThreadScheduledExecutor();
			keyThreadDelay--;
			keyPressExec.scheduleAtFixedRate(keyPressRunnable, 0, keyThreadDelay, TimeUnit.MILLISECONDS); // 25ms per keypress min
			System.err.println("New keyboard delay : "+keyThreadDelay+"ms");
		}

		if (st.compareTo("NumPad 4")==0) {
			if (recording) {
				System.err.println("RECORDING AUTO-OFF");
				recording = false;
			}
			statusCopy.clear();
			statusLookAhead--;
			System.err.println("New lookahead : "+statusLookAhead);
		}

		if (st.compareTo("NumPad 6")==0) {
			if (recording) {
				System.err.println("RECORDING AUTO-OFF");
				recording = false;
			}
			statusCopy.clear();
			statusLookAhead++;
			System.err.println("New lookahead : "+statusLookAhead);
		}

		if (st.compareTo("NumPad 1")==0) {
			fireWithoutKeyboardBuffer = !fireWithoutKeyboardBuffer;
			System.err.println("Firing without keyboard buffer turned "+fireWithoutKeyboardBuffer);
		}

		if (st.compareTo("NumPad 3")==0) {
			board.status.directFireOnly = !board.status.directFireOnly;
			System.err.println("Direct fire mode turned "+(board.status.directFireOnly?"on":"off"));
		}

		if (st.compareTo("NumPad 9")==0) {
			System.err.println("AUTOSTART");
			bot.keyPress(KeyEvent.VK_MULTIPLY);
			bot.delay(100);
			bot.keyRelease(KeyEvent.VK_MULTIPLY);
			bot.delay(15000);
			bot.keyPress(KeyEvent.VK_9);
			bot.delay(100);
			bot.keyRelease(KeyEvent.VK_9);
			bot.delay(100);
			board.status.autofire = false;
			bot.keyPress(KeyEvent.VK_K);
			bot.delay(100);
			bot.keyRelease(KeyEvent.VK_K);
			bot.delay(100);
			auto_on = false;
			bot.keyPress(KeyEvent.VK_H);
			bot.delay(100);
			bot.keyRelease(KeyEvent.VK_H);
			bot.delay(100);
		}

		// Keyboard is on, execute action
		if (st.compareTo("W")==0) {
			board.status.action[0] = true;
		}
		if (st.compareTo("A")==0) {
			board.status.action[1] = true;
		}
		if (st.compareTo("S")==0) {
			board.status.action[2] = true;
		}
		if (st.compareTo("D")==0) {
			board.status.action[3] = true;
		}
		if (st.compareTo(" ")==0) {
			board.status.action[4] = true;
		}

		if (st.compareTo("H")==0) {
			auto_on = !auto_on;
			if (auto_on) {
				if (statusList.size()>2) {
					statusTree = new KDTree<Status>(statusList); // Build k-D-Tree for faster access        		
					System.err.println("AUTOMATIC ON");
				} else {
					System.err.println("AUTOMATIC OFF : Not enough samples");
					auto_on = false;
					automaticOff();
				}
				if (recording) {
					System.err.println("RECORDING AUTO-OFF");
					recording = false;
				}
			} else {
				System.err.println("AUTOMATIC OFF");
				automaticOff();
				forceAllKeysUp();
			}
		}
		if (st.compareTo("R")==0) {
			recording = !recording;
			if (recording) {
				System.err.println("RECORDING ON");
				if (recording) {
					System.err.println("AUTOMATIC AUTO-OFF");
					auto_on = false;
				}
			} else {
				System.err.println("RECORDING OFF");
				statusCopy = new LinkedList<Status>();
			}
		}		
		if (st.compareTo("J")==0) {
			board.status.greedyfire = !board.status.greedyfire;
			if (board.status.greedyfire) {
				System.err.println("GREEDYFIRE ON");
			} else {
				System.err.println("GREEDYFIRE OFF");
				automaticOff();
			}
		}
		if (st.compareTo("K")==0) {
			board.status.autofire = !board.status.autofire;
			if (board.status.autofire) {
				keyPressQueueEmptyCounter = 0;
				System.err.println("AUTOFIRE ON");
			} else {
				System.err.println("AUTOFIRE OFF");
				lazyKeyUp(6);
			}
		}
		if (st.compareTo("L")==0) {
			board.status.autotrack = !board.status.autotrack;
			if (board.status.autotrack) {
				System.err.println("autotrack ON");
			} else {
				System.err.println("autotrack OFF");
				automaticOff();
				waitForKeyboardQueue();
				centerTurret();
			}
		}
		if (st.compareTo("T")==0) {
			board.status.idling = !board.status.idling;
			if (board.status.idling) {
				System.err.println("idling ON");
			} else {
				System.err.println("idling OFF");
			}
		}
		
		if (st.compareTo("6")==0) {
			System.err.println("SAVING");
			try
			{
				synchronized (statusList) {
					FileOutputStream fileOut =
							new FileOutputStream(battleConfig.getMapFilename());
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(statusList);
					out.close();
					fileOut.close();
					System.err.printf("Serialized data was saved to "+battleConfig.getMapFilename());
					System.err.println();
				}
			} catch(IOException i)
			{
				i.printStackTrace();
			}       		
		}          
		if (st.compareTo("9")==0) {
			System.err.println("LOADING");
			try
			{
				synchronized (statusList) {
					FileInputStream fileIn =
							new FileInputStream(battleConfig.getMapFilename());
					ObjectInputStream in = new ObjectInputStream(fileIn);
					statusList = (LinkedList<Status>) in.readObject();
					in.close();
					fileIn.close();
					System.err.printf("Serialized data was loaded from "+battleConfig.getMapFilename());
					System.err.println();
				}
			} catch(IOException i)
			{
				i.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}       		
		}              
		if (st.compareTo("0")==0) {
			System.err.println("Toggle visibility");
			board.annotatedImage.toggleVisible();
		}
		if (st.compareTo("I")==0) {
			board.annotateColor = (board.annotateColor+1)%(battleConfig.getTerrainTypesOnMap()+1);
			System.err.println("Annotate color = "+board.annotateColor);
		}
		if (st.compareTo("U")==0) {
			board.annotateColor = (board.annotateColor+battleConfig.getTerrainTypesOnMap())%(battleConfig.getTerrainTypesOnMap()+1);
			System.err.println("Annotate color = "+board.annotateColor);
		}
		if (st.compareTo("O")==0) { // Capture color information at mouse pointer position
			Point mousePoint = MouseInfo.getPointerInfo().getLocation();
			int hql = battleConfig.getHitQueueLength();
			Rectangle area1 = new Rectangle((int)(mousePoint.getX()-hql/2), (int)(mousePoint.getY()-hql/4), hql, hql/4);
			Rectangle area2 = new Rectangle((int)(mousePoint.getX()-hql/2), (int)(mousePoint.getY()-hql), hql, hql/4);
			BufferedImage bufferedImage1 = bot.createScreenCapture(area1);
			BufferedImage bufferedImage2 = bot.createScreenCapture(area2);

			int[] minmax1 = getMinMax(bufferedImage1);
			int[] minmax2 = getMinMax(bufferedImage2);

			int minMatchPercent = 2*(int)(100.0 * ((double)battleConfig.getMinHitQueueHits() / (double)battleConfig.getHitQueueLength()));
			int lcx=2;
			int lcy=2;
			gotMatch:
				for (lcx=2; lcx<51; lcx++) {
					for (lcy=2; lcy<51; lcy++) {
						int hits1=0;
						int hits2=0;
						int pixels=0;
						for (int lx=0; lx<bufferedImage1.getWidth(); lx++) {
							for (int ly=0; ly<bufferedImage1.getHeight(); ly++) {
								int c = bufferedImage1.getRGB(lx, ly);
								int r = (c >> 16) & 0xFF;
								int g = (c >> 8) & 0xFF;
								int b = c & 0xFF;
								boolean hit1=false;
								boolean hit2=false;
								if ( battleConfig.coldifdif(r,g,b,minmax1[0],minmax1[1],minmax1[2],minmax1[3],minmax1[4],minmax1[5]) < lcx ) hit1=true;
								if ( battleConfig.coldifdif(r,g,b,minmax2[0],minmax2[1],minmax2[2],minmax2[3],minmax2[4],minmax2[5]) < lcy ) hit2=true;
								if (hit1 && !hit2) hits1++;
								if (hit2 && !hit1) hits2++;
								pixels++;
							}
						}
						for (int lx=0; lx<bufferedImage2.getWidth(); lx++) {
							for (int ly=0; ly<bufferedImage2.getHeight(); ly++) {
								int c = bufferedImage2.getRGB(lx, ly);
								int r = (c >> 16) & 0xFF;
								int g = (c >> 8) & 0xFF;
								int b = c & 0xFF;
								boolean hit1=false;
								boolean hit2=false;
								if ( battleConfig.coldifdif(r,g,b,minmax1[0],minmax1[1],minmax1[2],minmax1[3],minmax1[4],minmax1[5]) < lcx ) hit1=true;
								if ( battleConfig.coldifdif(r,g,b,minmax2[0],minmax2[1],minmax2[2],minmax2[3],minmax2[4],minmax2[5]) < lcy ) hit2=true;
								if (hit1 && !hit2) hits1++;
								if (hit2 && !hit1) hits2++;
								pixels++;
							}
						}
						if ( (hits1>pixels*minMatchPercent/100) && (hits2>pixels*minMatchPercent/100) ) {
							break gotMatch;
						}
					}
				}
			if ( (lcx>50)&&(lcx>50) ) {
				System.err.println("NO MATCH, RETRY");
			} else {
				System.err.println("case XXXAAA:	if (coldifdif(r,g,b,"+minmax1[0]+","+minmax1[1]+","+minmax1[2]+","+minmax1[3]+","+minmax1[4]+","+minmax1[5]+")<"+lcx+") return true; break;");							
				System.err.println("case XXXBBB:	if (coldifdif(r,g,b,"+minmax2[0]+","+minmax2[1]+","+minmax2[2]+","+minmax2[3]+","+minmax2[4]+","+minmax2[5]+")<"+lcy+") return true; break;");
				System.err.println();
			}

		}

		if (st.compareTo("F")==0) {
			board.status.mouseIsEnemy = !board.status.mouseIsEnemy; 
			if (board.status.mouseIsEnemy) System.err.println("!!! Mouse is enemy !!!");
			else System.err.println("Enemy is enemy");
		}
		if (st.compareTo("⎋")==0) {
			System.err.println("Quitting...");
			forceAllKeysUp();
			System.exit(0);
		}
		if (st.compareTo("V")==0) {
			System.err.println("Quitting...");
			forceAllKeysUp();
			System.exit(0);
		}
	}

	/**
	 * Get the minimum and maximum value of red,green,blue channel components within the bufferedImage
	 * Used for detection of terrain types and tank colors etc.
	 * @param bufferedImage Image to extract the values from
	 * @return Minmax array (R,G,B min and R,G,B max)
	 */
	private int[] getMinMax(BufferedImage bufferedImage)
	{
		int[] minmax = new int[6];
		minmax[0]=999;
		minmax[1]=999;
		minmax[2]=999;
		minmax[3]=-1;
		minmax[4]=-1;
		minmax[5]=-1;
		for (int lcx=0; lcx<bufferedImage.getWidth(); lcx++) {
			for (int lcy=0; lcy<bufferedImage.getHeight(); lcy++) {
				int c = bufferedImage.getRGB(lcx, lcy);
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = c & 0xFF;
				if (r<minmax[0]) minmax[0]=r;
				if (g<minmax[1]) minmax[1]=g;
				if (b<minmax[2]) minmax[2]=b;
				if (r>minmax[3]) minmax[3]=r;
				if (g>minmax[4]) minmax[4]=g;
				if (b>minmax[5]) minmax[5]=b;
			}
		}
		return minmax;
	}

	/**
	 * React to the user globally releasing a key.
	 * @param e key event
	 */
	public void nativeKeyReleased(NativeKeyEvent e) {
		//		        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
		String st = NativeKeyEvent.getKeyText(e.getKeyCode());
		if (st.compareTo("W")==0) {
			board.status.action[0] = false;
		}
		if (st.compareTo("A")==0) {
			board.status.action[1] = false;
		}
		if (st.compareTo("S")==0) {
			board.status.action[2] = false;
		}
		if (st.compareTo("D")==0) {
			board.status.action[3] = false;
		}
		if (st.compareTo(" ")==0) {
			board.status.action[4] = false;
		}
	}

	/**
	 * Brute-force implementation of the k-dimensional find nearest. Find the nearest status in the statusList to the query. O(N).
	 * @param query The status to find the next neighbour
	 * @return The next neighbor status including the keys pressed there
	 */
	public Status hardFindNearest(Status query)
	{
		double mind = 999999999.9;
		Status bestStatus = statusList.get(0); // anyone ;)

		for (Status xstatus:statusList) {
			double d = 0.0;
			for (int cx=0; cx<xstatus.state.length; cx++) {
				double e = (xstatus.state[cx]-query.state[cx]);
				//				if (e<5.0) d=d-1.0;
				//				d=d+( (e>0)?e:-e );
								d = d + (e * e);				// M O D I F Y   D I S T A N C E   A S   N E C E S S A R Y
			}
			if (d<mind) {
				bestStatus=xstatus;
				mind = d;
			}
		}
		//System.err.print("MIND"+mind+" "+bestStatus);    		
		return bestStatus;    	
	}

	/**
	 * A target in range has been acquired. For the gunFireOnlyDelay, prevent movement of the tank to hold it still and not
	 * interfere with the firing process.
	 */
	public void setFiringOnlyMode()
	{
//		System.err.println("FIRING ONLY FOR "+battleConfig.gunFireOnlyDelay());
		if (fireOnlyTimerTask != null) fireOnlyTimerTask.cancel(); // Cancel already active tasks
		board.status.firingOnlyMode=true;
		fireOnlyTimerTask = new java.util.TimerTask() {
			@Override
			public void run() {
				board.status.firingOnlyMode=false;
			}
		};
		new java.util.Timer().schedule(	fireOnlyTimerTask,	battleConfig.gunFireOnlyDelay()	);
	}

	/**
	 * Check if there is an enemy ahead and, if there is, turn the turret if necessary and fire on him.
	 * A thread will be started to do that action if a target is found.
	 * This function also includes the "Greedy target tracking" functionality.
	 */
	public void fireOnEnemyAhead()
	{
		double fire_degree_limit = (360.0/360.0)  * (2.0*Math.PI);
		double directFireDegreeLimit = (1.0/360.0) * (2.0*Math.PI);				// if within 1° and turret not turning, fire!

		if(!battleConfig.isSingleShotWeapon() && !board.status.fireInterrupted && board.status.energyEmpty && keysDown[6]) {
			System.err.println("Fire interrupted");
			lazyKeyUp(6);
			board.status.fireInterrupted = true;
		}

		if(!battleConfig.isSingleShotWeapon() && board.status.fireInterrupted && !board.status.energyLow && !keysDown[6]){ 
			System.err.println("Fire resumed");
			lazyKeyDown(6);
			board.status.fireInterrupted = false;
		}
		
		if (!keyPressQueue.isEmpty()) {
			if (keyPressQueue.peek()==6) return; // already firing
		}

		// Calculate turret turning speed for direct fire
		double turretTurningSpeed = 0.0;
		double turretTurningLowerFireLimit = 0.0; 
		double turretTurningUpperFireLimit = 0.0; 
		if (board.status.turretTurning) {
			long s = Math.min( 500, System.currentTimeMillis() - board.status.turretTurningStartSystemTime);
			turretTurningSpeed = battleConfig.getTurretTurnSpeed(s); // at 350ms, max. speed is reached TODO Turret-specific
			turretTurningLowerFireLimit = (turretTurningSpeed/0.3*10.0/360.0) * (2.0*Math.PI);
			turretTurningUpperFireLimit = (turretTurningSpeed/0.3*40.0/360.0) * (2.0*Math.PI);
		}
//		System.err.println(s);
//		System.err.println(turretTurningLowerFireLimit+"    "+turretTurningUpperFireLimit);
		// Check direct fire
		if (board.status.autofire && board.status.directFireOnly) {
			if ( (Math.abs(board.status.enemyAheadDegrees)<=directFireDegreeLimit) || // !board.status.turretTurning&&
				  ( board.status.turretTurning &&  
				    (Math.abs(board.status.enemyAheadDegrees)>=turretTurningLowerFireLimit) &&
				    (Math.abs(board.status.enemyAheadDegrees)<=turretTurningUpperFireLimit) && 
  				    (System.currentTimeMillis()-board.status.tankMovementChangedMillis>200 ) // At least 100ms from "stop tank" etc.				   
			      )
				) {
				setFiringOnlyMode();
				board.status.turretTurning = false;
				board.status.turretIsTurned = true;
				lazyKeyUp(4);
				lazyKeyUp(5);
				lazyKeyDown(6);
				if (battleConfig.isSingleShotWeapon()) {
					lazyKeyUp(6);
				}
			}
		}
		
		// Make new plans only if keyboard queue is empty (killing has priority ;)
		if (!keyPressQueue.isEmpty()) return;
		{
			if  (
					(!board.status.turretTurning) &&
					(board.status.enemyAheadDegrees>-fire_degree_limit) &&
					(board.status.enemyAheadDegrees<=0)
					)
			{
				boolean queueEmpty = keyPressQueueEmpty;
				automaticOff();
				setFiringOnlyMode();
				if (!queueEmpty) return;

				if (Math.abs(board.status.enemyAheadDegrees)>directFireDegreeLimit) {
					if (fireWithoutKeyboardBuffer) {
						keyDown(4);			
						lazyKeyDown(9); // and a fake key down (don't push too fast)
					} else {
						lazyKeyDown(4);
					}
				}
				board.status.turretTurning=true;
				board.status.turretTurningStartSystemTime = System.currentTimeMillis();
				if (!board.status.directFireOnly) {
					
					leftTimerTask = new java.util.TimerTask() {
						@Override
						public void run() {
							try {
								board.status.turretTurning = false;
								board.status.turretIsTurned = true;
								//									waitForKeyboardQueue();
								if (board.status.autofire)
								{
									if (fireWithoutKeyboardBuffer) {
										keyDown(6);			
										lazyKeyDown(9); // and a fake key down (don't push too fast)
									} else {
										lazyKeyDown(6);
									}
									if (battleConfig.isSingleShotWeapon()) {
										lazyKeyUp(6);
									}
								}
								if (fireWithoutKeyboardBuffer) {
									keyUp(4);			
									lazyKeyDown(9); // and a fake key down (don't push too fast)
								} else {
									lazyKeyUp(4);
								}
								lazyKeyDown(9); // and a fake key down (don't push too fast)
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}; 
					if (leftTimerTask!=null) leftTimerTask.cancel();
					if (rightTimerTask!=null) rightTimerTask.cancel();
				new java.util.Timer().schedule( leftTimerTask,
							battleConfig.getTurretTurnTime(Math.abs(board.status.enemyAheadDegrees)) 
							);
				} else {
					new java.util.Timer().schedule( 
							new java.util.TimerTask() {
								@Override
								public void run() {
									board.status.turretTurning = false;
									board.status.turretIsTurned = true;
									lazyKeyUp(4);
								}
							}, 
							battleConfig.getTurretTurnTime(Math.abs(board.status.enemyAheadDegrees)) 
							);
				}
				return;
			}

			if  (
					(!board.status.turretTurning) &&
					(board.status.enemyAheadDegrees>=0) &&
					(board.status.enemyAheadDegrees<=fire_degree_limit)
					)
			{
				boolean queueEmpty = keyPressQueueEmpty;
				automaticOff();
				setFiringOnlyMode();
				if (!queueEmpty) return;

				if (Math.abs(board.status.enemyAheadDegrees)>directFireDegreeLimit) {
					if (fireWithoutKeyboardBuffer) {
						keyDown(5);			
						lazyKeyDown(9); // and a fake key down (don't push too fast)
					} else {
						lazyKeyDown(5);
					}
				}
				board.status.turretTurning=true;
				board.status.turretTurningStartSystemTime=System.currentTimeMillis();
				if (!board.status.directFireOnly) {
					rightTimerTask =new java.util.TimerTask() {
						@Override
						public void run() {
							try {
								board.status.turretTurning = false;
								board.status.turretIsTurned = true;
								//									waitForKeyboardQueue();
								if (board.status.autofire)
								{
									if (fireWithoutKeyboardBuffer) {
										keyDown(6);			
										lazyKeyDown(9); // and a fake key down (don't push too fast)
									} else {
										lazyKeyDown(6);
									}
									if (battleConfig.isSingleShotWeapon()) {
										lazyKeyUp(6);
									}
									if (fireWithoutKeyboardBuffer) {
										keyUp(5);			
										lazyKeyDown(9); // and a fake key down (don't push too fast)
									} else {
										lazyKeyUp(5);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}; 
					if (leftTimerTask!=null) leftTimerTask.cancel();
					if (rightTimerTask!=null) rightTimerTask.cancel();
					new java.util.Timer().schedule(rightTimerTask, 
							battleConfig.getTurretTurnTime(Math.abs(board.status.enemyAheadDegrees)) 
							); 
				} else {
					new java.util.Timer().schedule( 
							new java.util.TimerTask() {
								@Override
								public void run() {
									board.status.turretTurning = false;
									board.status.turretIsTurned = true;
									lazyKeyUp(5);
								}
							}, 
							battleConfig.getTurretTurnTime(Math.abs(board.status.enemyAheadDegrees)) 
							);
				}
				return;
			}

			// No target, try to track one out of range  
			if (
					(!board.status.turretTurning) &&
					(board.status.greedyfire) &&
					(board.status.nearestEnemyDistance<999) 
					) {
				//			System.err.println(board.status.nearestEnemyDegrees+" d "+board.status.nearestEnemyDistance);
				setFiringOnlyMode();
				centerTurret();
				if (board.status.nearestEnemyDegrees<-0.5) {
					//				lazyKeyDown(0);
					lazyKeyUp(0);
					lazyKeyDown(1);
					lazyKeyUp(3);
					if (keydelay>0) bot.delay(keydelay);
					if (board.status.autofire) lazyKeyUp(6);
					return;
				}
				if (board.status.nearestEnemyDegrees>0.5) {
					//				lazyKeyDown(0);
					lazyKeyUp(0);
					lazyKeyDown(3);
					lazyKeyUp(1);
					if (keydelay>0) bot.delay(keydelay);
					if (board.status.autofire) lazyKeyUp(6);
					return;
				}
				lazyKeyDown(0);
				lazyKeyUp(1);
				lazyKeyUp(3);
				return;
			}

			smoothFiring--;
			if (smoothFiring<0) {
				// No target
				smoothFiring=-1;
				if (!board.status.firingOnlyMode) {
					centerTurret();
					if (board.status.autofire && keysDown[6]) lazyKeyUp(6); // No target, we're still firing
				}
			}
			
		}
	}

	/**
	 * Update the board status
	 */
	public void updateStatus() 
	{		
		waitForKeyboardQueue();
//		board.readBoardFromScreen();

//		if (board.status.autotrack)
//		{
//			fireOnEnemyAhead();
//		}
		//		bot.delay(battleConfig.getMovingDelay());

		statusCopy.offerLast(new Status(board.status)); // Append to status copy list		

		if (board.status.idling) {
			// isida healing TODO
			if (!board.status.energyLow) {
				lazyKeyDown(6);
			}
			if (board.status.energyEmpty) {
				lazyKeyUp(6);
			}
			if (System.currentTimeMillis()%5000<10) {
				int n = (int)Math.floor(Math.random()*2);
				lazyKeyDown(n*2);
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lazyKeyUp(n);
			}
		}
		
		if ( (statusCopy.size() >= statusLookAhead) ) {
			if (recording) {
				for (int cx=0; cx<board.status.action.length; cx++) 
				{
					if ( board.status.action[cx] ) { // Only record if key is pressed
//						System.err.println(board.status.captureTimeMillis-statusCopy.getFirst().captureTimeMillis+" ms record shift");

						for (int ln=0; ln<board.status.action.length; ln++) {
							statusCopy.getFirst().action[ln] = board.status.action[ln]; // Copy the current action on the past status (compensate for recognition delay..)
						}
						statusList.push(new Status(statusCopy.getFirst())); // add the past status to the stack (this has priority...)

					}
				} 
			} 
			statusCopy.removeFirst();
		}
	}

	/**
	 * Turn the automatic driving off (release all the driving keys)
	 */
	public void automaticOff()
	{
		synchronized (keyPressQueue) {
			Iterator<Integer> it = keyPressQueue.iterator();
			while (it.hasNext()) { // iterate over terrain types not yet found
				if (it.next() < 4) it.remove();
			}
		}
		for (int lcx=0; lcx<4; lcx++) lazyKeyUp(lcx);
	}
	
	/**
	 * Keep on capturing the tanki window and firing if there is a target ahead - until the keyboard queue is empty
	 */
	private void waitForKeyboardQueue() {
		synchronized (keyPressRunnable) {
			do {
				try {					
					board.readBoardFromScreen();
					keyPressRunnable.wait(5);
				} catch (InterruptedException e) {
				}
				if (board.status.autotrack || board.status.autofire)
				{
					fireOnEnemyAhead();
				}
			} while (!keyPressQueueEmpty);
		}
	}

	/**
	 * Execute automatic action (Update the board status and execute an automatic driving action)
	 */
	public void automaticAction()
	{
		waitForKeyboardQueue();

		Status nearestStatus = null;
		
		/*try { // Compare "Brute force" and kD-Tree results
			nearestStatus = statusTree.findNearest(board.status);
			System.err.print("KD :");
			for (int lcx=0; lcx<6; lcx++)
				System.err.print(nearestStatus.action[lcx]?"x":"o");
			System.err.println();
		} catch (Exception e) {
			//System.err.println(e.getMessage());
		}*/
		
		if (!board.status.firingOnlyMode) {
			if (nearestStatus==null) {
				nearestStatus = hardFindNearest(board.status);
			}
//			System.err.println(System.currentTimeMillis()-board.status.captureTimeMillis+" m playback shift");
			for (int cx=0; cx<nearestStatus.action.length-1; cx++)
			{
				if (nearestStatus.action[cx]) {
					System.out.print("x");
					lazyKeyDown(cx);
				} else {
					System.out.print(".");
					lazyKeyUp(cx);
				}
			}
			System.out.println();
		}
		if (!auto_on) automaticOff();
	}

	/**
	 * Ignore nativeKeyTyped - this needs override
	 */
	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
	}

}