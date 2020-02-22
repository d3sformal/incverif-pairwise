/**
 * 
 */
package papabench.core.autopilot.tasks.pids;

import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;


/**
 * PID Controller interface
 * 
 *  Input: environment state (plane + sensors + estimator)
 *  Output: desired state 
 * 
 * @author Michal Malohlava
 *
 */
public interface PIDController {
	
	void control(AutopilotStatus status, Estimator estimator, Navigator navigator);

}
