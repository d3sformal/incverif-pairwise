package fse2006replicatedworkers;

import fse2006replicatedworkers.Collection;
import fse2006replicatedworkers.ReplicatedWorkers;
import java.util.Vector;
import java.util.Enumeration;

final class SynchronizedCollection {
  private Collection theCollection;

  public SynchronizedCollection(Collection  c)
  {
    theCollection = c; 
  } 

  public final synchronized int size()
  {
    return theCollection.size();
  }

  public final synchronized Vector take()
  {
    Vector returnVal = new Vector(theCollection.size());
    for (int i=0; i<theCollection.size(); i++) {
      returnVal.addElement(theCollection.take());
    }
    return returnVal;
  }

  public final synchronized void add(Vector v)
  {
     Enumeration vEnum = v.elements();

     while ( vEnum.hasMoreElements() ) {
       theCollection.add(vEnum.nextElement());
     }
  }
}

