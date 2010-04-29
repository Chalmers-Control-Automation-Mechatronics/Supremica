package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;


public class TauLoopRemovalTRSimplifier
  implements TransitionRelationSimplifier
{

  //#########################################################################
  //# Constructor
  public TauLoopRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  public boolean run()
  {
    setUp();
    boolean modified = false;
    for (int s = 0; s < mTarjan.length; s++) {
      if (mTarjan[s] == 0) {
        tarjan(s);
      }
    }
    if (modified || !mToBeMerged.isEmpty()) {
      final int numStates = mTransitionRelation.getNumberOfStates();
      mResultPartition = new ArrayList<int[]>(numStates);
      final BitSet merged = new BitSet(numStates);
      for (final TIntArrayList merge : mToBeMerged) {
        final int[] array = merge.toNativeArray();
        mResultPartition.add(array);
        modified |= array.length > 1;
        for (final int s : array) {
          merged.set(s);
        }
      }
      if (modified) {
        for (int s = 0; s < numStates; s++) {
          if (mTransitionRelation.isReachable(s) && !merged.get(s)) {
            final int[] array = new int[1];
            array[0] = s;
            mResultPartition.add(array);
          }
        }
      } else {
        mResultPartition = null;
      }
    }
    return modified;
  }

  public List<int[]> getResultPartition()
  {
    return mResultPartition;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUp()
  {
    final int numStates = mTransitionRelation.getNumberOfStates();
    mIndex = 1;
    mTarjan = new int[numStates];
    mLowLink = new int[numStates];
    mOnstack = new boolean[numStates];
    mStack = new TIntStack();
    mToBeMerged = new ArrayList<TIntArrayList>();
  }

  private void tarjan(final int state)
  {
    mTarjan[state] = mIndex;
    mLowLink[state] = mIndex;
    mIndex++;
    mOnstack[state] = true;
    mStack.push(state);
    final TransitionIterator iter =
      mTransitionRelation.createAnyIterator(state, EventEncoding.TAU);
    while (iter.advance()) {
      final int suc = iter.getCurrentToState();
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
      if (merge.size() > 1) {
        mToBeMerged.add(merge);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private ListBufferTransitionRelation mTransitionRelation;
  private List<int[]> mResultPartition;

  private int mIndex;
  private int[] mTarjan;
  private int[] mLowLink;
  private boolean[] mOnstack;
  private TIntStack mStack;
  private Collection<TIntArrayList> mToBeMerged;

}
