package net.sourceforge.waters.analysis.distributed.safetyverifier;

public class SynchronousOutputDispatcher extends AbstractOutputDispatcher
{
  private static final long serialVersionUID = 1L;

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