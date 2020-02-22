/**
 * 
 */
package papabench.core.autopilot.data;

/**
 * Position in 3D.
 * 
 * @author Michal Malohlava
 *
 */
public class Position3D extends Position2D {
	
	public Position3D(float x, float y, float z) {
		super(x,y);		
		this.z = z;
	}
	
	public float z;
	
	@Override
	public String toString() {		
		return "[" + x + "," + y + "," + z + "]";
	}
}
