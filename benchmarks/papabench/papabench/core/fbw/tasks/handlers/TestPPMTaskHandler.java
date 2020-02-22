/**
 * 
 */
package papabench.core.fbw.tasks.handlers;

import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.RadioCommands;
import papabench.core.fbw.devices.PPMDevice;
import papabench.core.fbw.modules.FBWModule;

/**
 * Task receiving 
 * 
 * T = 25ms
 *  
 * @author Michal Malohlava
 * 
 */
//@SCJAllowed
public class TestPPMTaskHandler implements Runnable {
	private FBWModule fbwModule;
	
	private int counterFromLastPPM;
	
	public TestPPMTaskHandler(FBWModule fbwModule) {		
		this.fbwModule = fbwModule;
	}
	
	public void run() {
		PPMDevice ppmDevice = fbwModule.getPPMDevice();
		
		if (ppmDevice.isValid()) {
			fbwModule.setRadioOK(true);
			fbwModule.setRadioReallyLost(false);
			counterFromLastPPM++;
			
			ppmDevice.lastRadioFromPpm();
			
			RadioCommands radioCommands = ppmDevice.getLastRadioCommands();
			FBWMode mode = this.fbwModule.getFBWMode();
			if (radioCommands.containsAveragedChannels()) {
				mode = radioCommands.getMode();
			}
			
			if (mode == FBWMode.MANUAL) {
				this.fbwModule.getServosController().setServos(radioCommands);
			}
		} else if (fbwModule.getFBWMode() == FBWMode.MANUAL && fbwModule.isRadioReallyLost()) {
			fbwModule.setFBWMode(FBWMode.AUTO);
		}
		
		if (counterFromLastPPM > RadioConf.STALLED_TIME) {
				fbwModule.setRadioOK(false);
		}
		if (counterFromLastPPM > RadioConf.REALLY_STALLED_TIME) {
			fbwModule.setRadioReallyLost(true);
		}
	}
}
