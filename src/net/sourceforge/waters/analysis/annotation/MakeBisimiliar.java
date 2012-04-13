package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongHashSet;

import java.util.Iterator;
import java.util.Set;



public class MakeBisimiliar
{
  private final TransitionRelation mTrans;
  private final int[] mInitial;

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

  public MakeBisimiliar(final TransitionRelation tr)
  {
    mTrans = tr;
    final TIntArrayList init = new TIntArrayList();
    final TIntArrayList mark = new TIntArrayList();
    for (int s = 0; s < mTrans.numberOfStates(); s++) {
      if (mTrans.isInitial(s)) {init.add(s);}
      if (mTrans.isMarked(s)) {mark.add(s);}
    }
    mInitial = init.toNativeArray();
    mark.toNativeArray();
  }

  public long mergeIntoLong(final int state, final int event)
  {
    long merge = state;
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

  private Tuple getSuccessors(final int s)
  {
    final TLongHashSet musthavesuccs = new TLongHashSet();
    final TIntHashSet mustSelf = new TIntHashSet();
    for (int e = 0; e < mTrans.numberOfEvents(); e++) {
      if (mTrans.isMarkingEvent(e)) {
        if (mTrans.isMarked(s)) {
          musthavesuccs.add(mergeIntoLong(-1, e));
        }
        continue;
      }
      final TIntHashSet succs = mTrans.getSuccessors(s, e);
      if (succs == null) {continue;}
      final int[] arrsuccs = succs.toArray();
      for (int i = 0; i < arrsuccs.length; i++) {
        final int succ = arrsuccs[i];
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
        final TIntHashSet preds = mTrans.getPredecessors(s, e);
        if (preds == null || preds.isEmpty()) {continue;}
        predsarr = preds.toArray();
      } else {
        if (!mTrans.isInitial(s)) {continue;}
        predsarr = new int[1];
        predsarr[0] = -1;
      }
      for (int i = 0; i < predsarr.length; i++) {
        final TLongHashSet tsuccs = new TLongHashSet();
        final TIntHashSet tself = new TIntHashSet();
        final Set<TIntHashSet> uncoveredAnns2 = new THashSet<TIntHashSet>(mTrans.getAnnotations2(s));
        if (uncoveredAnns != null) {uncoveredAnns2.removeAll(uncoveredAnns);}
        int[] arrsuccs;
        if (predsarr[0] != -1) {
          final int pred = predsarr[i];
          arrsuccs = mTrans.getSuccessors(pred, e).toArray();
        } else {
          arrsuccs = mInitial;
        }
        for (int j = 0; j < arrsuccs.length; j++) {
          final int succ = arrsuccs[j];
          if (succ == s) {continue;}
          final Iterator<TIntHashSet> it = uncoveredAnns2.iterator();
          while (it.hasNext()) {
            final TIntHashSet ann = it.next();
            final Iterator<TIntHashSet> it2 = mTrans.getAnnotations2(succ).iterator();
            while (it2.hasNext()) {
              final TIntHashSet ann2 = it2.next();
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
            final TIntHashSet succs2 = mTrans.getSuccessors(succ, e2);
            if (succs2 == null) {continue;}
            final int[] arrsuccs2 = succs2.toArray();
            for (int k = 0; k < arrsuccs2.length; k++) {
              final int succ2 = arrsuccs2[k];
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
    final int[] optionalSelfarr = optionalSelf.toArray();
    for (int i = 0; i < optionalSelfarr.length; i++) {
      final int ev = optionalSelfarr[i];
      optionalsuccs.add(mergeIntoLong(s, ev));
    }
    final Tuple tup = new Tuple(musthavesuccs, optionalsuccs, uncoveredAnns, mustSelf, optionalSelf);
    return tup;
  }

  public boolean annotationsCovered(final int s, final Set<TIntHashSet> annotations)
  {
    for (int e = 0; e < mTrans.numberOfEvents() + 1; e++) {
      int[] predsarr = null;
      if (e != mTrans.numberOfEvents()) {
        final TIntHashSet preds = mTrans.getPredecessors(s, e);
        if (preds == null || preds.isEmpty()) {continue;}
        predsarr = preds.toArray();
      } else {
        if (!mTrans.isInitial(s)) {continue;}
        predsarr = new int[1];
        predsarr[0] = -1;
      }
      for (int i = 0; i < predsarr.length; i++) {
        final Set<TIntHashSet> uncoveredAnns = new THashSet<TIntHashSet>(annotations);
        int[] arrsuccs;
        if (predsarr[0] != -1) {
          final int pred = predsarr[i];
          arrsuccs = mTrans.getSuccessors(pred, e).toArray();
        } else {
          arrsuccs = mInitial;
        }
        for (int j = 0; j < arrsuccs.length; j++) {
          final int succ = arrsuccs[j];
          final Iterator<TIntHashSet> it = uncoveredAnns.iterator();
          while (it.hasNext()) {
            final TIntHashSet ann = it.next();
            final Iterator<TIntHashSet> it2 = mTrans.getAnnotations2(succ).iterator();
            while (it2.hasNext()) {
              final TIntHashSet ann2 = it2.next();
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

  public boolean canbemadebisimiliar(final int state)
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
      final Tuple tup2 = getSuccessors(cand);
      if (tup2 == null) {continue;}
      if (!(tup.mOptional.containsAll(tup2.mMust.toArray()) &&
          tup2.mOptional.containsAll(tup.mMust.toArray()))) {continue;}
      int[] mustself = tup.mMustSelfLoops.toArray();
      for (int i = 0; i < mustself.length; i++) {
        final int must = mustself[i];
        if (tup2.mOptionalSelfLoops.contains(must)) {continue;}
        if (tup2.mOptional.contains(mergeIntoLong(state, must))) {continue;}
        continue Cands;
      }
      mustself = tup2.mMustSelfLoops.toArray();
      for (int i = 0; i < mustself.length; i++) {
        final int must = mustself[i];
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

    public Tuple(final TLongHashSet must, final TLongHashSet optional,
                 final Set<TIntHashSet> uncoveredAnns,
                 final TIntHashSet mustSelf, final TIntHashSet optionalSelf)
    {
      mMust = must;
      mOptional = optional;
      mUncoveredAnns = uncoveredAnns;
      mMustSelfLoops = mustSelf;
      mOptionalSelfLoops = optionalSelf;
    }
  }
}
