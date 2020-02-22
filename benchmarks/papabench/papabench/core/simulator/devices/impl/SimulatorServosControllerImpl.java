/**
 * 
 */
package papabench.core.simulator.devices.impl;

import papabench.core.commons.data.RadioCommands;
import papabench.core.fbw.devices.impl.ServosControllerImpl;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.LogUtils;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
public class SimulatorServosControllerImpl extends ServosControllerImpl implements SimulatedDevice {
	
	private boolean processed = true;

	public void update(FlightModel flightModel) {
		if (!processed) {
			flightModel.processCommands(this.radioCommands);			
			processed = true;
		}
	}
	
	@Override
	public void setServos(RadioCommands radioCommands) {		
		super.setServos(radioCommands);
		
		LogUtils.log(this, "Radio commands for servos: " + radioCommands.toString());
		
		processed = false;
	}
}
