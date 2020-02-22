package papabench.core.fbw.tasks.handlers;

import static papabench.core.utils.LogUtils.log;
import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;
import papabench.core.fbw.modules.FBWModule;
/**
 * Switch to fail save state of airplane control.
 * 
 * The switching depends on message lost.
 * 
 * T = 50ms
 * 
 * @author Michal Malohlava
 *
 */
public class CheckFailsafeTaskHandler implements Runnable {
	
	private FBWModule fbwModule;
	
	public CheckFailsafeTaskHandler(FBWModule fbwModule) {		
		this.fbwModule = fbwModule;		
	}
	
	public void run() {
		if (fbwModule.getFBWMode() == FBWMode.MANUAL && !fbwModule.isRadioOK()
			|| fbwModule.getFBWMode() == FBWMode.AUTO && !fbwModule.isMega128OK()) {

			this.fbwModule.getServosController().setServos(RadioConf.safestateRadioCommands);
			
			//log(this, "Airplane was switched into failsafe mode");
		}
	}
}
