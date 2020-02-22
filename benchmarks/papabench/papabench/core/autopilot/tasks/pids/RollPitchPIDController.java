/**
 * 
 */
package papabench.core.autopilot.tasks.pids;

import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.utils.PPRZUtils;

/**
 * Computes desired_aileron and desired_elevator from attitude estimation and expected attitude.
 *  
 * @author Michal Malohlava
 *
 */
public class RollPitchPIDController extends AbstractPIDController {

	public void control(AutopilotStatus status, Estimator estimator, Navigator navigator) {
		float err = estimator.getAttitude().phi - status.getRoll();
		
		int desiredAileron = (int) PPRZUtils.trimPPRZ(status.getRollPGain() * err);
		
		if (status.getPitchOfRoll() < 0f) {
			status.setPitchOfRoll(0f);
		}
		
		err = -(estimator.getAttitude().theta - status.getPitch() - status.getPitchOfRoll() * Math.abs(estimator.getAttitude().phi));
		
		int desiredElevator = (int) PPRZUtils.trimPPRZ(status.getPitchPGain() * err);
		
		status.setAileron(desiredAileron);
		status.setElevator(desiredElevator);
	}

}
