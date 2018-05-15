//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.iterator.TIntIterator;


public class RemoveSubsetTau
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;

  public static int ANNOTATIONSADDED = 0;
  public static int ANNOTATIONSREMOVEDSUBSET = 0;
  public static int STATESREMOVED = 0;
  public static int STATESTAUSREMOVEDFROM = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    ANNOTATIONSADDED = 0;
    ANNOTATIONSREMOVEDSUBSET = 0;
    STATESREMOVED = 0;
    STATESTAUSREMOVEDFROM = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "REMOVE SUBSET TAU: ANNOTATIONSADDED = " + ANNOTATIONSADDED +
            " ANNOTATIONSREMOVEDSUBSET = " + ANNOTATIONSREMOVEDSUBSET +
            " STATESREMOVED = " + STATESREMOVED +
            " STATESTAUSREMOVEDFROM = " + STATESTAUSREMOVEDFROM +
            " TIME = " + TIME;
  }

  public RemoveSubsetTau(TransitionRelation transitionrelation, int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    STATESREMOVED -= mTransitionRelation.unreachableStates();
    TIntArrayList stilltau = new TIntArrayList();
    TIntHashSet tausremoved = new TIntHashSet();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      TIntHashSet taus = mTransitionRelation.getSuccessors(s, mTau);
      if (taus == null || taus.isEmpty()) {
        continue;
      }
      //System.out.println("taus: + " + Arrays.toString(taus.toArray()));
      TIntIterator it = taus.iterator();
      stilltau.clear();
      while (it.hasNext()) {
        ANNOTATIONSADDED++;
        int target = it.next();
        TIntHashSet ae = mTransitionRelation.getActiveEvents(target);
        if (ae == null) {
          System.out.println("null ae");
          ae = new TIntHashSet();
        }
        mTransitionRelation.addAllSuccessors(target, s);
        boolean added = true;
        boolean subset = false;
        for (int j = 0; j < stilltau.size(); j++) {
          int othertau = stilltau.get(j);
          TIntHashSet ae2 = mTransitionRelation.getActiveEvents(othertau);
          if (ae2.size() < ae.size()) {
            if (subset) {
              continue;
            }
            if (ae.containsAll(ae2.toArray())) {
              ANNOTATIONSREMOVEDSUBSET++;
              added = false;
              break;
            }
          } else {
            if (ae2.containsAll(ae.toArray())) {
              ANNOTATIONSREMOVEDSUBSET++;
              stilltau.remove(j);
              subset = true;
            }
          }
        }
        if (added) {
          stilltau.add(target);
        }
      }
      taus = new TIntHashSet(taus.toArray());
      for (int j = 0; j < stilltau.size(); j++) {
        taus.remove(stilltau.get(j));
      }
      int[] arr = taus.toArray();
      for (int i = 0; i < arr.length; i++) {
        int t = arr[i];
        mTransitionRelation.removeTransition(s, mTau, t);
        tausremoved.add(t);
      }
      stilltau.clear();
    }
    STATESTAUSREMOVEDFROM += tausremoved.size();
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
