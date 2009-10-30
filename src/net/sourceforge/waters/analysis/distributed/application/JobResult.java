package net.sourceforge.waters.analysis.distributed.application;

import static net.sourceforge.waters.analysis.distributed.application.CommonJobAttributes.*;

/**
 * Stores results for a job. This provides accessor methods for
 * standard result attributes. These methods are the preferred way to
 * access the information. If the job data has been set to immutable,
 * then calling a mutator method will fail.
 */
public class JobResult extends AbstractJobDataDecorator
{
  public JobResult()
  {
    super(new BaseJobData());
  }

  public JobResult(JobResult other)
  {
    super(other);
  }

  /**
   * Gets the name attribute value
   * @return attribute value
   */
  public String getName()
  {
    return (String)get(NAME_ATTR);
  }

  /**
   * Sets the name attribute value.
   * @param name attribute value
   */
  public void setName(String name)
  {
    set(NAME_ATTR, name);
  }

  /**
   * Gets the exception attribute value
   * @return attribute value
   */
  public Exception getException()
  {
    return (Exception)get(EXCEPTION_ATTR);
  }

  /**
   * Sets the exception attribute value.
   * @param exception attribute value
   */
  public void setException(Exception exception)
  {
    set(EXCEPTION_ATTR, exception);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}