/**
 * 
 */
package papabench.core.commons.conf;

/**
 * Airframe parameters.
 * 
 * @author Michal Malohlava
 * 
 */
public interface AirframeParametersConf {
	
	public static final float ALTITUDE_PGAIN = -0.025f;

	public static final float MAX_PITCH = 0.35f;
	public static final float MIN_PITCH = -0.35f;

	public static final float ROLL_PGAIN = 10000.0f;
//	public static final float MAX_ROLL = 0.35f; // FIXME
	public static final float MAX_ROLL = 0.85f;
	
	public static final float CLIMB_PITCH_PGAIN = -0.1f;
	public static final float CLIMB_PITCH_IGAIN = 0.025f;

	public static final float CLIMB_MAX = 1.0f;
	
	public static final float CLIMB_PGAIN = -0.03f;
	public static final float CLIMB_IGAIN = 0.1f;

	public static final float CLIMB_LEVEL_GAZ = 0.31f;
	public static final float CLIMB_PITCH_OF_VZ_PGAIN = 0.05f;
	public static final float CLIMB_GAZ_OF_CLIMB = 0.2f;
	
	public static final float COURSE_PGAIN = -0.2f;

	public static final float PITCH_OF_ROLL = 0.0f;
	public static final float PITCH_PGAIN = 15000.0f;

	public static final float NAV_PITCH = 0.f;
	
	public static final float NOMINAL_AIRSPEED = 10.0f;
	public static final float MAXIMUM_AIRSPEED = NOMINAL_AIRSPEED * 1.5f;

	public static final float AUTO_THROTTLE_NOMINAL_CRUISE_THROTTLE = 0.35f;
	public static final float CRUISE_THROTTLE = AUTO_THROTTLE_NOMINAL_CRUISE_THROTTLE;
		
	public static final float WEIGHT = 1.3f;
	public static final float MAXIMUM_POWER = 5 * MAXIMUM_AIRSPEED * WEIGHT;
	
	public static final float G = 9.806f;
	
	public static final float CARROT = 5.f;
	
	public static final float ROLL_RESPONSE_FACTOR = 15f;
	public static final float YAW_RESPONSE_FACTOR = 1f;

}
