/**
 * 
 */
package papabench.core.commons.data;

import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;


/**
 * @author Michal Malohlava
 *
 */
public interface FlightPlan {
	
	/**
	 * Initialize the flight plan.
	 * 
	 * Allocate desired memory (preferably in mission memory).
	 */
	void init();

	String getName();	
	
	void execute();
	
	void setEstimator(Estimator estimator);
	void setAutopilotStatus(AutopilotStatus status);
	void setNavigator(Navigator navigator);
	
	float getGroundAltitude();
	
	float getSecureAltitude();
	
}
