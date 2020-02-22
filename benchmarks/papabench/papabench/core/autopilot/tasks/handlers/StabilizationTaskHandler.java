/**
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.RollPitchPIDController;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.commons.data.RadioCommands;
import papabench.core.utils.LogUtils;
import papabench.core.utils.PPRZUtils;

/**
 * f = TODO
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class StabilizationTaskHandler implements Runnable {	

	private AutopilotModule autopilotModule;
	
	private RollPitchPIDController pidController;

	public StabilizationTaskHandler(AutopilotModule autopilotModule) {
		this.autopilotModule = autopilotModule;
		this.pidController = new RollPitchPIDController();
	}

	public void run() {

		autopilotModule.getIRDevice().update();
		autopilotModule.getEstimator().updateIRState();
		
		pidController.control(autopilotModule, autopilotModule.getEstimator(), autopilotModule.getNavigator());
		
		InterMCUMsg msg = new InterMCUMsg(true);
		RadioCommands radioCommands = msg.radioCommands;
		
		radioCommands.setPitch(autopilotModule.getElevator());
		radioCommands.setRoll(autopilotModule.getAileron());
		radioCommands.setThrottle(autopilotModule.getGaz());
		radioCommands.setGain1((int) PPRZUtils.trimPPRZ(RadioConf.MAX_PPRZ/0.75f*(autopilotModule.getEstimator().getAttitude().phi)));		
		msg.setValid(true);
		
		//LogUtils.log(this, "Stabilization task");
		
		//LogUtils.log(this, "Sending msg: " + msg);
		
		//autopilotModule.getLinkToFBW().sendMessageToFBW(msg);
	}
}
