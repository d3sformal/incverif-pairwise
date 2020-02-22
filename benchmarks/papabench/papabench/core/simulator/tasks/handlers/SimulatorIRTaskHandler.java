/**
 * 
 */
package papabench.core.simulator.tasks.handlers;

import papabench.core.autopilot.data.Attitude;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.LogUtils;

/**
 * @author Michal Malohlava
 *
 */
public class SimulatorIRTaskHandler implements Runnable {
	
	private FlightModel flightModel;
	private SimulatedDevice irDevice;
	
	public SimulatorIRTaskHandler(FlightModel flightModel, AutopilotModule autopilotModule) {
		this.flightModel = flightModel;
		this.irDevice = (SimulatedDevice) autopilotModule.getIRDevice();
	}
	
	public void run() {
		Attitude attitude = flightModel.getState().getAttitude();
	
		//LogUtils.log(this, "Simulator - IRT");
	
		//LogUtils.log(this, "Att: " +attitude.toString());
	
		this.irDevice.update(this.flightModel);		
	}
}
