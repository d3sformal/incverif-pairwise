/**
 * 
 */
package papabench.core.autopilot.tasks.pids;

import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.utils.MathUtils;

/**
 * @author Michal Malohlava
 *
 */
public class CoursePIDController extends AbstractPIDController {
	
	private float maxRoll 		= MAX_ROLL;
	private float coursePGain 	= COURSE_PGAIN; 
	
	public void control(AutopilotStatus status, Estimator estimator, Navigator navigator) {
		  float err = estimator.getHorizontalSpeed().direction - navigator.getDesiredCourse();
		  
		  err = MathUtils.normalizeRadAngle(err);
		  
		  float navDesiredRoll = coursePGain * err; //  * fspeed / AIR_SPEED;
		  
		  navDesiredRoll = MathUtils.symmetricalLimiter(navDesiredRoll, maxRoll);
		  
		  // propagate value
		  navigator.setDesiredRoll(navDesiredRoll);		  
	}
}
