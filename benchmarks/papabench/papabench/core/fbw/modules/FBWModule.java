/**
 * 
 */
package papabench.core.fbw.modules;

import papabench.core.commons.modules.Module;

/**
 * @author Michal Malohlava
 *
 */
public interface FBWModule extends Module, FBWStatus, FBWDevices {
	
	LinkToAutopilot getLinkToAutopilot();
	void setLinkToAutopilot(LinkToAutopilot linkToAutopilot);

}
