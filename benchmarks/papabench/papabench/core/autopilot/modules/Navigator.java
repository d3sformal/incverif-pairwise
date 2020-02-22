/**
 * 
 */
package papabench.core.autopilot.modules;

import papabench.core.autopilot.data.Position2D;
import papabench.core.commons.data.FlightPlan;
import papabench.core.commons.modules.Module;

/**
 * Note: this interface should be used by flight plan to setup desired properties for autopilot.
 * 
 * @author Michal Malohlava
 *
 */
public interface Navigator extends Module {
	/* predefined flight plan */
	FlightPlan getFlightPlan();
	void setFlightPlan(FlightPlan flightPlan);
	
	void setAutopilotModule(AutopilotModule autopilotModule);
	
	/**
	 * Setup navigation parameters according to given {@link FlightPlan}
	 */
	void autoNavigate();
	
	/* 
	 * Navigator desires. 
	 */	
	Position2D getDesiredPosition();
	void setDesiredPosition(float x,float y);
	
	float getDesiredCourse();
	void setDesiredCourse(float course);
	
	// USE BY FP
	float getDesiredAltitude();
	void setDesiredAltitude(float altitude);
	
	// USED by Course PID
	float getDesiredRoll();
	void setDesiredRoll(float roll);
	
	// USED BY FP
	float getDesiredPitch();
	void setDesiredPitch(float pitch);
	
	// USED BY FP
	float getPreClimb();
	void setPreClimb(float preClimb);
	
	// USED by FP
	boolean isAutoPitch();
	void setAutoPitch(boolean value);

	// USED by FP
	int getDesiredGaz();
	void setDesiredGaz(int gaz);
	
	/**
	 * Returns number of calls of method {@link #autoNavigate()}.
	 *  
	 * @return
	 */
	int getAutoNavigationCycleNumber() ;
	
}
