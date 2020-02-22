/**
 * 
 */
package papabench.core.simulator.model;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.modules.Module;



/**
 * @author Michal Malohlava
 *
 */
public interface FlightModel extends Module {
	
	State getState();
	
	void updateState();
	
	void processCommands(RadioCommands commands);
}
