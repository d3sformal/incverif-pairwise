/**
 * 
 */
package papabench.core.commons.status;

import papabench.core.autopilot.modules.AutopilotStatus;

/**
 * @author Michal Malohlava
 *
 */
public interface AirplaneStatus {
	
	/**
	 * Returns mode of autopilot module.
	 * 
	 * @return
	 */
	AutopilotStatus getAutopilotStatus(); 

}
