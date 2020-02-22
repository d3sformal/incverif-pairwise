/**
 * 
 */
package papabench.core.fbw.tasks.handlers;

import papabench.core.commons.data.InterMCUMsg;
import papabench.core.fbw.modules.FBWModule;

/**
 * T = 25ms
 *  
 * @author Michal Malohlava
 * 
 */
//@SCJAllowed
public class SendDataToAutopilotTaskHandler implements Runnable {
	
	private FBWModule fbwModule;
	
	public SendDataToAutopilotTaskHandler(FBWModule fbwModule) {		
		this.fbwModule = fbwModule;
	}
		
	public void run() {	
		
		// send only valid 
		if (fbwModule.getPPMDevice().isValid()) {
			InterMCUMsg msg = new InterMCUMsg(); 
			msg.radioCommands = fbwModule.getPPMDevice().getLastRadioCommands().clone();
			
			msg.setRadioOK(fbwModule.isRadioOK());
			msg.setRadioReallyLost(fbwModule.isRadioReallyLost());
			msg.setAveragedChannelsSent(msg.radioCommands.containsAveragedChannels());
			
			msg.voltSupply = 0;
			msg.ppmCpt = 0; // FIXME <---this value should be computed
			
			fbwModule.getLinkToAutopilot().sendMessageToAutopilot(msg);			
		}
	}

}
