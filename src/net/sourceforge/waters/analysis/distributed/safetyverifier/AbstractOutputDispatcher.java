package net.sourceforge.waters.analysis.distributed.safetyverifier;

public abstract class AbstractOutputDispatcher implements OutputDispatcher
{
  private static final long serialVersionUID = 1L;

  public AbstractOutputDispatcher(StateDistribution dist)
  {
    mStateDistribution = dist;
  }

  public abstract void addState(StateTuple state) throws Exception;

  public void addStates(StateTuple[] states, int offset, int length) throws Exception
  {
    for (int i = 0; i < offset+length && i < states.length; i++)
      {
	addState(states[i]);
      }
  }

  public StateDistribution getStateDistribution()
  {
    return mStateDistribution;
  }

  /**
   * An empty shutdown hook.
   */
  public void shutdown()
  {
  }

  private final StateDistribution mStateDistribution;
}