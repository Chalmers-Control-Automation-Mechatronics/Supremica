package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Input data for a job. This is a generic wrapper 
 * around a serializable map of attributes.
 * @author Sam Douglas
 */
public class Job implements Serializable, Cloneable
{
  public Job()
  {
    this(new HashMap<String,Object>());
  }

  private Job(Map<String,Object> attributes)
  {
    mAttributes = attributes;
  }

  /**
   * Sets an attribute in this job. The attribute must be
   * serializable. If the attribute already exists, it
   * will be overwritten.
   * @param attr The attribute name to set.
   * @param obj A serializable object.
   */
  public <T extends Serializable> 
	    void setAttribute(String attr, T obj) throws IllegalStateException
  {
    if (getJobStatus() == JobStatus.NOT_RUN)
      mAttributes.put(attr, obj);
    else
      throw new IllegalStateException("Cannot set attributes for completed job");
  }

  /**
   * Gets an attribute from this job. The attribute must
   * exist.
   * @return The object corresponding to the attribute.
   * @throws IllegalArgumentException if the attribute 
   *                                  does not exist.
   */
  public Object getAttribute(String attr) throws IllegalArgumentException
  {
    if (mAttributes.containsKey(attr))
      return mAttributes.get(attr);
    else
      throw new IllegalArgumentException("Attribute '" + attr + 
					 "' does not exist");
  }

  /**
   * Check if this job contains an attribute.
   * @return True if the attribute is present in the job.
   */
  public boolean containsAttribute(String attr)
  {
    return mAttributes.containsKey(attr);
  }


  /**
   * Gets the status of this job.
   * @return the status of the job.
   */
  public JobStatus getJobStatus()
  {
    return mJobStatus;
  }


  /**
   * Gets the exception associated with this job. It is only
   * valid to call this method when the job status is EXCEPTION.
   * @return the exception that occurred.
   * @throws IllegalStateException if this is called when job status is not EXCEPTION
   */
  public Exception getException() throws IllegalStateException
  {
    if (getJobStatus() == JobStatus.EXCEPTION)
      return mException;
    else
      throw new IllegalStateException("Can only call getException " +
				      "when the job is in the exception state");
  }


  /**
   * Sets this job's status to complete. It is only valid to run this 
   * method from the NOT_RUN job status. This also prevents any 
   * further modification of the attribute mapping.
   * @throws IllegalStateException if called after the job status has
   *                               been set.
   */
  public void setComplete() throws IllegalStateException
  {
    if (getJobStatus() != JobStatus.NOT_RUN)
      throw new IllegalStateException("Cannot set job status from this state.");

    mJobStatus = JobStatus.COMPLETE;
  }


  /**
   * Sets this job's status to exception.  It is only valid to 
   * call this method from the NOT_RUN job status. This prevents further
   * modification of the attribute mapping.
   * @param e The exception to store.
   * @throws IllegalStateException if called after the job status has been set.
   */
  public void setException(Exception e) throws IllegalStateException
  {
    if (getJobStatus() != JobStatus.NOT_RUN)
      throw new IllegalStateException("Cannot set job status from this state.");

    mException = e;
    mJobStatus = JobStatus.EXCEPTION;
  }

  public Job clone()
  {
    Job job = new Job(new HashMap<String,Object>(mAttributes));
    job.mJobStatus = mJobStatus;
    job.mException = mException;
    return job;
  }

  private final Map<String,Object> mAttributes;
  private JobStatus mJobStatus = JobStatus.NOT_RUN;
  private Exception mException = null;
}