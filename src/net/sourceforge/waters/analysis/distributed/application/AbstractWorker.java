package net.sourceforge.waters.analysis.distributed.application;

/**
 * Abstract implementation of the worker interface. This provides a
 * default implementation of the WorkerLocal interface.
 */
public abstract class AbstractWorker implements Worker, WorkerLocal
{
  public void created() throws Exception
  {
  }

  public void deleted()
  {
  }

  public void setErrorCallback(ErrorCallback cb)
  {
    mErrorCallback = cb;
  }


  public void handle(Throwable throwable)
  {
    try
      {
	ErrorCallback cb = mErrorCallback;
	if (cb == null)
	  defaultErrorHandler(throwable);
	else
	  cb.handle(getWorkerID(), getWorkerProxy(), throwable);
      }
    catch (Exception e)
      {
	errorCallbackFailed(e, throwable);
	return;
      }
    
    errorCallbackSucceeded(throwable);
 }

  /**
   * Default error handler. This is called if there is no
   * remote callback set.
   */ 
  protected void defaultErrorHandler(Throwable throwable)
  {
    System.err.format("AbstractWorker default handler: %s", throwable);
    throwable.printStackTrace();
  }

  /**
   * Template method that is called after an error has been
   * successfully reported to the error callback.
   * @param throwable Error that was handled.
   */
  protected void errorCallbackSucceeded(Throwable throwable)
  {
  }

  /**
   * Template method that is called if an error callback 
   * fails.
   * @param why the callback failed.
   * @param throwable that was being handled.
   */
  protected void errorCallbackFailed(Exception why, Throwable throwable)
  {
    System.err.format("Worker handler callback failed");
    throw new RuntimeException(throwable);
  }

  public void setWorkerProxy(Worker proxy)
  {
    mWorkerProxy = proxy;
  }

  public Worker getWorkerProxy()
  {
    return mWorkerProxy;
  }

  public String getWorkerID()
  {
    return mWorkerID;
  }

  public void setWorkerID(String workerid)
  {
    mWorkerID = workerid;
  }
  
  private volatile Worker mWorkerProxy;
  private volatile ErrorCallback mErrorCallback;
  private volatile String mWorkerID;
}