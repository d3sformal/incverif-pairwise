/**
 * 
 */
package papabench.core.bus;

import papabench.core.commons.data.InterMCUMsg;
import papabench.core.commons.devices.Bus;

/**
 * @author Michal Malohlava
 * 
 * FIXME this class is kind of simplification of communication between FBW and Autopilot modules.
 * 
 */
public interface SPIBus extends Bus {
	
	void sendMessage(InterMCUMsg msg);
	
	boolean getMessage(InterMCUMsg msg);	
}
