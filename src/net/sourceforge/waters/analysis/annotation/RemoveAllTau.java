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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.set.hash.TIntHashSet;


public class RemoveAllTau
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

  public RemoveAllTau(TransitionRelation transitionrelation, int tau)
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
      if (target == state) {
        continue;
      } else {
        TIntHashSet tsuc = mTransitionRelation.getSuccessors(target, mTau);
        if (tsuc != null) {removeFollowons(target);}
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
    mTransitionRelation.removeEvent(mTau);
    mStatesRemoved += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
