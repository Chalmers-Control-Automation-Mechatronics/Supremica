//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   LessMarked
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntHashSet;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongProcedure;

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

  public void outputstats()
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
      tuple = tovisit.remove(tovisit.size() - 1);
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
