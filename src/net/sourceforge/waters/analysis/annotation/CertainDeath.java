//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;


public class CertainDeath
{
  private final TransitionRelation mTransitionRelation;
  private final boolean[] mReachable;

  public static int STATESREMOVED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESREMOVED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "CERTAINDEATH: STATESREMOVED = " + STATESREMOVED +
            " TIME = " + TIME;
  }

  public CertainDeath(final TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mReachable = new boolean[mTransitionRelation.numberOfStates()];
  }

  private void backtrack(int state)
  {
    final TIntStack stack = new TIntArrayStack();
    stack.push(state);
    while (stack.size() != 0) {
      state = stack.pop();
      if (mReachable[state]) {continue;}
      mReachable[state] = true;
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
        if (preds == null) {continue;}
        final TIntIterator it = preds.iterator();
        while (it.hasNext()) {//mark all state which can reach this state as reachable
          final int pred = it.next(); stack.push(pred);
        }
      }
    }
  }

  /*private void backtrack(int state)
  {
    TIntStack stack = new TIntStack
    if(mReachable[state]) {return;} //already done this state
    mReachable[state] = true;
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
      if (preds == null) {continue;}
      TIntIterator it = preds.iterator();
      while (it.hasNext()) {//mark all state which can reach this state as reachable
        int pred = it.next(); backtrack(pred);
      }
    }
  }*/

  public void run()
  {
    TIME -= System.currentTimeMillis();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (mTransitionRelation.isMarked(state)) {
        backtrack(state);
      }
    }
    for (int state = 0; state < mReachable.length; state++) {
      if (!mReachable[state]) {
        mTransitionRelation.removeAllIncoming(state); STATESREMOVED++;
        System.out.println("removed:" + STATESREMOVED);
      }
    }
    TIME += System.currentTimeMillis();
  }
}
