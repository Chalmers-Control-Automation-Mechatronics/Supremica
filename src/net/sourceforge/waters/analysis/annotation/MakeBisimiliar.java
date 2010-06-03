package net.sourceforge.waters.analysis.annotation;

import java.util.Map;
import net.sourceforge.waters.analysis.TransitionRelation;
import gnu.trove.TLongHashSet;
import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import java.util.Set;


public class MakeBisimiliar
{
  private final TransitionRelation mTrans;
  private final int[] mInitial;
  private final int[] mMarked;
  
  public static int SC = 0;
  public static int AE = 0;
  public static int OSO = 0;
  public static int OSI = 0;
  public static int TIME = 0;
  
  public static void clearStats()
  {
    SC = 0;
    AE = 0;
    OSO = 0;
    OSI = 0; 
  }
  
  public static String stats()
  {
    return "SC = " + SC +
           "\nAE = " + AE +
           "\nOSO = " + OSO +
           "\nOSI = " + OSI +
           "\nTIME = " + TIME;
  }
  
  public MakeBisimiliar(TransitionRelation tr)
  {
    mTrans = tr;
    TIntArrayList init = new TIntArrayList();
    TIntArrayList mark = new TIntArrayList();
    for (int s = 0; s < mTrans.numberOfStates(); s++) {
      if (mTrans.isInitial(s)) {init.add(s);}
      if (mTrans.isMarked(s)) {mark.add(s);}
    }
    mInitial = init.toNativeArray();
    mMarked = mark.toNativeArray();
  }
  
  public long mergeIntoLong(int state, int event)
  {
    long merge = state;
    long ev = event;
    merge <<= 32;
    merge |= event;
    return merge;
  }
  
  public int getStateFromLong(long merge)
  {
    merge >>= 32;
    return (int) merge;
  }
  
  public int getEventFromLong(long merge)
  {
    merge <<= 32;
    merge >>= 32;
    return (int) merge;
  }
  
  private Tuple getSuccessors(int s)
  {
    TLongHashSet musthavesuccs = new TLongHashSet();
    TIntHashSet mustSelf = new TIntHashSet();
    for (int e = 0; e < mTrans.numberOfEvents(); e++) {
      if (mTrans.isMarkingEvent(e)) {
        if (mTrans.isMarked(s)) {
          musthavesuccs.add(mergeIntoLong(-1, e));
        }
        continue;
      }
      TIntHashSet succs = mTrans.getSuccessors(s, e);
      if (succs == null) {continue;}
      int[] arrsuccs = succs.toArray();
      for (int i = 0; i < arrsuccs.length; i++) {
        int succ = arrsuccs[i];
        if (s != succ) {
          musthavesuccs.add(mergeIntoLong(succ, e));
        } else {
          mustSelf.add(e);
        }
      }
    }
    if (musthavesuccs.isEmpty()) {return null;}
    Set<TIntHashSet> uncoveredAnns = null;
    TLongHashSet optionalsuccs = null;
    TIntHashSet optionalSelf = null;
    for (int e = 0; e < mTrans.numberOfEvents() + 1; e++) {
      int[] predsarr = null;
      if (e != mTrans.numberOfEvents()) {
        TIntHashSet preds = mTrans.getPredecessors(s, e);
        if (preds == null || preds.isEmpty()) {continue;}
        predsarr = preds.toArray();
      } else {
        if (!mTrans.isInitial(s)) {continue;}
        predsarr = new int[1];
        predsarr[0] = -1;
      }
      for (int i = 0; i < predsarr.length; i++) {
        TLongHashSet tsuccs = new TLongHashSet();
        TIntHashSet tself = new TIntHashSet();
        Set<TIntHashSet> uncoveredAnns2 = new THashSet<TIntHashSet>(mTrans.getAnnotations2(s));
        if (uncoveredAnns != null) {uncoveredAnns2.removeAll(uncoveredAnns);}
        int[] arrsuccs;
        if (predsarr[0] != -1) {
          int pred = predsarr[i];
          arrsuccs = mTrans.getSuccessors(pred, e).toArray();
        } else {
          arrsuccs = mInitial;
        }
        for (int j = 0; j < arrsuccs.length; j++) {
          int succ = arrsuccs[j];
          if (succ == s) {continue;}
          Iterator<TIntHashSet> it = uncoveredAnns2.iterator();
          while (it.hasNext()) {
            TIntHashSet ann = it.next();
            Iterator<TIntHashSet> it2 = mTrans.getAnnotations2(succ).iterator();
            while (it2.hasNext()) {
              TIntHashSet ann2 = it2.next();
              if (ann.containsAll(ann2.toArray())) {it.remove();}
            }
          }
          for (int e2 = 0; e2 < mTrans.numberOfEvents(); e2++) {
            if (mTrans.isMarkingEvent(e2)) {
              if (mTrans.isMarked(succ)) {
                tsuccs.add(mergeIntoLong(-1, e2));
              }
              continue;
            }
            TIntHashSet succs2 = mTrans.getSuccessors(succ, e2);
            if (succs2 == null) {continue;}
            int[] arrsuccs2 = succs2.toArray();
            for (int k = 0; k < arrsuccs2.length; k++) {
              int succ2 = arrsuccs2[k];
              if (succ2 != s) {tsuccs.add(mergeIntoLong(succ2, e2));}
              else {tself.add(e2);}
            }
          }
        }
        if (optionalsuccs == null) {
          optionalsuccs = tsuccs;
          optionalSelf = tself;
        } else {
          optionalsuccs.retainAll(tsuccs.toArray());
          optionalSelf.retainAll(tself.toArray());
        }
        if (uncoveredAnns == null) {uncoveredAnns = uncoveredAnns2;}
        else {uncoveredAnns.addAll(uncoveredAnns2);}
        //if (tsuccs.isEmpty()) {return succs.toArray();}
      }
    }
    if (optionalsuccs == null) {
      optionalsuccs = new TLongHashSet();
      optionalSelf = new TIntHashSet();
    }
    if (uncoveredAnns == null) {uncoveredAnns = mTrans.getAnnotations2(s);}
    musthavesuccs.removeAll(optionalsuccs.toArray());
    mustSelf.removeAll(optionalSelf.toArray());
    //if (musthavesuccs.isEmpty()) {System.out.println("empty musthave: uncovered anns:" + uncoveredAnns.size() + " optional:" + optionalsuccs.size());
    //System.out.println(musthavesuccssize);}
    optionalsuccs.addAll(musthavesuccs.toArray());
    optionalSelf.addAll(mustSelf.toArray());
    int[] optionalSelfarr = optionalSelf.toArray();
    for (int i = 0; i < optionalSelfarr.length; i++) {
      int ev = optionalSelfarr[i];
      optionalsuccs.add(mergeIntoLong(s, ev));
    }
    Tuple tup = new Tuple(musthavesuccs, optionalsuccs, uncoveredAnns, mustSelf, optionalSelf);
    return tup;
  }
  
  public boolean annotationsCovered(int s, Set<TIntHashSet> annotations)
  {
    for (int e = 0; e < mTrans.numberOfEvents() + 1; e++) {
      int[] predsarr = null;
      if (e != mTrans.numberOfEvents()) {
        TIntHashSet preds = mTrans.getPredecessors(s, e);
        if (preds == null || preds.isEmpty()) {continue;}
        predsarr = preds.toArray();
      } else {
        if (!mTrans.isInitial(s)) {continue;}
        predsarr = new int[1];
        predsarr[0] = -1;
      }
      for (int i = 0; i < predsarr.length; i++) {
        Set<TIntHashSet> uncoveredAnns = new THashSet<TIntHashSet>(annotations);
        int[] arrsuccs;
        if (predsarr[0] != -1) {
          int pred = predsarr[i];
          arrsuccs = mTrans.getSuccessors(pred, e).toArray();
        } else {
          arrsuccs = mInitial;
        }
        for (int j = 0; j < arrsuccs.length; j++) {
          int succ = arrsuccs[j];
          Iterator<TIntHashSet> it = uncoveredAnns.iterator();
          while (it.hasNext()) {
            TIntHashSet ann = it.next();
            Iterator<TIntHashSet> it2 = mTrans.getAnnotations2(succ).iterator();
            while (it2.hasNext()) {
              TIntHashSet ann2 = it2.next();
              if (ann.containsAll(ann2.toArray())) {it.remove();}
            }
          }
        }
        if (!uncoveredAnns.isEmpty()) {return false;}
        //if (tsuccs.isEmpty()) {return succs.toArray();}
      }
    }
    return true;
  }
  
  public boolean canbemadebisimiliar(int state)
  {
    Tuple tup = getSuccessors(state);
    if (tup == null) {return false;}
    if (tup.mMust.isEmpty() && tup.mMustSelfLoops.isEmpty() && tup.mUncoveredAnns.isEmpty()) {
      mTrans.removeAllIncoming(state); mTrans.removeAllOutgoing(state); return true;
    }
    if (tup.mMust.isEmpty() && tup.mUncoveredAnns.isEmpty()) {
      //mTrans.removeAllIncoming(state); mTrans.removeAllOutgoing(state); return true;
      System.out.println("look into selfs");
    }
    //System.out.println(candidates.length);
    Cands:
    for (int cand = 0; cand < mTrans.numberOfStates(); cand++) {
      //int cand = candidates[i];
      if (cand == state) {continue;}
      Tuple tup2 = getSuccessors(cand);
      if (tup2 == null) {continue;}
      if (!(tup.mOptional.containsAll(tup2.mMust.toArray()) &&
          tup2.mOptional.containsAll(tup.mMust.toArray()))) {continue;}
      int[] mustself = tup.mMustSelfLoops.toArray();
      for (int i = 0; i < mustself.length; i++) {
        int must = mustself[i];
        if (tup2.mOptionalSelfLoops.contains(must)) {continue;}
        if (tup2.mOptional.contains(mergeIntoLong(state, must))) {continue;}
        continue Cands;
      }
      mustself = tup2.mMustSelfLoops.toArray();
      for (int i = 0; i < mustself.length; i++) {
        int must = mustself[i];
        if (tup.mOptionalSelfLoops.contains(must)) {continue;}
        if (tup.mOptional.contains(mergeIntoLong(state, must))) {continue;}
        continue Cands;
      }
      System.out.println("trans");
      if (annotationsCovered(state, tup2.mUncoveredAnns) &&
          annotationsCovered(cand, tup.mUncoveredAnns)) {
        System.out.println("covered");
        mTrans.setAnnotation(state, tup.mUncoveredAnns);
        mTrans.setAnnotation(cand, tup2.mUncoveredAnns);
        mTrans.mergewithannotations(new int[]{state, cand});
        tup = getSuccessors(state);
      }
    }
    //System.out.println(state + ", OSI");
    //OSI++;
    return true;
  }
  
  public void run()
  {
    TIME -= System.currentTimeMillis();
    for (int s = 0; s < mTrans.numberOfStates(); s++) {
      if (!mTrans.hasPredecessors(s)) {continue;}
      canbemadebisimiliar(s);
    }
    TIME += System.currentTimeMillis();
  }
  
  private class Tuple
  {
    final TLongHashSet mMust;
    final TLongHashSet mOptional;
    final Set<TIntHashSet> mUncoveredAnns;
    final TIntHashSet mMustSelfLoops;
    final TIntHashSet mOptionalSelfLoops;
    
    public Tuple(TLongHashSet must, TLongHashSet optional,
                 Set<TIntHashSet> uncoveredAnns,
                 TIntHashSet mustSelf, TIntHashSet optionalSelf)
    {
      mMust = must;
      mOptional = optional;
      mUncoveredAnns = uncoveredAnns;
      mMustSelfLoops = mustSelf;
      mOptionalSelfLoops = optionalSelf;
    }
  }
}
