/**
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.modules.AutopilotModule;

/**
 * Link to FBW unit control task handler.
 * 
 * @author Michal Malohlava
 * 
 * @do not edit !
 *
 */
public class LinkFBWSendTaskHandler implements Runnable {
	
	private AutopilotModule autopilotModule;
	
	public LinkFBWSendTaskHandler(AutopilotModule autopilotModule) {
		this.autopilotModule = autopilotModule;
	}

	public void run() {
		// FIXME currenty the task do nothing -> the implementation of send is in StabilizationTask, however it should be here
	}
	
}
