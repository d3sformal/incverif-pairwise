/**
 * 
 */
package papabench.core.fbw.devices;

import papabench.core.commons.conf.AirframeParametersConf;
import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.devices.Device;

/**
 * Device for receiving commands from ground controller.
 * 
 * @author Michal Malohlava
 *
 */
public interface PPMDevice extends Device {
	
	/**
	 * Returns array of length equal to {@link AirframeParametersConf#RADIO_CTL_NB}.
	 * 
	 * @return
	 */
	RadioCommands getLastRadioCommands();

	/**
	 * 
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Copy data from ppm buffer to the buffer used by MC0
	 */
	void lastRadioFromPpm();
	
}
