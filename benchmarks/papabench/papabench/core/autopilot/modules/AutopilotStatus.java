/**
 * 
 */
package papabench.core.autopilot.modules;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.LateralFlightMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.commons.modules.Module;

/**
 * @author Michal Malohlava
 *
 */
public interface AutopilotStatus extends Module {
	
	/* Autopilot modes */
	AutopilotMode getAutopilotMode();
	void setAutopilotMode(AutopilotMode mode);
	
	LateralFlightMode getLateralFlightMode();
	void setLateralFlightMode(LateralFlightMode mode);
	
	VerticalFlightMode getVerticalFlightMode();
	void setVerticalFlightMode(VerticalFlightMode mode);
	
	/**
	 * Should be called if the mission is finished. 
	 * Typically, it is called if the flight plan has no more block to execute. 
	 */
	void missionFinished();
	
	/* 
	 * Autopilot navigation parameters which are used to manage FBW 
	 */
	float getRoll();
	void setRoll(float roll);
	
	float getPitch();
	void setPitch(float pitch);	
	
	float getClimb();
	void setClimb(float climb);
	
	float getRollPGain();
	void setRollPGain(float value);
	
	float getPitchPGain();
	void setPitchPGain(float value);
	
	float getPitchOfRoll();
	void setPitchOfRoll(float value);
	
	int getVoltSupply();
	void setVoltSupply(int vSupply);
	
	int getMC1PpmCpt();
	void setMC1PpmCpt(int value);
	
	boolean isLaunched();
	void setLaunched(boolean launch);
	
	/* Airframe parameters */
	void setAileron(int aileron);
	int getAileron();
	
	void setElevator(int elevator);
	int getElevator();
	
	int getGaz();
	void setGaz(int gaz);	
}
