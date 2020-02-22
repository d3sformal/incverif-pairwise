/**
 * 
 */
package papabench.plain;

import papabench.core.commons.data.FlightPlan;
import papabench.core.utils.LogUtils;
import papabench.plain.commons.data.impl.Simple02FlightPlan;

/**
 * PapaBench application launcher class.
 * 
 * FIXME improve this
 * 
 * @author Michal Malohlava
 *
 */
public class PapaBenchApplication {
	
	public static void main(String[] args) {
		
		PlainPapabench papaBench = new PapaBenchImpl();
		FlightPlan plan = new Simple02FlightPlan();
		//LogUtils.log("papabench.plain.PapaBenchApplication", "Flight plan: " + plan.getName());
			
		papaBench.setFlightPlan(plan);
		papaBench.init();
			
		papaBench.start();					
	}
}
