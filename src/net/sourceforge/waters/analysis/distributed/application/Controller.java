package net.sourceforge.waters.analysis.distributed.application;

import java.util.Collection;

/**
 * A job controller. The job controller supervises the execution of a
 * job running on the distributed application.
 * @author Sam Douglas
 */
public interface Controller extends Runnable
{
  /**
   * Get the current state of the controller.
   */
  public ControllerState getState();

  
  /**
   * Get the exception that was thrown by the controller.
   * It is only valid to call this method if the controller
   * is in the EXCEPTION state.
   * @return The exception that was thrown inside the run method.
   * @throws IllegalStateException when called from a state that
   *                               is not EXCEPTION.
   */
  public Exception getException() throws IllegalStateException;


  /**
   * Sets the input job this controller is to work on.
   * @param job the job that will be processed.
   */
  public void setJob(Job job) throws IllegalStateException;

  
  /**
   * Gets the job for this controller. If no job has been set
   * then this method will return null.
   * @return the job for this controller, or null if not set.
   */
  public Job getJob();

  /**
   * Gets the job result. It is only valid to call this method in the
   * completed state. It is possible that the result of the job could 
   * be null.
   * @return the result of the controller execution.
   * @throws IllegalStateException if called before the job has
   *                               completed successfully.
   */
  public JobResult getResult() throws IllegalStateException;

  /**
   * Sets the collection of nodes this controller has 
   * available to it.
   *
   * The server will check to see if the nodes seem alive
   * when the job list is created, but after that there are
   * no guarantees the nodes will be available. The controller
   * must deal with nodes being unavailable as appropriate
   *
   * It is only valid to call this method before the controller
   * is run. Calling it in any state other than NOT_RUN will
   * cause an IllegalStateException to be thrown
   *
   * @param nodes a collection of nodes.
   */
  public void setNodes(Collection<Node> nodes) 
    throws IllegalStateException;


  /**
   * Gets the nodes that were assigned to this controller.
   * If nodes have not been assigned, then this method will
   * return a null value.
   * @return collection of nodes, or null if not set.
   */
  public Collection<Node> getNodes();

  /**
   * Sets the ControllerID for this object.
   * @param id controller ID to use
   */
  public void setControllerID(ControllerID id);

  /**
   * Gets the ControllerID for this controller.  The ID is used when
   * creating remote objects on nodes to manage cleanups.
   */
  public ControllerID getControllerID();

  /**
   * Run the controller. Any exceptions will be handled within the run
   * method. It is only valid to call this method from the NOT_RUN
   * state. If the controller is in the RUNNING state, then a repeated
   * call to this method will return immediately. If the controller is
   * in any other state, then an IllegalStateException will be set,
   * the controller state will change to EXCEPTION and this method will
   * terminate.
   */
  public void run();
}