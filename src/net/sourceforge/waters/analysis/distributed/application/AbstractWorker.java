package net.sourceforge.waters.analysis.distributed.application;

public abstract class AbstractWorker implements Worker, WorkerLocal
{
  public void created() throws Exception
  {
  }

  public void deleted()
  {
  }
}