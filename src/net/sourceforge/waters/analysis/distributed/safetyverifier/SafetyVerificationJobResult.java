package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.VerificationJobResult;

public class SafetyVerificationJobResult extends VerificationJobResult
{
  public SafetyVerificationJobResult()
  {
    super();
  }

  public SafetyVerificationJobResult(JobResult other)
  {
    super(other);
  }

  public JobStats getJobStats()
  {
    return (JobStats)get(STATS_ATTR);
  }

  public void setJobStats(JobStats stats)
  {
    set(STATS_ATTR, stats);
  }

  public static final String STATS_ATTR = "job-stats";
}