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

package net.sourceforge.waters.analysis.deadlock;

import java.util.Set;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;


/**
 * @author Hani al-Bahri, Simon Ware
 */

public class Annotator
{
  private final GeneralizedTransitionRelation mTransitionRelation;

  public static int ANNOTATIONS_ADDED = 0;
  public static int ANNOTATIONS_REMOVED_SUBSET = 0;
  public static int STATES_REMOVED = 0;
  public static int STATES_TAUS_REMOVED_FROM = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    ANNOTATIONS_ADDED = 0;
    ANNOTATIONS_REMOVED_SUBSET = 0;
    STATES_REMOVED = 0;
    STATES_TAUS_REMOVED_FROM = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "ANNOTATEGRAPH: ANNOTATIONSADDED = " + ANNOTATIONS_ADDED +
            " ANNOTATIONSREMOVEDSUBSET = " + ANNOTATIONS_REMOVED_SUBSET +
            " STATESREMOVED = " + STATES_REMOVED +
            " STATESTAUSREMOVEDFROM = " + STATES_TAUS_REMOVED_FROM +
            " TIME = " + TIME;
  }

  public Annotator(final GeneralizedTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
   // mTransitionRelation.removeAllSelfLoops(mTau);
   // mTransitionRelation.removeAllAnnotations(mTau);
    STATES_REMOVED -= mTransitionRelation.unreachableStates();
   // final TIntHashSet tausremoved = new TIntHashSet();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
     /* final TIntHashSet taus = mTransitionRelation.getSuccessors(s, mTau);
      if (taus == null || taus.isEmpty()) {
        continue;
      }*/
     // final TIntIterator it = taus.iterator();
      ANNOTATIONS_REMOVED_SUBSET += mTransitionRelation.getAnnotations2(s).size();
      final Set<TIntHashSet> anns = new THashSet<TIntHashSet>(mTransitionRelation.getAnnotations2(s));
      /*while (it.hasNext()) {
        ANNOTATIONS_ADDED++;
        final int target = it.next();
       // tausremoved.add(target);
        TIntHashSet ae = mTransitionRelation.getActiveEvents(target);
        if (ae == null) {
          System.out.println("null ae");
          ae = new TIntHashSet();
        }

        mTransitionRelation.addAllSuccessors(target, s);
        ANNOTATIONS_REMOVED_SUBSET += mTransitionRelation.getAnnotations2(target).size();
        anns = TransitionRelation.subsets(mTransitionRelation.getAnnotations2(target),
                                           anns);
      }*/

      ANNOTATIONS_REMOVED_SUBSET -= anns.size();
      if (!anns.isEmpty()) {
        mTransitionRelation.setAnnotation(s, anns);
      }
    }

    STATES_REMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }

}
