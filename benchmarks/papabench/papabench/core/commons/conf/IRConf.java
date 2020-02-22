/**
 * 
 */
package papabench.core.commons.conf;

/**
 * @author Michal Malohlava
 *
 */
public interface IRConf {

	public static final int DEFAULT_IR_CONTRAST = 200; 
	
//	public static final int DEFAULT_IR_ROLL_NEUTRAL = -915; // FIXME this value is not handled correctly by simulator
	public static final int DEFAULT_IR_ROLL_NEUTRAL = 0;
	
//	public static final int DEFAULT_IR_PITCH_NEUTRAL = 110; // FIXME this value is not handled correctly by simulator
	public static final int DEFAULT_IR_PITCH_NEUTRAL = 0;

	
	public static final float IR_RAD_OF_IR_CONTRAST = 0.75f;
	
	public static final float IR_RAD_OF_IR_MAX_VAL = 0.0045f;
	
	public static final float IR_RAD_OF_IR_MIN_VAL = 0.00075f;

}
