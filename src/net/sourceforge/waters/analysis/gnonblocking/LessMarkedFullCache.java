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
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongLongHashMap;


/**
 * @author Simon Ware
 */

public class LessMarkedFullCache
{
  private LessMarkedCache mCache;
  private TLongHashSet mStack;
  private TLongHashSet allvisited;
  private int mRevisited;
  private int mTarjanIndex;
  private final TLongLongHashMap mHighLinkLowLink;
  private final TIntHashSet mMoreMarked;
  private final TIntHashSet mLessMarked;
  private final TIntObjectHashMap<TLongHashSet> mStronglyConnected;
  
  private int numaddedtostrongconnect = 0;
  private int numaddedtoseen = 0;
  
  // #######################################################################
  // # Constructor
  public LessMarkedFullCache(ListBufferTransitionRelation automaton,
                             int marking, TIntHashSet nonCoreachable)
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
    mStronglyConnected = new TIntObjectHashMap();
  }
  
  private void statkeeping(long tuple) {
    int first = getFirst(tuple);
    int second = getSecond(tuple);
    if (first < second) {
      if (!allvisited.add(longify(first, second))) {mRevisited++;}
    } else {
      if (!allvisited.add(longify(second, first))) {mRevisited++;}
    }
  }
  
  public void outputstats()
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
  }
  
  public int run(final int first, final int second)
  {
    mTarjanIndex = 0;
    mMoreMarked.clear();
    mLessMarked.clear();
    mHighLinkLowLink.clear();
    mStronglyConnected.clear();
    mStack.clear();
    long tuple = longify(first, second); 
    tarjan(tuple);
    //System.out.println(numaddedtoseen + " " + numaddedtostrongconnect);
    //mCache.checkDifference(allvisited);
    if (mCache.isMoreMarked(tuple)) {return first;}
    if (mCache.isLessMarked(tuple)) {return second;}
    return -1;
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
  
  private static long swap(long tuple)
  {
    return longify(getSecond(tuple), getFirst(tuple));
  }
  
  private static class LessMarkedCache
  {
    private TLongHashSet mSeen;
    private TLongHashSet mLessMarked;
    
    public LessMarkedCache()
    {
      mSeen = new TLongHashSet();
      mLessMarked = new TLongHashSet();
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
    
    public void checkDifference(TLongHashSet set)
    {
      System.out.println("Visited: " + set.size() + " Seen: " + mSeen.size());
    }
    
    public void lessMarked(TLongHashSet set)
    {
      mLessMarked.addAll(set.toArray());
    }
    
    public void makeSeen(TLongHashSet set)
    {
      mSeen.addAll(set.toArray());
    }
    
    public boolean isTupleSeen(long tuple)
    {
      return mSeen.contains(tuple) || mSeen.contains(swap(tuple));
    }
    
    public boolean isLessMarked(long tuple)
    {
      return mLessMarked.contains(tuple);
    }
    
    public boolean isMoreMarked(long tuple)
    {
      return mLessMarked.contains(swap(tuple));
    }
  }
  
  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mNonCoreachable;
  private final int mMarking;
}
