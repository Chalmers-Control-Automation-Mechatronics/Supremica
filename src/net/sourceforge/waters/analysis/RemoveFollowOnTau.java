package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;


public class RemoveFollowOnTau
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;
  private final boolean[] mVisited;
  public static int mTausRemoved = 0;
  public static int mStatesRemoved = 0;
  public static int TIME = 0;
  
  public static void clearStats()
  {
    mTausRemoved = 0;
    mStatesRemoved = 0;
    TIME = 0;
  }
  
  public static String stats()
  {
    return "RemoveFollowOnTau: mTausRemoved = " + mTausRemoved + " mStatesRemoved = " + mStatesRemoved +
            " TIME = " + TIME;
  }
  
  public RemoveFollowOnTau(TransitionRelation transitionrelation, int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
    mVisited = new boolean[mTransitionRelation.numberOfStates()];
  }
  
  private void removeFollowons(int state)
  {
    mVisited[state] = true;
    TIntHashSet targets = mTransitionRelation.getSuccessors(state, mTau);
    int[] targs = targets.toArray();
    for (int i = 0; i < targs.length; i++) {
      int target = targs[i];
      TIntHashSet targettaus = mTransitionRelation.getSuccessors(target, mTau);
      if (targettaus == null || targettaus.isEmpty()) {
        //continue;
      } else if (target == state) {
        continue;
      } else {
        removeFollowons(target);
        mTausRemoved++;
        mTransitionRelation.removeTransition(state, mTau, target);
        mTransitionRelation.addAllSuccessors(target, state);
      }
    }
  }
  
  /*private void removeFollowons(int state)
  {
    mVisited[state] = true;
    TIntHashSet targets = mTransitionRelation.getSuccessors(state, mTau);
    int[] targs = targets.toArray();
    for (int i = 0; i < targs.length; i++) {
      int target = targs[i];
      TIntHashSet targettaus = mTransitionRelation.getSuccessors(target, mTau);
      if (targettaus == null || targettaus.isEmpty()) {
        continue;
      } else if (target == state) {
        continue;
      }
      //if (!mVisited[target]) {
        removeFollowons(target);
      //}
      mTausRemoved++;
      mTransitionRelation.removeTransition(state, mTau, target);
      mTransitionRelation.addAllSuccessors(target, state);
    }
  }*/
  
  public void run()
  {
    TIME -= System.currentTimeMillis();
    mStatesRemoved -= mTransitionRelation.unreachableStates();
    for (int s = 0; s < mVisited.length; s++) {
      TIntHashSet targets = mTransitionRelation.getSuccessors(s, mTau);
      if (mVisited[s] || targets == null) {
        continue;
      }
      removeFollowons(s);
    }
    mStatesRemoved += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
