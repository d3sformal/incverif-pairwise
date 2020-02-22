/**
 * 
 */
package papabench.core.simulator.tasks.handlers;

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.LogUtils;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
public class SimulatorFlightModelTaskHandler implements Runnable {
	
	private FlightModel flightModel;
	private AutopilotModule autopilotModule;
	//private FBWModule fbwModule;
	
	public SimulatorFlightModelTaskHandler(FlightModel flightModel, AutopilotModule autopilotModule) { //, FBWModule fbwModule) {		
		this.flightModel = flightModel;
		this.autopilotModule = autopilotModule;
		//this.fbwModule = fbwModule;
	}
	
	public void run() {
		/*
		SimulatedDevice sensors = (SimulatedDevice) fbwModule.getServosController();
		sensors.update(flightModel);
		*/

		flightModel.updateState();
		
		//LogUtils.log(this, "SIMULATOR - Flight model"); // state:");
		//LogUtils.log(this, flightModel.getState().toString());
	}
}
