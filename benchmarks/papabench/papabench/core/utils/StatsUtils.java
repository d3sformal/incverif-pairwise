/**
 * 
 */
package papabench.core.utils;

import java.util.Random;

/**
 * @author Michal Malohlava
 *
 */
final public class StatsUtils {
	
	public static float randNormalDistribution(float mu, float sigma) {
		
		float u1 = 0.2f;
		float u2 = 0.65f;
		
		return (float) (mu + sigma * Math.sqrt(-2 * Math.log(u1)) * Math.cos(2*Math.PI * u2));
	}
}
