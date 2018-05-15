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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * @author Simon Ware
 */

public class LessMarked
{
  private final LessMarkedCache mCache;
  private final TLongHashSet allvisited;
  private int mRevisited;

  // #######################################################################
  // # Constructor
  public LessMarked(final ListBufferTransitionRelation automaton,
                    final int marking, final TIntHashSet nonCoreachable)
  {
    mAutomaton = automaton;
    mNonCoreachable = nonCoreachable;
    mMarking = marking;
    mCache = new LessMarkedCache();
    allvisited = new TLongHashSet();
    mRevisited = 0;
  }

  private void statkeeping(final long tuple) {
    final int first = getFirst(tuple);
    final int second = getSecond(tuple);
    if (first < second) {
      if (!allvisited.add(longify(first, second))) {mRevisited++;}
    } else {
      if (!allvisited.add(longify(second, first))) {mRevisited++;}
    }
    //if (mRevisited % 1000000 == 0) {outputstats();}
  }

  @SuppressWarnings("unused")
  private void outputstats()
  {
    System.out.println("Distinct States: " + allvisited.size());
    System.out.println("Revisits: " + mRevisited);
  }

  public int run(final int first, final int second)
  {
    final TLongHashSet visited = new TLongHashSet();
    final TLongArrayList tovisit = new TLongArrayList();
    long tuple = longify(first, second);
    visited.add(tuple);
    tovisit.add(tuple);
    boolean secgreater = true;
    boolean firgreater = true;
    while (!tovisit.isEmpty()) {
      tuple = tovisit.removeAt(tovisit.size() - 1);
      final int f = getFirst(tuple);
      final int s = getSecond(tuple);
      if (f == s) {System.out.println("the same"); continue;}
      final int cache = mCache.isMoreOrLessMarked(tuple);
      if (cache < 0) {
        //System.out.println("cache used");
        firgreater = false; continue;
      } else if (cache > 1) {
        //System.out.println("cache used");
        secgreater = false; continue;
      }
      if (mCache.isIncomparable(tuple)) {
        System.out.println("incomparable");
        //return -1;
      }
      statkeeping(tuple);
      //System.out.println("f: " + f + "s:" + s);
      final boolean fmarked = mAutomaton.isMarked(f, mMarking);
      final boolean smarked = mAutomaton.isMarked(s, mMarking);
      firgreater = firgreater && (fmarked || !smarked);
      secgreater = secgreater && (smarked || !fmarked);
      if (!firgreater && !secgreater) {
        return -1;
      }
      for (int e = 0; e < mAutomaton.getNumberOfProperEvents(); e++) {
        int ft = -1; int st = -1;
        final TransitionIterator fti = mAutomaton.createSuccessorsReadOnlyIterator(f, e);
        final TransitionIterator sti = mAutomaton.createSuccessorsReadOnlyIterator(s, e);
        if (fti.advance()) {
          ft = fti.getCurrentTargetState();
          ft = mNonCoreachable.contains(ft) ? -1 : ft;
        }
        if (sti.advance()) {
          st = sti.getCurrentTargetState();
          st = mNonCoreachable.contains(st) ? -1 : st;
        }
        firgreater = firgreater && (ft != -1 ||st == -1);
        secgreater = secgreater && (st != -1 ||ft == -1);
        if (!firgreater && !secgreater) {
          return -1;
        }
        if (ft == st) {continue;}//if they equal each other they will have the same successors
        if (ft != -1 && st != -1) {
          final long successor = longify(ft, st);
          if (visited.add(successor)) {
            tovisit.add(successor);
          }
        }
      }
    }
    if (!firgreater && !secgreater) {
      return -1;
    }
    if (firgreater) {
      mCache.moreMarked(visited);
      return second;
    }
    mCache.lessMarked(visited);
    return first;
  }

  private static long longify(final int firststate, final int secondstate)
  {
    long l = firststate;
    l <<= 32;
    final long l2 = secondstate;
    l |= l2;
    return l;
  }

  private static int getFirst(long tuple)
  {
    tuple >>= 32;
    return (int)tuple;
  }

  private static int getSecond(final long tuple)
  {
    return (int)tuple;
  }


  private static class LessMarkedCache
  {
    private final TLongHashSet mLessMarked;
    private final TLongHashSet mIncomparable;

    public LessMarkedCache()
    {
      mLessMarked = new TLongHashSet();
      mIncomparable = new TLongHashSet();
    }

    public void moreMarked(final TLongHashSet set)
    {
      set.forEach(new TLongProcedure() {
          @Override
          public boolean execute(final long tuple) {
            mLessMarked.add(longify(getSecond(tuple), getFirst(tuple)));
            return true;
          }
      });
    }

    public void lessMarked(final TLongHashSet set)
    {
      mLessMarked.addAll(set.toArray());
    }

    @SuppressWarnings("unused")
    public void incomparable(final long tuple)
    {
      mIncomparable.add(tuple);
      mIncomparable.add(longify(getSecond(tuple), getFirst(tuple)));
    }

    public boolean isIncomparable(final long tuple)
    {
      return mIncomparable.contains(tuple);
    }

    public int isMoreOrLessMarked(final long tuple)
    {
      if (mLessMarked.contains(tuple)) {return -1;}
      final long tuple2 = longify(getSecond(tuple), getFirst(tuple));
      if (mLessMarked.contains(tuple2)) {return 1;}
      return 0;
    }
  }

  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mNonCoreachable;
  private final int mMarking;
}
