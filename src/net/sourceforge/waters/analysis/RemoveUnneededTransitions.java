package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.Collection;
import gnu.trove.TIntArrayList;
import java.util.Arrays;

public class RemoveUnneededTransitions
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;
  public static int TIME = 0;
  
  public static void clearStats()
  {
    TIME = 0;
  }
  
  public static String stats()
  {
    return "TIME = " + TIME;
  }
  
  public RemoveUnneededTransitions(TransitionRelation transitionrelation, int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
  }
  
  private void removeTransitions(int state)
  {
    TIntHashSet targets = mTransitionRelation.getSuccessors(state, mTau);
    if (targets == null) {return;}
    int[] targs = targets.toArray();
    for (int i = 0; i < targs.length; i++) {
      int target = targs[i];
      mTransitionRelation.removeSharedSuccessors(target, state);
    }
  }
  
  public void run()
  {
    TIME -= System.currentTimeMillis();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      removeTransitions(s);
    }
    TIME += System.currentTimeMillis();
  }
}
