/**
 * 
 */
package papabench.plain;

import papabench.core.autopilot.conf.PapaBenchAutopilotConf.AltitudeControlTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.ClimbControlTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.LinkFBWSendTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.NavigationTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.RadioControlTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.ReportingTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.StabilizationTaskConf;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.handlers.AltitudeControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.ClimbControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.LinkFBWSendTaskHandler;
import papabench.core.autopilot.tasks.handlers.NavigationTaskHandler;
import papabench.core.autopilot.tasks.handlers.RadioControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.ReportingTaskHandler;
import papabench.core.autopilot.tasks.handlers.StabilizationTaskHandler;
import papabench.core.bus.SPIBusChannel;
import papabench.core.commons.data.FlightPlan;
import papabench.core.fbw.conf.PapaBenchFBWConf.CheckFailsafeTaskConf;
import papabench.core.fbw.conf.PapaBenchFBWConf.CheckMega128ValuesTaskConf;
import papabench.core.fbw.conf.PapaBenchFBWConf.SendDataToAutopilotTaskConf;
import papabench.core.fbw.conf.PapaBenchFBWConf.TestPPMTaskConf;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.tasks.handlers.CheckFailsafeTaskHandler;
import papabench.core.fbw.tasks.handlers.CheckMega128ValuesTaskHandler;
import papabench.core.fbw.tasks.handlers.SendDataToAutopilotTaskHandler;
import papabench.core.fbw.tasks.handlers.TestPPMTaskHandler;
import papabench.core.simulator.conf.PapaBenchSimulatorConf.SimulatorFlightModelTaskConf;
import papabench.core.simulator.conf.PapaBenchSimulatorConf.SimulatorGPSTaskConf;
import papabench.core.simulator.conf.PapaBenchSimulatorConf.SimulatorIRTaskConf;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.tasks.handlers.SimulatorFlightModelTaskHandler;
import papabench.core.simulator.tasks.handlers.SimulatorGPSTaskHandler;
import papabench.core.simulator.tasks.handlers.SimulatorIRTaskHandler;
import papabench.plain.commons.tasks.PeriodicTask;

/**
 * RTSJ-based implementation of PapaBench.
 * 
 * @author Michal Malohlava
 *
 */
public class PapaBenchImpl implements PlainPapabench {
	
	private AutopilotModule autopilotModule;
	private FlightPlan flightPlan;
	
	private static final int AUTOPILOT_TASKS_COUNT = 4;
	private static final int FBW_TASKS_COUNT = 4;
	private static final int SIMULATOR_TASKS_COUNT = 3;
	
	private static final int REPEAT_COUNT = 2; // 10,1000
	

	private Thread[] autopilotTasks;
	private Thread[] simulatorTasks;

	public AutopilotModule getAutopilotModule() {
		return autopilotModule;
	}

	public void setFlightPlan(FlightPlan flightPlan) {
		this.flightPlan = flightPlan;		
	}

	public FBWModule getFBWModule()
	{
		return null;
	}

	public void init() {	
		// Allocate and initialize global objects: 
		//  - MC0 - autopilot
		autopilotModule = PapaBenchFactory.createAutopilotModule();
				
		// Create & configure SPIBusChannel and connect both PapaBench modules
		SPIBusChannel spiBusChannel = PapaBenchFactory.createSPIBusChannel();
		spiBusChannel.init();
		autopilotModule.setSPIBus(spiBusChannel.getMasterEnd()); // = MC0: SPI master mode
		
		// setup flight plan
		assert(this.flightPlan != null);
		autopilotModule.getNavigator().setFlightPlan(this.flightPlan);
		
		// initialize both modules - if the modules are badly initialized the runtime exception is thrown
		autopilotModule.init();
		
		// Register interrupt handlers
		/*
		 * TODO
		 */
		
		// Register period threads
		createAutopilotTasks(autopilotModule);
		
		// Create a flight simulator
		FlightModel flightModel = PapaBenchFactory.createSimulator();
		flightModel.init();
		
		// Register simulator tasks
		createSimulatorTasks(flightModel, autopilotModule);

	}
	
	protected void createAutopilotTasks(AutopilotModule autopilotModule) {
		autopilotTasks = new Thread[AUTOPILOT_TASKS_COUNT];
		autopilotTasks[0] = new PeriodicTask(new AltitudeControlTaskHandler(autopilotModule), REPEAT_COUNT * 250 / AltitudeControlTaskConf.PERIOD_MS);
		autopilotTasks[1] = new PeriodicTask(new ClimbControlTaskHandler(autopilotModule), REPEAT_COUNT * 250 / ClimbControlTaskConf.PERIOD_MS);
		autopilotTasks[2] = new PeriodicTask(new NavigationTaskHandler(autopilotModule), REPEAT_COUNT * 250 / NavigationTaskConf.PERIOD_MS);
		
		// StabilizationTask allocates messages which are sent to FBW unit -> allocate them in scope memory
		autopilotTasks[3] = new PeriodicTask(new StabilizationTaskHandler(autopilotModule), REPEAT_COUNT * 250 / StabilizationTaskConf.PERIOD_MS);		
	}
	
	protected void createSimulatorTasks(FlightModel flightModel, AutopilotModule autopilotModule) {
		simulatorTasks = new Thread[SIMULATOR_TASKS_COUNT];
		simulatorTasks[0] = new PeriodicTask(new SimulatorFlightModelTaskHandler(flightModel,autopilotModule), REPEAT_COUNT * 250 / SimulatorFlightModelTaskConf.PERIOD_MS);
		simulatorTasks[1] = new PeriodicTask(new SimulatorGPSTaskHandler(flightModel,autopilotModule), REPEAT_COUNT * 250 / SimulatorGPSTaskConf.PERIOD_MS);
		simulatorTasks[2] = new PeriodicTask(new SimulatorIRTaskHandler(flightModel,autopilotModule), REPEAT_COUNT * 250 / SimulatorIRTaskConf.PERIOD_MS);		
	}

	public void start() {
		// FIXME put here rendez-vous for all tasks		
		
		for (int i = 0; i < SIMULATOR_TASKS_COUNT; i++) {
			simulatorTasks[i].start();
		}

		for (int i = 0; i < AUTOPILOT_TASKS_COUNT; i++) {
			autopilotTasks[i].start();
		}		
	}
}
