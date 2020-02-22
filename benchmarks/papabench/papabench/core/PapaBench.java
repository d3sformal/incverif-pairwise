/**
 * 
 */
package papabench.core;

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.commons.data.FlightPlan;
import papabench.core.commons.modules.Module;
import papabench.core.fbw.modules.FBWModule;

/**
 * Interface of PapaBench benchmark. 
 * 
 * It represents top-level module providing access to autopilot and fly-by-wire subsystems.
 * 
 * @author Michal Malohlava
 *
 */
public interface PapaBench extends Module {
	
	/**
	 * Returns autopilot module.
	 * 
	 * @return
	 */
	AutopilotModule getAutopilotModule();
	
	/**
	 * Returns fly-by-wire module.
	 * 
	 * @return
	 */
	FBWModule getFBWModule();
	
	/**
	 * Setup the mission flight-plan.
	 * 
	 * @param flightPlan
	 */
	void setFlightPlan(FlightPlan flightPlan);
}
