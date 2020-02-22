/**
 * 
 */
package papabench.plain.commons.tasks;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
public class PeriodicTask extends Thread {
	
	protected Runnable targetRunnable;
	
	protected int repeatCount;
	
	public PeriodicTask(Runnable targetRunnable, int repeatCount) {
		super();
		
		this.targetRunnable = targetRunnable;
		
		this.repeatCount = repeatCount;
	}
	
	public PeriodicTask(Runnable targetRunnable) {
		this(targetRunnable, 1);
	}
	
	@Override
	public void run() {
		
		int count = 0;
		
		while (count < repeatCount) {
		//while (true) {
			targetRunnable.run();
			count++;
		}
	}
}
