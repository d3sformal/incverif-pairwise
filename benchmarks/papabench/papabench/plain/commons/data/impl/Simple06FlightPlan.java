/**
 * 
 */
package papabench.plain.commons.data.impl;

import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.data.Position3D;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.UTMPosition;
import papabench.core.commons.data.impl.AbstractFlightPlan;
import papabench.core.utils.LogUtils;
import papabench.core.utils.PPRZUtils;


/**
 * This is a modification of flight plan 3.
 * 
 * @see Simple03FlightPlan
 * 
 * @author Michal Malohlava
 * 
 */
public class Simple06FlightPlan extends AbstractFlightPlan {
	
	protected static final int GROUND_ALTITUDE = 125;
	protected static final int SECURE_ALTITUDE = 150;
	

	public String getName() {
		return "Simple 06 flight plan";
	}
	
	public float getGroundAltitude() {
		return GROUND_ALTITUDE;
	}
	
	public float getSecureAltitude() {		
		return SECURE_ALTITUDE;
	}

	@Override
	protected int getNumberOfNavBlocks() {
		return 2;
	}

	@Override
	protected int getNumberOfWaypoints() {
		return 7;
	}

	@Override
	protected void initNavigationBlocks() {
		/* navigation block 01 */
		addNavBlock(new NavigationBlock(3))
			.addNavStage(new NavigationStage() {				
				@Override
				protected void execute() {					
					status().setLaunched(true);
					nextStage();
				}
			}).addNavStage(new NavigationStage() {				
				@Override
				protected void execute() {
					if (estimator.getFlightTime() > 8) {
						nextStage();
					} else {
						navigator().setDesiredCourse((float) Math.toRadians(15));
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0.15f);
						status.setVerticalFlightMode(VerticalFlightMode.AUTO_GAZ);
						navigator().setDesiredGaz((int) PPRZUtils.trimuPPRZ(0.8f * RadioConf.MAX_PPRZ));
					}																									
				}
			}).addNavStage(new NavigationStage() {
				@Override
				protected void execute() {
					LogUtils.log(this, "Estimator Z="+estimator.getPosition().z);
					if (estimator.getPosition().z > getSecureAltitude()
						|| estimator.getFlightTime() > 100) {
						nextStage(); // --> nextBlock
					} else {
						navigator().setDesiredCourse((float) Math.toRadians(270));
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0.0f);
						status.setVerticalFlightMode(VerticalFlightMode.AUTO_CLIMB);
						status.setClimb(8f);
					}
				}
			});
		
		/* navigation block 02 */
		addNavBlock(new NavigationBlock(3)).addNavStage(new NavigationStage() { // stage 0
				@Override
				protected void execute() {					
					if (approaching(1)) {
						LogUtils.log(this, "Current position: " + estimator.getPosition());
						LogUtils.log(this, "WP 1 " + WP(1).toString() + " reached! Going to the next stage");
						nextStageFrom(1);
					} else {
						flyToWP(1); // 0, 0, 200
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0f);
						status().setVerticalFlightMode(VerticalFlightMode.AUTO_ALTITUDE);
						navigator().setDesiredAltitude(WPALT(1));
						navigator().setPreClimb(0);
					}
				}
			}).addNavStage(new NavigationStage() { // stage 1				
				@Override
				protected void execute() {
					if (approaching(4)) {
						LogUtils.log(this, "Current position: " + estimator.getPosition());
						LogUtils.log(this, "WP 4 " + WP(4).toString() + " reached! Going to the next stage");
						nextStageFrom(4);					
					} else {
						flyToWP(4); // 115,0,200
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0f);
						status().setVerticalFlightMode(VerticalFlightMode.AUTO_ALTITUDE);
						navigator().setDesiredAltitude(WPALT(4));
						navigator().setPreClimb(0);
					}
				}
			}).addNavStage(new NavigationStage() { // stage 2				
				@Override
				protected void execute() {
					// ping 10 times between the points
					if (estimator.getFlightTime() < 4000) {							
							gotoStage(0);
					} else {
						LogUtils.log(this, "Mission is DONE");
						// finished mission
						missionFinished();					
					}
				}
			});

	}

	@Override
	protected void initWaypoints() {		
		addWaypoint(new Position3D(0f, 0f, 200f)); // 0
		addWaypoint(new Position3D(0f, 0f, 200f)); // 1
		addWaypoint(new Position3D(115f, -75f, 200f)); // 2
		addWaypoint(new Position3D(156.7f, -41.7f, 200f)); // 3
		addWaypoint(new Position3D(115, 0f, 200f)); // 4
		addWaypoint(new Position3D(0f, -75f, 200f)); // 5
		addWaypoint(new Position3D(-51.7f, -36.7f, 200f)); // 6
	}

	@Override
	protected UTMPosition getCenterUTMPosition() {
		return null;
	}
}
