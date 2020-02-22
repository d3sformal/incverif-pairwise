/**
 * 
 */
package papabench.core.commons.data;

import papabench.core.commons.conf.FBWMode;

/**
 * - should be generated according to aircraft description.
 * - radio mc3030
 * 
 * @author Michal Malohlava
 *
 */
public interface RadioCommands {
	
	public static final byte RADIO_THROTTLE = 0;
	public static final byte RADIO_ROLL 	= 1;
	public static final byte RADIO_PITCH 	= 2;
	public static final byte RADIO_YAW 		= 3;
	public static final byte RADIO_MODE 	= 4;
	public static final byte RADIO_GAIN1 	= 5;
	public static final byte RADIO_GAIN2 	= 6;
	public static final byte RADIO_LLS   	= 7;
	public static final byte RADIO_CALIB 	= 8;
	
	int[] getChannels();
	
	int getThrottle();	
	int getRoll();
	int getPitch();
	int getYaw();
	FBWMode getMode();
	int getGain1();
	int getGain2();
	int getLLS();
	int getCalib();
	
	void setThrottle(int value);	
	void setRoll(int value);
	void setPitch(int value);
	void setYaw(int value);
	void setMode(FBWMode mode);
	void setGain1(int value);
	void setGain2(int value);
	void setLLS(int value);
	void setCalib(int value);
	
	boolean containsAveragedChannels();
	
	public RadioCommands clone();
	
	public void fillFrom(RadioCommands radioCommands);
}
