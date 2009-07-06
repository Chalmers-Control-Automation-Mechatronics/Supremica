package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.Remote;
import java.rmi.RemoteException;


import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;

public interface SafetyVerifierWorker extends Remote, StateHandler
{
  /**
   * Sets the job that is being processed. This provides access to the
   * original model, as well as any additional parameters.
   * @param job the job information
   */
  public void setJob(Job job) throws RemoteException;

  public void setModelSchema(ProductDESSchema des) throws RemoteException;

  /**
   * Sets a (hopefully) unique ID for the worker. This should uniquely
   * identify workers in the current job/controller.
   * @param id the worker id.
   */
  public void setWorkerID(String id) throws RemoteException;

  /**
   * Gets the ID that is associated with this worker. If no 
   * ID is set, this returns a null value.
   * @return the worker id, null if undefined.
   */
  public String getWorkerID() throws RemoteException;

  /**
   * Sets the state distribution for this worker. This call depends on
   * the worker id being set -- it will set the handler corresponding
   * to its worker ID to a local object, avoiding the RMI system (or
   * whatever) for local state additions.
   * @param stateDist state distribution to use.
   * @throws IllegalStateException if the worker ID is unset.
   */
  public void setStateDistribution(StateDistribution stateDist) throws RemoteException;

  /**
   * Gets the state distribution object for this worker, if set.
   * Returns a null value if unset.
   * @return a state distribution or null.
   */
  public StateDistribution getStateDistribution() throws RemoteException;

  /**
   * Sets the state encoding to use.
   * @param encoding the encoder to use
   */
  public void setStateEncoding(StateEncoding encoding) throws RemoteException;

  /**
   * Gets the state encoding used by this worker, or null if undefined.
   * @return the state encoder, or null.
   */
  public StateEncoding getStateEncoding() throws RemoteException;

  /**
   * Returns a bad state if one has been found, or null if
   * no bad state has yet been found.
   * @return a bad state, or null if no bad state has been found.
   */
  public StateTuple getBadState() throws RemoteException;

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

  
  public int getStateCount() throws RemoteException;
  public long getIncomingStateCount() throws RemoteException;
  public long getOutgoingStateCount() throws RemoteException;
  public int getWaitingStateCount() throws RemoteException;
  
  /**
   * Sets the worker to pause. This will prevent it from processing
   * new states. This method may not take effect immediately, that is
   * to say it states may still be processed after the call but it
   * will stop processing new states eventually. Calling this method 
   * on an paused worker will have no effect.
   */
  public void pause() throws RemoteException;

  /**
   * Sets the worker to resume. Any processing threads will be allowed
   * to resume. Calling this on a running worker will have no effect.
   */
  public void resume() throws RemoteException;

  /**
   * Checks if the worker is set to pause.
   */
  public boolean isPaused() throws RemoteException;

  /**
   * Sets the worker to kill off all processing threads, so that the
   * the worker's resources are released. This will be triggered
   * automatically in the worker's local cleanup method.
   */
  public void kill() throws RemoteException;

  /**
   * Begins a search for a predecessor. This may terminate an existing
   * search, or change the search goal.
   * @param state to generate predecessors for
   * @param callback to send send generated states to
   */
  public void predecessorSearch(StateTuple state, PredecessorCallback callback) throws RemoteException;

  /**
   * Begin an idle test on this worker. This probably sets a boolean
   * flag which is reset when the worker state changes in a way that
   * would be considered non-idle.
   */
  public void startIdleTest() throws RemoteException;

  /**
   * Finishes the idle test and returns whether the worker has been
   * idle since the test began. The value this method returns on
   * repeat calls or before a test has been started is undefined.
   * @return the state of the canary: true if nothing interesting has happened
   *         since the flag was set.
   */
  public boolean finishIdleTest() throws RemoteException;
}