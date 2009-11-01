package net.sourceforge.waters.analysis.distributed.safetyverifier;

import gnu.trove.THashMap;
import java.util.ArrayList;
import java.util.List;

public class BasicStateStorage implements StateStorage
{
  public BasicStateStorage()
  {
    mStateList = new ArrayList<StateTuple>();
    mObservedSet = new THashMap<StateTuple,StateTuple>();
  }

  public synchronized void addState(StateTuple state)
  {
    if (!mObservedSet.containsKey(state))
      {
	mObservedSet.put(state, state);
	mStateList.add(state);
      }
  }
  
  public synchronized StateTuple getNextState()
  {
    return mStateList.get(mCurrentStateIndex++);
  }
  
  public synchronized int getUnprocessedStateCount()
  {
    return mStateList.size() - mCurrentStateIndex;
  }

  public synchronized int getStateDepth(StateTuple state)
  {
    StateTuple t = mObservedSet.get(state);
    if (t == null)
      return -1;
    else
      return t.getDepthHint();
  }

  public synchronized int getStateCount()
  {
    return mObservedSet.size();
  }

  public synchronized int getProcessedStateCount()
  {
    return mCurrentStateIndex;
  }

  public boolean containsState(StateTuple state)
  {
    return mObservedSet.containsKey(state);
  }

  private final THashMap<StateTuple,StateTuple> mObservedSet;
  private final List<StateTuple> mStateList;
  private volatile int mCurrentStateIndex = 0;
}