//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.Set;


public class RedundantTransitions
{
  private final TransitionRelation mTransitionRelation;
  private final TLongIntHashMap mCache;

  public static int TRANSITIONSREMOVED = 0;
  public static int STATESREMOVED = 0;
  public static int TIME = 0;

  private static final TIntHashSet EMPTYSET = new TIntHashSet(0);

  private long key(int s1, final int s2)
  {
    long l = s1;
    s1 <<= 32;
    final long l2 = s2;
    l |= l2;
    return l;
  }

  public static void clearStats()
  {
    TRANSITIONSREMOVED = 0;
    TIME = 0;
    STATESREMOVED = 0;
  }


  public static String stats()
  {
    return "RedundantTransitions: TRANSITIONSREMOVED = " + TRANSITIONSREMOVED +
            " States Removed" + STATESREMOVED + " TIME = " + TIME;
  }


  public RedundantTransitions(final TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mCache = new TLongIntHashMap();
  }

  public boolean coversAnnotations(final int s1, final int s2)
  {
    final Set<TIntHashSet> ann1 = mTransitionRelation.getAnnotations2(s1);
    final Set<TIntHashSet> ann2 = mTransitionRelation.getAnnotations2(s2);
    for (final TIntHashSet a1 : ann1) {
      final int[] array = a1.toArray();
      boolean covered = false;
      for (final TIntHashSet a2 : ann2) {
        if (a2.containsAll(array)) {
          covered = true;
          break;
        }
      }
      if (!covered) {return false;}
    }
    return true;
  }

  public boolean coversOutGoing(final int s1, final int s2, final int state, final int event)
  {
    if (s2 == state) {
      final TIntHashSet out1 = mTransitionRelation.getSuccessors(s1, event);
      if (out1 != null) {
        if (out1.contains(s1)) {return false;}
      }
    }
    if (mTransitionRelation.isMarked(s1) && !mTransitionRelation.isMarked(s2)){
      return false;
    }
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      TIntHashSet out1 = mTransitionRelation.getSuccessors(s1, e);
      TIntHashSet out2 = mTransitionRelation.getSuccessors(s2, e);
      out1 = out1 == null ? EMPTYSET : out1;
      out2 = out2 == null ? EMPTYSET : out2;
      /*if (out1.contains(s1) && out2.contains(s2)) {
        out1.remove(s1);
      }*/
      if (!out2.containsAll(out1.toArray())) {
        return false;
      }
    }
    return true;
  }

  public int redundantState(final int s1, final int s2, final int state, final int event)
  {
    //TODO this has significance to do with follow on equivalence
    /*int res1 = mCache.get(key(s1, s2));
    int res2 = mCache.get(key(s2, s1));
    if (res1 == -1 && res2 == -1) { return -1;}
    if (res1 == 1) {return s1;}
    if (res2 == 1) {return s2;}*/
    if (coversAnnotations(s1, s2) && coversOutGoing(s1, s2, state, event)) {
      mCache.put(key(s1, s2), 1);
      mCache.put(key(s2, s1), -1);
      return s1;
    } else if (coversAnnotations(s2, s1) && coversOutGoing(s2, s1, state, event)) {
      mCache.put(key(s2, s1), 1);
      mCache.put(key(s1, s2), -1);
      return s2;
    }
    mCache.put(key(s1, s2), -1);
    mCache.put(key(s2, s1), -1);
    return -1;
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    System.out.println("start");
    STATESREMOVED -= mTransitionRelation.unreachableStates();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (!mTransitionRelation.hasPredecessors(state)) {continue;}
      for (int event = 0; event < mTransitionRelation.numberOfEvents(); event++) {
        //System.out.println("(s,e): (" + state + ", " + event + ")");
        final TIntHashSet setsuccs = mTransitionRelation.getSuccessors(state, event);
        if (setsuccs == null) {continue;}
        final int[] sucs = setsuccs.toArray();
        final TIntHashSet toberemoved = new TIntHashSet();
        for (int i = 0; i < sucs.length; i++) {
          final int suc1 = sucs[i];
          for (int j = i + 1; j < sucs.length; j++) {
            final int suc2 = sucs[j];
            final int torem = redundantState(suc1, suc2, state, event);
            if (torem != -1) {toberemoved.add(torem);}
          }
        }
        //System.out.println("toberemmed: " + toberemoved.size());
        final int[] array = toberemoved.toArray();
        for (int i = 0; i < array.length; i++) {
          mTransitionRelation.removeTransition(state, event, array[i]);
          TRANSITIONSREMOVED++;
          /*System.out.println();
          System.out.println("redundant transition");
          System.out.println();*/
        }
      }
    }
    System.out.println("TRANSITIONSREMOVED: " + TRANSITIONSREMOVED);
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
