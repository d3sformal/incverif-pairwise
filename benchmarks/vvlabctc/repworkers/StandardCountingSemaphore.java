package fse2006util;

// Author: Doug Lea
public final class StandardCountingSemaphore {
  protected long permits_;

  public StandardCountingSemaphore(long initial) { permits_ = initial; }
  public StandardCountingSemaphore() { permits_ = 0; }

  public synchronized void await() {
    permits_ = permits_ - 1;
    if (permits_ < 0)
      try { wait(); } catch (InterruptedException ex) {}
  }

  public synchronized void signal() {
    long oldPermits = permits_;
    permits_ = permits_ + 1;
    if (oldPermits < 0)
      notify();
  }
}
