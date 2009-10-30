package net.sourceforge.waters.analysis.distributed;

import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.model.des.EventProxy;


public class VerificationJobResult extends JobResult
{
  public VerificationJobResult()
  {
    super();
  }

  public VerificationJobResult(JobResult other)
  {
    super(other);
  }

  public Boolean getResult()
  {
    return (Boolean)get(RESULT_ATTR);
  }

  public void setResult(Boolean result)
  {
    set(RESULT_ATTR, result);
  }

  public EventProxy[] getTrace()
  {
    return (EventProxy[])get(TRACE_ATTR);
  }

  public void setTrace(EventProxy[] trace)
  {
    set(TRACE_ATTR, trace);
  }

  public static final String TRACE_ATTR = "trace";
  public static final String RESULT_ATTR = "result";
  private static final long serialVersionUID = 1L;

}