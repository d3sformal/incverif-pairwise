package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.LinkToFBW;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.utils.PPRZUtils;

/**
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class RadioControlTaskHandler implements Runnable {
	
	private AutopilotModule autopilotModule;
	
	public RadioControlTaskHandler(AutopilotModule autopilotModule) {		
		this.autopilotModule = autopilotModule;
	}
	
	public void run() {
		LinkToFBW linkToFBW = autopilotModule.getLinkToFBW();
		
		InterMCUMsg msg = linkToFBW.getMessageFromFBW();
		AutopilotMode autopilotMode = autopilotModule.getAutopilotMode();
		
		// message is totally received
		if (msg.isValid()) {
			boolean modeChanged = false;
			
			if (msg.isRadioReallyLost() 
					&& 
				(autopilotMode == AutopilotMode.AUTO1 || autopilotMode == AutopilotMode.MANUAL)) {
				autopilotModule.setAutopilotMode(AutopilotMode.HOME);
				autopilotMode = AutopilotMode.HOME;
				modeChanged = true;				
			}
			
			//if (msg.isAveragedChannelsSent()) {
				// TODO	setup modes, currently we are interested in in automatic mode AUTO2			
			//}
			
			//if (modeChanged) {
				// TODO sent telemetry data to the ground
			//}
					
			if (autopilotMode == AutopilotMode.AUTO1) {
				autopilotModule.setRoll(PPRZUtils.floatOfPPRZ(msg.radioCommands.getRoll(), 0.0f, -0.6f));
				autopilotModule.setPitch(PPRZUtils.floatOfPPRZ(msg.radioCommands.getPitch(), 0.0f, 0.5f));
			} 
			
			if (autopilotMode == AutopilotMode.AUTO1 || autopilotMode == AutopilotMode.MANUAL) {
				autopilotModule.setGaz(msg.radioCommands.getThrottle());
			}
			
			autopilotModule.setMC1PpmCpt(msg.ppmCpt);
			autopilotModule.setVoltSupply(msg.voltSupply);
			
			if (autopilotModule.getEstimator().getFlightTime() == 0) {
				// TODO FIXME here we should put ground_calibration !
				if (autopilotMode == AutopilotMode.AUTO2
						&& msg.radioCommands.getThrottle() > RadioConf.GAZ_THRESHOLD_TAKEOFF) {
					autopilotModule.setLaunched(true);					
				}
			}
		}
	}
}
