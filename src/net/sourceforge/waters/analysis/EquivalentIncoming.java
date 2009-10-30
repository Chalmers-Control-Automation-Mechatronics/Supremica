package net.sourceforge.waters.analysis;

import gnu.trove.TIntStack;


public class EquivalentIncoming
{
  private final TransitionRelation mTransitionRelation;
  public static int STATESMERGED = 0;
  public static int ANNOTIONSSUBSET = 0;
  public static int TIME = 0;
  
  public static void clearStats()
  {
    STATESMERGED = 0;
    ANNOTIONSSUBSET = 0;
    TIME = 0;
  }
  
  public static String stats()
  {
    return "EquivalentIncoming: STATESMERGED = " + STATESMERGED + " ANNOTIONSSUBSET = " + ANNOTIONSSUBSET +
            " TIME = " + TIME;
  }
  
  public EquivalentIncoming(TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
  }
  
  public void run()
  {
    TIME -= System.currentTimeMillis();
    boolean[] onstack = new boolean[mTransitionRelation.numberOfStates()];
    TIntStack stack = new TIntStack(mTransitionRelation.numberOfStates());
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (mTransitionRelation.hasPredecessors(s)) {
        stack.push(s); onstack[s] = true;
      }
    }
    while(stack.size() != 0) {
      int state = stack.pop(); onstack[state] = false;
      if (!mTransitionRelation.hasPredecessors(state)) {continue;}
      for (int other = 0; other < mTransitionRelation.numberOfStates(); other++) {
        if (other == state) {continue;}
        if (!mTransitionRelation.equivalentIncoming(other, state)) {continue;}
        ANNOTIONSSUBSET += mTransitionRelation.getAnnotation(state) == null ?
                            1 : mTransitionRelation.getAnnotation(state).size();
        ANNOTIONSSUBSET += mTransitionRelation.getAnnotation(other) == null ?
                            1 : mTransitionRelation.getAnnotation(other).size();
        mTransitionRelation.mergewithannotations(new int[] {state, other});
        STATESMERGED++;
        ANNOTIONSSUBSET -= mTransitionRelation.getAnnotation(state).size();
      }
    }
    TIME += System.currentTimeMillis();
  }
}
