package net.sourceforge.waters.analysis.annotation;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Set;


public class AnnotateGraph
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;

  public static int ANNOTATIONSADDED = 0;
  public static int ANNOTATIONSREMOVEDSUBSET = 0;
  public static int STATESREMOVED = 0;
  public static int STATESTAUSREMOVEDFROM = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    ANNOTATIONSADDED = 0;
    ANNOTATIONSREMOVEDSUBSET = 0;
    STATESREMOVED = 0;
    STATESTAUSREMOVEDFROM = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "ANNOTATEGRAPH: ANNOTATIONSADDED = " + ANNOTATIONSADDED +
            " ANNOTATIONSREMOVEDSUBSET = " + ANNOTATIONSREMOVEDSUBSET +
            " STATESREMOVED = " + STATESREMOVED +
            " STATESTAUSREMOVEDFROM = " + STATESTAUSREMOVEDFROM +
            " TIME = " + TIME;
  }

  public AnnotateGraph(final TransitionRelation transitionrelation, final int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    mTransitionRelation.removeAllSelfLoops(mTau);
    mTransitionRelation.removeAllAnnotations(mTau);
    STATESREMOVED -= mTransitionRelation.unreachableStates();
    final TIntHashSet tausremoved = new TIntHashSet();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TIntHashSet taus = mTransitionRelation.getSuccessors(s, mTau);
      if (taus == null || taus.isEmpty()) {
        continue;
      }
      //System.out.println("taus: + " + Arrays.toString(taus.toArray()));
      final TIntIterator it = taus.iterator();
      ANNOTATIONSREMOVEDSUBSET += mTransitionRelation.getAnnotations2(s).size();
      Set<TIntHashSet> anns = new THashSet<TIntHashSet>(mTransitionRelation.getAnnotations2(s));
      while (it.hasNext()) {
        ANNOTATIONSADDED++;
        final int target = it.next();
        tausremoved.add(target);
        TIntHashSet ae = mTransitionRelation.getActiveEvents(target);
        if (ae == null) {
          System.out.println("null ae");
          ae = new TIntHashSet();
        }
        /*if (s == 13) {
          System.out.println("annotate");
          System.out.println(Arrays.toString(ae.toArray()));
        }*/
        mTransitionRelation.addAllSuccessors(target, s);
        ANNOTATIONSREMOVEDSUBSET += mTransitionRelation.getAnnotations2(target).size();
        anns = TransitionRelation.subsets(mTransitionRelation.getAnnotations2(target),
                                           anns);
      }
      /*if (s == 13) {
        System.out.println("13's anns");
        for (TIntHashSet ann : anns) {
          System.out.println(Arrays.toString(ann.toArray()));
        }
      }*/
      ANNOTATIONSREMOVEDSUBSET -= anns.size();
      if (!anns.isEmpty()) {
        mTransitionRelation.setAnnotation(s, anns);
      }
    }
    //System.out.println("mTau: " + mTau);
    mTransitionRelation.removeAllAnnotations(mTau);
    mTransitionRelation.removeEvent(mTau);
    STATESTAUSREMOVEDFROM += tausremoved.size();
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }

  /*public void run()
  {
    TIME -= System.currentTimeMillis();
    STATESREMOVED -= mTransitionRelation.unreachableStates();
    TIntArrayList stilltau = new TIntArrayList();
    TIntHashSet tausremoved = new TIntHashSet();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      TIntHashSet taus = mTransitionRelation.getSuccessors(s, mTau);
      if (taus == null || taus.isEmpty()) {
        continue;
      }
      //System.out.println("taus: + " + Arrays.toString(taus.toArray()));
      TIntIterator it = taus.iterator();
      stilltau.clear();
      while (it.hasNext()) {
        ANNOTATIONSADDED++;
        int target = it.next();
        tausremoved.add(target);
        TIntHashSet ae = mTransitionRelation.getActiveEvents(target);
        if (ae == null) {
          System.out.println("null ae");
          ae = new TIntHashSet();
        }
        mTransitionRelation.addAllSuccessors(target, s);
        boolean added = true;
        boolean subset = false;
        for (int j = 0; j < stilltau.size(); j++) {
          int othertau = stilltau.get(j);
          TIntHashSet ae2 = mTransitionRelation.getActiveEvents(othertau);
          if (ae2.size() < ae.size()) {
            if (subset) {
              continue;
            }
            if (ae.containsAll(ae2.toArray())) {
              ANNOTATIONSREMOVEDSUBSET++;
              added = false;
              break;
            }
          } else {
            if (ae2.containsAll(ae.toArray())) {
              ANNOTATIONSREMOVEDSUBSET++;
              stilltau.remove(j);
              subset = true;
            }
          }
        }
        if (added) {
          stilltau.add(target);
        }
      }
      Set<TIntHashSet> annotation = new THashSet<TIntHashSet>(stilltau.size());
      for (int j = 0; j < stilltau.size(); j++) {
        int target = stilltau.get(j);
        TIntHashSet ann = mTransitionRelation.getActiveEvents(target);
        ann = ann == null ? new TIntHashSet() : ann;
        annotation.add(ann);
      }
      annotation.remove(mTransitionRelation.getActiveEvents(s));
      if (!annotation.isEmpty()) {
        mTransitionRelation.setAnnotation(s, annotation);
      }
      stilltau.clear();
    }
    mTransitionRelation.removeAllAnnotations(mTau);
    mTransitionRelation.removeEvent(mTau);
    STATESTAUSREMOVEDFROM += tausremoved.size();
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }*/
}

