package iDotsBot;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class DotFigures {

	int basic_delay = 400; // 400
	int after_turn_delay=1200; // 1400


	public int dotXscrX(int dotX)
	{
		return 40+(410/12)+(410/6)*dotX;
	}

	public int dotYscrY(int dotY)
	{
		return 270+(410/12)+(410/6)*dotY;
	}

	public void temp() throws AWTException {
		Robot bot = new Robot();
		bot.setAutoDelay(100);

		int [][]v = new int[6][6];

		further:

		while (true) {

				// SQUARE
				for (int vy=4; vy>=0; vy--) {
					for (int vx=0; vx<=4; vx++) {
						if (
								( v[vx][vy] == v[vx+1][vy  ] ) &&
								( v[vx][vy] == v[vx  ][vy+1] ) &&
								( v[vx][vy] == v[vx+1][vy+1] )
								) {
							System.err.println("SQUARE at "+vx+","+vy);
							// Square
							bot.mouseMove(dotXscrX(vx), dotYscrY(vy));
							bot.mousePress(InputEvent.BUTTON1_MASK);
							bot.delay(basic_delay); // Click one second
							bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy  ));
							bot.delay(basic_delay); // Click one second
							bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+1));
							bot.delay(basic_delay); // Click one second
							bot.mouseMove(dotXscrX(vx  ), dotYscrY(vy+1));
							bot.delay(basic_delay); // Click one second
							bot.mouseMove(dotXscrX(vx  ), dotYscrY(vy  ));
							bot.delay(basic_delay); // Click one second
							bot.mouseRelease(InputEvent.BUTTON1_MASK);
							bot.delay(after_turn_delay); // Click one second
							break further;
						}
					}
				}

		// BURGER
		for (int vy=3; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx][vy] == v[vx+1][vy  ] ) &&
						( v[vx][vy] == v[vx  ][vy+2] ) &&
						( v[vx][vy] == v[vx+1][vy+2] ) &&
						( v[vx][vy+1] == v[vx+1][vy+1] )
						) {
					System.err.println("BURGER at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+1  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}


		// A?, A?, XA, XA
		for (int vy=2; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx][vy] == v[vx][vy+1] ) &&
						( v[vx][vy] == v[vx+1][vy+2] ) &&
						( v[vx][vy] == v[vx+1][vy+3] ) &&

						( v[vx][vy+2] == v[vx][vy+3] )
						) {
					System.err.println("KILL-LEFT-VERTICAL-TWO-DROP-2-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+2));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+3  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		// These need to be extended to longer!!!!



		// A?, X?, XA, AA
		for (int vy=2; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx][vy] == v[vx+1][vy+2] ) &&
						( v[vx][vy] == v[vx  ][vy+3] ) &&
						( v[vx][vy] == v[vx+1][vy+3] ) &&
						( v[vx][vy+1] == v[vx][vy+2] )
						) {
					System.err.println("KILL-LEFT-VERTICAL-TWO-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+2  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}


		// A?, X?, X?, XA, AA
		for (int vy=1; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx][vy] == v[vx+1][vy+3] ) &&
						( v[vx][vy] == v[vx  ][vy+4] ) &&
						( v[vx][vy] == v[vx+1][vy+4] ) &&
						( v[vx][vy+1] == v[vx][vy+2] ) &&
						( v[vx][vy+1] == v[vx][vy+3] )
						) {
					System.err.println("KILL-LEFT-VERTICAL-THREE-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+2));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+3));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		// A?, X?, X?, XA, AA
		for (int vy=0; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx][vy] == v[vx+1][vy+4] ) &&
						( v[vx][vy] == v[vx  ][vy+5] ) &&
						( v[vx][vy] == v[vx+1][vy+5] ) &&
						( v[vx][vy+1] == v[vx][vy+2] ) &&
						( v[vx][vy+1] == v[vx][vy+3] ) &&
						( v[vx][vy+1] == v[vx][vy+4] )
						) {
					System.err.println("KILL-LEFT-VERTICAL-FOUR-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+2));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+3));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy+4));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		// A?, A?, XA, XA
		for (int vy=2; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx+1][vy] == v[vx+1][vy+1] ) &&
						( v[vx+1][vy] == v[vx][vy+2] ) &&
						( v[vx+1][vy] == v[vx][vy+3] ) &&

						( v[vx+1][vy+2] == v[vx+1][vy+3] )
						) {
					System.err.println("KILL-RIGHT-VERTICAL-TWO-DROP-2-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+2));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+3  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		// These need to be extended to longer!!!!

		// A?, X?, XA, AA
		for (int vy=2; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx+1][vy] == v[vx][vy+2] ) &&
						( v[vx+1][vy] == v[vx+1  ][vy+3] ) &&
						( v[vx+1][vy] == v[vx][vy+3] ) &&
						( v[vx+1][vy+1] == v[vx+1][vy+2] )
						) {
					System.err.println("KILL-RIGHT-VERTICAL-TWO-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+2  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		// A?, X?, XA, AA
		for (int vy=1; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx+1][vy] == v[vx][vy+3] ) &&
						( v[vx+1][vy] == v[vx+1  ][vy+4] ) &&
						( v[vx+1][vy] == v[vx][vy+4] ) &&
						( v[vx+1][vy+1] == v[vx+1][vy+2] ) &&
						( v[vx+1][vy+1] == v[vx+1][vy+3] )
						) {
					System.err.println("KILL-RIGHT-VERTICAL-THREE-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+2  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+3 ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		// A?, X?, XA, AA
		for (int vy=0; vy>=0; vy--) {
			for (int vx=0; vx<=4; vx++) {
				if (
						( v[vx+1][vy] == v[vx][vy+3] ) &&
						( v[vx+1][vy] == v[vx+1  ][vy+4] ) &&
						( v[vx+1][vy] == v[vx][vy+4] ) &&
						( v[vx+1][vy+1] == v[vx+1][vy+2] ) &&
						( v[vx+1][vy+1] == v[vx+1][vy+3] ) &&
						( v[vx+1][vy+1] == v[vx+1][vy+4] )
						) {
					System.err.println("KILL-RIGHT-VERTICAL-FOUR-MAKE-SQUARE at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+1));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+2  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+3 ));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+4 ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}

		boolean ONEPAYKILL = false;

		if (ONEPAYKILL)
		{

			// KILL 1 LOW LEFT (PAID!!!)

			// A?, X?, XA, AA
			for (int vy=3; vy>=0; vy--) {
				for (int vx=0; vx<=4; vx++) {
					if (
							( v[vx][vy] == v[vx][vy+1] ) &&
							( v[vx][vy] == v[vx+1][vy+1] ) &&
							( v[vx][vy] == v[vx+1][vy+2] )
							) {
						System.err.println("KILL-ONE-LOW-LEFT (PAID!) at "+vx+","+vy);
						// Square
						bot.mouseMove(dotXscrX(vx), dotYscrY(vy+2));
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.delay(after_turn_delay); // Click one second
						break further;
					}
				}
			}

			// KILL 1 LOW RIGHT (PAID!!!)

			// A?, X?, XA, AA
			for (int vy=3; vy>=0; vy--) {
				for (int vx=0; vx<=4; vx++) {
					if (
							( v[vx+1][vy] == v[vx+1][vy+1] ) &&
							( v[vx+1][vy] == v[vx][vy+1] ) &&
							( v[vx+1][vy] == v[vx][vy+2] )
							) {
						System.err.println("KILL-ONE-LOW-RIGHT (PAID!) at "+vx+","+vy);
						// Square
						bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+2));
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.delay(after_turn_delay); // Click one second
						break further;
					}
				}
			}


			// KILL 1 MID LEFT (PAID!!!)

			// A?, X?, XA, AA
			for (int vy=3; vy>=0; vy--) {
				for (int vx=0; vx<=4; vx++) {
					if (
							( v[vx][vy] == v[vx][vy+2] ) &&
							( v[vx][vy] == v[vx+1][vy+1] ) &&
							( v[vx][vy] == v[vx+1][vy+2] )
							) {
						System.err.println("KILL-ONE-MID-LEFT (PAID!) at "+vx+","+vy);
						// Square
						bot.mouseMove(dotXscrX(vx), dotYscrY(vy+1));
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.delay(after_turn_delay); // Click one second
						break further;
					}
				}
			}

			// KILL 1 MID RIGHT (PAID!!!)

			// A?, X?, XA, AA
			for (int vy=3; vy>=0; vy--) {
				for (int vx=0; vx<=4; vx++) {
					if (
							( v[vx+1][vy] == v[vx+1][vy+2] ) &&
							( v[vx+1][vy] == v[vx][vy+1] ) &&
							( v[vx+1][vy] == v[vx][vy+2] )
							) {
						System.err.println("KILL-ONE-MID-LEFT (PAID!) at "+vx+","+vy);
						// Square
						bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy+1));
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.mousePress(InputEvent.BUTTON1_MASK);
						bot.mouseRelease(InputEvent.BUTTON1_MASK);
						bot.delay(after_turn_delay); // Click one second
						break further;
					}
				}
			}
		} // OF ONEPAYKILL


		// ?? ANY HORIZONTAL FOUR ??

		// A?, X?, XA, AA
		for (int vy=5; vy>=0; vy--) {
			for (int vx=0; vx<=2; vx++) {
				if (
						( v[vx+1][vy] == v[vx][vy] ) &&
						( v[vx+2][vy] == v[vx][vy] ) &&
						( v[vx+3][vy] == v[vx][vy] )
						) {
					System.err.println("KILL-HORIZONTAL-4 at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+2), dotYscrY(vy ));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+3), dotYscrY(vy ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}


		// ?? ANY HORIZONTAL THREESOME ??

		// A?, X?, XA, AA
		for (int vy=5; vy>=0; vy--) {
			for (int vx=0; vx<=3; vx++) {
				if (
						( v[vx+1][vy] == v[vx][vy] ) &&
						( v[vx+2][vy] == v[vx][vy] )
						) {
					System.err.println("KILL-HORIZONTAL-3 at "+vx+","+vy);
					// Square
					bot.mouseMove(dotXscrX(vx), dotYscrY(vy));
					bot.mousePress(InputEvent.BUTTON1_MASK);
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy  ));
					bot.delay(basic_delay); // Click one second
					bot.mouseMove(dotXscrX(vx+2), dotYscrY(vy ));
					bot.delay(basic_delay); // Click one second
					bot.mouseRelease(InputEvent.BUTTON1_MASK);
					bot.delay(after_turn_delay); // Click one second
					break further;
				}
			}
		}



		/*
		         // ?? ANY HORIZONTAL TWIN ??

		         // A?, X?, XA, AA
		         for (int vy=5; vy>=0; vy--) {
		         for (int vx=0; vx<=4; vx++) {
		         if (
		         ( v[vx+1][vy] == v[vx][vy] )
		         ) {
		         System.err.println("KILL-HORIZONTAL-2 at "+vx+","+vy);
		         // Square
		         bot.mouseMove(dotXscrX(vx), dotYscrY(vy));
		         bot.mousePress(InputEvent.BUTTON1_MASK);
		         bot.delay(basic_delay); // Click one second
		         bot.mouseMove(dotXscrX(vx+1), dotYscrY(vy  ));
		         bot.delay(basic_delay); // Click one second
		         bot.mouseRelease(InputEvent.BUTTON1_MASK);
		         bot.delay(after_turn_delay); // Click one second
		         break further;
		         }
		         }
		         }
		 */

		}
	}

}
