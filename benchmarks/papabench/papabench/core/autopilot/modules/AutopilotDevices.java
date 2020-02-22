/**
 * 
 */
package papabench.core.autopilot.modules;

import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.bus.SPIBus;

/**
 * @author Michal Malohlava
 *
 */
public interface AutopilotDevices {
	
	void setGPSDevice(GPSDevice gpsDevice);
	GPSDevice getGPSDevice();
	
	void setIRDevice(IRDevice irDevice);
	IRDevice getIRDevice();
	
	void setSPIBus(SPIBus spiBus);
	SPIBus getSpiBus();
}
