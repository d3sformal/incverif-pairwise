/**
 * 
 */
package papabench.core.fbw.devices;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.devices.Device;

/**
 * @author Michal Malohlava
 *
 */
public interface ServosController extends Device {
	
	void setServos(RadioCommands radioCommands);

}
