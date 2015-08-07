package sirTankiBot;

import java.util.LinkedList;

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

public class UndetectedTerrainType {
	
	public LinkedList<Integer> terrainHits = new LinkedList<Integer>(); 
	public Integer notFoundTerrainType;
	
	public UndetectedTerrainType(Integer _notFoundTerrainType)
	{
		notFoundTerrainType = _notFoundTerrainType;
	}
	
}
