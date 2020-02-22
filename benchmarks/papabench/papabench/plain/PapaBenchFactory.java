/**
 * 
 */
package papabench.plain;

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.impl.AutopilotModuleImpl;
import papabench.core.autopilot.modules.impl.EstimatorModuleImpl;
import papabench.core.autopilot.modules.impl.LinkToFBWImpl;
import papabench.core.autopilot.modules.impl.NavigatorImpl;
import papabench.core.bus.SPIBusChannel;
import papabench.core.bus.impl.SPIBusChannelImpl;
import papabench.core.fbw.devices.impl.PPMDeviceImpl;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.modules.impl.FBWModuleImpl;
import papabench.core.fbw.modules.impl.LinkToAutopilotImpl;
import papabench.core.simulator.devices.impl.SimulatorGPSDeviceImpl;
import papabench.core.simulator.devices.impl.SimulatorIRDeviceImpl;
import papabench.core.simulator.devices.impl.SimulatorServosControllerImpl;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.model.impl.FlightModelImpl;

/**
 * Factory creating an instance of PapaBench.
 * 
 * The factory does not initialize created modules !
 * 
 * @author Michal Malohlava
 *
 */
public class PapaBenchFactory {
	
	public static AutopilotModule createAutopilotModule() {
		
		AutopilotModule autopilotModule = new AutopilotModuleImpl();
		
		autopilotModule.setLinkToFBW(new LinkToFBWImpl());
		autopilotModule.setGPSDevice(new SimulatorGPSDeviceImpl());
		autopilotModule.setIRDevice(new SimulatorIRDeviceImpl());
		autopilotModule.setNavigator(new NavigatorImpl());
		autopilotModule.setEstimator(new EstimatorModuleImpl());
		
		return autopilotModule;
	}
	
	public static FBWModule createFBWModule() {
		FBWModule fbwModule = new FBWModuleImpl();
		
		fbwModule.setLinkToAutopilot(new LinkToAutopilotImpl());
		fbwModule.setPPMDevice(new PPMDeviceImpl());
		fbwModule.setServosController(new SimulatorServosControllerImpl());
		
		return fbwModule;		
	}
	
	public static SPIBusChannel createSPIBusChannel() {
		return new SPIBusChannelImpl(); 
	}
	
	public static FlightModel createSimulator() {
		return new FlightModelImpl();
	}
}
