package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for storing statistics and data about a job.
 * This is based on a map, so that attributes can be 
 * looked up by name. This is a trade-off between 'safety'
 * and binary compatibility (if the JobStats object is 
 * stored in serialised form). Hopefully this will allow
 * job stat dumps to remain useful as the software changes.
 *
 * @author Sam Douglas 
 */
public class JobStats implements Serializable
{
  public <T extends Serializable>
	    void set(String attr, T obj) 
  {
    if (obj != null && !(Serializable.class.isAssignableFrom(obj.getClass())))
      throw new IllegalArgumentException("Attribute value for '" + attr 
					 + "' was not serializable");
    
    mStatsMap.put(attr, obj);
  }


  public Object get(String attr)
  {
    return mStatsMap.get(attr);
  }


  public boolean contains(String attr)
  {
    return mStatsMap.containsKey(attr);
  }


  public Map<String,Object> getStatsMap()
  {
    return Collections.unmodifiableMap(mStatsMap);
  }


  private final Map<String,Object> mStatsMap = new HashMap<String,Object>();
}