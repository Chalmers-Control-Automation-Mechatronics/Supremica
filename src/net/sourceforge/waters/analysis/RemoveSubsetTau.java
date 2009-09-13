package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.Collection;
import gnu.trove.TIntArrayList;
import java.util.Set;
import gnu.trove.THashSet;
import java.util.Arrays;

public class RemoveSubsetTau
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
    return "REMOVE SUBSET TAU: ANNOTATIONSADDED = " + ANNOTATIONSADDED +
            " ANNOTATIONSREMOVEDSUBSET = " + ANNOTATIONSREMOVEDSUBSET +
            " STATESREMOVED = " + STATESREMOVED +
            " STATESTAUSREMOVEDFROM = " + STATESTAUSREMOVEDFROM +
            " TIME = " + TIME;
  }
  
  public RemoveSubsetTau(TransitionRelation transitionrelation, int tau)
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
      taus = new TIntHashSet(taus.toArray());
      for (int j = 0; j < stilltau.size(); j++) {
        taus.remove(stilltau.get(j));
      }
      int[] arr = taus.toArray();
      for (int i = 0; i < arr.length; i++) {
        int t = arr[i];
        mTransitionRelation.removeTransition(s, mTau, t);
        tausremoved.add(t);
      }
      stilltau.clear();
    }
    STATESTAUSREMOVEDFROM += tausremoved.size();
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
