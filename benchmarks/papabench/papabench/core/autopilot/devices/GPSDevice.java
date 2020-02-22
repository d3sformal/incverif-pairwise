package papabench.core.autopilot.devices;

import papabench.core.commons.devices.Device;

/**
 * GPS device access interface.
 * 
 * @author Michal Malohlava
 *
 */
public interface GPSDevice extends Device {

	/**
	 * Get GPS device mode.
	 * 
	 * @return
	 */
	int getMode();

	float getTow();

	float getAltitude();

	float getSpeed();

	float getClimb();

	float getCourse();

	int getUtmEast();

	int getUtmNorth();

	float getEast();

	float getNorth();

	boolean isPositionAvailable();
}