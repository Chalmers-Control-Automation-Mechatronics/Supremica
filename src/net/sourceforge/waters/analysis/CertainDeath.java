package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;


public class CertainDeath
{
  private final TransitionRelation mTransitionRelation;
  private final boolean[] mReachable;
  
  public static int STATESREMOVED = 0;
  public static int TIME = 0;
  
  public static void clearStats()
  {
    STATESREMOVED = 0;
    TIME = 0;
  }
  
  public static String stats()
  {
    return "CERTAINDEATH: STATESREMOVED = " + STATESREMOVED +
            " TIME = " + TIME;
  }
  
  public CertainDeath(TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mReachable = new boolean[mTransitionRelation.numberOfStates()];
  }
  
  private void backtrack(int state)
  {
    TIntStack stack = new TIntStack();
    stack.push(state);
    while (stack.size() != 0) {
      state = stack.pop();
      if (mReachable[state]) {continue;}
      mReachable[state] = true;
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
        if (preds == null) {continue;}
        TIntIterator it = preds.iterator();
        while (it.hasNext()) {//mark all state which can reach this state as reachable
          int pred = it.next(); stack.push(pred);
        }
      }
    }
  }
  
  /*private void backtrack(int state)
  {
    TIntStack stack = new TIntStack
    if(mReachable[state]) {return;} //already done this state
    mReachable[state] = true;
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
      if (preds == null) {continue;}
      TIntIterator it = preds.iterator();
      while (it.hasNext()) {//mark all state which can reach this state as reachable
        int pred = it.next(); backtrack(pred);
      }
    }
  }*/
  
  public void run()
  {
    TIME -= System.currentTimeMillis();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (mTransitionRelation.isMarked(state)) {
        backtrack(state);
      }
    }
    for (int state = 0; state < mReachable.length; state++) {
      if (!mReachable[state]) {
        mTransitionRelation.removeAllIncoming(state); STATESREMOVED++;
      }
    }
    TIME += System.currentTimeMillis();
  }
}
