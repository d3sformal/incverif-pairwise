/**
 * 
 */
package papabench.core.commons.conf;

/**
 * @author Michal Malohlava
 * 
 */
public enum FBWMode {
	MANUAL(0), AUTO(1), FAILSAFE(2);

	int value;

	private FBWMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static FBWMode valueOf(int i) {
		return values()[i];				
	}
}
