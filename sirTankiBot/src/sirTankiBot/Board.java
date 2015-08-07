package sirTankiBot;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import sirTankiBot.Status.TankColor;


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

/**
 * The representation of the tanki board 
 *
 */
public class Board {

	Robot bot;
	public Status status;
	public BattleConfig battleConfig;
	public AnnotatedImage annotatedImage;
	public int annotateColor;
	public long lastTimeCall;

	static int winx=1200;
	static int winy=winx*12/16; // Optimum view is 9/16

	// Screen capture position/width and resulting rectangle
	public int capx = (winx/2)-(winx/5);
	public int capy = (winy/2)-(winy/5)+40; // 40 for mac os menu
	int capwidth = (2*winx/5);
	int capheight = (2*winy/5);
	Rectangle area = new Rectangle(capx, capy, capwidth, capheight);

	// Normal view parameters
	int centerx = capwidth/2;
	int centery = capheight*10/16;
	//int centerRadius = capwidth/6;					// LOWER BEFORE RE-LEARNING!!!CAP/5 IS SAFE
//	int centerRadius = capwidth/5;					// LOWER BEFORE RE-LEARNING!!!CAP/5 IS SAFE
	int centerRadius = capwidth/10;					// LOWER BEFORE RE-LEARNING!!!CAP/5 IS SAFE

	// Point that is always more or less directly ahead/behind of the tank
	int aheadx = capwidth/2;
	int aheady = capheight*8/16;
	int behindx = capwidth/2;
	int behindy = capheight*12/16;
	

	public Board(Robot _bot, BattleConfig _battleConfig)
	{
		battleConfig = _battleConfig;
		status = new Status(battleConfig.getTerrainRays(), battleConfig.getTerrainTypesOnMap());
		bot = _bot;
		annotatedImage = new AnnotatedImage();
		annotateColor=0;
	}

	private void determineTankColor(BufferedImage bufferedImage)
	{

		int x=centerx-30;
		int y;

		for (y=behindy; y>aheady; y--) {
			if (
					(battleConfig.isYellow(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y)))) ||  
					(battleConfig.isDarkYellow(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y))))   
					) {
				break;
			}
		}
		

		int lchange = -80;
		int rchange = -80;
		int nn=-80;
		for (; nn<80; nn++)
		{
			if (battleConfig.isYellow(bufferedImage.getRGB((int)Math.round(centerx+nn),(int)Math.round(y)))) {
				lchange = nn;
				break;
			}
		}
		for (; nn<80; nn++)
		{
			if (!battleConfig.isYellow(bufferedImage.getRGB((int)Math.round(centerx+nn),(int)Math.round(y)))) {
				rchange = nn;
				break;
			}
		}
		
//		System.err.print(lchange+"  "+rchange+"  ");

			// Correct for movement
			int poffset;
			if ( Math.abs(lchange) >  Math.abs(rchange) ) {
				poffset = -(lchange+48);
			} else {
				poffset = -(rchange-48);
			}
			lchange +=poffset;
			rchange +=poffset;
			// Correct for movement
//		System.err.println(lchange+"  "+rchange);
			
		if ((lchange<rchange) && (lchange > -65) && (rchange < 65)) { // valid
			
//			System.err.println(lchange+"  "+rchange);
			if (rchange < -40) {
				status.energyLow   = true;
				status.energyEmpty = true;
			} else if (rchange < -20) {
				status.energyLow   = true;
				status.energyEmpty = false;
			} else {
				status.energyLow   = false;
				status.energyEmpty = false;
			}
		}

		x=centerx;
		// Yellow found, 7 above is tank color
		if (
				(battleConfig.isRedBarColor(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y-8))))  
				//				(battleConfig.isRedBarColor(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y-6)))) 
				) {
			if ((status.tankColor) != TankColor.red) {
				System.err.println("Tank Color -> Red");
				status.tankColor = TankColor.red;
			}
		}				
		if (
				(battleConfig.isGreenBarColor(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y-8))))  
				//				(battleConfig.isGreenBarColor(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y-6)))) 
				) {
			//			System.err.println("Tank Color -> Green");
			if ((status.tankColor) != TankColor.green) {
				System.err.println("Tank Color -> Green");
				status.tankColor = TankColor.green;
			}
		}				
		if (
				(battleConfig.isBlueBarColor(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y-8))))  
				//				(battleConfig.isBlueBarColor(bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y-6)))) 
				) {
			if ((status.tankColor) != TankColor.blue) {
				System.err.println("Tank Color -> Blue");
				status.tankColor = TankColor.blue;
			}
		}				
	}	
	private void createAnnotatedImage(BufferedImage bufferedImage) {
		annotatedImage.setImage(bufferedImage);
		String fps = Long.toString(1000/(System.currentTimeMillis()-lastTimeCall))+" fps";
		annotatedImage.setFPS(fps);
		ArrayList<String> statusString = new ArrayList<String>();
		statusString.add("autofire : "+status.autofire);
		statusString.add("autotrack : "+status.autotrack);
		statusString.add("directFireOnly : "+status.directFireOnly);
		statusString.add("enemyAheadDegrees : "+status.enemyAheadDegrees);
		statusString.add("energyEmpty : "+status.energyEmpty);
		statusString.add("energyLow : "+status.energyLow);
		statusString.add("fireInterrupted : "+status.fireInterrupted);
		statusString.add("firingOnlyMode : "+status.firingOnlyMode);
		statusString.add("nearestEnemyDegrees : "+status.nearestEnemyDegrees);
		statusString.add("nearestEnemyDistance : "+status.nearestEnemyDistance);
		statusString.add("turretIsTurned : "+status.turretIsTurned);
		statusString.add("turretTurning : "+status.turretTurning);
		annotatedImage.setStatusString(statusString);
		lastTimeCall = System.currentTimeMillis();
	}

	private void determineEnemiesInRange(BufferedImage bufferedImage)
	{
		// DETERMINE ENEMIES IN RANGE
		{
			
			int mpx = 0;
			int mpy = 0;
			if (status.mouseIsEnemy) {
				mpx = (int) MouseInfo.getPointerInfo().getLocation().getX()-capx;
				mpy = (int) MouseInfo.getPointerInfo().getLocation().getY()-capy;
			}
			double x=0.0;
			double y=0.0;
			status.enemyAheadDegrees = -999.9;

			status.nearestEnemyDegrees = -999.9;
			status.nearestEnemyDistance = 999.9;

			int rays = 360; // Number of targeting rays																			MODIFY ME AS NECESSARY
			int degrees = 360; // from left over top : 180= upper half only, 360=all around										MODIFY ME AS NECESSARY
			// 180 for full forward perspective, 360 for full out
			
			// This vector is filled with the distances to enemies for all rays
			// The offset "closestEnemyOffset" contains the offset to the enemy in range closest to the center (shoot this foe!) 
			int closestEnemyOffset = 0;
			int[] enemyDistanceInPixels = new int[rays];

			try {

				// annotate temps
				int anno_nearest_x = 0;
				int anno_nearest_y = 0;
//				int anno_ahead_x = 0;
//				int anno_ahead_y = 0;

				// Borders
				for (int lcx=0; lcx<rays; lcx++) {
					x = centerx;
					y = centery;
					double angle = lcx*2.0*degrees/360*Math.PI/rays; // from left over top, right to left = 2pi
					double dx = -Math.cos(angle);
					double dy = -Math.sin(angle);

					int currentTargetDistance = 0;
					for (int minDist=0; minDist<centerRadius; minDist++) {			//  MODIFY 50 IF NECESSARY
						x += dx;
						y += dy;
						currentTargetDistance += 1;
					}
					rayWhile:
						while ((x>=5) && (x+5<capwidth) && (y>=5) && (y+5<=capheight))
						{
							try {
								//						if (currentTargetDistance>battleConfig.getMaximumWeaponDistance()) break;

								
								if (
										( !status.mouseIsEnemy  && (battleConfig.isEnemyColor(status, bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y)))) )
										||
										( status.mouseIsEnemy && Math.abs(x-mpx)<8.0 && Math.abs(y-mpy)<8.0 )
										)
								{				
									// Test if this really an enemy
									int redcount=0;
									for (int ccx=-16; ccx<=16; ccx+=2) {
										if ((x+ccx<=5)||(x+ccx>=capwidth-5)) continue;
										for (int ccy=-16; ccy<=16; ccy+=2) {
											if ((y+ccy<=5)||(y+ccy>=capheight-5)) continue;
											int pixel = bufferedImage.getRGB((int)Math.round(x+ccx),(int)Math.round(y+ccy));
											// is it the CP?
											if (battleConfig.isWhiteColor(pixel)) break rayWhile;
											if (ccx<5&&ccx>-5&&ccy<5&&ccy>-5&&battleConfig.isRankYellow(pixel)) break rayWhile; // Rank icon is not a target but can contain RED
											// is it a red boxx?
											if (battleConfig.isBoxColor(pixel)) redcount++;
											if (redcount>28) break rayWhile; // BOX TODO BOX TODO BOX TODO BOX TODO BOX TODO BOX TODO BOX TODO 
										}									
									}

									// Direction to shoot at
									double thisDegrees = -(Math.PI/2.0)+angle;

									if (
											(currentTargetDistance>battleConfig.getMaximumWeaponDistance() * (status.firingOnlyMode?1.4:1.0)  ) &&
											(currentTargetDistance<battleConfig.getMaximumFollowToKillDistance()) // TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO  TODO 
											)	{
										// Enemy, out of range, is it the nearest one?
										if (currentTargetDistance<status.nearestEnemyDistance) {
											status.nearestEnemyDistance = currentTargetDistance;
											status.nearestEnemyDegrees = thisDegrees;
											anno_nearest_x = (int)x; anno_nearest_y = (int)y;
										}
										break;
									}
									// Enemy, out of range and not the nearest enemy found so far
									if (currentTargetDistance>battleConfig.getMaximumWeaponDistance()) {
										break;
									}
									// Add this enemy to the "Enemy radar"
									enemyDistanceInPixels[lcx] = currentTargetDistance;
									// If there is an enemy closer to "ahead of cannon", take out this foe
									if (Math.abs(thisDegrees)<Math.abs(status.enemyAheadDegrees)) {
										// Enemy radar offset to the enemy closest to "ahead" to to a better estimate of "enemyAheadDegrees" later
										closestEnemyOffset = lcx;				 
										status.enemyAheadDegrees = thisDegrees;
//										anno_ahead_x = (int)x; anno_ahead_y = (int)y;
									}
								} else { // Not an enemy
								}
								x += dx;
								y += dy;
								currentTargetDistance++;
							} catch (Exception e) {
								System.err.println(e.getMessage());
							}
						}
				}
				if (status.nearestEnemyDegrees>-999.0) annotatedImage.annotate(anno_nearest_x,anno_nearest_y,8);
				if (status.enemyAheadDegrees>-999.0) {
					// old method : annotatedImage.annotate(anno_ahead_x,anno_ahead_y,9);

					// Optimize the "enemy ahead degrees" value using the enemyDistanceInPixels array (Optional)
					// -----------------------------------------------------------------------------------------
					final int rayMissesUntilTargetEnds = 10; // if n rays miss the target, this is the end of the target
					final int distanceOffsetLimit = 50;     // if the point is more than n pixels more distant, this is a different foe, ignore him
					final int rankIconWidthInRays = 5;		// how "wide" is the rank icon to the left of the name in #rays? (Guessed ;)
					int rayMissCounter;						// counts down rays misses to zero, then breaks (not target anymore)
					int rayNumber;							// rayNumber counter
					// Left optimization scan 
					int left_ray_offset = 0;
					rayMissCounter = rayMissesUntilTargetEnds;
					rayNumber = closestEnemyOffset;
					while (rayMissCounter>0) {
						if (rayNumber<=0) break;
						double angle = rayNumber*2.0*degrees/360*Math.PI/rays; // from left over top, right to left = 2pi
						double dx = -Math.cos(angle);
						double dy = -Math.sin(angle);
						x = centerx + (rayNumber*dx);
//						y = centery + (rayNumber*dy);
						y = centery + (closestEnemyOffset*dy);
						if (Math.abs(enemyDistanceInPixels[rayNumber]-enemyDistanceInPixels[closestEnemyOffset])<distanceOffsetLimit) // This is the same foe
						{
							left_ray_offset = rayNumber;
						} else {
							rayMissCounter--;
						}
						rayNumber--; // scan left
						rayNumber = (rayNumber + rays) % rays; // e.g with 360 : -1 -> 359
					}
					// Right optimization scan 
					int right_ray_offset = rays-1;
					rayMissCounter = rayMissesUntilTargetEnds;
					rayNumber = closestEnemyOffset;
					while (rayMissCounter>0) {
						if (rayNumber>=rays) break;
						double angle = rayNumber*2.0*degrees/360*Math.PI/rays; // from left over top, right to left = 2pi
						double dx = -Math.cos(angle);
						double dy = -Math.sin(angle);
						x = centerx + (rayNumber*dx);
						//y = centery + (rayNumber*dy);
						y = centery + (closestEnemyOffset*dy);
						
						if (Math.abs(enemyDistanceInPixels[rayNumber]-enemyDistanceInPixels[closestEnemyOffset])<distanceOffsetLimit) // This is the same foe
						{
							right_ray_offset = rayNumber;
						} else {
							rayMissCounter--;
						}
						rayNumber++; // scan right
						rayNumber = (rayNumber + rays) % rays; // e.g with 360 : 361 -> 1, +rays is not necessary if "+", but this is a safer command
					}
					// Left and Right ray offset are determined. The foe is a little right of the center (left is the icon. this is fixed size.)
					left_ray_offset -= rankIconWidthInRays; // Correct for rank
					// Direction to shoot at
					double targetRay = (left_ray_offset+right_ray_offset)/2.0;
					{
						double angle = targetRay*2.0*degrees/360*Math.PI/rays;
						double thisDegrees = -(Math.PI/2.0)+angle;
						status.enemyAheadDegrees = thisDegrees;
					}
					// Annotate center
					{
						double angle, dx, dy;
						angle = targetRay*2.0*degrees/360*Math.PI/rays;	
						dx = -Math.cos(angle); 
						dy = -Math.sin(angle); 
						x = centerx + enemyDistanceInPixels[closestEnemyOffset]*dx; 
						y = centery + enemyDistanceInPixels[closestEnemyOffset]*dy;
						annotatedImage.annotate((int)x,(int)y,7); // New Method, CENTER
						angle = left_ray_offset*2.0*degrees/360*Math.PI/rays;	
						dx = -Math.cos(angle); 
						dy = -Math.sin(angle); 
						x = centerx + enemyDistanceInPixels[closestEnemyOffset]*dx; 
						y = centery + enemyDistanceInPixels[closestEnemyOffset]*dy;
						annotatedImage.annotate((int)x,(int)y,8); // New Method, LEFT
						angle = right_ray_offset*2.0*degrees/360*Math.PI/rays;	
						dx = -Math.cos(angle); 
						dy = -Math.sin(angle); 
						x = centerx + enemyDistanceInPixels[closestEnemyOffset]*dx; 
						y = centery + enemyDistanceInPixels[closestEnemyOffset]*dy;
						annotatedImage.annotate((int)x,(int)y,8); // New Method, RIGHT
					}
				}
				
			} catch (Exception e) {
				System.err.println("Capture fatal "+x+":"+y+":"+e);
			}

			if (status.enemyAheadDegrees>Math.PI)
				status.enemyAheadDegrees = -(2*Math.PI)+status.enemyAheadDegrees;
		}
	}

	private void determineDistanceToPatters(BufferedImage bufferedImage)
	{
		double x=0.0;
		double y=0.0;
		int degrees = 360; // from left over top : 180= upper half only, 360=all around									MODIFY ME AS NECESSARY

		try {
			for (int lcx=0; lcx<battleConfig.getTerrainRays(); lcx++)
				for (int tcx=0; tcx<battleConfig.getTerrainTypesOnMap(); tcx++) 
					status.state[(tcx*battleConfig.getTerrainRays())+lcx] = 0.0;

			for (int lcx=0; lcx<battleConfig.getTerrainRays(); lcx++) { // one direction/ray after another

				x = centerx;
				y = centery;
				double angle = lcx*2.0*degrees/360*Math.PI/battleConfig.getTerrainRays(); // from left over top, right to left = 2pi
				double dx = -Math.cos(angle);
				double dy = -Math.sin(angle);

				ArrayList<UndetectedTerrainType> notFoundTerrainTypes = new ArrayList<UndetectedTerrainType>(battleConfig.getTerrainTypesOnMap());

				for (int tcx=0; tcx<battleConfig.getTerrainTypesOnMap(); tcx++) {
					notFoundTerrainTypes.add(new UndetectedTerrainType(tcx));
				}

				while ((x>=8) && (x+8<capwidth) && (y>=8) && (y+8<=capheight))
				{
					int pixelValue = bufferedImage.getRGB((int)Math.round(x),(int)Math.round(y));
					Iterator<UndetectedTerrainType> it = notFoundTerrainTypes.iterator();
					if (!it.hasNext()) break; // found each terrain type already on this ray
					while (it.hasNext()) { // iterate over terrain types not yet found
						UndetectedTerrainType terrainType = it.next();
						//if (surrounding.contains(pixelValue) break;
						if (status.state[(terrainType.notFoundTerrainType*battleConfig.getTerrainRays())+lcx]>centerRadius) // MINIMUM DISTANCE 							MODIFY ME AS NECESSARY
						{
							int hitThreshold1 = battleConfig.getMinHitQueueHits();
							int hitThreshold2 = battleConfig.getMinHitQueueHits();

							terrainType.terrainHits.addLast( battleConfig.isTerrain(pixelValue, terrainType.notFoundTerrainType) );
							if (terrainType.terrainHits.size()>battleConfig.getHitQueueLength()) terrainType.terrainHits.removeFirst();

							// count hits out of 10
							int hits1=0; int hits2=0;
							for (Integer t:terrainType.terrainHits) {
								if (t==0) hits1++;
								if (t==1) hits2++;
								//if (t==2) {hits1++; hits2++;}
							}

							if (hits1>=hitThreshold1 && hits2>=hitThreshold2) {
								if ( 
										(terrainType.notFoundTerrainType==annotateColor) ||
										(annotateColor==battleConfig.getTerrainTypesOnMap()) 
										)
									annotatedImage.annotate((int)Math.round(x),(int)Math.round(y),terrainType.notFoundTerrainType);
								it.remove();
								break;
							}
						}
						status.state[(terrainType.notFoundTerrainType*battleConfig.getTerrainRays())+lcx] += 1; // not found this terrain yet, so increment minimum distance at which it is seen and look at next pixel
					}
					x += dx;
					y += dy;
				}
			}
		} catch (Exception e) {
			System.err.println("Capture fatal "+x+":"+y+":"+e);
		}
	}
	/*
	private BufferedImage filter(BufferedImage i)
	{
		BufferedImage o = new BufferedImage(i.getWidth(), i.getHeight(), i.getType());
		int sx=5; int sy=5;
		for (int x=0; x<i.getWidth()-sx; x++)
		{
			for (int y=0; y<i.getHeight()-sy; y++)
			{
				BufferedImage si=i.getSubimage(x, y, sx, sy);
				int[] rgb = ((DataBufferInt) si.getRaster().getDataBuffer()).getData();

				Arrays.sort(rgb);
				int median;
				if (rgb.length % 2 == 0)
					median = rgb[rgb.length/2] + rgb[rgb.length/2 - 1]/2;
				else
					median = rgb[rgb.length/2];

				o.setRGB(x, y, median);
			}
		}
		return o;

	}
*/
	private BufferedImage filter(BufferedImage img)
	{

		BufferedImage o = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		try {
			int sx=3; int sy=3;
			int nr=(sx+1)*(sy+1);
			Color[] pixel = new Color[nr];
			int[] R = new int[nr];
			int[] G = new int[nr];
			int[] B = new int[nr];
			for(int i=1;i<img.getWidth()-sx;i++)
				for(int j=1;j<img.getHeight()-sy;j++) {
					int n=0;
					for (int x=0; x<sx; x++)
						for (int y=0; y<sy; y++)
						{
							pixel[n++]=new Color(img.getRGB(i+x, j+y));
							for(int k=0;k<n;k++){
								R[k]=pixel[k].getRed();
								B[k]=pixel[k].getBlue();
								G[k]=pixel[k].getGreen();
							}
						}
					Arrays.sort(R);
					Arrays.sort(G);
					Arrays.sort(B);
					o.setRGB(i,j,new Color(R[n/2],G[n/2],B[n/2]).getRGB());
				}
		} catch (Exception e) {
			System.err.println(e);
		}

		return o;
	}
	public void readBoardFromScreen()
	{
		synchronized (status) {
			BufferedImage bufferedImage;
			try { 
				bufferedImage = filter(bot.createScreenCapture(area));
				
				status.captureTimeMillis = System.currentTimeMillis();
			} catch (Exception e) {
				System.err.println("Failed capturing screen. Fatal.");
				return;
			}
			determineTankColor(bufferedImage);
			createAnnotatedImage(bufferedImage);
			if (status.autofire || status.mouseIsEnemy)
			{
				determineEnemiesInRange(bufferedImage);
			} else {
				determineDistanceToPatters(bufferedImage);
			}
				if ( (status.nearestEnemyDegrees<-999.0 || !status.greedyfire) && status.enemyAheadDegrees<-999.0 && // no enemy		 	
				    !status.firingOnlyMode && !status.fireInterrupted && !status.turretTurning) { // not care-for-enemies-only
				determineDistanceToPatters(bufferedImage);
			}
		}
		annotatedImage.repaint();
	}

}
