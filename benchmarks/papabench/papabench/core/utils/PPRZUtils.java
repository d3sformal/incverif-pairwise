/**
 * 
 */
package papabench.core.utils;

import papabench.core.commons.conf.RadioConf;

/**
 * PPRZ converter.
 * 
 * Should be generated according to a target PPRZ device.
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
final public class PPRZUtils {
	
//	@SCJAllowed
	public static final float floatOfPPRZ(int value, float center, float travel) {
		return center + (value / RadioConf.MAX_PPRZ * travel);		
	}
	
//	@SCJAllowed
	public static final float trimPPRZ(float value) {
		return MathUtils.asymmetricalLimiter(value, RadioConf.MIN_PPRZ, RadioConf.MAX_PPRZ);
	}
	
	public static final float trimuPPRZ(float value) {
		return MathUtils.asymmetricalLimiter(value, 0, RadioConf.MAX_PPRZ);
	}
	
}
