package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A worker is the code that runs on a node as part of a job.
 */
public interface Worker extends Remote
{
  /**
   * Gets the ID that is associated with this worker. If no 
   * ID is set, this returns a null value.
   * @return the worker id, null if undefined.
   */
  public String getWorkerID() throws RemoteException;
  
  /**
   * Sets a (hopefully) unique ID for the worker. This should uniquely
   * identify workers in the current job/controller.
   * @param id the worker id.
   */
  public void setWorkerID(String id) throws RemoteException;
}