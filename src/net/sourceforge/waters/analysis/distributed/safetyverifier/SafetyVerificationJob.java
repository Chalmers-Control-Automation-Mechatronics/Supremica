package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.analysis.distributed.VerificationJob;

public class SafetyVerificationJob extends VerificationJob
{
  public SafetyVerificationJob()
  {
    super();
  }

  public SafetyVerificationJob(Job other)
  {
    super(other);
  }

  public Integer getProcessingThreadCount()
  {
    return (Integer)get(PROCESSING_THREADS_ATTR);
  }

  public void setProcessingThreadCount(Integer threads)
  {
    set(PROCESSING_THREADS_ATTR, threads);
  }

  public String getStateDistribution()
  {
    return (String)get(STATE_DISTRIBUTION_ATTR);
  }

  public void setStateDistribution(String dist)
  {
    set(STATE_DISTRIBUTION_ATTR, dist);
  }



  //#########################################################################
  //# Class Constants
  public static final String PROCESSING_THREADS_ATTR = "processing-threads";
  public static final String STATE_DISTRIBUTION_ATTR = "state-distribution";

  private static final long serialVersionUID = 1L;

}