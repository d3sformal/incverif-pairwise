/**
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.LateralFlightMode;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.CoursePIDController;
import papabench.core.utils.LogUtils;

/**
 * Navigation task handler.
 * 
 * @author Michal Malohlava
 *
 * @do not edit!
 */
//@SCJAllowed
public class NavigationTaskHandler implements Runnable {
	
	private AutopilotModule autopilotModule;
	
	private CoursePIDController coursePIDController;
	
	public NavigationTaskHandler(AutopilotModule autopilotModule) {		
		this.autopilotModule = autopilotModule;
		
		this.coursePIDController = new CoursePIDController();
	}

	public void run() {
		// FIXME 4Hz is a frequency of this task -> update time every 4th call		
		
		autopilotModule.getEstimator().updateFlightTime();
		
		//LogUtils.log(this, "\n\n\n============================ Navigation task");
		
		//LogUtils.log(this, "\n\n\n============================ Navigation cycle: " + autopilotModule.getEstimator().getFlightTime());
		
		// FIXME following line should be in dedicated task: if (gps_msg_received) => update state
		autopilotModule.getEstimator().updatePosition();
		
		autopilotModule.setLateralFlightMode(LateralFlightMode.COURSE);
		
		if (autopilotModule.getAutopilotMode() == AutopilotMode.HOME) {
			//nav_home()
		} else {
			//LogUtils.log(this, "calling navigator to compute the navigation parameters, autopilot status:");
			//LogUtils.log(this, this.autopilotModule.toString());
			
			autopilotModule.getNavigator().autoNavigate();
		}
		
		//LogUtils.log(this, "course recomputation");
		courseComputation();
		
		//LogUtils.log(this, "Final autopilot status:");
		//LogUtils.log(this, this.autopilotModule.toString());
	}
	
	protected void courseComputation() {
		AutopilotMode autopilotMode = autopilotModule.getAutopilotMode();
		if (autopilotMode == AutopilotMode.AUTO2 || autopilotMode ==AutopilotMode.HOME) {
			
			LateralFlightMode lateralFlightMode = autopilotModule.getLateralFlightMode();
			if (lateralFlightMode == LateralFlightMode.COURSE || lateralFlightMode == LateralFlightMode.NB) {
				coursePIDController.control(autopilotModule, autopilotModule.getEstimator(), autopilotModule.getNavigator());								
			}
			
			autopilotModule.setRoll(autopilotModule.getNavigator().getDesiredRoll());
		}
	}	
}
