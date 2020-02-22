package papabench.core.fbw.devices.impl;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.RadioCommandsImpl;
import papabench.core.fbw.devices.ServosController;
import papabench.core.utils.LogUtils;

/**
 * Simple servos controller.
 * 
 * @author Michal Malohlava
 *
 */
public class ServosControllerImpl implements ServosController {
	
	protected RadioCommands radioCommands;

	public void setServos(RadioCommands radioCommands) {
		// this should lead to memory access error - reference from tasks will disappear
		this.radioCommands.fillFrom(radioCommands);
		LogUtils.log(this, "Servos set to: " + radioCommands);		
	}

	public void init() {
		radioCommands = new RadioCommandsImpl();
	}

	public void reset() {
	}

}
