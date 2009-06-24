package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * A mapping of serialisable attributes to specify information about
 * jobs.
 * @author Sam Douglas
 */
class BaseJobData implements Serializable, JobData
{
  /**
   * Uses an existing map of attributes. This map will not be copied.
   * @param attributes map to use.
   */
  protected BaseJobData(Map<String,Object> attributes)
  {
    mAttributes = attributes;
  }

  /**
   * Copy constructor. Copies the attribute map from another
   * job data object. The immutable flag is not copied, that is to
   * say the new object will be mutable.
   * @param other job data to copy attributes from.
   */
  protected BaseJobData(BaseJobData other)
  {
    this(new HashMap<String,Object>(other.mAttributes));
  }

  /**
   * Creates an empty attribute mapping.
   */
  public BaseJobData()
  {
    this(new HashMap<String,Object>());
  }

 
  public <T extends Serializable>
	    void set(String attr, T obj) 
    throws IllegalStateException,
    IllegalArgumentException
  {
    if (isImmutable())
      throw new IllegalStateException(IMMUTABLE_MESSAGE);

    if (obj != null && !(Serializable.class.isAssignableFrom(obj.getClass())))
      throw new IllegalArgumentException("Attribute value for '" + attr 
					 + "' was not serializable");

    mAttributes.put(attr, obj);
  }


  public void merge(JobData other)
  {
    if (isImmutable())
      throw new IllegalStateException(IMMUTABLE_MESSAGE);

    if (other == null)
      throw new NullPointerException("Other job data was null");
    
    if (this == other)
      throw new IllegalArgumentException("Cannot merge with self");
    
    for (Map.Entry<String,Object> entry : other.getAttributeMap().entrySet())
      {
	if (!mAttributes.containsKey(entry.getKey()))
	  mAttributes.put(entry.getKey(), entry.getValue());
      }
  }


  public Object get(String attr)
  {
    return mAttributes.get(attr);
  }


  public boolean contains(String attr)
  {
    return mAttributes.containsKey(attr);
  }


  public boolean isImmutable()
  {
    return mImmutable;
  }


  public void setImmutable()
  {
    mImmutable = true;
  }

  public Map<String,Object> getAttributeMap()
  {
    return Collections.unmodifiableMap(mAttributes);
  }

  public JobData getJobData()
  {
    return this;
  }

  private boolean mImmutable = false;
  private final Map<String,Object> mAttributes;
  private static final String IMMUTABLE_MESSAGE = "Cannot modify immutable job data";
}