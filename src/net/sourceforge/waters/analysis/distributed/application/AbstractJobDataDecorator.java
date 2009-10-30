package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;
import java.util.Map;


public abstract class AbstractJobDataDecorator implements JobData, Serializable
{

  private static final long serialVersionUID = 1L;

  protected AbstractJobDataDecorator(JobData jobdata)
  {
    mJobData = jobdata;
  }

  public <T extends Serializable> void set(String attr, T obj)
  {
    mJobData.set(attr, obj);
  }

  public void merge(JobData other)
  {
    mJobData.merge(other);
  }

  public Object get(String attr)
  {
    return mJobData.get(attr);
  }

  public boolean contains(String attr)
  {
    return mJobData.contains(attr);
  }

  public boolean isImmutable()
  {
    return mJobData.isImmutable();
  }

  public void setImmutable()
  {
    mJobData.setImmutable();
  }

  public Map<String, Object> getAttributeMap()
  {
    return mJobData.getAttributeMap();
  }

  public JobData getJobData()
  {
    return mJobData;
  }

  private final JobData mJobData;
}