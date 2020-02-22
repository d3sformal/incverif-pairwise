/**
 * 
 */
package papabench.core.autopilot.data;

/**
 * Horizontal ground speed in module (m/s) and direction (radians - CW/North).
 * 
 * @author Michal Malohlava
 *
 */
public class HorizSpeed {
		
	public HorizSpeed(float module, float direction) {
		super();
		this.module = module;
		this.direction = direction;
	}
	
	public float module;
	public float direction;
	@Override
	public String toString() {
		return "HorizSpeed(dir=" + direction + ", mod=" + module+ ")";
	}
	
	

}
