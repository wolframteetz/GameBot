package sirTankiBot;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Comparator;

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

public class Status extends KDNodeComparator<Status> implements Serializable {

	private static final long serialVersionUID = 6302403882954768992L;
	// leftx, rightx, topy, bottomy
	
//	int rays = status.state.length/mapTerrainTypes;
	
	public double [] state;		 	// 2 * # OF RAYS TO CAST FOR EACH TERRAIN TYPE (DEFAULT 12) TIMES MAP TERRAIN TYPES (SEE BOARD). MODIFY AS NECESSARY
	//public boolean w,a,s,d,sp;
	public boolean [] action;
	// Enemy ahead?
	public double enemyAheadDegrees;

	public int terrainRays;
	public int terrainTypesOnMap;
	
	public double nearestEnemyDistance;
	public double nearestEnemyDegrees;
	
	public boolean mouseIsEnemy = false;
	public boolean turretTurning = false;
	/** "Autopilot" meaning automatic terrain-based driving service enabled */
	public long turretTurningStartSystemTime;
	public long tankMovementChangedMillis;
	boolean turretIsTurned = false;			
	public boolean firingOnlyMode = false;
	
	public boolean energyLow = false;
	public boolean energyEmpty = false;
	public boolean fireInterrupted = false;
	
	boolean autofire = false;		// Automatic firing
	/** Do not turn turret to target and fire, instead turn turret until target ahead and fire. */
	boolean directFireOnly = true;
	boolean autotrack = false;		// Automatic target tracking
	boolean greedyfire = false;		// Automatic driving towards closest target out of range (if there is one close enough, for freeze/fire/isi)
	boolean idling = false;
	
	public long captureTimeMillis; // System time milliseconds at the time of the screen capture

	// Own tank color
	public enum TankColor{blue, red, green};
	public TankColor tankColor = TankColor.green;
	
	public Status(int _terrainRays, int _terrainTypesOnMap)
	{
		super();
		terrainRays = _terrainRays;
		terrainTypesOnMap = _terrainTypesOnMap;
		state = new double[terrainTypesOnMap*terrainRays];	
		action = new boolean[5];
	}
	
	public Status(Status _status)
	{
		super();
		
		terrainRays = _status.terrainRays;
		terrainTypesOnMap = _status.terrainTypesOnMap;
		state = new double[terrainTypesOnMap*terrainRays];
		action = new boolean[5];		
		for (int cx=0; cx<_status.state.length; cx++)
			state[cx] = _status.state[cx];
		for (int cx=0; cx<_status.action.length; cx++)
			action[cx] = _status.action[cx];
		captureTimeMillis = _status.captureTimeMillis;
	}
	
    @Override
    public String toString() {
    	String s = new String();
		DecimalFormat df2 = new DecimalFormat( "00" );
    	for (int cx=0; cx<state.length; cx++) {
    		s = s+df2.format(state[cx]/10);
    		if (((cx+1)%terrainRays)==0) s = s +" - ";
    		else s = s +".";
    	}
		return  s+" "+(action[0]?"w":"")+(action[1]?"a":"")+(action[2]?"s":"")+(action[3]?"d":"")+(action[4]?"< >":"");
	}

    // Plot only vector #c
    public String toString(int c) {
    	String s = new String();
		DecimalFormat df2 = new DecimalFormat( "00" );
    	for (int cx=c*terrainRays; cx<(c+1)*terrainRays; cx++) {
    		s = s+df2.format(state[cx]/10);
    		if (((cx+1)%terrainRays)==0) s = s +" - ";
    		else s = s +".";
    	}
		return  s+" "+(action[0]?"w":"")+(action[1]?"a":"")+(action[2]?"s":"")+(action[3]?"d":"")+(action[4]?"< >":"");
	}

    @Override
    protected double squaredDistance(Status other) {
    	double sd = 0.0;
    	for (int x=0; x<state.length; x++) {
    		double t = (state[x] - other.state[x]);
    		sd = sd + ( t*t );
    	}
    	return sd;
    }

    @Override
    protected double axisSquaredDistance(Status other, int axis) {
        double distance = state[axis] - other.state[axis];
        return distance * distance;
    }

    @Override
    protected Comparator<Status> getComparator(int axis) {
    	return StatusComparator.values()[axis];
    }
    
    protected static enum StatusComparator implements Comparator<Status> {
        x {
            @Override
            public int compare(Status a, Status b) {
                return Double.compare(a.state[0], b.state[0]);
            }
        },
        y {
            @Override
            public int compare(Status a, Status b) {
                return Double.compare(a.state[1], b.state[1]);
            }
        },
        z{
            @Override
            public int compare(Status a, Status b) {
                return Double.compare(a.state[2], b.state[2]);
            }
        }
    }
}
