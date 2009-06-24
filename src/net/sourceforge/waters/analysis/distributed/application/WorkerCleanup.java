package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of workers that have been created on this
 * node, and which controller was responsible. This allows
 * all the workers for a particular controller to be cleaned
 * up and unexported.
 *
 * @author Sam Douglas 
 */
class WorkerCleanup
{
  public WorkerCleanup()
  {
    mWorkers = new HashMap<ControllerID, Set<WorkerLocal>>();
  }

  public synchronized void registerWorker(ControllerID id, WorkerLocal worker)
  {
    assert(id != null);
    assert(worker != null);

    Set<WorkerLocal> wset = mWorkers.get(id);
    if (wset == null)
      {
	wset = new HashSet<WorkerLocal>();
	mWorkers.put(id, wset);
      }

    wset.add(worker);
  }

  /**
   * Cleanup the workers associated with an id. Errors that occur when
   * trying to clean up the object will be ignored.
   *
   * Remote objects will be unexported forcefully, which may cause calls 
   * that are in progress to fail.
   * 
   * After calling this method, there will be no workers associated 
   * with the controller ID, even if some of them failed to be cleaned
   * up properly (nothing we can do?)
   *
   * @param id The controller ID to clean up for.
   */
  public synchronized void cleanup(ControllerID id)
  {
    Set<WorkerLocal> wset = null;

    synchronized (this)
      {
	wset = mWorkers.get(id);
	mWorkers.remove(id);
      }
    
    if (wset == null)
      return;

    for (WorkerLocal w : wset)
      {
	//Ideally, we want to cast to a Worker (the remote interface)
	//and forcefully unexport the object.
	try
	  {
	    Worker remote = (Worker)w;
	    UnicastRemoteObject.unexportObject(remote, true);
	  }
	catch (ClassCastException e)
	  {
	    //The worker is a bit messed up, not remote.
	    //This probably shouldn't be able to happen.
	  }
	catch (NoSuchObjectException e)
	  {
	    //The worker wasn't exported. This is no big 
	    //deal. We will just ignore it and continue
	  }

	w.deleted();    
      }
  }

  //Map of controller id to set of workers. This really assumes
  //that workers implement object equality and hashcode.
  private final Map<ControllerID, Set<WorkerLocal>> mWorkers;
}