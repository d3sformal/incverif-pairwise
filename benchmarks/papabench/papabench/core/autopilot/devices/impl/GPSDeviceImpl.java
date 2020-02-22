/**
 * 
 */
package papabench.core.autopilot.devices.impl;

import papabench.core.autopilot.devices.GPSDevice;

/**
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class GPSDeviceImpl implements GPSDevice {
	private int mode;
	private float tow; /* ms */
	private float altitude; /* m */
	private float speed; /* m/s */
	private float climb; /* m/s */
	private float course; /* rad */
	private int utmEast, utmNorth;
	private float east, north; /* m */
	
	private boolean positionAvailable = true;

	public void init() {		
	}
	
	public void reset() {
	}

	public int getMode() {
		return mode;
	}

	public float getTow() {
		return tow;
	}

	public float getAltitude() {
		return altitude;
	}

	public float getSpeed() {
		return speed;
	}

	public float getClimb() {
		return climb;
	}

	public float getCourse() {
		return course;
	}

	public int getUtmEast() {
		return utmEast;
	}

	public int getUtmNorth() {
		return utmNorth;
	}

	public float getEast() {
		return east;
	}

	public float getNorth() {
		return north;
	}

	public boolean isPositionAvailable() {
		return positionAvailable;
	}
}
