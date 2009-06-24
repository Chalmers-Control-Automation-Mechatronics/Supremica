package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.Remote;
import java.rmi.RemoteException;


import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

public interface SafetyVerifierWorker extends Remote
{
  /**
   * Sets the job that is being processed. This provides access to the
   * original model, as well as any additional parameters.
   * @param job the job information
   */
  public void setJob(Job job) throws RemoteException;

  public void setModelSchema(ProductDESSchema des) throws RemoteException;

  /**
   * Starts 'n' processing threads running on the worker. The
   * buffersize parameter sets how many states each thread should
   * buffer up (to avoid constantly hitting the state list monitor)
   * @param n the number of processing threads to start
   * @param buffersize the number of states to buffer in each thread.
   */
  public void startProcessingThreads(int n, int buffersize) throws RemoteException;

  /**
   * Add a state to this worker to be processed. In order to ensure
   * the application works as expected, states should be added to the
   * appropriate workers.
   * @param state the state to add.
   */
  public void addState(StateTuple state) throws RemoteException;
}