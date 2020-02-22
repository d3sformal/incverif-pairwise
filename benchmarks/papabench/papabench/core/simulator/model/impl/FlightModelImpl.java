/**
 * 
 */
package papabench.core.simulator.model.impl;

import papabench.core.autopilot.data.Position3D;
import papabench.core.commons.data.RadioCommands;
import papabench.core.simulator.conf.SimulatorConf;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.model.State;

/**
 * Flight model.
 * 
 * @author Michal Malohlava
 *
 */
public class FlightModelImpl implements FlightModel {
	
	private State state;
	private Position3D wind;

	public void init() {
		state = new StateImpl();
		state.init();
		
		wind = new Position3D(0, 0, 0);		
	}
	
	public State getState() {
		return this.state;
	}

	public void updateState() {
		state.updateState(SimulatorConf.FM_PERIOD, wind);		
	}
	
	public void processCommands(RadioCommands commands) {
		state.updateState(commands);
	}
}
