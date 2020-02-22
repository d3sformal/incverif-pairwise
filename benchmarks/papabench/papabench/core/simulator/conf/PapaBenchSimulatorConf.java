package papabench.core.simulator.conf;

import papabench.core.commons.conf.CommonTaskConfiguration;

/**
 * 
 * @author Michal Malohlava
 *
 */
public interface PapaBenchSimulatorConf {
	
	public static interface SimulatorFlightModelTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SimulatorFlightModelTask";		
		public static final int PRIORITY = 26;		
		public static final int PERIOD_MS = 25;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	public static interface SimulatorGPSTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SimulatorGPSTask";		
		public static final int PRIORITY = 37;		
		public static final int PERIOD_MS = 250;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	public static interface SimulatorIRTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SimulatorIRTask";		
		public static final int PRIORITY = 36;		
		public static final int PERIOD_MS = 50;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
}
