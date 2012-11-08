package net.sourceforge.waters.analysis.annotation;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntStack;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


public class ListBufferTauLoopRemoval
{
  private final ListBufferTransitionRelation mTransitionRelation;
  private final int mTau;
  private int mIndex;
  private final int[] mTarjan;
  private final int[] mLowLink;
  private final boolean[] mOnstack;
  private final TIntStack mStack;
  private final List<int[]> mToBeMerged;

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

  public ListBufferTauLoopRemoval(final ListBufferTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mTau = EventEncoding.TAU;
    mIndex = 1;
    mTarjan = new int[mTransitionRelation.getNumberOfStates()];
    mLowLink = new int[mTransitionRelation.getNumberOfStates()];
    mOnstack = new boolean[mTransitionRelation.getNumberOfStates()];
    mStack = new TIntStack();
    mToBeMerged = new ArrayList<int[]>();
  }

  private void tarjan(final int state)
  {
    mTarjan[state] = mIndex;
    mLowLink[state] = mIndex;
    mIndex++;
    mOnstack[state] = true;
    mStack.push(state);
    final TransitionIterator targets = mTransitionRelation.createSuccessorsReadOnlyIterator(state, mTau);
    while (targets.advance()) {
      final int suc = targets.getCurrentTargetState();
      if(mOnstack[suc]) {
        mLowLink[state] = mTarjan[suc] < mLowLink[state] ? mTarjan[suc]
                                                         : mLowLink[state];
      } else if (mTarjan[suc] == 0) {
        tarjan(suc);
        mLowLink[state] = mLowLink[suc] < mLowLink[state] ? mLowLink[suc]
                                                          : mLowLink[state];
      }
    }
    if (mTarjan[state] == mLowLink[state]) {
      final TIntArrayList merge = new TIntArrayList();
      while (true) {
        final int pop = mStack.pop();
        merge.add(pop);
        mOnstack[pop] = false;
        if (pop == state) {
          break;
        }
      }
      mToBeMerged.add(merge.toNativeArray());
    }
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    mTransitionRelation.removeTauSelfLoops();
    for (int s = 0; s < mTarjan.length; s++) {
      if (mTarjan[s] == 0) {
        tarjan(s);
      }
    }
    mTransitionRelation.merge(mToBeMerged);
    mTransitionRelation.removeTauSelfLoops();
    TIME += System.currentTimeMillis();
  }
}
