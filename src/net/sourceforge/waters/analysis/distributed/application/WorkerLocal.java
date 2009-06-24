package net.sourceforge.waters.analysis.distributed.application;

/**
 * The non-remote interface for a worker. This contains methods 
 * that are called when the worker is created and deleted etc.
 */
public interface WorkerLocal
{
  /**
   * Called once the worker has been created. This method can
   * create threads, allocate resources etc. that cannot be done
   * in the constructor.
   *
   * It is called by the node after a worker is created, but before
   * the stub for the worker is returned to the controller.
   *
   * If an exception occurs in this method, it will be returned to 
   * the controller that created it, and the worker will be deleted.
   * This will result in the deleted() method being called.
   */
  public void created() throws Exception;

  /**
   * Called when the worker is deleted. This happens before the RMI 
   * object is unexported.
   *
   * There is no guarantee that the created() method has been executed
   * when this runs, nor is there a guarantee that the worker has been
   * exported correctly, or that the controller still exists.
   *
   * This method should ensure that any threads have been stopped and 
   * resources freed up so that the worker object can be garbage 
   * collected.
   */
  public void deleted();
}