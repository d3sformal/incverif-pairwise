/**
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.AltitudePIDController;
import papabench.core.autopilot.tasks.pids.PIDController;


/**
 * Altitude control task handler.
 * 
 * @author Michal Malohlava
 *
 * @do not edit !
 */
public class AltitudeControlTaskHandler implements Runnable {
	
	// instantiated in mission memory to preserve PID specific attributes
	private AutopilotModule autopilotModule;
	
	// PID controller for altitude - it cannot be only a static method because it can 
	// have inner state (e.g., last error value)
	private PIDController pidController; 

	public AltitudeControlTaskHandler(AutopilotModule autopilotModule) {		
		this.autopilotModule = autopilotModule;
		this.pidController = new AltitudePIDController();
	}
	
	public void run() {
		if (autopilotModule.getAutopilotMode() == AutopilotMode.AUTO2
				|| autopilotModule.getAutopilotMode() == AutopilotMode.HOME) {
				
				if (autopilotModule.getVerticalFlightMode() == VerticalFlightMode.AUTO_ALTITUDE) {
					pidController.control(autopilotModule, autopilotModule.getEstimator(), autopilotModule.getNavigator());								
				}
		}		
	}
}
