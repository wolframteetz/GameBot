package iDotsBot;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JPanel;

public class Board implements Cloneable {
    static int nnn=0;
    // Debug variables
    boolean DebugThreads = false;
    
    // CLONED CONTENT
    
    // The board. Contains int = color number (1-6), 0 for white and -1 for undefined/random color.
    public int[][] v =  new int[6][6];
    public int turn;   // turn #
    public int turnlimit;
//    public int boardScore;
    
    public int depth;   // prediction depth #
    public int depthlimit; // prediction depth limit in turns
    public int score;  // current score
    public float depthScore; // number of undefined fields divided by iteration depth
    	// it is good to have many undefined "possibilities" ASAP
    	// this is the only way the algorithm deals with the probability of a "lucky drop"
	public int maxScore;

    // NON CLONED CONTENT //
	public int maxPath=0; 
    
    // xy coordinate pairs. lower 3 bits x = (val & 7), higher 3 bits y = (val >> 3)
    private Vector< Vector<Integer> > loops = new Vector< Vector<Integer> > ();
    private HashSet< HashSet< Integer > > loopsSet = new HashSet< HashSet< Integer > > ();
    private Vector< Vector<Integer> > tings = new Vector< Vector<Integer> > ();
    private HashSet< HashSet< Integer > > tingsSet = new HashSet< HashSet< Integer > > ();
    // Graphical view of the board
    private Circles circles = new Circles();
    private BoardJPanel boardJPanel = new BoardJPanel( new BufferedImage(400*8/6/2, 400*8/6/2, BufferedImage.TYPE_INT_RGB), circles );
    
    public Object clone() {
        Board _board = new Board();
        
        for (int cx=0; cx<6; cx++)
            for (int cy=0; cy<6; cy++)
            	_board.v[cx][cy] = v[cx][cy];
        _board.turn = this.turn;
        _board.turnlimit = this.turnlimit;
//        _board.boardScore = this.boardScore;
        
        _board.depth = this.depth;
        _board.depthlimit = this.depthlimit;
        _board.score = this.score; 
        _board.depthScore = this.depthScore; 
        _board.maxScore = this.maxScore;
        return _board;
    }
    
    // Compare boards by dots on it
    public boolean hasEqualDots(Board _board)
    {
        for (int cx=0; cx<6; cx++)
            for (int cy=0; cy<6; cy++)
            	if (_board.v[cx][cy] != v[cx][cy]) return false;
        return true;
    }
    
    public void readBoardFromScreen()
    {
        
        int x = 40;
        int y = 270;
        int width = 410;
        int height = 410;
        Rectangle area = new Rectangle(x, y, width, height);
        try {
            Robot bot = new Robot();
            BufferedImage bufferedImage = bot.createScreenCapture(area);
            int vx=0;
            for (int xx=410/12; xx<410; xx += 410/6) {
                int vy=0;
                for (int yy=410/12; yy<410; yy += 410/6) {
//                    System.err.print("xx" + xx + "yy" + yy + "vx" + vx + "vy"+vy);
                    int rgb = bufferedImage.getRGB(xx,yy);
                    int R = (rgb >> 16) & 0xFF;
                    int G = (rgb >> 8) & 0xFF;
                    int B = rgb & 0xFF;
                    
                    if ((R<100)&&(G>150)&&(B>150)) v[vx][vy] = 1; // BLUE
                    if ((R>150)&&(G<150)&&(B<50))  v[vx][vy] = 2; // RED
                    if ((R<50)&&(G>100)&&(B>50))   v[vx][vy] = 3; // GREEN
                    if ((R>150)&&(G>150)&&(B<100)) v[vx][vy] = 4; // YELLOW
                    if ((R>150)&&(G<150)&&(B>100)) v[vx][vy] = 5; // PINK
//                    System.err.print("R"+red+"G"+green+"B"+blue)
                    vy++;
                }
                vx++;
//                System.err.println();
            }
            depthScore = 0.0f; // no undefined fields
        } catch (Exception e) {
            System.err.println("Failed capturing screen. Fatal.");
            return;
        }
    }
    
    public void printBoardToConsole()
    {
        System.err.println("Board at turn #"+turn+" of "+turnlimit+" score "+score+" depthscore "+depthScore);
        for (int vy=0; vy<6; vy++) {
            for (int vx=0; vx<6; vx++)
            {
                System.err.print(v[vx][vy]);
            }
            System.err.println();
        }
        System.err.println("");
    }
    
    public void simRemoveDotsVector(Vector<Integer> d) // "Open" string
    {
        // SORT VECTOR BY ASCENDING Y
    	Vector<Integer> dd = (Vector<Integer>) d.clone();
        Collections.sort(dd);
        
//        System.err.println("Sorted remove dots : "+d);
        
        for (int xy:dd) {
            for (int cy=xy>>3; cy>0; cy--)
            {
                v[xy&7][cy] = v[xy&7][cy-1];
            }
            score++;
//            depthScore = 0.0f;
            depthScore += 0.25f/(float)(depth+1); // THIS 0.25 IS JUST A GUESS AND NEEDS TO BE ESTIMATED PROPERLY
            // This is the "Value" of the undefined field - an undefined field at iteration depth 1 gives 0.25 points
            v[xy&7][0] = -1; // undefined field
        }
    }
    
    public void simRemoveDotsLoop(Vector<Integer> d) // "Closed" loop / square / rectangle etc.
    {
        // Create a polygon
    	int xy_loop_start_end = d.lastElement(); // Only the loop part, ignore the rest (remember, first and last points are connected by java!!)
        Polygon polygon = new Polygon();
        boolean loop_start_reached = false;
        for (int xy:d) {
        	if (xy==xy_loop_start_end) loop_start_reached=true;
        	if (loop_start_reached) polygon.addPoint(xy&7, xy>>3);
        }
        
        Vector<Integer> pointsToRemove = new Vector<Integer>();
        for (int cx=0; cx<6; cx++) {
            for (int cy=0; cy<6; cy++) {
                // Add all points in polygon or of border color
                if (
                    (v[cx][cy] == v[d.firstElement()&7][d.firstElement()>>3]) ||
                    (polygon.contains(cx, cy))
                    ) {
                	//if (v[cx][cy] == v[d.firstElement()&7][d.firstElement()>>3]) System.err.println("  ColorEqual "+cx+"/"+cy);
                	//if (polygon.contains(cx, cy)) System.err.println("  Contains   "+cx+"/"+cy);
                    pointsToRemove.add((cy<<3)+cx);
                }
            }
        }
        simRemoveDotsVector(pointsToRemove); // Remove 'em
    }
    
    // Recursive helper function for add all loops and tings
    @SuppressWarnings("unchecked")
	public void completeThread(Vector<Integer> d, int p) // helper function. completes a thread by all possibilities
    {
        if (!d.contains(p)) {
            // wenn nicht nacht rechts gegangen, links nicht ausserhalb des brettes und die farbe links stimmt, gehe nach links
            if ( (p-d.lastElement()) !=  1 ) {
//                System.err.println("L "+(p&7)+"/"+(p>>3));
                if (  ( (p&7) >0 ) && ( v[(p&7)-1][p>>3] == v[d.firstElement()&7][d.firstElement()>>3] )  )
                {
                    Vector<Integer> dd = (Vector<Integer>) d.clone();
                    dd.add(p);
//                    if (DebugThreads) System.err.println("  Thread L ->"+((p-1)&7)+"/"+((p-1)>>3));                
                    completeThread(dd, p-1);
                }
            }
            // etc.
            if ( (p-d.lastElement()) != -1 ) {
//                System.err.println("R "+(p&7)+"/"+(p>>3));
                if ( ( (p&7) <5 ) && ( v[(p&7)+1][p>>3] == v[d.firstElement()&7][d.firstElement()>>3] )  )
                {
                    Vector<Integer> dd = (Vector<Integer>) d.clone();
                    dd.add(p);
//                    if (DebugThreads) System.err.println("  Thread R ->"+((p+1)&7)+"/"+((p+1)>>3));                
                    completeThread(dd, p+1);
                }
            }
            if ( (p-d.lastElement()) !=  8 ) {
//                System.err.println("U "+(p&7)+"/"+(p>>3));
                if ( ( (p>>3)>0 ) && ( v[p&7][(p>>3)-1] == v[d.firstElement()&7][d.firstElement()>>3] )  )
                {
//                    System.err.println("U2");
                    Vector<Integer> dd = (Vector<Integer>) d.clone();
                    dd.add(p);
//                    if (DebugThreads) System.err.println("  Thread U ->"+((p-8)&7)+"/"+((p-8)>>3));                
                    completeThread(dd, p-8);
                }
            }
            if ( (p-d.lastElement()) != -8 ) {
//                System.err.println("D "+(p&7)+"/"+(p>>3));
                if ( ( (p>>3)<5 ) && ( v[p&7][(p>>3)+1] == v[d.firstElement()&7][d.firstElement()>>3] )  )
                {
                    Vector<Integer> dd = (Vector<Integer>) d.clone();
                    dd.add(p);
//                	if (DebugThreads) System.err.println("  Thread D ->"+((p+8)&7)+"/"+((p+8)>>3));                
                    completeThread(dd, p+8);
                }
            }
//            System.err.println("B");

        	d.add(p);
        	HashSet<Integer> dHashSet = new HashSet<Integer>(d);
        	if (!tingsSet.contains(dHashSet))       			
        	{
        		tingsSet.add(dHashSet);
        		tings.add(d);
            } else {
//            	System.err.println("DOUBLE TING"+d.toString());
            }
        } else {
            // loop, always an endpoint
            d.add(p);
            HashSet<Integer> dHashSet = new HashSet<Integer>(d);
            
            if (!loopsSet.contains(dHashSet)) {
//            	System.err.println("NOT CONTAINS"+d.toString());
//            	System.err.println(dHashSet);
                loopsSet.add(dHashSet);
                loops.add(d);
            } else {
//            	System.err.println("DOUBLE LOOP"+d.toString());
            }
        }
        if (DebugThreads) System.err.println("  Thread END");                
    }
    
    public boolean removeDuplicateLoops()
    {
        // Remove loops with similar effect
		
        for (Vector<Integer> vi : loops)
		{
            for (Vector<Integer> vj : loops)
    		{
            	if (vj==vi) continue;
            	
            	Board newBoard1;
            	newBoard1 = (Board)this.clone();    		
            	newBoard1.simRemoveDotsLoop(vi);
            	Board newBoard2;
            	newBoard2 = (Board)this.clone();    		
            	newBoard2.simRemoveDotsLoop(vj);
            	
            	if (newBoard1.hasEqualDots(newBoard2))
            	{
            		if (vj.size()>vi.size())
            			loops.remove(vj); 
            		else
            			loops.remove(vi);             			
            		return true;
            	}
    		}
		}    	
        return false;
    }
    
    public boolean removeDuplicateTings()
    {
        // Remove loops with similar effect
		
        for (Vector<Integer> vi : tings)
		{
            for (Vector<Integer> vj : tings)
    		{
            	if (vj==vi) continue;
            	
            	Board newBoard1;
            	newBoard1 = (Board)this.clone();    		
            	newBoard1.simRemoveDotsVector(vi);
            	Board newBoard2;
            	newBoard2 = (Board)this.clone();    		
            	newBoard2.simRemoveDotsVector(vj);
            	
            	if (newBoard1.hasEqualDots(newBoard2))
            	{
            		if (vj.size()>vi.size())
            			tings.remove(vj); 
            		else
            			tings.remove(vi);             			
            		return true;
            	}
            	
    		}
		}    	
        return false;
    }
    
    public void removeTingsThatAreRemovedByLoops() {
   	 // THIS IS A "RADICAL" ASSUMPTION making the whole
    	// process MUCH quicker. It means that no substrings that can be cleared by a square are considered.
    	// This might MISS CHANCES!!!

    	// This is a "Really new loop effect", 
		// add it to the loops hashmap with all effects, so that removeTingsThatAreRemovedByLoops
		// can terminate them
    	   	
    	for (Vector<Integer> vi : loops)    		
		{
        	Vector<Integer> deleteTheseTings = new Vector<Integer>();
    		int c = v[vi.firstElement()&7][vi.firstElement()>>3];
			HashSet<Integer> loopHash = new HashSet<Integer>();
			for (int cx=0; cx<6; cx++) {
	    		for (int cy=0; cy<6; cy++) {
	    			if (v[cx][cy]==c) {
//	    				System.err.println("add" + (cy<<3)+cx);	    				
	    				loopHash.add((cy<<3)+cx);
	    			}
	    		}
			}
//			System.err.println();
			for (int tingcx=0; tingcx<tings.size(); tingcx++)
	    	{
				HashSet<Integer> localTingSet = new HashSet<Integer>(tings.elementAt(tingcx));
	        	if (loopHash.containsAll(localTingSet)) {
	        		deleteTheseTings.add(new Integer(tingcx));
	        	}
	    	}
	    	Collections.sort(deleteTheseTings);
	    	//System.err.println("L"+deleteTheseTings.toString());
	    	
//	    	System.err.print(tings.size()+"-->");
	    	for(int tingcx=deleteTheseTings.size()-1; tingcx >= 0; tingcx--)
	    	{
	    		int tingIndex = deleteTheseTings.elementAt(tingcx);
	    		//System.err.print("X"+tingIndex+"Y");
	    		tings.removeElementAt(tingIndex);
	    	}
//	    	System.err.println(tings.size());
		}		
    }
    
    // Add all loops and tings (all possible "sweeps", circular and open
    public void addAllLoopsAndTings()
    {
    	// There SEEMS to be some rare error, where #2 and #3 of a thread are mixed up... keep watching for bugs ;)
        loops.clear();
        loopsSet.clear();
        tings.clear();
        tingsSet.clear();
        for (int vy=5; vy>=0; vy--) {
//        	System.err.println("vy="+vy);
            for (int vx=0; vx<6; vx++) {
//            	System.err.println(" vx="+vx);
                // not "unknown" and not at the left border, try start left
                if (vx>0) {
                    if (  (v[vx][vy] > 0) && (v[vx][vy] == v[vx-1][vy])  ) {
                        Vector<Integer> vv = new Vector<Integer>();
                        vv.add( (vy<<3)+vx );
                        if (DebugThreads) System.err.println("Start Thread Left "+vx+","+vy+"/"+(vx-1)+","+vy);
                        completeThread( vv, (vy<<3)+vx-1);
                    }
                }
                // not at right border, try to start direction right
                if (vx<5) { //<5, only at the border not to add the same stuff backwards. 
                	// be aware that this leads to EXCLUSION of figures : one right and loop is NOT FOUND
                	// - but loops are all found so this is not a problem, if appendixes are lost as they're removed anyway -
                    if (  (v[vx][vy] > 0) && (v[vx][vy] == v[vx+1][vy])  ) {
                        Vector<Integer> vv = new Vector<Integer>();
                        vv.add( (vy<<3)+vx );
                        if (DebugThreads) System.err.println("Start Thread Right "+vx+","+vy+"/"+(vx+1)+","+vy);
                        completeThread( vv, (vy<<3)+vx+1);
                    }
                }
                // not at upper border, try to start direction upper
                if (vy>0) { 
                    if (  (v[vx][vy] > 0) && (v[vx][vy] == v[vx][vy-1])  ) {
                        Vector<Integer> vv = new Vector<Integer>();
                        vv.add( (vy<<3)+vx );
                        if (DebugThreads) System.err.println("Start Thread Upper "+vx+","+vy+"/"+vx+","+(vy-1));
                        completeThread( vv, ((vy-1)<<3)+vx);
                    }
                }
                // not at lower border, try to start direction down
                if (vy<5) { //<5, only at the border not to add the same stuff backwards
                    if (  (v[vx][vy] > 0) && (v[vx][vy] == v[vx][vy+1])  ) {
                        Vector<Integer> vv = new Vector<Integer>();
                        vv.add( (vy<<3)+vx );
                        if (DebugThreads) System.err.println("Start Thread Lower "+vx+","+vy+"/"+vx+","+(vy+1));
                        completeThread( vv, ((vy+1)<<3)+vx);
                    }
                }
            }
        }
        int loopsSize = loops.size();
        //System.err.println(loops.size() + " Loops and "+tings.size()+" Tings");
        if (depth<2) {
        	while (removeDuplicateLoops()) {};
        	removeTingsThatAreRemovedByLoops();
        }
        //while (removeDuplicateTings()) {};
        //System.err.println(loops.size() + " Loops and "+tings.size()+" Tings (reduced)");
//        if (loops.size()<loopsSize) System.err.println("Removed "+(loopsSize-loops.size())+" loops (duplicates)");
    }
    
    public void printLoopsAndTingsToConsole()
    {
        System.err.println(loops.size() + " Loops and "+tings.size()+" Tings");
        System.err.println("---------------");
        
        int ln=0;
        for (Vector<Integer> vi : loops)
        {
            System.err.print("Loop "+(ln++)+" : ");
            for (Integer i : vi)
            {
                int x = i.intValue() & 7;
                int y = i.intValue() >> 3;
                System.err.print("("+x+","+y+")");
            }
            System.err.println();
        }
        
        int tn=0;
        for (Vector<Integer> vi : tings)
        {
            System.err.print("Ting "+(tn++)+" : ");
            for (Integer i : vi)
            {
                int x = i.intValue() & 7;
                int y = i.intValue() >> 3;
                System.err.print("("+x+","+y+")");
            }
            System.err.println();
        }
        
        System.err.println();
    }
    
    public JPanel getBoardPanel()
    {
    	return (JPanel) boardJPanel;
    }
    
    public void plotBoard()
    {
    	circles.clear();
        for (int vy=0; vy<6; vy++) {
            for (int vx=0; vx<6; vx++)
            {
            	circles.addCircle(new Circle(vx, vy, v[vx][vy]));
            }
        }
        System.err.println("Circles updated");
        boardJPanel.repaint();
    }
    
    public int getScores(int _depthlimit, int _depth)
    {    	
    	nnn++; if ((nnn%100)==0) System.err.print(".");
    	depthlimit = _depthlimit;
    	depth = _depth;
        if (_depth==0) maxScore = 0;

    	
//    	System.err.println("DEPTH "+depth);
//    	if (depth <= depthlimit) {
    		{

    		// # of the maximum element path corr. to maxScore
//    		if (depth==0) System.err.println(" Max score : ");
    		
    		//System.err.println("XXX+"+tings.size());
    		int tcx=0;
    		for (Vector<Integer> vi : tings)
    		{
    	        
    			//if (depth==0) System.err.print("T"+(tcx)+"/"+tings.size()+" ");
//    		for (int i = tings.size(); i-- > 0; ) 
//    		{
//    			Vector<Integer> vi = tings.elementAt(i);
    			//if (depth==0) System.err.print(".");
    			//    		System.err.print("Ting "+(tn++)+" depth "+depth+" of "+depthlimit+" : ");
    			Board newBoard;
    			newBoard = (Board)this.clone();    		
    			newBoard.simRemoveDotsVector(vi);
        		newBoard.addAllLoopsAndTings();
    			//    			System.err.println("RECURSE");
    			//    			newBoard.printBoardToConsole();
    			//    			newBoard.printLoopsAndTingsToConsole();
//    			System.err.println("SCORE "+newBoard.score+" DEPTHSCORE "+newBoard.depthScore);
        		if ((newBoard.score+newBoard.depthScore)>maxScore) maxScore=Float.valueOf(newBoard.score+newBoard.depthScore).intValue();
        		int t;
        		if (depth < depthlimit) 
        			t = newBoard.getScores(newBoard.depthlimit, newBoard.depth+1);
        		else
        			t = newBoard.score;

    			//if (t>maxScore) maxPath=(1<<16)+tcx;// The later, the lower(in terms of y), the better
    			if (t>maxScore) { 
//    				System.err.println("Score : "+newBoard.score);
    				System.err.print(t+"(T"+tcx+")");
//    				if (depth<1) System.err.println();
//    				maxPath = (1<<16)+i;
    			}
    			if ((depth==0)&&(t>maxScore))
    			{
    				System.err.print(t+"(XT"+tcx+")");
    				maxScore = t;
    				maxPath = (1<<16)+tcx;    				
    			}
    			tcx++;
    		}
    		
    		// Now, try to make a loop with the same score. Always replace until the loop is as "low" as possible
    		//if (depth==0) System.err.print(loops.size()+tings.size());
    		int lcx=0;
    		for (Vector<Integer> vi : loops)
    		{
//    		for (int i = loops.size(); i-- > 0; ) 
//    		{
//    			Vector<Integer> vi = loops.elementAt(i);
//    			if (depth==0) System.err.print(".");
    			//    		System.err.print("Loop "+(ln++)+" depth "+depth+" of "+depthlimit+" : ");
    			Board newBoard;
    			newBoard = (Board)this.clone();    		
    			newBoard.simRemoveDotsLoop(vi);
        		newBoard.addAllLoopsAndTings();
    			//    			System.err.println("RECURSE");
    			//    			newBoard.printBoardToConsole();
    			//    			newBoard.printLoopsAndTingsToConsole();
//    			System.err.println("SCORE "+newBoard.score+" DEPTHSCORE "+newBoard.depthScore);
        		if ((newBoard.score+newBoard.depthScore)>maxScore) maxScore=Float.valueOf(newBoard.score+newBoard.depthScore).intValue();
        		int t;
        		if (depth < depthlimit) 
        			t = newBoard.getScores(newBoard.depthlimit, newBoard.depth+1);
        		else
        			t = newBoard.score;
    			
    			//if (t==maxScore) maxPath=lcx;// The later, the lower(in terms of y), the better
    			if (t>maxScore) { 
    				System.err.print(t+"(L"+lcx+")");
//    				if (depth<1) System.err.println();
//    				maxPath = i;
    			}
    			if ((depth==0)&&(t>maxScore))
    			{
    				System.err.print(t+"(XL"+lcx+")");
    				maxScore = t;
    				maxPath = lcx;
    			}
    			//    		System.err.println("Score : "+newBoard.score);
    			lcx++;    			
    		}    		
    		
//        	System.err.print(" Depth : "+depth+", ");
    		
    	}
        if (depth==0) System.err.println("Max score : "+maxScore);

    	return maxScore;
    }
    
    public Vector<Integer> getBestPath() {    	
		if (maxPath<(1<<16)) {
			return loops.elementAt(maxPath);
		} else {
			return tings.elementAt(maxPath-(1<<16));
		}   				
    }
    
    public void plotBestPathToConsole()
    {
		if (maxPath<(1<<16)) {
			Vector<Integer> elements = loops.elementAt(maxPath);
			for (Integer element:elements )
			{
				System.err.print((element&7)+"/"+(element>>3)+" ");
			}
		} else {
			Vector<Integer> elements = tings.elementAt(maxPath-(1<<16));
			for (Integer element:elements )
			{
				System.err.print((element&7)+"/"+(element>>3)+" ");
			}
		}
		System.err.println();
    }
    
    public int numberOfLoopsAndTings()
    {
    	System.err.println("Loops : "+loops.size()+" and Tins : "+tings.size());
    	return loops.size()+tings.size();
    }
    
}