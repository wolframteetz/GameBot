package iDotsBot;

import java.awt.BorderLayout;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.time.temporal.JulianFields;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class iDotsPane extends JPanel {

	Board board = new Board();
	Robot bot;

	int basic_delay = 550; // 400
	int after_turn_delay=1500; // 1400,critical is delay after last turn

	public int dotXscrX(int dotX)
	{
		return 40+(410/12)+(410/6)*dotX;
	}

	public int dotYscrY(int dotY)
	{
		return 270+(410/12)+(410/6)*dotY;
	}

	private JTextArea ta;

	public void restartField()
	{
		// NOTHING FOUND
		// Activate
		bot.mouseMove(dotXscrX(0), dotYscrY(-1));
		bot.mousePress(InputEvent.BUTTON1_MASK);
		bot.mouseRelease(InputEvent.BUTTON1_MASK);

		bot.mouseMove(180, 910);
		bot.mousePress(InputEvent.BUTTON1_MASK);
		bot.delay(basic_delay); 
		bot.mouseRelease(InputEvent.BUTTON1_MASK);
		bot.delay(after_turn_delay); 

		bot.mouseMove(240, 440);
		bot.mousePress(InputEvent.BUTTON1_MASK);
		bot.delay(basic_delay); 
		bot.mouseRelease(InputEvent.BUTTON1_MASK);
		bot.delay(2*after_turn_delay); 
		board.turn = 0;
	}

	public void playAgain()
	{
		bot.delay(1000);
		// NOTHING FOUND
		// Activate
		bot.mouseMove(200, 35);
		bot.mousePress(InputEvent.BUTTON1_MASK);
		bot.delay(basic_delay); 
		bot.mouseRelease(InputEvent.BUTTON1_MASK);

		bot.mouseMove(250, 830);
		bot.mousePress(InputEvent.BUTTON1_MASK);
		bot.delay(basic_delay); 
		bot.mouseRelease(InputEvent.BUTTON1_MASK);
		bot.delay(3*after_turn_delay); 
		board.turn = 0;
		board.score = 0;
//		board.boardScore = 0;
	}

	public iDotsPane() {
		int scanDepth = 3;

		setLayout(new BorderLayout());
		ta = new JTextArea(20, 20);
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		add(new JScrollPane(ta));

		add(new JScrollPane(board.getBoardPanel()));

		JButton btn = new JButton("Start");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ta.requestFocusInWindow();
				ta.append("Start\n");
				SwingWorker worker = new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {

						bot = new Robot();
						bot.setAutoDelay(100);
						int duration = 15*60000; // 15 mins                       
						long start = System.currentTimeMillis();

						/*                            Runtime.getRuntime().exec("osascript -e 'tell application "TeamViewer" to activate''");
                         osascript -e 'tell application "System Events" to set position of first window of application process "TeamViewer" to {0, 0}'
                         bot.keyPress(KeyEvent.VK_META);
                         bot.keyPress(KeyEvent.VK_TAB);
                         bot.keyRelease(KeyEvent.VK_TAB);
                         bot.keyRelease(KeyEvent.VK_META);
						 */

						/* START APPLICATION
                         //Home
                         bot.mouseMove(300,900);
                         bot.mousePress(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay); // Click one second
                         bot.mouseRelease(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay); // Click one second
                         //Home
                         bot.mouseMove(300,900);
                         bot.mousePress(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay); // Click one second
                         bot.mouseRelease(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay); // Click one second

                         bot.mouseMove(151,146); // Second Tab
                         bot.mousePress(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay);
                         bot.mouseRelease(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay);

                         bot.mouseMove(180,650); // dots
                         bot.mousePress(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay);
                         bot.mouseRelease(InputEvent.BUTTON1_MASK);
                         bot.delay(60000);

                         bot.mouseMove(350,420); // züge
                         bot.mousePress(InputEvent.BUTTON1_MASK);
                         bot.delay(basic_delay);
                         bot.mouseRelease(InputEvent.BUTTON1_MASK);
                         bot.delay(20000);

						 */

						while (System.currentTimeMillis() - start < duration) {

							board.turnlimit = 30;
							int[][] v = board.v;

							further:
							{
								System.err.println((board.turnlimit-board.turn)+" turn(s) left");
								
								if (board.turn >= board.turnlimit) {
									System.err.println("Sleeping...");
									bot.mouseMove(250, 830);
									bot.delay(5000);
/*									System.err.println("Prereading board...");
									bot.mousePress(InputEvent.BUTTON1_MASK);
									bot.delay(basic_delay); 
									bot.mouseRelease(InputEvent.BUTTON1_MASK);
									bot.delay(2500);*/
									playAgain();
								}
								// 6x6                        	
								System.err.println("Reading board...");
								board.readBoardFromScreen();
								board.printBoardToConsole();
								//                            System.err.println();

								//                            System.err.println("Checking squares...");
								for (int vy=5; vy>=0; vy--) {
									for (int vx=0; vx<=5; vx++) {
										if( v[vx][vy] == 0 ) {
											System.err.println("Met a zero field. Starting over.");
											playAgain();
											break further;
										}
									}
								}

								//                                System.err.println("Loops and tings...");
								board.addAllLoopsAndTings();
								//System.err.println("Print loops and tings...");
								//board.printLoopsAndTingsToConsole();
						        //board.removeTingsThatAreRemovedByLoops();
								//board.printLoopsAndTingsToConsole();
								//                                System.err.println("Plot board...");
								board.plotBoard();
								System.err.print("Getting scores... ");

								board.nnn=0;
								int bestMove;
								System.err.print("Long version... ");
								// If it's one of the last moves, optimize on # moves remaining
								bestMove = board.getScores( Math.min((board.turnlimit-board.turn),scanDepth-1) , 0);
								System.err.println("NNN = " + board.nnn);
								
								if ((board.turn==0) && (board.maxScore<15)) {
									System.err.println("15 Quick points not visible, restart...");
									restartField();
									break further;
								}

								if (board.maxPath<(1<<16)) {
									System.err.println("Best choice is loop # "+board.maxPath+" with "+board.maxScore+" Points");
									board.simRemoveDotsLoop(board.getBestPath());  // To calculate score
									//                    				board.plotBestPathToConsole();
									//System.err.println(board.loops.elementAt(board.maxPath).toString());
								} else {
									System.err.println("Best choice is ting # "+(board.maxPath-(1<<16))+" with "+board.maxScore+" Points");
									board.simRemoveDotsVector(board.getBestPath()); // To calculate score
									//                    				board.plotBestPathToConsole();
									//System.err.println(board.tings.elementAt(board.maxPath-(1<<16)).toString());
								}   				

								/*
                                try { // TODO::XXX
                            		Thread.sleep(2000);
                            	} catch (InterruptedException e) {
                            		e.printStackTrace();                            	
                            	}*/


								Vector<Integer> bestPath = board.getBestPath();
								{
									// Activate
									bot.mouseMove(dotXscrX(0), dotYscrY(-1));
									bot.mousePress(InputEvent.BUTTON1_MASK);
									bot.mouseRelease(InputEvent.BUTTON1_MASK);

									// Move to first point and click
									Integer p = bestPath.firstElement();
									bot.mouseMove(dotXscrX(p&7), dotYscrY(p>>3));
									bot.mousePress(InputEvent.BUTTON1_MASK);
								}
								for (Integer p:bestPath) {
									bot.mouseMove(dotXscrX(p&7), dotYscrY(p>>3));
									bot.delay(basic_delay);
								}
								bot.mouseRelease(InputEvent.BUTTON1_MASK);
								bot.delay(after_turn_delay); // Click one second
//								board.boardScore += board.score;
								board.turn++;

							}
							
							bot.delay(1000); // Wait one second
						}
						return null;
					}

					@Override
					protected void done() {
						ta.append("\nDone");
					}
				};
				worker.execute();
			}
		});
		add(btn, BorderLayout.SOUTH);
	}
}