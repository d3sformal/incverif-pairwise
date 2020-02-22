/**
 * 
 */
package papabench.core.commons.devices;

/**
 * Type interface to mark a hardware device.
 * 
 * @author Michal Malohlava
 *
 */
public interface Device {
	
	/**
	 * Initialize this device.
	 * 
	 */
	public void init();
	
	/**
	 * Reset the device.
	 */
	public void reset();
}
