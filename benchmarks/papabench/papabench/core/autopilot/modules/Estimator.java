/**
 * 
 */
package papabench.core.autopilot.modules;

import papabench.core.autopilot.data.Attitude;
import papabench.core.autopilot.data.HorizSpeed;
import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.commons.modules.Module;

/**
 * @author Michal Malohlava
 *
 */
public interface Estimator extends Module /*RequireGPSDevice, RequireIRDevice */ {
	
	void setGPSDevice(GPSDevice gpsDevice);
	
	void setIRDevice(IRDevice irDevice);
	
	Position3D getPosition();
	
	Attitude getAttitude();
	
	/**
	 * Returns horizontal speed in module(m/s) and direction (rad)
	 *  
	 * @return horizontal speed
	 */
	HorizSpeed getHorizontalSpeed();
	
	Position3D getSpeed(); // -> estimator_z_dot
	
	int getFlightTime();
	void setFlightTime(int time);
	
	void updatePosition(); // -> estimator_update_state_gps
	
	void updateIRState(); // ->  estimator_update_state_infrared
	
	void updateIR(); // -> estimator_update_ir_estim
	
	void updateFlightTime();
}
