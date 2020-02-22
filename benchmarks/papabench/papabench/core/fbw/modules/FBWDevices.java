package papabench.core.fbw.modules;

import papabench.core.bus.SPIBus;
import papabench.core.fbw.devices.PPMDevice;
import papabench.core.fbw.devices.ServosController;

/**
 * 
 * @author Michal Malohlava
 *
 */
public interface FBWDevices {
	
	PPMDevice getPPMDevice();
	void setPPMDevice(PPMDevice ppmDevice);
	
	ServosController getServosController();
	void setServosController(ServosController servosController);
	
	SPIBus getSPIBus();
	void setSPIBus(SPIBus spiBus);
}
