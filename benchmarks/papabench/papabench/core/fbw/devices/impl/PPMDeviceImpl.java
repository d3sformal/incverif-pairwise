/**
 * 
 */
package papabench.core.fbw.devices.impl;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.RadioCommandsImpl;
import papabench.core.fbw.devices.PPMDevice;

/**
 * Device for receiving commands from the ground station.
 * 
 * @author Michal Malohlava
 *
 */
public class PPMDeviceImpl implements PPMDevice {
	
	private RadioCommands lastRadioCommands;

	public RadioCommands getLastRadioCommands() {
		return this.lastRadioCommands;		
	}

	public boolean isLastRadioContainsAvgChannels() {
		return false;
	}

	public void lastRadioFromPpm() {
	}

	public void init() {
		this.lastRadioCommands = new RadioCommandsImpl();
	}

	public void reset() {
	}

	public boolean isValid() {		
		return false;
	}

	public void setValid(boolean value) {				
	}
}
