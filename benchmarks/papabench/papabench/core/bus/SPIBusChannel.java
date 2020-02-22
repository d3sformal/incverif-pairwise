package papabench.core.bus;

import papabench.core.commons.devices.Device;


/**
 * Simple representation of SPI link between two devices.
 * 
 * @author Michal Malohlava
 *
 */
public interface SPIBusChannel extends Device {
	
	SPIBus getMasterEnd();
	
	SPIBus getSlaveEnd();	
}
