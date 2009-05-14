package net.sourceforge.waters.analysis.distributed.application;

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