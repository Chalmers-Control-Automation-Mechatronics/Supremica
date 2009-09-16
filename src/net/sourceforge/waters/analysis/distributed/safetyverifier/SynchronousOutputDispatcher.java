package net.sourceforge.waters.analysis.distributed.safetyverifier;

public class SynchronousOutputDispatcher extends AbstractOutputDispatcher
{
  public SynchronousOutputDispatcher(StateDistribution dist)
  {
    super(dist);
    mDistribution = getStateDistribution();
  }

  public void addState(StateTuple state) throws Exception
  {
    mDistribution.addState(state);
  }

  private final StateDistribution mDistribution;
}