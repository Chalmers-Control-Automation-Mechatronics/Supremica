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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.procedure.TLongProcedure;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * @author Simon Ware
 */

public class LessMarkedFullCache
{
  private final LessMarkedCache mCache;
  private final TLongHashSet mStack;
  private final TLongHashSet allvisited;
  private int mRevisited;
  private int mTarjanIndex;
  private final TLongLongHashMap mHighLinkLowLink;
  private final TIntHashSet mMoreMarked;
  private final TIntHashSet mLessMarked;
  private final TIntObjectHashMap<TLongHashSet> mStronglyConnected;

  // #######################################################################
  // # Constructor
  public LessMarkedFullCache(final ListBufferTransitionRelation automaton,
                             final int marking, final TIntHashSet nonCoreachable)
  {
    mAutomaton = automaton;
    mNonCoreachable = nonCoreachable;
    mMarking = marking;
    mCache = new LessMarkedCache();
    allvisited = new TLongHashSet();
    mStack = new TLongHashSet();
    mRevisited = 0;
    mTarjanIndex = 0;
    mMoreMarked = new TIntHashSet();
    mLessMarked = new TIntHashSet();
    mHighLinkLowLink = new TLongLongHashMap();
    mStronglyConnected = new TIntObjectHashMap<TLongHashSet>();
  }

  private void statkeeping(final long tuple) {
    final int first = getFirst(tuple);
    final int second = getSecond(tuple);
    if (first < second) {
      if (!allvisited.add(longify(first, second))) {mRevisited++;}
    } else {
      if (!allvisited.add(longify(second, first))) {mRevisited++;}
    }
  }

  @SuppressWarnings("unused")
  private void outputstats()
  {
    System.out.println("Distinct States: " + allvisited.size());
    System.out.println("Revisits: " + mRevisited);
  }

  /*private int tarjaniterative(final long origtuple)
  {
    TLongArrayList stack = new TLongArrayList();
    TLongArrayList.add(tuple);
    for (int i = 0; i < stack.size(); i++) {
      long tuple = stack.get(i);
      mHighLinkLowLink.put(tuple, longify(mTarjanIndex, mTarjanIndex));
      mTarjanIndex++;
      if (mCache.isTupleSeen(tuple)) {
        //System.out.println("was seen");
        continue;
        if (mCache.isMoreMarked(tuple)) {mMoreMarked.add(mTarjanIndex - 1);}
        if (mCache.isLessMarked(tuple)) {mLessMarked.add(mTarjanIndex - 1);}
      }
      mStack.add(tuple);
      int f = getFirst(tuple);
      int s = getSecond(tuple);
      boolean fmarked = mAutomaton.isMarked(f, mMarking);
      boolean smarked = mAutomaton.isMarked(s, mMarking);
      boolean firgreater = (fmarked || !smarked);
      boolean secgreater = (smarked || !fmarked);
      //if (f == s) {return mTarjanIndex - 1;}
      statkeeping(tuple);
      for (int e = 0; e < mAutomaton.getNumberOfProperEvents(); e++) {
        int ft = -1; int st = -1;
        TransitionIterator fti = mAutomaton.createSuccessorsReadOnlyIterator(f, e);
        TransitionIterator sti = mAutomaton.createSuccessorsReadOnlyIterator(s, e);
        if (fti.advance()) {
          ft = fti.getCurrentTargetState();
          ft = mNonCoreachable.contains(ft) ? -1 : ft;
        }
        if (sti.advance()) {
          st = sti.getCurrentTargetState();
          st = mNonCoreachable.contains(st) ? -1 : st;
        }
        if (ft == st) {continue;}//if they equal each other they will have the same successors
        firgreater = firgreater && (ft != -1 || st == -1);
        secgreater = secgreater && (st != -1 || ft == -1);
        if (ft != -1 && st != -1) {
          long successor = longify(ft, st);
          long highlow = mHighLinkLowLink.get(tuple);
          int high = getFirst(highlow);
          int lowlink = getSecond(highlow);
          int suclow = -1;
          if (mHighLinkLowLink.contains(successor)) {
            if (!mStack.contains(successor)) {continue;}
            suclow = getFirst(mHighLinkLowLink.get(successor));
          } else {
            suclow = tarjan(successor);
            firgreater = firgreater && mLessMarked.contains(suclow);
            secgreater = secgreater && mMoreMarked.contains(suclow);
          }
          lowlink = suclow < lowlink ? suclow : lowlink;
          mHighLinkLowLink.put(tuple, longify(high, lowlink));
        }
      }
    }
    int highlink = getFirst(mHighLinkLowLink.get(tuple));
    int lowlink = getSecond(mHighLinkLowLink.get(tuple));
    if (firgreater) {
      mLessMarked.add(lowlink);
    } else {
      mLessMarked.remove(lowlink);
    }
    if (secgreater) {
      mMoreMarked.add(lowlink);
    } else {
      mMoreMarked.remove(lowlink);
    }
    TLongHashSet strongconnect = mStronglyConnected.get(lowlink);
    if (strongconnect == null) {
      strongconnect = new TLongHashSet();
      mStronglyConnected.put(lowlink, strongconnect);
    }
    strongconnect.add(tuple);
    numaddedtostrongconnect++;
    //System.out.println("lowlink: " + lowlink + " highlink: " + highlink);
    if (lowlink == highlink) {
      mCache.makeSeen(strongconnect);
      numaddedtoseen += strongconnect.size();
      if (firgreater) {mCache.lessMarked(strongconnect);}
      if (secgreater) {mCache.moreMarked(strongconnect);}
    }
    mStack.remove(tuple);
    return lowlink;
  }*/

  private int tarjan(final long tuple)
  {
    mHighLinkLowLink.put(tuple, longify(mTarjanIndex, mTarjanIndex));
    mTarjanIndex++;
    if (mCache.isTupleSeen(tuple)) {
      //System.out.println("was seen");
      if (mCache.isMoreMarked(tuple)) {mMoreMarked.add(mTarjanIndex - 1);}
      if (mCache.isLessMarked(tuple)) {mLessMarked.add(mTarjanIndex - 1);}
      return mTarjanIndex - 1;
    }
    mStack.add(tuple);
    final int f = getFirst(tuple);
    final int s = getSecond(tuple);
    final boolean fmarked = mAutomaton.isMarked(f, mMarking);
    final boolean smarked = mAutomaton.isMarked(s, mMarking);
    boolean firgreater = (fmarked || !smarked);
    boolean secgreater = (smarked || !fmarked);
    //if (f == s) {return mTarjanIndex - 1;}
    statkeeping(tuple);
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
      if (ft == st) {continue;}//if they equal each other they will have the same successors
      firgreater = firgreater && (ft != -1 || st == -1);
      secgreater = secgreater && (st != -1 || ft == -1);
      if (ft != -1 && st != -1) {
        final long successor = longify(ft, st);
        final long highlow = mHighLinkLowLink.get(tuple);
        final int high = getFirst(highlow);
        int lowlink = getSecond(highlow);
        int suclow = -1;
        if (mHighLinkLowLink.contains(successor)) {
          if (!mStack.contains(successor)) {continue;}
          suclow = getFirst(mHighLinkLowLink.get(successor));
        } else {
          suclow = tarjan(successor);
          firgreater = firgreater && mLessMarked.contains(suclow);
          secgreater = secgreater && mMoreMarked.contains(suclow);
        }
        lowlink = suclow < lowlink ? suclow : lowlink;
        mHighLinkLowLink.put(tuple, longify(high, lowlink));
      }
    }
    final int highlink = getFirst(mHighLinkLowLink.get(tuple));
    final int lowlink = getSecond(mHighLinkLowLink.get(tuple));
    if (firgreater) {
      mLessMarked.add(lowlink);
    } else {
      mLessMarked.remove(lowlink);
    }
    if (secgreater) {
      mMoreMarked.add(lowlink);
    } else {
      mMoreMarked.remove(lowlink);
    }
    TLongHashSet strongconnect = mStronglyConnected.get(lowlink);
    if (strongconnect == null) {
      strongconnect = new TLongHashSet();
      mStronglyConnected.put(lowlink, strongconnect);
    }
    strongconnect.add(tuple);
    //System.out.println("lowlink: " + lowlink + " highlink: " + highlink);
    if (lowlink == highlink) {
      mCache.makeSeen(strongconnect);
      if (firgreater) {mCache.lessMarked(strongconnect);}
      if (secgreater) {mCache.moreMarked(strongconnect);}
    }
    mStack.remove(tuple);
    return lowlink;
  }

  public int run(final int first, final int second)
  {
    mTarjanIndex = 0;
    mMoreMarked.clear();
    mLessMarked.clear();
    mHighLinkLowLink.clear();
    mStronglyConnected.clear();
    mStack.clear();
    final long tuple = longify(first, second);
    tarjan(tuple);
    //System.out.println(numaddedtoseen + " " + numaddedtostrongconnect);
    //mCache.checkDifference(allvisited);
    if (mCache.isMoreMarked(tuple)) {return first;}
    if (mCache.isLessMarked(tuple)) {return second;}
    return -1;
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

  private static long swap(final long tuple)
  {
    return longify(getSecond(tuple), getFirst(tuple));
  }

  private static class LessMarkedCache
  {
    private final TLongHashSet mSeen;
    private final TLongHashSet mLessMarked;

    public LessMarkedCache()
    {
      mSeen = new TLongHashSet();
      mLessMarked = new TLongHashSet();
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

    public void makeSeen(final TLongHashSet set)
    {
      mSeen.addAll(set.toArray());
    }

    public boolean isTupleSeen(final long tuple)
    {
      return mSeen.contains(tuple) || mSeen.contains(swap(tuple));
    }

    public boolean isLessMarked(final long tuple)
    {
      return mLessMarked.contains(tuple);
    }

    public boolean isMoreMarked(final long tuple)
    {
      return mLessMarked.contains(swap(tuple));
    }
  }

  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mNonCoreachable;
  private final int mMarking;
}









