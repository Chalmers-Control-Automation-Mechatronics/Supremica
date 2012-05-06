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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * @author Simon Ware
 */

public class LessMarked
{
  // #######################################################################
  // # Constructor
  public LessMarked(final ListBufferTransitionRelation automaton,
                    final int marking, final TIntHashSet nonCoreachable)
  {
    mAutomaton = automaton;
    mNonCoreachable = nonCoreachable;
    mMarking = marking;
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
    return firgreater ? second : first;
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

  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mNonCoreachable;
  private final int mMarking;
}
