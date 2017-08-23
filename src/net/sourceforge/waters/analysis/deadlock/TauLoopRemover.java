//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.deadlock;

import java.util.ArrayList;
import java.util.Collection;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

public class TauLoopRemover
{
  private final GeneralizedTransitionRelation mTransitionRelation;
  private final int mTau;
  private int mIndex;
  private final int[] mTarjan;
  private final int[] mLowLink;
  private final boolean[] mOnstack;
  private final TIntStack mStack;
  private final Collection<TIntHashSet> mToBeMerged;

  public static int STATESMERGED = 0;
  public static int TIME = 0;

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
    mTransitionRelation.removeAllUnreachable();
    mTransitionRelation.removeUnnecessaryStates();
    TIME += System.currentTimeMillis();
  }

  //#########################################################################
  //# Auxiliary Methods

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

  public TauLoopRemover(final GeneralizedTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mTau = 0;
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

}
