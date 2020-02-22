/**
 * 
 */
package papabench.core.utils;

import papabench.core.autopilot.data.Position2D;


/**
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
final public class MathUtils {
	
	public static final double TWO_PI = 2*Math.PI; 
	 
	public static float symmetricalLimiter(float value, float limit) {
		if (value > limit) return limit;
		if (value < -limit) return -limit;
		
		return value;
	}
	
	public static float asymmetricalLimiter(float value, float lowLimit, float highLimit) {
		if (value > highLimit) return highLimit;
		if (value < lowLimit) return lowLimit;
		
		return value;
	}
	
	public static float normalizeRadAngle(float angle) {
		while(angle > Math.PI) angle -= TWO_PI;
		while(angle < - Math.PI) angle += TWO_PI;
		
		return angle;
	}
	
	public static float scalarProduct(Position2D a, Position2D b, Position2D c) {
		return (a.x - b.x) * (a.x-c.x) + (a.y-b.y)*(a.y - c.y);
	}
	
}
