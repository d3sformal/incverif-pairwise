/**
 * 
 */
package papabench.core.autopilot.modules.impl;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.LateralFlightMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.commons.conf.AirframeParametersConf;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class AutopilotStatusImpl implements AutopilotStatus {
	
	/** autopilot mode */
	private AutopilotMode autopilotMode;
	/** */
	private LateralFlightMode lateralFlightMode;
	/** */
	private VerticalFlightMode verticalFlightMode;
	
	private float roll;
	private float pitch;
	private float climb;
	private float rollPGain;
	private float pitchPGain;
	private float pitchOfRoll;
	
	
	private int gaz;
	private int aileron;
	private int elevator;
	
	
	private int voltSupply;
	private int MC1PpmCpt;
	
	private boolean launched = false;
	
	public void init() {
		autopilotMode = AutopilotMode.AUTO2;
		lateralFlightMode = LateralFlightMode.MANUAL;
		verticalFlightMode = VerticalFlightMode.MANUAL;
		this.rollPGain = AirframeParametersConf.ROLL_PGAIN;
		this.pitchPGain = AirframeParametersConf.PITCH_PGAIN;
	}

	public AutopilotMode getAutopilotMode() {
		return autopilotMode;
	}
	public void setAutopilotMode(AutopilotMode mode) {
		this.autopilotMode = mode;		
	}

	public LateralFlightMode getLateralFlightMode() {
		return lateralFlightMode;		
	}
	public void setLateralFlightMode(LateralFlightMode mode) {
		this.lateralFlightMode = mode;		
	}

	public VerticalFlightMode getVerticalFlightMode() {
		return verticalFlightMode;
	}
	public void setVerticalFlightMode(VerticalFlightMode mode) {
		this.verticalFlightMode = mode;
	}
	public float getRoll() {
		return roll;
	}
	public void setRoll(float roll) {
		this.roll = roll;
	}
	public float getPitch() {
		return pitch;
	}
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	public float getClimb() {
		return climb;
	}
	public void setClimb(float climb) {
		this.climb = climb;
	}
	public float getRollPGain() {
		return rollPGain;
	}
	public void setRollPGain(float rollPGain) {
		this.rollPGain = rollPGain;
	}
	public float getPitchPGain() {
		return pitchPGain;
	}
	public void setPitchPGain(float pitchPGain) {
		this.pitchPGain = pitchPGain;
	}
	public float getPitchOfRoll() {
		return pitchOfRoll;
	}
	public void setPitchOfRoll(float pitchOfRoll) {
		this.pitchOfRoll = pitchOfRoll;
	}	
	public int getVoltSupply() {
		return voltSupply;
	}
	public void setVoltSupply(int voltSupply) {
		this.voltSupply = voltSupply;
	}
	public int getMC1PpmCpt() {
		return MC1PpmCpt;
	}
	public void setMC1PpmCpt(int mC1PpmCpt) {
		MC1PpmCpt = mC1PpmCpt;
	}
	public boolean isLaunched() {
		return launched;
	}
	
	public void setLaunched(boolean launch) {
		this.launched = launch;
	}
	/* Airframe parameters */
	public int getAileron() {
		return this.aileron;
	}
	public void setAileron(int aileron) {
		this.aileron = aileron;		
	}
	public void setElevator(int elevator) {
		this.elevator = elevator;		
	}
	public int getElevator() {
		return this.elevator;
	}
	public int getGaz() {
		return gaz;
	}
	public void setGaz(int gaz) {
		this.gaz = gaz;
	}

	@Override
	public String toString() {
		return "AutopilotStatusImpl [aileron=" + aileron + ", autopilotMode="
				+ autopilotMode + ", climb=" + climb + ", elevator=" + elevator
				+ ", gaz=" + gaz + ", lateralFlightMode=" + lateralFlightMode
				+ ", launched=" + launched + ", pitch=" + pitch + ", roll="
				+ roll + ", verticalFlightMode=" + verticalFlightMode + "]";
	}

	public void missionFinished() {
		// FIXME finish this mission end
		System.exit(1);
	}
}
