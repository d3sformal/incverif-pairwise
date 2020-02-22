/**
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.modules.AutopilotModule;

/**
 * Reports airplane state to ground central.
 * 
 * Notes:
 *   - called as 'downlink'
 *   
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class ReportingTaskHandler implements Runnable {
	
	private AutopilotModule autopilotModule;
	int counter = 0;
	
	public ReportingTaskHandler(AutopilotModule autopilotModule) {	
		this.autopilotModule = autopilotModule;
	}

	public void run() {
		// This task has period 100ms => count the number of executions and send 
		// different information according to a counter.
		
		// send attitude (500ms)		
		// send GPS position
		// send IR status
		// send ability to take-off
	}
	
	protected void sendGPS() {	
		
	}
	
	protected void sendIRStatus() {		
	}
	
	protected void sendTakeOff() {		
	}
}
