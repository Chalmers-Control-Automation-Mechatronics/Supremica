package net.sourceforge.waters.analysis.annotation;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TIntArrayList;
import java.util.Arrays;
import gnu.trove.TLongByteHashMap;
import java.util.Set;
import gnu.trove.TLongIntHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.THashSet;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.analysis.TransitionRelation;

public class SilentOutGoing
{
  private final TransitionRelation mTransitionRelation;
  private final int mTau;
  private final TIntHashSet[][] mPossibleSuccs;
  private final TIntHashSet[] mPossibleTauStates;
  private final Set<TIntHashSet>[] mAnns;
  
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
    TIME = 0;
    STATESREMOVED = 0;
  }
  
  
  public static String stats()
  {
    return "SilentOutgoing: " +
            " States Removed" + STATESREMOVED + " TIME = " + TIME;
  }
  
  
  public SilentOutGoing(TransitionRelation transitionrelation, int tau)
  {
    mTransitionRelation = transitionrelation;
    mPossibleSuccs = 
      new TIntHashSet[mTransitionRelation.numberOfStates()]
                     [mTransitionRelation.numberOfEvents()];
    mTau = tau;
    mPossibleTauStates = new TIntHashSet[mTransitionRelation.numberOfStates()];
    mAnns = new Set[mTransitionRelation.numberOfStates()];
    for (int s = 0; s < mPossibleTauStates.length; s++) {
      mPossibleTauStates[s] = new TIntHashSet();
      mAnns[s] = new THashSet<TIntHashSet>();
    }
  }
  
  private void calculateAnns()
  {
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      TIntHashSet taus = mTransitionRelation.getSuccessors(s, mTau);
      if (taus == null || taus.isEmpty()) {
        mAnns[s].add(mTransitionRelation.getActiveEvents(s));
      } else {
        final int state = s;
        taus.forEach(new TIntProcedure() {
          public boolean execute(int tausucc)
          {
            mAnns[state].add(mTransitionRelation.getActiveEvents(tausucc));
            return true;
          }
        });
      }
    }
  }
  
  private void calculatePossibleTauSuccs()
  {
    STATES:
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (!mTransitionRelation.hasPredecessors(s)) {continue;}
      TIntHashSet possible = null;
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        if (e == mTau) {continue;}
        TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {continue;}
        int[] succsarr = succs.toArray();
        for (int i = 0; i < succsarr.length; i++) {
          int succ = succsarr[i];
          TIntHashSet succpreds = mTransitionRelation.getPredecessors(succ, e);
          if (possible == null) {
            possible = new TIntHashSet(succpreds.toArray());
            possible.remove(s);
          } else {
            possible.retainAll(succpreds.toArray());
          }
          if (possible.isEmpty()) {continue STATES;}
        }
      }
      if (possible == null) {continue;}
      if (mTransitionRelation.isMarked(s)) {
        int[] possiblearr = possible.toArray();
        for (int i = 0; i < possiblearr.length; i++) {
          int poss = possiblearr[i];
          if (!mTransitionRelation.isMarked(poss)) {possible.remove(poss);}
        }
        if (possible.isEmpty()) {continue STATES;}
      }
      int[] possiblearr = possible.toArray();
      CoversAnnoations:
      for (int i = 0; i < possiblearr.length; i++) {
        int poss = possiblearr[i];
        Annotations:
        for (TIntHashSet ann1 : mAnns[s]) {
          for (TIntHashSet ann2 : mAnns[poss]) {
            if (ann1.containsAll(ann2.toArray())) {
              continue Annotations;
            }
          }
          possible.remove(poss); continue CoversAnnoations;
        }
      }
      if (possible.isEmpty()) {continue STATES;}
      for (int i = 0; i < possiblearr.length; i++) {
        int poss = possiblearr[i];
        mPossibleTauStates[poss].add(s);
      }
    }
  }
  
  private boolean isCandidateState(int s)
  {
    TIntHashSet tausuccs = mTransitionRelation.getSuccessors(s, mTau);
    if (tausuccs == null) {return false;}
    int[] tausarr = tausuccs.toArray();
    TIntHashSet followonactive = new TIntHashSet();
    for (int i = 0; i < tausarr.length; i++) {
      int tausucc = tausarr[i];
      followonactive.addAll(mTransitionRelation.getActiveEvents(tausucc).toArray());
    }
    followonactive.add(mTau);
    TIntHashSet activeEvents = mTransitionRelation.getActiveEvents(s);
    /*for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      if (e == mTau) {continue;}
      TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
      if (succs == null) {continue;}
      if (succs.contains(s)) {return false;}
    }*/
    activeEvents.removeAll(followonactive.toArray());
    if (activeEvents.isEmpty()) {return true;}
    int[] posstaus = mPossibleTauStates[s].toArray();
    for (int i = 0; i < posstaus.length; i++) {
      int poss = posstaus[i];
      if (mTransitionRelation.getActiveEvents(poss).containsAll(activeEvents.toArray())) {
        return true;
      }
    }
    return false;
  }
  
  public void calculateCandidateTransitions(int s)
  {
    TIntHashSet taupreds = mTransitionRelation.getPredecessors(s, mTau);
    int[] activeevents = mTransitionRelation.getActiveEvents(s).toArray();
    int[] predsarr = taupreds.toArray();
    for (int i = 0; i < activeevents.length; i++) {
      int e = activeevents[i];
      if (mTransitionRelation.isMarkingEvent(e)) {continue;}
      mPossibleSuccs[s][e] =
        new TIntHashSet(mTransitionRelation.getSuccessors(s, e).toArray());
    }
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      if (e == mTau) {continue;}
      if (mTransitionRelation.getPredecessors(s, e) != null &&
          !mTransitionRelation.getPredecessors(s, e).isEmpty()) {return;}
    }
    for (int i = 0; i < predsarr.length; i++) {
      int pred = predsarr[i];
      for (int j = 0; j < activeevents.length; j++) {
        int e = activeevents[j];
        if (mTransitionRelation.isMarkingEvent(e)) {continue;}
        mPossibleSuccs[s][e].retainAll(mTransitionRelation.getSuccessors(pred, e).toArray());
      }
    }
  }
  
  public void attemptToRemove(int s)
  {
    int[] tausuccs = mTransitionRelation.getSuccessors(s, mTau).toArray();
    Arrays.sort(tausuccs);
    int[] activeevents = mTransitionRelation.getActiveEvents(s).toArray();
    TIntHashSet[] tobeCovered = new TIntHashSet[activeevents.length];
    TIntHashSet[][] tobeadded = new TIntHashSet[activeevents.length][tausuccs.length];
    boolean needextra = false;
    for (int j = 0; j < activeevents.length; j++) {
      int e = activeevents[j];
      if (e == mTau) {continue;}
      if (mTransitionRelation.isMarkingEvent(e)) {continue;}
      tobeCovered[j] = new TIntHashSet(mTransitionRelation.getSuccessors(s, e).toArray());
      for (int i = 0; i < tausuccs.length; i++) {
        int tausucc = tausuccs[i];
        tobeadded[j][i] = new TIntHashSet();
        int[] arrtobecovered = tobeCovered[j].toArray();
        for (int k = 0; k < arrtobecovered.length; k++) {
          int succ = arrtobecovered[k];
          TIntHashSet psuccs = mPossibleSuccs[tausucc][e];
          if (psuccs == null) {continue;}
          if (psuccs.contains(succ)) {
            tobeadded[j][i].add(succ); tobeCovered[j].remove(succ);
          }
        }
      }
      if (tobeCovered[j].isEmpty()) {continue;}
      needextra = true;
      int[] possarr = mPossibleTauStates[s].toArray();
      for (int i = 0; i < possarr.length; i++) {
        int poss = possarr[i];
        TIntHashSet posssuccs = mTransitionRelation.getSuccessors(poss, e);
        if (posssuccs == null ||
            !posssuccs.containsAll(tobeCovered[j].toArray())) {
          mPossibleTauStates[s].remove(poss);
        }
      }
      if (mPossibleTauStates[s].isEmpty()) {return;}
    }
    for (int j = 0; j < activeevents.length; j++) {
      int e = activeevents[j];
      if (e == mTau) {continue;}
      if (mTransitionRelation.isMarkingEvent(e)) {continue;}
      for (int i = 0; i < tausuccs.length; i++) {
        int tausucc = tausuccs[i];
        int[] succs = tobeadded[j][i].toArray();
        for (int k = 0; k < succs.length; k++) {
          int succ = succs[k];
          mTransitionRelation.addTransition(tausucc, e, succ);
        }
      }
    }
    for (int i = 0; i < tausuccs.length; i++) {
      int tausucc = tausuccs[i];
      mTransitionRelation.addAllPredeccessors(s, tausucc);
    }
    if (needextra) {
      System.out.println("shouldn't");
      int poss = mPossibleTauStates[s].toArray()[0];
      mTransitionRelation.addAllPredeccessors(s, poss);
    }
    mTransitionRelation.removeAllIncoming(s);
    mTransitionRelation.removeAllOutgoing(s);
    STATESREMOVED++;
  }
  
  public void run(final ProductDESProxyFactory factory)
  {
    TIME -= System.currentTimeMillis();
    System.out.println("start");
    calculateAnns();
    calculatePossibleTauSuccs();
    TIntHashSet tausuccs = new TIntHashSet();
    TIntHashSet candidates = new TIntHashSet();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (isCandidateState(state)) {
        candidates.add(state);
        tausuccs.addAll(mTransitionRelation.getSuccessors(state, mTau).toArray());
      }
    }
    System.out.println("candidates: " + candidates.size());
    tausuccs.forEach(new TIntProcedure() {
      public boolean execute(int state)
      {
        calculateCandidateTransitions(state); return true;
      }
    });
    candidates.forEach(new TIntProcedure() {
      public boolean execute(int state)
      {
        /*int rem = STATESREMOVED;   
        AutomatonProxy bef = mTransitionRelation.getAutomaton(factory);*/
        attemptToRemove(state);
        /*if (STATESREMOVED != rem) {
          System.out.println("before");
          System.out.println(bef);
          System.out.println("after");
          System.out.println(mTransitionRelation.getAutomaton(factory));
        }*/
        return true;
      }
    });
    System.out.println("remmed: " + STATESREMOVED);
    TIME += System.currentTimeMillis();
  }
}
