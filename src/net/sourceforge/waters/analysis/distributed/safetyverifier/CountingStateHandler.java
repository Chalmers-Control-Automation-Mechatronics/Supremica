package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A state handler that counts the number of states handled
 * in a threadsafe manner. The counter is incremented
 * after the addState method of the actual state handler
 * returns, if an exception is thrown the counter will not
 * be incremented.
 * @author Sam Douglas
 */
class CountingStateHandler implements StateHandler
{
  public CountingStateHandler(StateHandler handler)
  {
    mHandler = handler;
  }

  public void addState(StateTuple state) throws Exception
  {
    mHandler.addState(state);
    mStateCount.incrementAndGet();
  }

  /**
   * Gets the count of states succesfully handled.
   * @return count of states handled
   */
  public int getCount()
  {
    return mStateCount.get();
  }

  private final AtomicInteger mStateCount = new AtomicInteger();
  private final StateHandler mHandler;

  private static final long serialVersionUID = 1L;
}