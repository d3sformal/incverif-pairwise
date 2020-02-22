/**
 * 
 */
package papabench.core.autopilot.tasks.pids;

import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.utils.MathUtils;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 * 
 */
//@SCJAllowed
public class AltitudePIDController extends AbstractPIDController {

	private float altitudePGain = ALTITUDE_PGAIN;

	public void control(AutopilotStatus status, Estimator estimator, Navigator navigator) {		
		 float err = estimator.getPosition().z -  navigator.getDesiredAltitude();
		 
		 float desiredClimb = navigator.getPreClimb() + altitudePGain * err;
		 desiredClimb = MathUtils.symmetricalLimiter(desiredClimb, CLIMB_MAX);
		 
		 status.setClimb(desiredClimb);
	}
}
