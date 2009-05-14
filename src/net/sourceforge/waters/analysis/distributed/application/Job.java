package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Input data for a job. This is a generic wrapper 
 * around a serializable map of attributes.
 * @author Sam Douglas
 */
public class Job implements Serializable
{
  public Job()
  {
    mAttributes = new HashMap<String,Object>();
  }

  /**
   * Set an attribute in the job. The attribute must be
   * serializable. If the attribute already exists, it
   * will be overwritten.
   * @param attr The attribute name to set.
   * @param obj A serializable object.
   */
  public <T extends Serializable> 
	    void setAttribute(String attr, T obj)
  {
    mAttributes.put(attr, obj);
  }

  /**
   * Get an attribute from the job. The attribute must
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
   * Check if the job contains an attribute.
   * @return True if the attribute is present in the job.
   */
  public boolean containsAttribute(String attr)
  {
    return mAttributes.containsKey(attr);
  }

  private final Map<String,Object> mAttributes;
}