/**
 * 
 */
package papabench.core.autopilot.modules;

import papabench.core.bus.SPIBus;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.commons.modules.Module;

/**
 * 
 *  
 * @author Michal Malohlava
 *
 */
public interface LinkToFBW extends Module {
	
	InterMCUMsg getMessageFromFBW();
	
	void sendMessageToFBW(InterMCUMsg msg);
	
	void setSPIBus(SPIBus spiBus);
}
