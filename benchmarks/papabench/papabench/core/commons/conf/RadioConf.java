/**
 * 
 */
package papabench.core.commons.conf;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.RadioCommandsImpl;

/**
 * @author Michal Malohlava
 *
 */
public interface RadioConf {
	
	public static final RadioCommands safestateRadioCommands = new RadioCommandsImpl(); 
	
	/* depends on selected airframe - in Paparazzi implementation it is generated */
	public static final byte RADIO_CTL_NB = 9;
	
	// FIXME - see link_autopilot.h
	public static final int MAX_PPRZ = 600*16; // depends on CPU frequency
	public static final int MIN_PPRZ = - MAX_PPRZ;
	
	public static final int GAZ_THRESHOLD_TAKEOFF = (int)(MAX_PPRZ * 0.9f);
	
	public static final int STALLED_TIME = 30;  // ~500ms with a 60Hz timer
	public static final int REALLY_STALLED_TIME = 300; // 5s with a 60Hz timer
	
	public static final float MAX_THRUST = MAX_PPRZ;
	public static final float MIN_THRUST = 0;
	
}
