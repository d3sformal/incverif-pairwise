/**
 * 
 */
package papabench.core.utils;

import java.io.PrintStream;

/**
 * Simple log utils.
 * 
 * @author Michal Malohlava
 *
 */
final public class LogUtils {
	
	public static final void log(Object o, String msg) {
		log(System.out, o.getClass().getName(), msg);
	}
	
	public static final void log(PrintStream os, String s, String msg) {
		os.print(s);
		os.print(": ");
		os.println(msg);		
	}
	
	public static final void log(String name, String msg) {
		log(System.out, name, msg);
	}

}
