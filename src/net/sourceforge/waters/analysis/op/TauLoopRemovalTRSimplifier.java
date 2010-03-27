package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.Collection;


public class TauLoopRemovalTRSimplifier
  implements TransitionRelationSimplifier
{

  //#########################################################################
  //# Constructor
  public TauLoopRemovalTRSimplifier
    (final ObserverProjectionTransitionRelation transitionrelation,
     final int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  public ObserverProjectionTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setTransitionRelation
    (final ObserverProjectionTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  public boolean run()
  {
    setUp();
    boolean modified = mTransitionRelation.removeAllSelfLoops(mTau);
    for (int s = 0; s < mTarjan.length; s++) {
      if (mTarjan[s] == 0) {
        tarjan(s);
      }
    }
    if (modified || !mToBeMerged.isEmpty()) {
      final int numStates = mTransitionRelation.getNumberOfStates();
      mClassMap = new TIntObjectHashMap<int[]>(numStates);
      for (final TIntArrayList merge : mToBeMerged) {
        final int[] clazz = merge.toNativeArray();
        mTransitionRelation.merge(clazz, mTau);
        final int code = clazz[0];
        mClassMap.put(code, clazz);
        modified |= clazz.length > 1;
      }
      if (modified) {
        for (int s = 0; s < numStates; s++) {
          if (mTransitionRelation.hasPredecessors(s) &&
              !mClassMap.containsKey(s)) {
            final int[] clazz = new int[1];
            clazz[0] = s;
            mClassMap.put(s, clazz);
          }
        }
      } else {
        mClassMap = null;
      }
    }
    return modified;
  }

  public TIntObjectHashMap<int[]> getStateClasses()
  {
    return mClassMap;
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
    final TIntHashSet successors =
      mTransitionRelation.getSuccessors(state, mTau);
    if (successors == null) {
      return;
    }
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
  private ObserverProjectionTransitionRelation mTransitionRelation;
  private final int mTau;
  private TIntObjectHashMap<int[]> mClassMap;

  private int mIndex;
  private int[] mTarjan;
  private int[] mLowLink;
  private boolean[] mOnstack;
  private TIntStack mStack;
  private Collection<TIntArrayList> mToBeMerged;

}
