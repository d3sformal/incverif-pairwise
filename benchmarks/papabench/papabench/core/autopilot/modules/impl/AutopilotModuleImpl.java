/**
 * 
 */
package papabench.core.autopilot.modules.impl;

import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.LinkToFBW;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.bus.SPIBus;

/**
 * @author Michal Malohlava
 *
 */
//@SCJAllowed(members=true)
//@SCJAllowed
public class AutopilotModuleImpl extends AutopilotStatusImpl implements AutopilotModule {
	
	/* Module dependencies: */

	/* GPS device obtaining current position */
	private GPSDevice gpsDevice;
	/* IR device returning roll of airplane */
	private IRDevice irDevice;
	/* Communication SPI bus */
	private SPIBus spiBus;
	
	/* Estimator is responsible for holding/computing current position, altitude, speed */
	private Estimator estimator;
	
	/* Navigator module responsible for autopilot navigation according to the flight plan */
	private Navigator navigator;
	
	/* Communication link to FBW */
	private LinkToFBW linkToFBW;
	
	/* ------------------
	 * module iface impl. 
	 * ------------------ */
	public void init() {
		super.init();
				
		if (gpsDevice == null || irDevice == null || spiBus == null || estimator == null || navigator == null || linkToFBW == null) {
			throw new IllegalArgumentException("Autopilot module has wrong configuration");
		}
		
		gpsDevice.init();
		irDevice.init();
		
		estimator.setGPSDevice(gpsDevice);
		estimator.setIRDevice(irDevice);
		estimator.init();
		
		navigator.setAutopilotModule(this);
		navigator.init();
		
		linkToFBW.setSPIBus(this.spiBus);
		linkToFBW.init();
	}

	/* ---------------------------
	 * autopilot device iface impl. 
	 * --------------------------- */
	
	public void setGPSDevice(GPSDevice gpsDevice) {
		this.gpsDevice = gpsDevice;
	}
	
	public GPSDevice getGPSDevice() {		
		return this.gpsDevice;
	}	
	
	public void setIRDevice(IRDevice irDevice) {
		this.irDevice = irDevice;
	}
	
	public IRDevice getIRDevice() {
		return this.irDevice;		
	}

	public SPIBus getSpiBus() {		
		return this.spiBus;
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;
	}


	/* ---------------------------
	 * autopilot module iface impl.
	 * --------------------------- */
	
	public Estimator getEstimator() {
		return this.estimator;
	}

	public void setEstimator(Estimator estimator) {
		this.estimator = estimator;
	}
	
	public Navigator getNavigator() {		
		return this.navigator;
	}
	
	public void setNavigator(Navigator navigator) {
		this.navigator = navigator;
	}

	public LinkToFBW getLinkToFBW() {
		return this.linkToFBW;
	}

	public void setLinkToFBW(LinkToFBW fbwLink) {
		this.linkToFBW = fbwLink;
	}

	@Override
	public String toString() {
		/*
		return "AutopilotModuleImpl [estimator=" + estimator + ", gpsDevice="
				+ gpsDevice + ", irDevice=" + irDevice + ", linkToFBW="
				+ linkToFBW + ", navigator=" + navigator + ", spiBus=" + spiBus
				+ "]"; */		
		return super.toString();
	}
	
}
