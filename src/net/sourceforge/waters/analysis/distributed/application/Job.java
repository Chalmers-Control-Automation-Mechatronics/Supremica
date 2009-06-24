package net.sourceforge.waters.analysis.distributed.application;

import static net.sourceforge.waters.analysis.distributed.application.CommonJobAttributes.*;

/**
 * Information for a job in the distributed application. This also
 * specifies the common attributes for jobs. Using the accessor
 * methods provided in this class is the preferred way to modify job
 * data. If the job data has been set to immutable, then any of the
 * mutator methods will fail.
 * @author Sam Douglas
 */ 
public class Job extends AbstractJobDataDecorator
{
  /**
   * Create a new job with no attributes set.
   */
  public Job()
  {
    super(new BaseJobData());
  }

  public Job(Job other)
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
   * Gets the controller attribute value
   * @return attribute value
   */
  public String getController()
  {
    return (String)get(CONTROLLER_ATTR);
  }

  /**
   * Sets the controller attribute value.
   * @param controller attribute value
   */
  public void setController(String controller)
  {
    set(CONTROLLER_ATTR, controller);
  }

  /**
   * Gets the node count attribute value
   * @return attribute value
   */
  public Integer getNodeCount()
  {
    return (Integer)get(NODECOUNT_ATTR);
  }

  /**
   * Sets the node count attribute value.
   * @param count attribute value
   */
  public void setNodeCount(Integer count)
  {
    set(NODECOUNT_ATTR, count);
  }
}