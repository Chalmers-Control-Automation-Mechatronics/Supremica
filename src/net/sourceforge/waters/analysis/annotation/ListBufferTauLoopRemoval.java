//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;


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
    mStack = new TIntArrayStack();
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
      mToBeMerged.add(merge.toArray());
    }
  }

  public void run() throws OverflowException
  {
    TIME -= System.currentTimeMillis();
    mTransitionRelation.removeTauSelfLoops();
    for (int s = 0; s < mTarjan.length; s++) {
      if (mTarjan[s] == 0) {
        tarjan(s);
      }
    }
    final TRPartition partition = new TRPartition(mToBeMerged, mTarjan.length);
    mTransitionRelation.merge(partition);
    mTransitionRelation.removeTauSelfLoops();
    TIME += System.currentTimeMillis();
  }
}
