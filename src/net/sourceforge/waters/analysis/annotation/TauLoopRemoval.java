package net.sourceforge.waters.analysis.annotation;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.Collection;


public class TauLoopRemoval
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;
  private int mIndex;
  private final int[] mTarjan;
  private final int[] mLowLink;
  private final boolean[] mOnstack;
  private final TIntStack mStack;
  private final Collection<TIntHashSet> mToBeMerged;

  public static int STATESMERGED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESMERGED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "TauLoopRemoval: STATESMERGED = " + STATESMERGED +
            " TIME = " + TIME;
  }

  public TauLoopRemoval(final TransitionRelation transitionrelation, final int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
    mIndex = 1;
    mTarjan = new int[mTransitionRelation.numberOfStates()];
    mLowLink = new int[mTransitionRelation.numberOfStates()];
    mOnstack = new boolean[mTransitionRelation.numberOfStates()];
    mStack = new TIntArrayStack();
    mToBeMerged = new ArrayList<TIntHashSet>();
  }

  private void tarjan(final int state)
  {
    mTarjan[state] = mIndex;
    mLowLink[state] = mIndex;
    mIndex++;
    final TIntHashSet successors = mTransitionRelation.getSuccessors(state, mTau);
    if (successors == null) {return;}
    mOnstack[state] = true;
    mStack.push(state);
    final TIntIterator targets = successors.iterator();
    while (targets.hasNext()) {
      final int suc = targets.next();
      if(mOnstack[suc]) {
        mLowLink[state] = mTarjan[suc] < mLowLink[state] ? mTarjan[suc]
                                                         : mLowLink[state];
      } else if (mTarjan[suc] == 0) {
        tarjan(suc);
        mLowLink[state] = mLowLink[suc] < mLowLink[state] ? mLowLink[suc]
                                                          : mLowLink[state];
      }
      if (mTransitionRelation.isMarked(suc)) {
        mTransitionRelation.markState(state, true);
      }
    }
    if (mTarjan[state] == mLowLink[state]) {
      final TIntHashSet merge = new TIntHashSet();
      while (true) {
        final int pop = mStack.pop();
        merge.add(pop);
        mOnstack[pop] = false;
        if (pop == state) {
          break;
        }
      }
      if (merge.size() > 1) {
        mToBeMerged.add(merge);
      }
    }
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    mTransitionRelation.removeAllSelfLoops(mTau);
    for (int s = 0; s < mTarjan.length; s++) {
      if (mTarjan[s] == 0) {
        tarjan(s);
      }
    }
    for (final TIntHashSet merge : mToBeMerged) {
      STATESMERGED += merge.size() - 1;
      mTransitionRelation.mergewithannotations(merge.toArray());
    }
    mTransitionRelation.removeAllSelfLoops(mTau);
    mTransitionRelation.removeAllAnnotations(mTau);
    TIME += System.currentTimeMillis();
  }
}

