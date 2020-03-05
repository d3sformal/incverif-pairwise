/**
 * 
 */
package papabench.core.bus.impl;

/**
 * Simple cyclic queue.
 * 
 * @author Michal Malohlava
 *
 */
public class ArrayCyclicQueue /* implements Queue */ {
	
	private Object[] queue;
	// the head of queue
	private int queueStart;
	// the last item of queue
	private int queueEnd;
	private int queueSize;
	
	private int maxSize;
	
	public ArrayCyclicQueue(int maxSize) {
		this.maxSize = maxSize;
		this.queue = new Object[maxSize];
		this.queueEnd = 0; // tail - first empty slot
		this.queueStart = -1; // first element
		this.queueSize = 0;
	}
	
	public synchronized void offer(Object o) {
		this.queue[this.queueEnd] = o;		
		this.queueSize++;
		if (this.queueSize == 1) {
			this.queueStart = this.queueEnd;
		}
		this.queueEnd = (this.queueEnd + 1) % this.maxSize;
	}
	
	public synchronized Object poll() {
		if (this.queueSize == 0) {
			return null;
		} else {
			Object o = this.queue[this.queueStart];
			this.queue[this.queueStart] = null;
			this.queueStart = (this.queueStart + 1) % this.maxSize;
			this.queueSize--;
			
			return o;
		}
	}
	
	public synchronized Object peek() {
		if (this.queueSize == 0) {
			return null;
		} else {
			return this.queue[this.queueStart];
		}
	}
	
	// INJECTED ERROR
	public /*synchronized*/ boolean isEmpty() {
		return this.queueSize == 0;
	}
}
