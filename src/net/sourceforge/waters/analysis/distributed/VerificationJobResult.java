package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;

import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.model.des.TraceProxy;

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

  public TraceProxy getTrace()
  {
    return (TraceProxy)get(TRACE_ATTR);
  }

  public void setTrace(TraceProxy trace)
  {
    set(TRACE_ATTR, (Serializable)trace);
  }

  public static final String TRACE_ATTR = "trace";
  public static final String RESULT_ATTR = "result";
}