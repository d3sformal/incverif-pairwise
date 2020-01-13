//
// The replicated workers coordination abstraction consists
// of a collection of 3 kinds of objects:
//
//   a single instance of the ReplicatedWorkers type
//   a single instance of the Coordinator type
//   multiple instances of the Worker type
//
// Instantition of the abstraction requires a user to provide
//   a configuration object
//   work and result collections for pool data
//   work and result computations
//   and the number and granularity of workers
// as parameters to the ReplicatedWorkers constructor.  
//
// The user interacts with the instance of the abstractions by making
// calls on methods of the ReplicatedWorkers class.  We refer to the
// thread making such calls as the "main" thread.  The Worker objects are
// called "worker" threads. 
//
// The architecture of the implementation is as follows:
//
//  picture  of main interacting with workers through coordinator
//
// The role of the coordinator is to control access to shared
// objects of the abstraction, synchronize execution and detect
// termination.  To do this it encapsulates, hides, and controls
// access to all components of abstraction state that can effect
// guard-conditions on wait statements.  A single flat, centralized
// abstraction  is used to eliminate nested-monitor problems and
// decouple pool, "main" and "worker" thread functionality from coordination
// issues.
// 


package fse2006replicatedworkers;

import fse2006replicatedworkers.WorkItemComputation;
import fse2006replicatedworkers.ResultItemComputation;
import fse2006replicatedworkers.Collection;
import fse2006replicatedworkers.SynchronizedCollection;
import fse2006replicatedworkers.Configuration;
import fse2006util.StandardCountingSemaphore;
import fse2006util.StandardBarrier;
import java.util.Vector;
import java.util.Enumeration;

final public class ReplicatedWorkers {
  private Vector workers;
  protected boolean stop;
  protected boolean done;
  protected StandardCountingSemaphore resultLock;
  protected StandardBarrier mainWorkerBarrier;
  protected Collection workPool; 
  protected SynchronizedCollection resultPool;
  protected Coordinator theCoord;
  protected Configuration theConfig;
  protected int numWorkers;
  protected int numItems;

  public ReplicatedWorkers(Configuration newConfig,
                           Collection workCollection,
                           Collection resultCollection, 
                           int newNumWorkers, int newNumItems)
  {
    done = true;
    stop = false;
    numWorkers = newNumWorkers;
    numItems = newNumItems;
    theConfig = newConfig;

    workPool = workCollection;
    resultPool = new SynchronizedCollection(resultCollection);
    resultLock = new StandardCountingSemaphore(1);
    mainWorkerBarrier = new StandardBarrier(numWorkers+1);
    theCoord = new Coordinator(this);


    workers = new Vector(numWorkers);
    for ( int i=0; i<numWorkers; i++ ) {
       workers.addElement(new Worker(this));      
    }

  } // end public ReplicatedWorkers

  public final synchronized void destroy()
  {
    stop = true; 
    mainWorkerBarrier.await();
  }

  public final synchronized void putWork(Vector v)
  {
    theCoord.add(v); 
  } // end public void putWork(Vector v)

  public final synchronized Vector getResults() 
  {
    return resultPool.take();
  } // end public Vector getResults()

  public final synchronized void execute()
  {

    done = false; 
    mainWorkerBarrier.await();

// Add conditional for asynch execution

    // Detect termination
    //
    // Workers can signal termination based on local computation
    // results.  This will initiate a shutdown (release of all blocked
    // workers).
    //
    // Draining the work pool can also terminate the computation.
    // In which case we need to initiate a shutdown here.
    theCoord.mainAwaitTerminate();
    done = true; 
    theCoord.notifyDoneChange();

    // In any case, we need to make sure that all workers have
    // sucessfully terminated their computations and are awaiting
    // execution.
    mainWorkerBarrier.await();

  } // end public void execute()
}


final class Coordinator 
{
  private int blockedTakers;
  private ReplicatedWorkers memberOf;

  public Coordinator(ReplicatedWorkers instance)
  {
    memberOf = instance;
    blockedTakers = 0;
  } 

  protected final synchronized void notifyTakerChange()
  {
    if ( blockedTakers == memberOf.numWorkers ) 
      notifyAll();
  }

  protected final synchronized void notifyDoneChange()
  {
    if ( memberOf.done ) 
      notifyAll();
  }

  protected final synchronized void notifyPoolChange()
  {
     if ( (memberOf.workPool.size() >= memberOf.numItems) ||
          (memberOf.theConfig.isSome() && memberOf.workPool.size() >= 0) )
       notifyAll();
  }


  // termination : one of the termination conditions is met and all
  //               workers either block taking work or head to the finalization
  //               point, the main thread must free any blocked threads
  //  conditions : done is true OR
  //               all workers are waiting and 
  //               (config.IsSOme and size<num  OR size==0)

  protected final synchronized void mainAwaitTerminate()
  {
    while ( blockedTakers != memberOf.numWorkers && 
            !memberOf.done ) {
      try { wait(); } catch ( InterruptedException ex) {}
    }
  }

  // CHANGE: push synch inward
  public final /*synchronized*/ Vector take(int num)
  {
     ++blockedTakers;

     if ( (memberOf.workPool.size() < num) && 
          // EASY BUG: change to "||" to "&&"
          (!memberOf.theConfig.isSome() || memberOf.workPool.size() == 0) &&
          !memberOf.done) 
       memberOf.theCoord.notifyTakerChange();

	// INJECTED ERROR
	synchronized(this) {
     // Only accept a get request if:
     //    workPool.size() >= num
     // or
     //    theConfig.isSome() & workPool.size() > 0
     // or
     //    done
     //
     // EASY BUG: change from "< num" to "== num"
     // EASY BUG: change from "while" to "if"
     while ( (memberOf.workPool.size() < num) && 
             (!memberOf.theConfig.isSome() || memberOf.workPool.size() == 0) &&
             !memberOf.done) {
       try { wait(); } catch ( InterruptedException ex) {}
     }

     // Create a vector of up to num elements
     if (memberOf.done) {
       --blockedTakers;
       memberOf.theCoord.notifyTakerChange();

       return new Vector();

     } else {
       Vector returnVal = new Vector(num);
       if ( num > memberOf.workPool.size() ) num = memberOf.workPool.size();

       for (int i=0; i<num; i++) {
         returnVal.addElement(memberOf.workPool.take());
       }

       --blockedTakers;
       memberOf.theCoord.notifyTakerChange();

       return returnVal;
     }
	}
  }

  // EASY BUG if both add and take are un-synchronized
  public final synchronized void add(Vector v)
  {
     Enumeration vEnum = v.elements();

     while ( vEnum.hasMoreElements() ) {
       memberOf.workPool.add(vEnum.nextElement());
     }
     notifyPoolChange();
  }
}

final class Worker implements Runnable {
  private ReplicatedWorkers memberOf; 
  private Thread thisThread;

  public Worker(ReplicatedWorkers rwInstance)
  {
    memberOf = rwInstance;
    thisThread = new Thread(this);
    thisThread.start();
  } // end public Worker()

  public void run() 
  {
    Vector theWork, newWork, theResults;
    boolean done = false;
    Enumeration vEnum;

    theWork = new Vector();
    newWork = new Vector();
    theResults = new Vector();

    while (true) {
      memberOf.mainWorkerBarrier.await(); 

      if ( memberOf.stop ) break;

      // Repeatedly get work, process it, put new work
      while (true) {
        theWork.removeAllElements();
        newWork.removeAllElements();
        theResults.removeAllElements();

        // Attempt to get new work
        theWork = memberOf.theCoord.take(memberOf.numItems);

        // An empty vector is only returned when the computation
        // has terminated
        if (theWork.size() == 0) break;

        vEnum = theWork.elements();
        while ( vEnum.hasMoreElements() ) {
          done = ((WorkItemComputation)vEnum.nextElement()).
                 doWork(newWork, theResults);
          if (done) break;
        }

        // Short-circuit the computation if indicated
        if (done) {
          memberOf.done = true;
          memberOf.theCoord.notifyDoneChange();
        }

        // Process the results
        if ( theResults.size() !=0 ) {
          if (! memberOf.theConfig.isResultNone() ) {
            if ( memberOf.theConfig.isResultExclusive() ) {
              memberOf.resultLock.await();
            }

            vEnum = theResults.elements();
            while ( vEnum.hasMoreElements() ) {
              done = ((ResultItemComputation)vEnum.nextElement()).doResults();
              if (done) break;
            }
  
            if ( memberOf.theConfig.isResultExclusive() ) {
              memberOf.resultLock.signal();
            }

            // Short-circuit the computation if indicated
            if (done) {
              memberOf.done = true;
              memberOf.theCoord.notifyDoneChange();
            }

          } else {
            // save up the results
            memberOf.resultPool.add(theResults);

          } // end if process some results
        } // end if any results

        // Put the new work back
        memberOf.theCoord.add(newWork);

      } // end work phase loop

      memberOf.mainWorkerBarrier.await(); 
    } // end outer loop
  } // end public void run()

}
