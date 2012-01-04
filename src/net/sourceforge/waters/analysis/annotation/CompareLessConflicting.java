package net.sourceforge.waters.analysis.annotation;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import java.util.Set;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import java.util.Map;
import gnu.trove.TObjectIntHashMap;
import java.util.List;
import gnu.trove.TIntArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import gnu.trove.THashSet;
import java.util.Collections;
import net.sourceforge.waters.analysis.gnonblocking.FindBlockingStates;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import java.util.Arrays;
import java.lang.RuntimeException;


public class CompareLessConflicting
{
  private final ListBufferTransitionRelation mFirstRelation;
  private final ListBufferTransitionRelation mSecondRelation;
  private final Map<TIntHashSet, TIntHashSet> mSetCache;
  private final TObjectIntHashMap<Tuple> mTupleCache;
  private final List<Tuple> mStates;
  //private final TObjectIntHashMap<Triple> mTripleCache;
  //private final List<Triple> mMCStates;
  private final List<TIntArrayList> mSuccessors;
  private final List<TIntHashSet[]> mPredeccessors;
  private final TIntHashSet mFirstLC;
  private final TIntHashSet mSecondBlocking;
  //private final TIntHashSet mLessConflicting;
  //private final TIntHashSet mNotLessConflicting;
  private final int mMarking;
  private int mExpanded;
  
  public CompareLessConflicting(final ListBufferTransitionRelation first,
                                final ListBufferTransitionRelation second,
                                final int marking)
  {
    mFirstRelation = first;
    mSecondRelation = second;
    mSetCache = new HashMap<TIntHashSet, TIntHashSet>();
    mTupleCache = new TObjectIntHashMap<Tuple>();
    mStates = new ArrayList<Tuple>();
    mFirstLC = new TIntHashSet();
    mExpanded = 0;
    mMarking = marking;
    FindBlockingStates fbs = new FindBlockingStates(second, mMarking);
    mSecondBlocking = fbs.getBlockingStates();
    fbs = new FindBlockingStates(first, mMarking);
    System.out.println("block: " + Arrays.toString(fbs.getBlockingStates().toArray()));
    mSuccessors = new ArrayList<TIntArrayList>();
    mPredeccessors = new ArrayList<TIntHashSet[]>();
  }
  
  public TIntHashSet calculateTauReachable(int state, ListBufferTransitionRelation trans)
  {
    TIntHashSet set = new TIntHashSet();
    set.add(state);
    return calculateTauReachable(set, trans);
  }
  
  public TIntHashSet calculateTauReachable(TIntHashSet set,
                                           ListBufferTransitionRelation trans)
  {
    TIntHashSet taureach = new TIntHashSet(set.toArray());
    TIntArrayList togo = new TIntArrayList(set.toArray());
    while (!togo.isEmpty()) {
      int state = togo.remove(togo.size() - 1);
      TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(state,
                                                                     EventEncoding.TAU);
      while (ti.advance()) {
        if (taureach.add(ti.getCurrentTargetState())) {
          togo.add(ti.getCurrentTargetState());
        }
      }
    }
   
    return taureach;
  }
  
  public TIntHashSet calculateSuccessor(TIntHashSet set, int event,
                                        ListBufferTransitionRelation trans)
  {
    set = calculateTauReachable(set, trans);// this shouldn't be needed
    TIntHashSet succ = new TIntHashSet();
    TIntIterator it = set.iterator();
    while (it.hasNext()) {
      int s = it.next();
      if (s == -1) {return null;}
      if (event != trans.getNumberOfProperEvents()) {
        TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(s, event);
        while (ti.advance()) {
          succ.add(ti.getCurrentTargetState());
        }
      } else {
        if (trans.isMarked(s, mMarking)) {
          succ.add(-1);
          return succ;
        }
      }
    }
    return calculateTauReachable(succ, trans);
  }
  
  private int getState(Tuple tup)
  {
    if (!mTupleCache.containsKey(tup)) {
      int state = mStates.size();
      mTupleCache.put(tup, state);
      mStates.add(tup);
      int[] sucs = new int[mFirstRelation.getNumberOfProperEvents() + 1];
      for (int i = 0; i < sucs.length; i++) {
        sucs[i] = -1;
      }
      mSuccessors.add(new TIntArrayList(sucs));
      mPredeccessors.add(new TIntHashSet[mFirstRelation.getNumberOfProperEvents() + 1]);
      if (tup.firstset.contains(-1)) {
        mFirstLC.add(state);
      }
      TIntIterator it = tup.firstset.iterator();
      while (it.hasNext()) {
        if (mSecondBlocking.contains(it.next())) {
          mFirstLC.add(state); continue;
        }
      }
    }
    int state = mTupleCache.get(tup);
    return state;
  }
  
  public void expandStates()
  {
    for (;mExpanded < mStates.size(); mExpanded++) {
      int state = mExpanded;
      Tuple tup = mStates.get(state);
      if (tup.firstset.contains(-1) || tup.secondset.contains(-1)) {
        continue;
      }
      //System.out.println(tup);
      TIntIterator it = tup.firstset.iterator();
      while (it.hasNext()) {
        TIntHashSet f = new TIntHashSet();
        f.add(it.next());
        getState(new Tuple(f, tup.secondset));
      }
      for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
        if (e == EventEncoding.TAU) {continue;}
        TIntHashSet first = calculateSuccessor(tup.firstset, e, mFirstRelation);
        TIntHashSet second = calculateSuccessor(tup.secondset, e, mSecondRelation);
        int target = getState(new Tuple(first, second));
        mSuccessors.get(state).set(e, target);
        TIntHashSet preds = mPredeccessors.get(target)[e];
        if (preds == null) {
          preds = new TIntHashSet();
          TIntHashSet[] predsarr = mPredeccessors.get(target);
          predsarr[e] = preds;
        }
        preds.add(state);
      }
      TIntHashSet first = calculateSuccessor(tup.firstset, mMarking, mFirstRelation);
      TIntHashSet second = calculateSuccessor(tup.secondset, mMarking, mSecondRelation);
      int target = getState(new Tuple(first, second));
      mSuccessors.get(state).set(mMarking, target);
    }
  }
  
  public void calculateLCStates()
  {
    boolean modified = true;
    int LC = 0;
    while (modified) {
      System.out.println("LC: " + LC++ + " " + mFirstLC.size());
      modified = false;
      TIntArrayList makelc = new TIntArrayList();
      Set<Triple> MCTriples = new THashSet<Triple>();
      List<Triple> tobeexpanded = new ArrayList<Triple>();
      for (int s = 0; s < mStates.size(); s++) {
        //System.out.println(mStates.get(s));
        if (!mFirstLC.contains(s)) {
          makelc.add(s);
          Tuple state = mStates.get(s);
          TIntHashSet moreset = state.secondset;
          if (moreset.contains(-1)) {
            //System.out.println("MC:" + Arrays.toString(state.firstset.toArray()) + " : " + Arrays.toString(moreset.toArray()));
            Triple triple = new Triple(mStates.get(s), -1);
            MCTriples.add(triple);
            tobeexpanded.add(triple);
          }
        }
      }
      while (!tobeexpanded.isEmpty()) {
        System.out.println(MCTriples.size());
        Triple triple = tobeexpanded.remove(tobeexpanded.size() - 1);
        for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
          if (e == EventEncoding.TAU) {continue;}
          TIntHashSet preds = mPredeccessors.get(mTupleCache.get(triple.tuple))[e];
          if (preds == null) {continue;}
          TIntIterator it = preds.iterator();
          while (it.hasNext()) {
            int pred = it.next();
            if (mFirstLC.contains(pred)) {continue;}
            Tuple predtuple = mStates.get(pred);
            TIntHashSet moreset = predtuple.secondset;
            TIntIterator itstates = moreset.iterator();
            while (itstates.hasNext()) {
              int state = itstates.next();
              TIntHashSet newset = new TIntHashSet(); newset.add(state);
              TIntHashSet statesuccessors = calculateSuccessor(newset, e, mSecondRelation);
              if (statesuccessors.contains(triple.state)) {
                //System.out.println("MC:" + Arrays.toString(predtuple.firstset.toArray()) + " : " + Arrays.toString(moreset.toArray()) + ":" + state);
                Triple add = new Triple(mStates.get(pred), state);
                if (MCTriples.add(add)) {tobeexpanded.add(add);}
              }
            }
          }
        }
      }
      LCPAIRS:
      for (int i = 0; i < makelc.size(); i++) {
        int state = makelc.get(i);
        Tuple tup = mStates.get(state);
        TIntIterator it2 = tup.secondset.iterator();
        while (it2.hasNext()) {
          int propstate = it2.next();
          Triple triple = new Triple(tup, propstate);
          if (!MCTriples.contains(triple)) {
            mFirstLC.add(state);
            modified = true;
          }
        }
      }
    }
  }
  
  public boolean isLessConflicting()
  {
    TIntHashSet first = new TIntHashSet();
    TIntHashSet second = new TIntHashSet();
    for (int s = 0; s < mFirstRelation.getNumberOfStates(); s++) {
      if (mFirstRelation.isInitial(s)) {
        first.add(s);
        continue;
      }
    }
    for (int s = 0; s < mSecondRelation.getNumberOfStates(); s++) {
      if (mSecondRelation.isInitial(s)) {
        second.add(s);
        continue;
      }
    }
    return isLessConflicting(new Tuple(calculateTauReachable(first, mFirstRelation),
                                       calculateTauReachable(second, mSecondRelation)));
  }
  
  public boolean isLessConflicting(Tuple tuple)
  {
    int initial = getState(tuple);
    // adds the certain conflict states to the calculation
    getState(new Tuple(new TIntHashSet(), tuple.secondset));
    expandStates();
    System.out.println("tuples: " + mStates.size());
    calculateLCStates();
    //System.out.println("LC:" + mFirstLC.size());
    TIntHashSet explored = new TIntHashSet();
    TIntArrayList toexplore = new TIntArrayList();
    explored.add(initial);
    toexplore.add(initial);
    while (!toexplore.isEmpty()) {
      //System.out.println(mStates.size());
      int s = toexplore.remove(toexplore.size() -1);
      Tuple state = mStates.get(s);
      if (state.firstset.isEmpty()) {continue;}
      if (state.firstset.size() > 1) {
        TIntIterator it = state.firstset.iterator();
        while (it.hasNext()) {
          TIntHashSet set = new TIntHashSet();
          set.add(it.next());
          if (explored.add(getState(new Tuple(set, state.secondset)))) {
            toexplore.add(getState(new Tuple(set, state.secondset)));
          }
        }
        continue;
      }
      //calculateLCStates();
      ////System.out.println("LC:" + mFirstLC.size());
      //System.out.println(state);
      if (mFirstLC.contains(getState(new Tuple(new TIntHashSet(), state.secondset)))) {continue;}
      if (!mFirstLC.contains(s)) {
        TIntArrayList states = new TIntArrayList();
        TIntHashSet visited2 = new TIntHashSet();
        visited2.add(s);
        states.add(s);
        while (!states.isEmpty()) {
          int snum = states.remove(0);
          state = mStates.get(snum);
          System.out.println("tuple: " + state);
          for (int e = 0; e < mSuccessors.get(snum).size(); e++) {
            if (e == EventEncoding.TAU) {continue;}
            if (mSuccessors.get(snum).get(e) != -1) {
              Tuple target = mStates.get(mSuccessors.get(snum).get(e));
              int tnum = mSuccessors.get(snum).get(e);
              System.out.println(state + " " + mFirstLC.contains(snum) + " -" + e + "-> " + target + " " + mFirstLC.contains(tnum));
              if (visited2.add(tnum) && !mFirstLC.contains(tnum)) {
                states.add(tnum);
              }
            }
          }
        }
        return false;
      }
      TIntArrayList succs = mSuccessors.get(s);
      for (int e = 0; e < succs.size(); e++) {
        if (e == EventEncoding.TAU) {continue;}
        int suc = succs.get(e);
        if (suc == -1) {continue;}
        if (explored.add(suc)) {
          //System.out.println(state + " -" + e + "-> " + mStates.get(suc));
          toexplore.add(suc);
        }
      }
    }
    return true;
  }
  
  public TIntHashSet getSet(TIntHashSet set)
  {
    TIntHashSet tset = mSetCache.get(set);
    if (tset == null) {
      tset = set;
      mSetCache.put(set, set);
    }
    return tset;
  }
  
  private abstract class View
  {
    abstract TIntHashSet getMoreSet(Tuple t);
    abstract TIntHashSet getLessSet(Tuple t);
    abstract ListBufferTransitionRelation getMoreRelation();
    abstract ListBufferTransitionRelation getLessRelation();
    abstract Tuple createTuple(TIntHashSet less, TIntHashSet more);
  }
  
  private class Triple
  {
    public final Tuple tuple;
    public final int state;
    
    public Triple(Tuple ptuple, int s)
    {
      tuple = ptuple;
      state = s; 
    }
    
    public int hashCode()
    {
      return tuple.hashCode() * 13 + state;
    }
    
    public boolean equals(Object o)
    {
      Triple other = (Triple)o;
      return state == other.state && tuple.equals(other.tuple); 
    }
  }
  
  private class Tuple
  {
    public final TIntHashSet firstset;
    public final TIntHashSet secondset;
    
    public Tuple(TIntHashSet first, TIntHashSet second)
    {
      firstset = getSet(first);
      secondset = getSet(second);
      if (firstset == null || secondset == null) {
        throw new RuntimeException();
      }
    }
    
    public Tuple(TIntHashSet first, TIntHashSet second, boolean alreadycanon)
    {
      firstset = first;
      secondset = second;
    }
    
    public int hashCode()
    {
      return firstset.hashCode() * 13 + secondset.hashCode();
    }
    
    public boolean equals(Object o)
    {
      Tuple other = (Tuple)o;
      return firstset == other.firstset && secondset == other.secondset; 
    }
    
    public String toString()
    {
      return Arrays.toString(firstset.toArray()) + " : " + Arrays.toString(secondset.toArray());
    }
  }
}
