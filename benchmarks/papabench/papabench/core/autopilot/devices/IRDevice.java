package papabench.core.autopilot.devices;

import papabench.core.commons.devices.Device;

/**
 * IR device access interface.
 * 
 * @author Michal Malohlava
 *
 */
public interface IRDevice extends Device {

	 /**
	  * Read and update the device values.
	  */
	 void update();

	 /**
	  * Calibration.
	  */
	 void calibrate();

	 int getIrRoll();

	 int getIrPitch();
	 
	 int getIrTop();

	 int getIrContrast();

	 int getIrRollNeutral();

	 int getIrPitchNeutral();

	 float getIrRadOfIr();

	 void setSimulIrRoll(int simulIrRoll);

	 void setSimulIrPitch(int simulIrPitch);

}