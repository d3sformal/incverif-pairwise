/**
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.ClimbPIDController;

/**
 * Climb control task handler.
 * 
 * f = 40Hz
 * 
 * @author Michal Malohlava
 * 
 * @do not edit !
 *
 */
public class ClimbControlTaskHandler implements Runnable {
	
	private AutopilotModule autopilotModule;
	
	private ClimbPIDController pidController;

	public ClimbControlTaskHandler(AutopilotModule autopilotModule) {
		this.autopilotModule = autopilotModule;
		this.pidController = new ClimbPIDController();
	}

	public void run() {
		AutopilotMode autopilotMode = autopilotModule.getAutopilotMode();
		VerticalFlightMode vfMode = autopilotModule.getVerticalFlightMode();
		
		if (autopilotMode == AutopilotMode.AUTO2
			|| autopilotMode == AutopilotMode.HOME) {
			
			if (vfMode == VerticalFlightMode.AUTO_CLIMB
				|| vfMode == VerticalFlightMode.AUTO_ALTITUDE
				|| vfMode == VerticalFlightMode.MODE_NB) {
				
				pidController.control(autopilotModule, autopilotModule.getEstimator(), autopilotModule.getNavigator());
			}
			
			if (vfMode == VerticalFlightMode.AUTO_GAZ) {
				autopilotModule.setGaz(autopilotModule.getNavigator().getDesiredGaz());
			}

			// switch off motor if the battery is to low
//			if (low_battery || (!estimator_flight_time && !launch)) {
//		   		 desired_gaz = 0.;
//		   	}  			
		}
	}	
}
