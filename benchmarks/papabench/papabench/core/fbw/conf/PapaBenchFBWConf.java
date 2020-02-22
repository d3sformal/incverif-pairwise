/**
 * 
 */
package papabench.core.fbw.conf;

import papabench.core.commons.conf.CommonTaskConfiguration;

/**
 * @author Michal Malohlava
 *
 */
public interface PapaBenchFBWConf {
	
	/**
	 * Test PPM (?) task configuration.
	 */
	public static interface TestPPMTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "TestPPM";		
		public static final int PRIORITY = 35;		
		public static final int PERIOD_MS = 25;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Check Failsafe task configuration.
	 */
	public static interface CheckFailsafeTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "CheckFailsafe";		
		public static final int PRIORITY = 23;		
		public static final int PERIOD_MS = 50;	
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Check mega128 values task configuration.
	 */
	public static interface CheckMega128ValuesTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "CheckMega128Values";		
		public static final int PRIORITY = 22;		
		public static final int PERIOD_MS = 50;	
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Send data to autopilot task configuration.
	 */
	public static interface SendDataToAutopilotTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SendDataToAutopilot";		
		public static final int PRIORITY = 34;		
		public static final int PERIOD_MS = 25;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	
}
