package net.sourceforge.waters.analysis.distributed.safetyverifier;

public class HashStateDistribution extends StateDistribution
{
  public HashStateDistribution(String[] handlers)
  {
    super();
    mHandlerIDs = handlers.clone();
    mHandlerCache = new StateHandler[handlers.length];
  }

  public StateHandler lookupStateHandler(StateTuple state)
  {
    int hashcode = state.hashCode();
    return mHandlerCache[Math.abs(hashcode % mHandlerCache.length)];
  }

  protected void handlersUpdated()
  {
    for (int i = 0; i < mHandlerIDs.length; i++)
      {
	mHandlerCache[i] = getHandler(mHandlerIDs[i]);
      }
  }

  private String[] mHandlerIDs;
  private StateHandler[] mHandlerCache;
}