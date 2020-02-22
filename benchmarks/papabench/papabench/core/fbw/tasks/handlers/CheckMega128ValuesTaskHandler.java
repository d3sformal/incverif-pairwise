package papabench.core.fbw.tasks.handlers;

import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.fbw.modules.FBWModule;

/**
 * T = 50ms
 * @author Michal Malohlava
 *
 */
public class CheckMega128ValuesTaskHandler implements Runnable {
	
	private FBWModule fbwModule;
	
	private int counterSinceLastMega128 = 0;
	
	public CheckMega128ValuesTaskHandler(FBWModule fbwModule) {
		this.fbwModule = fbwModule;
	}
	
	public void run() {
		// there should be condition reflecting SPI state on real hardware
		InterMCUMsg msg = fbwModule.getLinkToAutopilot().getMessageFromAutopilot();
		// message if message is fully received (SPI reception takes a time, however we return message 
		// which is preallocated for the given SPI reception)
		if (msg.isValid()) {
			counterSinceLastMega128 = 0;
			fbwModule.setMega128OK(true);
			if (fbwModule.getFBWMode() == FBWMode.AUTO) {
				this.fbwModule.getServosController().setServos(msg.radioCommands);
			}
		}
		
		if (counterSinceLastMega128 > RadioConf.STALLED_TIME) {
			fbwModule.setMega128OK(false);
		}
	}

}
