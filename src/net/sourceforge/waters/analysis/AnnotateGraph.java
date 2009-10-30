package net.sourceforge.waters.analysis;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;

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
  
  public AnnotateGraph(TransitionRelation transitionrelation, int tau)
  {
    mTransitionRelation = transitionrelation;
    mTau = tau;
  }
  
  public void run()
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
    mTransitionRelation.removeEvent(mTau);
    STATESTAUSREMOVEDFROM += tausremoved.size();
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
