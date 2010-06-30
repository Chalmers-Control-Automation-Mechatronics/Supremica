package net.sourceforge.waters.analysis.annotation;

import gnu.trove.TIntHashSet;
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
    Arrays.sort(targs);
    for (int i = 0; i < targs.length; i++) {
      int target = targs[i];
      if (target == state) {continue;}
      mTransitionRelation.removeSharedSuccessors(target, state);
      mTransitionRelation.removeSharedPredeccessors(state, target);
      mTransitionRelation.addTransition(state, mTau, target);
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
