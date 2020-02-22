/**
 * 
 */
package papabench.core.commons.status;

/**
 * @author Michal Malohlava
 *
 */
public interface Command {
	
	float getAileron();
	
	float getElevator();	
	
	float getRoll();
	
	float getGaz();
	
	float getPitch();
	
	float getClimb();
}
