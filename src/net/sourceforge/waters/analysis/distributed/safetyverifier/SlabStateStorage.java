package net.sourceforge.waters.analysis.distributed.safetyverifier;

import gnu.trove.TIntArrayList;

public class SlabStateStorage implements StateStorage
{
  /**
   * Creates a state storage. The memory allocator used needs
   * to know how long each state is.
   * @param stateLength Number of 32 bit words each state tuple
   * encodes into.
   */
  public SlabStateStorage(int stateLength)
  {
    mStateLength = stateLength;
    mStateSet = new StateTupleSet(mStateLength, INITIAL_CAPACITY);
    mStateList = new TIntArrayList(INITIAL_CAPACITY);
  }

  public synchronized void addState(StateTuple state)
  {
    int p = mStateSet.add(state);
    if (p >= 0)
      {
	//The state was newly added to the set. Also add
	//it to the state queue.
	mStateList.add(p);
      }
  }

  public synchronized StateTuple getNextState()
  {
    int p = mStateList.get(mCurrentStateIndex++);
    return mStateSet.get(p);
  }

  public synchronized int getUnprocessedStateCount()
  {
    return mStateList.size() - mCurrentStateIndex;
  }

  public synchronized int getStateDepth(StateTuple state)
  {
    int p = mStateSet.lookup(state);
    if (p < 0)
      return -1;
    else
      {
	return mStateSet.getDepth(p);
      }
  }

  public synchronized int getStateCount()
  {
    return mStateSet.size();
  }

  public synchronized int getProcessedStateCount()
  {
    return mCurrentStateIndex;
  }

  public boolean containsState(StateTuple state)
  {
    return mStateSet.contains(state);
  }

  private final int mStateLength;
  private final StateTupleSet mStateSet;
  private final TIntArrayList mStateList;
  private volatile int mCurrentStateIndex = 0;

  private static final int INITIAL_CAPACITY = 100000;
}
