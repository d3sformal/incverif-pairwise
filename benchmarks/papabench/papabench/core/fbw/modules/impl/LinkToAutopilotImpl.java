/**
 * 
 */
package papabench.core.fbw.modules.impl;

import papabench.core.bus.SPIBus;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.fbw.modules.LinkToAutopilot;

/**
 * TODO comment this
 * 
 * @author Michal Malohlava
 *
 */
public class LinkToAutopilotImpl implements LinkToAutopilot {
	
	private SPIBus spiBus;

	/**
	 * Returns message from autopilot. 
	 * 
	 * Message is allocate in this method call!
	 */
	public InterMCUMsg getMessageFromAutopilot() {
		// allocate new message
		InterMCUMsg msg = getEmptyMessage(); 

		// let the bus fill the message
		spiBus.getMessage(msg);		
		
		return msg; 
	}
	
	protected InterMCUMsg getEmptyMessage() {
		return new InterMCUMsg(true);
	}

	public void sendMessageToAutopilot(InterMCUMsg msg) {
		spiBus.sendMessage(msg);
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;		
	}

	public void init() {
		if (spiBus == null) {
			throw new IllegalArgumentException("Module LinkToAutopilot is not configured.");
		}
	}

}
