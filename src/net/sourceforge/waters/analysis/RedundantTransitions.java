package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TIntArrayList;
import java.util.Arrays;
import gnu.trove.TLongByteHashMap;
import java.util.Set;
import gnu.trove.TLongIntHashMap;

public class RedundantTransitions
{
  private final TransitionRelation mTransitionRelation;
  private final boolean[] mReachable;
  private final TLongIntHashMap mCache;
  
  public static int TRANSITIONSREMOVED = 0;
  public static int STATESREMOVED = 0;
  public static int TIME = 0;
  
  private static final TIntHashSet EMPTYSET = new TIntHashSet(0);
  
  private long key(int s1, int s2)
  {
    long l = s1;
    s1 <<= 32;
    long l2 = s2;
    l |= l2;
    return l;
  }
  
  public static void clearStats()
  {
    TRANSITIONSREMOVED = 0;
    TIME = 0;
    STATESREMOVED = 0;
  }
  
  
  public static String stats()
  {
    return "RedundantTransitions: TRANSITIONSREMOVED = " + TRANSITIONSREMOVED +
            " States Removed" + STATESREMOVED + " TIME = " + TIME;
  }
  
  
  public RedundantTransitions(TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mReachable = new boolean[mTransitionRelation.numberOfStates()];
    mCache = new TLongIntHashMap();
  }
  
  public boolean coversAnnotations(int s1, int s2)
  {
    Set<TIntHashSet> ann1 = mTransitionRelation.getAnnotations2(s1);
    Set<TIntHashSet> ann2 = mTransitionRelation.getAnnotations2(s2);
    for (TIntHashSet a1 : ann1) {
      int[] array = a1.toArray();
      boolean covered = false;
      for (TIntHashSet a2 : ann2) {
        if (a2.containsAll(array)) {
          covered = true;
          break;
        }
      }
      if (!covered) {return false;}
    }
    return true;
  }
  
  public boolean coversOutGoing(int s1, int s2, int state, int event)
  {
    if (s2 == state) {
      TIntHashSet out1 = mTransitionRelation.getSuccessors(s1, event);
      if (out1 != null) {
        if (out1.contains(s1)) {return false;}
      }
    }
    if (mTransitionRelation.isMarked(s1) && !mTransitionRelation.isMarked(s2)){
      return false;
    }
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      TIntHashSet out1 = mTransitionRelation.getSuccessors(s1, e);
      TIntHashSet out2 = mTransitionRelation.getSuccessors(s2, e);
      out1 = out1 == null ? EMPTYSET : out1;
      out2 = out2 == null ? EMPTYSET : out2;
      /*if (out1.contains(s1) && out2.contains(s2)) {
        out1.remove(s1);
      }*/
      if (!out2.containsAll(out1.toArray())) {
        return false;
      }
    }
    return true;
  }
  
  public int redundantState(int s1, int s2, int state, int event)
  {
    //TODO this has significance to do with follow on equivalence
    /*int res1 = mCache.get(key(s1, s2));
    int res2 = mCache.get(key(s2, s1));
    if (res1 == -1 && res2 == -1) { return -1;}
    if (res1 == 1) {return s1;}
    if (res2 == 1) {return s2;}*/
    if (coversAnnotations(s1, s2) && coversOutGoing(s1, s2, state, event)) {
      mCache.put(key(s1, s2), 1);
      mCache.put(key(s2, s1), -1);
      return s1;
    } else if (coversAnnotations(s2, s1) && coversOutGoing(s2, s1, state, event)) {
      mCache.put(key(s2, s1), 1);
      mCache.put(key(s1, s2), -1);
      return s2;
    }
    mCache.put(key(s1, s2), -1);
    mCache.put(key(s2, s1), -1);
    return -1;
  }
  
  public void run()
  {
    TIME -= System.currentTimeMillis();
    System.out.println("start");
    STATESREMOVED -= mTransitionRelation.unreachableStates();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (!mTransitionRelation.hasPredecessors(state)) {continue;}
      for (int event = 0; event < mTransitionRelation.numberOfEvents(); event++) {
        //System.out.println("(s,e): (" + state + ", " + event + ")");
        TIntHashSet setsuccs = mTransitionRelation.getSuccessors(state, event);
        if (setsuccs == null) {continue;}
        int[] sucs = setsuccs.toArray();
        TIntHashSet toberemoved = new TIntHashSet();
        for (int i = 0; i < sucs.length; i++) {
          int suc1 = sucs[i];
          for (int j = i + 1; j < sucs.length; j++) {
            int suc2 = sucs[j];
            int torem = redundantState(suc1, suc2, state, event);
            if (torem != -1) {toberemoved.add(torem);}
          }
        }
        //System.out.println("toberemmed: " + toberemoved.size());
        int[] array = toberemoved.toArray();
        for (int i = 0; i < array.length; i++) {
          mTransitionRelation.removeTransition(state, event, array[i]);
          TRANSITIONSREMOVED++;
          /*System.out.println();
          System.out.println("redundant transition");
          System.out.println();*/
        }
      }
    }
    System.out.println("TRANSITIONSREMOVED: " + TRANSITIONSREMOVED);
    STATESREMOVED += mTransitionRelation.unreachableStates();
    TIME += System.currentTimeMillis();
  }
}
