/**
 * 
 */
package papabench.core.fbw.modules.impl;

import papabench.core.bus.SPIBus;
import papabench.core.commons.conf.FBWMode;
import papabench.core.fbw.devices.PPMDevice;
import papabench.core.fbw.devices.ServosController;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.modules.LinkToAutopilot;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class FBWModuleImpl extends FBWStatusImpl implements FBWModule {
	
	private LinkToAutopilot linkToAutopilot;
	
	private PPMDevice ppmDevice;
	private ServosController servosController;
	private SPIBus spiBus;

	public void init() {
		if (ppmDevice == null || servosController == null || linkToAutopilot == null || spiBus == null) {
			throw new IllegalArgumentException("Module FBWModule has wrong configuration");
		}
		
		// FIXME this is hard-written switch to AUTO mode
		setFBWMode(FBWMode.AUTO);
		linkToAutopilot.setSPIBus(this.spiBus);
		linkToAutopilot.init();
		
		ppmDevice.init();
		servosController.init();				
	}

	public LinkToAutopilot getLinkToAutopilot() {
		return this.linkToAutopilot;
	}

	public PPMDevice getPPMDevice() {
		return this.ppmDevice;
	}

	public SPIBus getSPIBus() {
		return this.spiBus;
	}

	public ServosController getServosController() {
		return this.servosController;
	}

	public void setPPMDevice(PPMDevice ppmDevice) {
		this.ppmDevice = ppmDevice;
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;
	}

	public void setServosController(ServosController servosController) {
		this.servosController = servosController;
	}

	public void setLinkToAutopilot(LinkToAutopilot linkToAutopilot) {
		this.linkToAutopilot = linkToAutopilot;		
	}
}
