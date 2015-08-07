package sirTankiBot;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

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

/** Battle configuration (Which tank, map, weapon is used, which terrain types are there) */
public class BattleConfig {

	public enum ViewType { FirstView, LowView, NormView, TopView };
	public enum WeaponType {SmokyM0, TwinsM0, SmokyM2, ThunderM0, ThunderM0CP, FireM0, FreezeM3, IsidaM3, RicoM2};
	public enum TankType {WaspM0, HunterM0, DictatorM0, WaspM3, HornM2, WikiM2, WikiM1};
	public enum MapName {Arena, Duel, Erdhuegel, Garder, Insel, Koeln, Kolchose, Laerm, Sandal, Sandkasten, Schiessplatz, Serpuchow, Station, Stadion, Stille, Wueste, Beginner};
	public enum MapTime {Sommer, Winter, Nacht};

	public ViewType view;
	public TankType tank;
	public WeaponType weapon;
	public MapName mapName;
	public MapTime mapTime;

	public BattleConfig(ViewType _view, TankType _tank, WeaponType _weapon, MapName _mapName, MapTime _mapTime)
	{
		view = _view;
		tank = _tank;
		weapon = _weapon;
		mapName = _mapName;
		mapTime = _mapTime;
	}

	public String getMapFilename()
	{
		return ""+mapName+"_"+mapTime+"_"+view+"_"+tank+"_"+weapon+".cfg";
	}

	public int getHitQueueLength()
	{
		return 20; // hit 8 out of 10 pixels -> hitQueueLength 10, hitThreshold=8
	}

	public int getMinHitQueueHits()
	{
		return 4;
	}

	public int getHullTurnTime(double degrees)
	{
		switch(tank)
		{
		case WaspM0: return (int)(800*degrees);
		case HunterM0: return (int)(800*degrees);
		case DictatorM0: return (int)(800*degrees);
		case WaspM3: return (int)(600*degrees);  
		case HornM2: return (int)(750*degrees);  
		case WikiM2: return (int)(900*degrees);  
		case WikiM1: return (int)(1100*degrees);  
		}
		return 0;
	}

	public boolean isSingleShotWeapon()
	{
		switch (weapon) {
		case SmokyM0:
			return true;
		case TwinsM0:
			return false;
		case SmokyM2:
			return true;
		case RicoM2:
			return true;
		case ThunderM0:
			return true;
		case ThunderM0CP:
			return true;
		case FireM0:
			return false;
		case FreezeM3:
			return false;
		case IsidaM3:
			return false;
		default:
			return true;
		}
	}

	public int gunFireOnlyDelay() {
		switch (weapon) {
		case SmokyM0:
			return 3500; // 4sek
		case TwinsM0:
			return 500; // 4sek
		case SmokyM2:
			return 3500; // 4sek
		case ThunderM0:
			return 3500; // 4sek
		case RicoM2:
			return 500; // 4sek
		case ThunderM0CP:
			return 2500; // 4sek
		case FireM0:
			return 2000; // 4sek
		case FreezeM3:
			return 500; // 4sek
		case IsidaM3:
			return 500; // 4sek
		default:
			return 200;
		}
	}

	private double getTurnSpeedFactor()
	{
		double viewSpeedFactor;
		switch(view)
		{
		case FirstView:
			viewSpeedFactor = 0.5;
			break;
		case LowView:
			viewSpeedFactor = 0.8;
			break;
		case NormView:
			viewSpeedFactor = 1.0;
			break;
		case TopView:
			viewSpeedFactor = 1.5;
			break;
		default:
			viewSpeedFactor = 1.0;
			break;
		}
		return viewSpeedFactor;
	}
	
	public int getTurretTurnTime(double degrees)
	{
		double[][] data = new double[][]{ {0.0, 150}, {0.1,300}, {0.2,500}, {0.3,600}, {0.4,720}, {0.5,820}, {0.6,950}, {0.8,1200}, {1.0,1400}, {1.2,1700}, {1.4,2000}, {2.4,3000}, {3.2,3800}};
		double[] x = new double[data.length]; for (int lx=0; lx<data.length; lx++) x[lx] = data[lx][0];
		double[] y = new double[data.length]; for (int lx=0; lx<data.length; lx++) y[lx] = data[lx][1] * 58.1 * getTurnSpeedFactor();
		// Thunder M0  Turning degrees (of 2 PI) to turning time in milliseconds.
		// Turning speed in tanki units : 58.1.

		switch(weapon)
		{
		case SmokyM0: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 58.7; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case TwinsM0: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 74.5; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case SmokyM2: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 97.6; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }		
		case ThunderM0: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 58.1; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case RicoM2: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 101.4; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case ThunderM0CP: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 58.1; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case FireM0: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 75.6; // Turning rate in tanki units	
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case IsidaM3: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 117.4; // Turning rate in tanki units	
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		case FreezeM3: {
			for (int lx=0; lx<data.length; lx++) y[lx] /= 168.2; // Turning rate in tanki units	
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return (int) polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnTime - returning default"); 
				return (int) degrees*1000;
			} }
		}
		return 0;
	}

	public double getTurretTurnSpeed(double degrees)
	{
		double[][] data = new double[][]{ {0.0, 0}, {0.0, 150}, {0.1,300}, {0.2,500}, {0.3,600}, {0.4,720}, {0.5,820}, {0.6,950}, {0.8,1200}, {1.0,1400}, {1.2,1700}, {1.4,2000}, {2.4,3000}, {3.2,3800}};
		double[] x = new double[data.length]; for (int lx=0; lx<data.length; lx++) x[lx] = data[lx][1] * 58.1 * getTurnSpeedFactor();
		double[] y = new double[data.length]; for (int lx=0; lx<data.length; lx++) y[lx] = data[lx][0];

		switch(weapon)
		{
		case ThunderM0: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 58.1; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case RicoM2: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 101.4; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case ThunderM0CP: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 58.1; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);
			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case SmokyM0: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 58.7; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case TwinsM0: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 74.5; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }		
		case SmokyM2: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 97.6; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case FireM0: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 75.6; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case IsidaM3: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 117.4; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		case FreezeM3: {
			for (int lx=0; lx<data.length; lx++) x[lx] /= 168.2; // Turning rate in tanki units
			PolynomialSplineFunction polySplineF = new SplineInterpolator().interpolate(x, y);

			try {
				return polySplineF.value(degrees);
			} catch (ArgumentOutsideDomainException e) {
				System.err.println("Interpolation FAILED for getTurretTurnSpeed - returning default"); 
				return degrees*1000;
			} }
		default:
			break;
		}
		return 0;
	}

	public int getMaximumWeaponDistance()
	{
		switch(weapon)
		{
		case SmokyM0: return 400;
		case TwinsM0: return 400;
		case SmokyM2: return 400;
		case ThunderM0: return 220;
		case RicoM2: return 220;
		case ThunderM0CP: return 210;
		case FireM0: return 160;
		case IsidaM3: return 160;
		case FreezeM3: return 160;
		}
		return 0;
	}

	public int getMaximumFollowToKillDistance() {

		switch(weapon)
		{
		case SmokyM0: return 400;
		case TwinsM0: return 400;
		case SmokyM2: return 400;
		case ThunderM0: return 400;
		case RicoM2: return 400;
		case ThunderM0CP: return 400;
		case FireM0: return 220;
		case IsidaM3: return 220;
		case FreezeM3: return 220;
		}
		return 0;
	}

	public boolean getDistance(TerrainType terrainType, int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;

		switch (terrainType)
		{
		case CP:		if (coldif(r,g,b,         108,         108,         108,         0) <   3) return true; // gray invisible
		if (coldif(r,g,b,         201,          17,          17,         0) <   3) return true; // red invisible
		if (coldif(r,g,b,         240,         210,         210,        10) <  20) return true; // red invisible
		if (coldif(r,g,b,         255,         255,         255,         0) <  20) return true; // white writing on "A" / Point
		if (coldif(r,g,b,          18,          71,         203,         0) <   3) return true; break; // blue invisible

		case BoxGreenStone:	if (coldifdif(r,g,b,58,62,24,109,118,53)<17) return true; break;
		case BoxStoneGreen:	if (coldifdif(r,g,b,28,30,18,105,111,99)<6) return true; break;
		case BoxStoneWall:	if (coldifdif(r,g,b,23,26,21,93,99,89)<5) return true; if (coldifdif(r,g,b,20,22,17,86,91,80)<9) return true; break;
		case BoxWallStone:	if (coldifdif(r,g,b,82,89,74,105,115,94)<3) return true; if (coldifdif(r,g,b,39,48,42,59,72,66)<4) return true; break;
		case BoxGreenOuter:	if (coldifdif(r,g,b,49,54,17,117,126,55)<20) return true; break;
		case BoxOuterGreen:	if (coldifdif(r,g,b,105,98,67,161,150,117)<7) return true; break;
		case BoxGreenDarkOuter:	if (coldifdif(r,g,b,56,58,21,115,122,62)<17) return true; break;
		case BoxDarkOuterGreen:	if (coldifdif(r,g,b,56,59,42,81,88,71)<3) return true; break;
		case BoxGreenEarth:	if (coldifdif(r,g,b,66,45,24,116,95,54)<12) return true; break;
		case BoxEarthGreen:	if (coldifdif(r,g,b,49,52,24,118,127,64)<19) return true; break;
		case BoxEarthHouse:	if (coldifdif(r,g,b,56,41,22,119,93,67)<14) return true; break;
		case BoxHouseEarth:	if (coldifdif(r,g,b,41,36,28,68,63,49)<5) return true; break;
		case BoxGreenSky:	if (coldifdif(r,g,b,25,35,22,102,110,55)<22) return true; break;
		case BoxSkyGreen:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;

		case PolyDarkgrayLightgray:	if (coldifdif(r,g,b,54,50,38,97,93,80)<3) return true; break;
		case PolyDarkgrayLightgray2:	if (coldifdif(r,g,b,127,129,103,157,161,135)<4) return true; break;
		case PolyEarthLightGray:	if (coldifdif(r,g,b,101,79,30,172,149,88)<10) return true; break;
		case PolyEarthLightGray2:	if (coldifdif(r,g,b,118,121,98,154,159,133)<6) return true; break;
		case PolyEarthDarkgray:	if (coldifdif(r,g,b,105,82,32,184,163,103)<9) return true; break;
		case PolyEarthDarkgray2:	if (coldifdif(r,g,b,27,25,15,121,111,89)<13) return true; break;
		case PolyLightgrayRedbulding:	if (coldifdif(r,g,b,34,33,19,164,169,143)<10) return true; break;
		case PolyLightgrayRedbulding2:	if (coldifdif(r,g,b,64,33,19,89,56,37)<7) return true; break;
		case PolyAnySkybox:	if (coldifdif(r,g,b,27,21,12,145,141,61)<29) return true; break;
		case PolyAnySkybox2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		case PolyGraywallDarkroof:	if (coldifdif(r,g,b,103,111,96,116,127,111)<4) return true; break;
		case PolyGraywallDarkroof2:	if (coldifdif(r,g,b,15,16,14,31,33,26)<4) return true; break;
		
		case InselWinterWhiteSky:	if (coldifdif(r,g,b,94,92,87,181,186,194)<17) return true; break;
		case InselWinterWhiteSky2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		case InselWinterWhiteGray:	if (coldifdif(r,g,b,156,163,179,179,186,191)<5) return true; break;
		case InselWinterWhiteGray2:	if (coldifdif(r,g,b,36,37,37,178,179,183)<9) return true; break;
		case InselWinterBuildingRoof:	if (coldifdif(r,g,b,27,26,25,170,175,187)<13) return true; break;
		case InselWinterBuildingRoof2:	if (coldifdif(r,g,b,67,62,47,122,118,103)<19) return true; break;

		case InselSummerFloorSky:	if (coldifdif(r,g,b,42,37,17,167,143,85)<31) return true; break;
		case InselSummerFloorSky2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		case InselSummerTreeSky:	if (coldifdif(r,g,b,0,0,0,0,100,0)<50) return true; break;
		case InselSummerTreeSky2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		case InselSummerFloorDarkfloor:	if (coldifdif(r,g,b,94,73,27,192,170,93)<20) return true; break;
		case InselSummerFloorDarkfloor2:	if (coldifdif(r,g,b,33,30,26,99,96,76)<11) return true; break;
		case InselSummerRedBuilding:	if (coldifdif(r,g,b,57,55,38,76,76,57)<25) return true; break;
		case InselSummerRedBuilding2:	if (coldifdif(r,g,b,55,32,18,74,48,33)<15) return true; break;
		case InselSummerWhiteBuilding:	if (coldifdif(r,g,b,60,65,56,90,99,89)<5) return true;
										if (coldifdif(r,g,b,56,69,65,68,81,80)<5) return true; break;
		case InselSummerWhiteBuilding2:	if (coldifdif(r,g,b,11,11,10,19,19,17)<3) return true; 
										if (coldifdif(r,g,b,10,10,10,22,23,21)<3) return true; break;

//		case NewBox11:	if (coldifdif(r,g,b,38,36,28,38,36,28)<1) return true; break;
//		case NewBox12:	if (coldifdif(r,g,b,61,62,57,74,77,76)<25) return true; break;
//		case NewBox21:	if (coldifdif(r,g,b,38,36,28,38,36,28)<1) return true; break;
//		case NewBox22:	if (coldifdif(r,g,b,36,36,18,117,110,80)<18) return true; break;
		case NewBox11:	if (coldifdif(r,g,b,55,55,26,106,115,54)<14) return true; break;
		case NewBox12:	if (coldifdif(r,g,b,33,36,23,146,149,135)<8) return true; break;
		case NewBox21:	if (coldifdif(r,g,b,42,46,19,94,104,39)<20) return true; break;
		case NewBox22:	if (coldifdif(r,g,b,81,73,48,140,131,93)<5) return true; break;
		case NewBox31:	if (coldifdif(r,g,b,36,23,15,86,65,38)<15) return true; 
						if (coldifdif(r,g,b,56,36,18,137,117,68)<18) return true; break;
		case NewBox32:	if (coldifdif(r,g,b,11,10,8,51,54,50)<10) return true; 
						if (coldifdif(r,g,b,34,29,23,64,60,47)<7) return true; break;

		case NewIsland11:	if (coldifdif(r,g,b,47,31,20,168,148,83)<28) return true; break;
		case NewIsland12:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		case NewIsland21:	if (coldifdif(r,g,b,121,94,45,177,153,95)<11) return true; break;
		case NewIsland22:	if (coldifdif(r,g,b,50,46,34,113,110,93)<7) return true; break;

		case ErdhuegelWinterSky1:	if (coldifdif(r,g,b,94,92,87,181,186,194)<17) return true; break;
		case ErdhuegelWinterSky2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;

		case LaermSommerSky1:	if (coldifdif(r,g,b,94,92,87,181,186,194)<17) return true; break;
		case LaermSommerSky2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		case StilleSommerSky1:	if (coldifdif(r,g,b,94,92,87,181,186,194)<17) return true; break;
		case StilleSommerSky2:	if (coldifdif(r,g,b,38,36,28,38,36,28)<2) return true; break;
		
		}

		return false;
	}

	// EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE
	// Latest addition above & below this line and the same marker below
	// EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE

	public enum TerrainType { CP, 
		PolyLightgrayRedbulding,PolyLightgrayRedbulding2,PolyAnySkybox,PolyAnySkybox2,PolyGraywallDarkroof,PolyGraywallDarkroof2,
		PolyDarkgrayLightgray,PolyDarkgrayLightgray2,PolyEarthLightGray,PolyEarthLightGray2,PolyEarthDarkgray,PolyEarthDarkgray2,
		BoxGreenEarth,BoxEarthGreen,BoxEarthHouse,BoxHouseEarth,BoxGreenSky,BoxSkyGreen,
		BoxGreenStone,BoxStoneGreen,BoxStoneWall,BoxWallStone,BoxGreenOuter,BoxOuterGreen,BoxGreenDarkOuter,BoxDarkOuterGreen,
		InselWinterWhiteSky, InselWinterWhiteSky2, InselWinterWhiteGray, InselWinterWhiteGray2, InselWinterBuildingRoof, InselWinterBuildingRoof2,
		InselSummerFloorSky,InselSummerFloorSky2,InselSummerTreeSky,InselSummerTreeSky2,InselSummerFloorDarkfloor,InselSummerFloorDarkfloor2,
		InselSummerRedBuilding,InselSummerRedBuilding2,InselSummerWhiteBuilding,InselSummerWhiteBuilding2,
		NewBox11, NewBox12, NewBox21, NewBox22, NewBox31, NewBox32, NewBox41, NewBox42,
		NewIsland11,NewIsland12,NewIsland21,NewIsland22,
		ErdhuegelWinterSky1, ErdhuegelWinterSky2,
		LaermSommerSky1, LaermSommerSky2,
		StilleSommerSky1, StilleSommerSky2,
	}
/*
	private TerrainType SandkastenSommer[][] = new TerrainType[][] {
			{ TerrainType.BoxGreenStone, TerrainType.BoxStoneGreen },
			{ TerrainType.BoxStoneWall, TerrainType.BoxWallStone },
			{ TerrainType.BoxGreenOuter, TerrainType.BoxOuterGreen },
			{ TerrainType.BoxGreenDarkOuter, TerrainType.BoxDarkOuterGreen },
			{ TerrainType.BoxGreenEarth, TerrainType.BoxEarthGreen },
			{ TerrainType.BoxEarthHouse, TerrainType.BoxHouseEarth },
			{ TerrainType.BoxGreenSky, TerrainType.BoxSkyGreen },
	};
*/
	private TerrainType SandkastenSommer[][] = new TerrainType[][] {
			{ TerrainType.NewBox11, TerrainType.NewBox12 },
			{ TerrainType.NewBox21, TerrainType.NewBox22 },
			{ TerrainType.NewBox31, TerrainType.NewBox32 },
	};
	
	private TerrainType SchiessplatzSommer[][] = new TerrainType[][] {
			{ TerrainType.PolyDarkgrayLightgray, TerrainType.PolyDarkgrayLightgray2 },
			{ TerrainType.PolyEarthLightGray, TerrainType.PolyEarthLightGray2 },
			{ TerrainType.PolyEarthDarkgray, TerrainType.PolyEarthDarkgray2 },
			{ TerrainType.PolyLightgrayRedbulding, TerrainType.PolyLightgrayRedbulding2 },
			{ TerrainType.PolyAnySkybox, TerrainType.PolyAnySkybox2 },
			{ TerrainType.PolyGraywallDarkroof, TerrainType.PolyGraywallDarkroof2 },
	};

	private TerrainType InselWinter[][] = new TerrainType[][] {
			{ TerrainType.InselWinterBuildingRoof, TerrainType.InselWinterBuildingRoof2 },
			{ TerrainType.InselWinterWhiteGray, TerrainType.InselWinterWhiteGray2 },
			{ TerrainType.InselWinterWhiteSky, TerrainType.InselWinterWhiteSky2 },
	};

//	private TerrainType InselSommer[][] = new TerrainType[][] {
//			{ TerrainType.InselSummerFloorSky, TerrainType.InselSummerFloorSky2 },
//			{ TerrainType.InselSummerTreeSky, TerrainType.InselSummerTreeSky2 },
//			{ TerrainType.InselSummerFloorDarkfloor, TerrainType.InselSummerFloorDarkfloor2 },
//			{ TerrainType.InselSummerRedBuilding, TerrainType.InselSummerRedBuilding2 },
//			{ TerrainType.InselSummerWhiteBuilding, TerrainType.InselSummerWhiteBuilding2 },
//	};

	private TerrainType InselSommer[][] = new TerrainType[][] {
			{ TerrainType.NewIsland11, TerrainType.NewIsland12 },
			{ TerrainType.NewIsland21, TerrainType.NewIsland22 },
	};

	private TerrainType ErdhuegelWinter[][] = new TerrainType[][] {
			{ TerrainType.ErdhuegelWinterSky1, TerrainType.ErdhuegelWinterSky2 },
	};
	
	private TerrainType LaermSommer[][] = new TerrainType[][] {
			{ TerrainType.LaermSommerSky1, TerrainType.LaermSommerSky2 },
	};
	private TerrainType StilleSommer[][] = new TerrainType[][] {
			{ TerrainType.StilleSommerSky1, TerrainType.StilleSommerSky2 },
	};

	public int getTerrainRays()
	{
		switch(mapName)
		{
		case Sandkasten:
			return 128;
		case Beginner:
			return 32;
		case Insel: 
			return 128;
		default:
			return 64;
		}
	}

	public int getTerrainTypesOnMap()
	{

		switch(mapName)
		{
		case Sandkasten:
		case Beginner:
			switch(mapTime)
			{
			case Nacht:
				break;
			case Sommer:
				return SandkastenSommer.length;
			case Winter:
				break;
			default:
				break;
			}
		case Schiessplatz:
			switch(mapTime)
			{
			case Nacht:
				break;
			case Sommer:
				return SchiessplatzSommer.length;
			case Winter:
				break;
			default:
				break;
			}
		case Insel:
			switch(mapTime)
			{
			case Nacht:
				break;
			case Sommer:
				return InselSommer.length;
			case Winter:
				return InselWinter.length;
			default:
				break;
			}
		case Erdhuegel:
			switch(mapTime)
			{
			case Nacht:
				break;
			case Sommer:
				break;
			case Winter:
				return ErdhuegelWinter.length;
			default:
				break;
			}
		case Laerm:
			switch(mapTime)
			{
			case Nacht:
				break;
			case Sommer:
				return LaermSommer.length;
			case Winter:
				break;
			default:
				break;
			}
		case Stille:
			switch(mapTime)
			{
			case Nacht:
				break;
			case Sommer:
				return StilleSommer.length;
			case Winter:
				break;
			default:
				break;
			}
		default:
			return 6;		
		}
	}

	// EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE
	// Latest addition above & below this line and the same marker above
	// EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE EDIT HERE

	public int isTerrain(int c, int type) // type = terrain class 0..5 (Supported are up to 6 terrain types per map)
	{
		boolean a,b;
		switch (mapName) {

		case Schiessplatz:
			switch(mapTime) {
			case Winter:
				System.err.println("Not implemented");
				break;
			case Sommer:
				a=getDistance(SchiessplatzSommer[type][0], c);
				b=getDistance(SchiessplatzSommer[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Nacht:
				System.err.println("Not implemented");
				break;
			}
			break;

		case Insel:
			switch(mapTime) {
			case Winter:
				 a=getDistance(InselWinter[type][0], c);
				 b=getDistance(InselWinter[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Sommer:
				 a=getDistance(InselSommer[type][0], c);
				 b=getDistance(InselSommer[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Nacht:
				System.err.println("Not implemented");
				break;
			}
			break;
		case Erdhuegel:
			switch(mapTime) {
			case Winter:
				 a=getDistance(ErdhuegelWinter[type][0], c);
				 b=getDistance(ErdhuegelWinter[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Sommer:
				System.err.println("Not implemented");
				break;
			case Nacht:
				System.err.println("Not implemented");
				break;
			}
			break;
		case Laerm:
			switch(mapTime) {
			case Winter:
				System.err.println("Not implemented");
				break;
			case Sommer:
				 a=getDistance(LaermSommer[type][0], c);
				 b=getDistance(LaermSommer[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Nacht:
				System.err.println("Not implemented");
				break;
			}
			break;
		case Stille:
			switch(mapTime) {
			case Winter:
				System.err.println("Not implemented");
				break;
			case Sommer:
				 a=getDistance(StilleSommer[type][0], c);
				 b=getDistance(StilleSommer[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Nacht:
				System.err.println("Not implemented");
				break;
			}
			break;
		case Sandkasten:
		case Beginner:
			switch(mapTime) {
			case Winter:
				System.err.println("Not implemented");
				break;
			case Sommer:
				a=getDistance(SandkastenSommer[type][0], c);
				b=getDistance(SandkastenSommer[type][1], c);
				if (a&&b) return 2; if (a) return 0; if (b) return 1; return -1;
			case Nacht:
				System.err.println("Not implemented");
				break;
			}
			break;

		default:
			System.err.println("Not implemented");
			break;
		}
		return -1;
	}


	public boolean isEnemyColor(Status status, int c) {
		switch(status.tankColor) {
		case red : return isBlueColor(c);
		case blue : return isRedColor(c);
		case green : return isGreenColor(c);
		}
		return isGreenColor(c); // can't happen
	}

	public boolean isYellow(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;

		//        if ((r>150)&&(g>150)&&(b<100)) {
		if (coldif(r,g,b,207,192,16,3)<3) {
			//        	printColorToConsole(r,g,b);;
			return true;
		}
		return false;
	}

	public boolean isRankYellow(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;

		if ((r>150)&&(g>100)&&(b<11)) {
			return true;
		}
		return false;
	}
	
	public boolean isDarkYellow(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;

		if (coldif(r,g,b,98,80,7,3)<3) {
			//        	printColorToConsole(r,g,b);;
			return true;
		}
		return false;
	}


	public boolean isRedBarColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if ( (r>210) && (g<70) && (b<70) ) { // Red BAR
			return true;
		}
		return false;
	}

	public boolean isWhiteColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if ( (r>215) && (g>150) && (b>150) ) { // White writing e.g. on CP "A"/"B"/...
			return true;
		}
		if ( (r>150) && (g>150) && (b>215) ) { // White writing e.g. on CP "A"/"B"/...
			return true;
		}
		return false;
	}

	public boolean isRedColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		//		if ( (r>210) && (g<60) && (b<50) ) { // RED
		if (coldif(r, g, b, 201, 17, 17, 1)<2) return false; // this is the red flag
		if (coldif(r, g, b, 202, 55, 46, 1)<2) return false; // this is the red flag
		if (coldif(r, g, b, 180, 51, 45, 1)<2) return false; // this is the red flag
		if (coldif(r, g, b, 155, 50, 21, 5)<5) return false; // this is the red building
		if (
				(coldif(r, g, b, 162, 49, 34, 5)<5) ||
				(coldif(r, g, b, 138, 28, 19, 5)<5) ||
				(coldif(r, g, b, 187, 27, 19, 5)<5) ||
				(coldif(r, g, b, 142, 30, 20, 5)<5) ||
				(coldif(r, g, b, 168, 38, 26, 5)<5) ||
				(coldif(r, g, b, 183, 28, 19, 5)<5) ||
				(coldif(r, g, b, 150, 39, 27, 5)<5)
				) {
			//        	printColorToConsole(r,g,b);
			return true; // this is the red flag
		}
		return false;		
	}	

	// Check if we have a powerup box // TODO::: FOR NOW JUST VERIFY RED BOXES
	public boolean isBoxColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if ((r>2*g)&&(r>2*b)&&(r>50)) return true;// quite red
		return false;		
	}	
	
	public boolean isGreenBarColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if (coldif(r,g,b,64,182,23,3)<3) {
			//        	if ( (r<70) && (g>180) && (b<30) ) { // Green BAR
			return true;
			//			if ( (r<80) && (g>170) && (b<50) ) { // Green BAR
			//				return true;
		}
		return false;
	}

	public boolean isGreenColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if (
				(coldifdif(r, g, b, 42,98,20,44,109,20)<5) ||
				(coldifdif(r, g, b, 39,100,17,47,119,22)<5) ||
				(coldifdif(r, g, b, 50,133,19,58,161,21)<5) ||
				(coldifdif(r, g, b, 41,93,16,46,114,18)<5) ||
				(coldifdif(r, g, b, 46,103,19,58,118,23)<5) ||
				(coldifdif(r, g, b, 43,91,18,47,106,20)<5) ||
				(coldifdif(r, g, b, 41,100,17,48,127,19)<5) 
				) {
			//            	printColorToConsole(r,g,b);
			return true; 
		}
		return false;		
	}

	public boolean isBlueColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if (coldif(r, g, b, 18, 71, 203, 1)<2) return false; // blue flag
		if (coldif(r, g, b, 39, 92, 209, 1)<2) return false; // blue flag
		if (coldif(r, g, b, 50, 97, 194, 1)<2) return false; // blue flag
		if (coldif(r, g, b, 15, 55, 151, 5)<5) return false; // blue flag
		if (
				(coldif(r, g, b,  45, 92, 173, 10)<5) ||
				(coldif(r, g, b,  43, 86, 157, 10)<5) ||
				(coldif(r, g, b,  41, 77, 137, 10)<5) ||
				(coldif(r, g, b,  38, 69, 119, 5)<5) ||
				(coldif(r, g, b,  38, 67, 111, 5)<5) ||
				(coldif(r, g, b,  39, 71, 125, 10)<5) ||
				(coldif(r, g, b,  40, 68, 110, 5)<5)
				) {
			//        	printColorToConsole(r,g,b);;
			return true;
		}
		return false;		
	}		

	public boolean isBlueBarColor(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		if ( (r<140) && (g<140) && (b>200) ) { // Blue BAR
			//        	printColorToConsole(r,g,b);;
			return true;		
		}
		return false;
	}

	// Helper functions

	@SuppressWarnings("unused")
	private void printColorToConsole(int c)
	{
		int r = (c >> 16) & 0xFF;
		int g = (c >> 8) & 0xFF;
		int b = c & 0xFF;
		System.err.println("<r"+r+"g"+g+"b"+b+">");
	}	

	public int coldifdif(int r, int g, int b, int _rmin, int _gmin, int _bmin, int _rmax, int _gmax, int _bmax)
	{
		return coldif(r, g, b, (_rmin+_rmax)/2, (_gmin+_gmax)/2, (_bmin+_bmax)/2, (_rmax-_rmin+_gmax-_gmin+_bmax-_bmin)/3 );
	}

	public int coldif(int r, int g, int b, int _r, int _g, int _b, int max_mean_diff)
	{
		// distance of (r,g,b) to (_r,_g,_b) with a maximum brightness shift (up or down) of max_mean_diff
		// e.g. color (100,100,100) to (120,120,120) is rec'd by (r,g,b,110,110,110,10)
		int dr = r-_r;
		int dg = g-_g;
		int db = b-_b;
		int meand = (dr+dg+db)/3;
		if (Math.abs(meand) > max_mean_diff) meand = meand>0?max_mean_diff:-max_mean_diff;
		return Math.abs(dr-meand)+Math.abs(dg-meand)+Math.abs(db-meand);
	}

}
