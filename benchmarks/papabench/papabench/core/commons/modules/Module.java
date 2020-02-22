/**
 * 
 */
package papabench.core.commons.modules;

/**
 * @author Michal Malohlava
 *
 */
public interface Module {
	/**
	 * Initialize a module.
	 * 
	 * The module should internally check if all references are OK, if not then runtime exception should be thrown.
	 */
	void init();
	
}
