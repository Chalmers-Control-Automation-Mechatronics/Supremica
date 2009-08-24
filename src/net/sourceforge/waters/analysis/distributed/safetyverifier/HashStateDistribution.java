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
    return hashLookupStateHandler(hashcode);
  }

  protected StateHandler hashLookupStateHandler(int hash)
  {
    return mHandlerCache[Math.abs(rehash(hash) % mHandlerCache.length)];
  }

  protected int getStateHandlerCount()
  {
    return mHandlerCache.length;
  }

  /**
   * A supplemental hash function. This will hopefully increase the
   * quality of hash codes. This technique is borrowed from Java HashMap 
   * class.
   */
  private static int rehash(int h)
  {
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
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