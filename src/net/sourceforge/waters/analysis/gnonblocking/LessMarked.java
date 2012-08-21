//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AbstractionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.Determinizer;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import gnu.trove.TIntArrayList;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongArrayList;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import gnu.trove.TLongProcedure;
import gnu.trove.TLongIntHashMap;


/**
 * @author Simon Ware
 */

public class LessMarked
{
  private LessMarkedCache mCache;
  private TLongHashSet allvisited;
  private int mRevisited;
  
  // #######################################################################
  // # Constructor
  public LessMarked(ListBufferTransitionRelation automaton,
                    int marking, TIntHashSet nonCoreachable)
  {
    mAutomaton = automaton;
    mNonCoreachable = nonCoreachable;
    mMarking = marking;
    mCache = new LessMarkedCache();
    allvisited = new TLongHashSet();
    mRevisited = 0;
  }
  
  private void statkeeping(long tuple) {
    int first = getFirst(tuple);
    int second = getSecond(tuple);
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
    TLongHashSet visited = new TLongHashSet();
    TLongArrayList tovisit = new TLongArrayList();
    long tuple = longify(first, second);
    visited.add(tuple);
    tovisit.add(tuple);
    boolean secgreater = true;
    boolean firgreater = true;
    while (!tovisit.isEmpty()) {
      tuple = tovisit.remove(tovisit.size() - 1);
      int f = getFirst(tuple);
      int s = getSecond(tuple);
      if (f == s) {System.out.println("the same"); continue;}
      int cache = mCache.isMoreOrLessMarked(tuple);
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
      boolean fmarked = mAutomaton.isMarked(f, mMarking);
      boolean smarked = mAutomaton.isMarked(s, mMarking);
      firgreater = firgreater && (fmarked || !smarked);
      secgreater = secgreater && (smarked || !fmarked);
      if (!firgreater && !secgreater) {
        return -1;
      }
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
        firgreater = firgreater && (ft != -1 ||st == -1);
        secgreater = secgreater && (st != -1 ||ft == -1);
        if (!firgreater && !secgreater) {
          return -1;
        }
        if (ft == st) {continue;}//if they equal each other they will have the same successors
        if (ft != -1 && st != -1) {
          long successor = longify(ft, st);
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
  
  private static long longify(int firststate, int secondstate)
  {
    long l = firststate;
    l <<= 32;
    long l2 = secondstate;
    l |= l2;
    return l;
  }
  
  private static int getFirst(long tuple)
  {
    tuple >>= 32;
    return (int)tuple;
  }
  
  private static int getSecond(long tuple)
  {
    return (int)tuple;
  }
  
  private static class LessMarkedCache
  {
    private TLongHashSet mLessMarked;
    private TLongHashSet mIncomparable;
    
    public LessMarkedCache()
    {
      mLessMarked = new TLongHashSet();
      mIncomparable = new TLongHashSet();
    }
    
    public void moreMarked(TLongHashSet set)
    {
      set.forEach(new TLongProcedure() {
          public boolean execute(long tuple) {
            mLessMarked.add(longify(getSecond(tuple), getFirst(tuple)));
            return true;
          }
      });
    }
    
    public void lessMarked(TLongHashSet set)
    {
      mLessMarked.addAll(set.toArray());
    }
    
    public void incomparable(long tuple)
    {
      mIncomparable.add(tuple);
      mIncomparable.add(longify(getSecond(tuple), getFirst(tuple)));
    }
    
    public boolean isIncomparable(long tuple)
    {
      return mIncomparable.contains(tuple);
    }
    
    public int isMoreOrLessMarked(long tuple)
    {
      if (mLessMarked.contains(tuple)) {return -1;}
      long tuple2 = longify(getSecond(tuple), getFirst(tuple));
      if (mLessMarked.contains(tuple2)) {return 1;}
      return 0;
    }
  }
  
  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mNonCoreachable;
  private final int mMarking;
}
