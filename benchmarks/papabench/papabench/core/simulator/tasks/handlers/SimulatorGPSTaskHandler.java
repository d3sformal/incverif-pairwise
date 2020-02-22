/**
 * 
 */
package papabench.core.simulator.tasks.handlers;

import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.LogUtils;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
public class SimulatorGPSTaskHandler implements Runnable {
	private FlightModel flightModel;
	private SimulatedDevice gpsDevice;
	
	public SimulatorGPSTaskHandler(FlightModel flightModel, AutopilotModule autopilotModule) {		
		this.flightModel = flightModel;
		this.gpsDevice = (SimulatedDevice) autopilotModule.getGPSDevice();
	}
	
	public void run() {
		Position3D pos = flightModel.getState().getPosition();
		
		//LogUtils.log(this, "Simulator - GPS");
	
		//LogUtils.log(this, "Position: " + pos.x + "," + pos.y + "," + pos.z + "               " + flightModel.getState().getAirSpeed());		
		
		gpsDevice.update(flightModel);
	}
}
