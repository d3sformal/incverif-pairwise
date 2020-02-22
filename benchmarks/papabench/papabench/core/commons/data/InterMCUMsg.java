/**
 * 
 */
package papabench.core.commons.data;

import papabench.core.commons.conf.AirframeParametersConf;
import papabench.core.commons.data.impl.RadioCommandsImpl;

/**
 * Message for communication between FBW and Autopilot module.
 * 
 * @author Michal Malohlava
 * 
 * TODO: define interface for manipulation with data
 */
//@SCJAllowed
public class InterMCUMsg {
	protected static final byte STATUS_RADIO_OK = 0;
	protected static final byte RADIO_REALLY_LOST = 1;
	protected static final byte AVERAGED_CHANNELS_SENT = 2;
	
	// updated by SPI interrupt
	volatile boolean isValid = false;
	
	public InterMCUMsg() {
		this(false);
	}
	
	public InterMCUMsg(boolean allocateRadioCommands) {
		if (allocateRadioCommands) {
			radioCommands = new RadioCommandsImpl();
		}
	}
	
	/**
	 * @see AirframeParametersConf#RADIO_CTL_NB
	 */
	public RadioCommands radioCommands;
	
	public byte ppmCpt;
	public byte status;
	public byte numberOfErrors;
	public byte voltSupply;
	
	/**
	 * Returns true if the message is completely received.
	 *  
	 * @return true/false depending of message state
	 */
	public boolean isValid() {
		return isValid;
	}
	
	public void setValid(boolean valid) {
		this.isValid = valid;
	}
	
	public boolean isRadioOK() {
		return (this.status & (1 << STATUS_RADIO_OK)) == 1;		
	}
	
	public void setRadioOK(boolean value) {
		this.status |= value ? 1 << STATUS_RADIO_OK : 0;
	}
	
	public boolean isRadioReallyLost() {		
		return (this.status & (1 << RADIO_REALLY_LOST)) == 1;
	}
	
	public void setRadioReallyLost(boolean value) {
		this.status |= value ? 1 << RADIO_REALLY_LOST : 0;		
	}
	
	public boolean isAveragedChannelsSent() {		 
		return (this.status & (1 << AVERAGED_CHANNELS_SENT)) == 1;
	}
	
	public void setAveragedChannelsSent(boolean value) {
		this.status |= value ? 1 << AVERAGED_CHANNELS_SENT : 0;
	}
	
	public void fillFrom(InterMCUMsg msg) {
		this.ppmCpt = msg.ppmCpt;
		this.status = msg.status;
		this.numberOfErrors = msg.numberOfErrors;
		this.voltSupply = msg.voltSupply;
		this.isValid = msg.isValid;
		
		this.radioCommands.fillFrom(msg.radioCommands);
	}

	@Override
	public String toString() {
		return "InterMCUMsg [isValid=" + isValid + ", numberOfErrors="
				+ numberOfErrors + ", ppmCpt=" + ppmCpt + ", radioCommands="
				+ radioCommands + ", status=" + status + ", voltSupply="
				+ voltSupply + "]";
	}
	
	
}
