package net.sourceforge.waters.analysis.annotation;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.Set;


public class CertainConflict
{
  private final TransitionRelation mTransitionRelation;
  private final boolean[] mReachable;
  private final int mTau;

  public static int STATESREMOVED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESREMOVED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "CERTAINCONFLICT: STATESREMOVED = " + STATESREMOVED +
            " TIME = " + TIME;
  }

  public CertainConflict(final TransitionRelation transitionrelation, final int tau)
  {
    mTransitionRelation = transitionrelation;
    mReachable = new boolean[mTransitionRelation.numberOfStates()];
    mTau = tau;
  }

  private void backtrack(int state)
  {
    final TIntStack stack = new TIntArrayStack();
    stack.push(state);
    while (stack.size() != 0) {
      state = stack.pop();
      if (mReachable[state]) {continue;}
      mReachable[state] = true;
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
        if (preds == null) {continue;}
        final TIntIterator it = preds.iterator();
        while (it.hasNext()) {//mark all state which can reach this state as reachable
          final int pred = it.next(); stack.push(pred);
        }
      }
    }
  }

  /*private void backtrack(int state)
  {
    TIntStack stack = new TIntStack
    if(mReachable[state]) {return;} //already done this state
    mReachable[state] = true;
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
      if (preds == null) {continue;}
      TIntIterator it = preds.iterator();
      while (it.hasNext()) {//mark all state which can reach this state as reachable
        int pred = it.next(); backtrack(pred);
      }
    }
  }*/

  public boolean run()
  {
    TIME -= System.currentTimeMillis();
    final TIntHashSet empty = new TIntHashSet();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      final Set<TIntHashSet> anns = mTransitionRelation.getAnnotation(state);
      if (anns != null) {
        if (anns.contains(empty)) {
          mTransitionRelation.removeAllOutgoing(state);
        }
      }
    }
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      if (mTransitionRelation.isMarked(state)) {
        backtrack(state);
      }
    }
    int dumpstate = -1;
    final TIntHashSet redirect = new TIntHashSet();
    final TIntStack stack = new TIntArrayStack();
    for (int state = 0; state < mReachable.length; state++) {
      if (!mTransitionRelation.hasPredecessors(state)) {continue;}
      if (!mReachable[state]) {
        dumpstate = dumpstate == -1 ? state : dumpstate;
        //System.out.println("dumpstate: " + dumpstate);
        redirect.add(state); // all transitions leading to this state will be redirected here
        stack.push(state);
      }
    }
    while (true) {
      while (stack.size() != 0) {
        final int state = stack.pop();
        STATESREMOVED++;
        mTransitionRelation.removeAllOutgoing(state);
        if (mTransitionRelation.isInitial(state)) {
          TIME += System.currentTimeMillis();
          return false;
        }
        /*if (mTransitionRelation.isInitial(state)) {
          for (int i = 0; i < mTransitionRelation.numberOfStates(); i++) {
            if (mTransitionRelation.isInitial(i)) {
              if (redirect.add(i)) {
                stack.push(i);
              }
            }
            mTransitionRelation.makeInitialState(i, false);
          }
          mTransitionRelation.makeInitialState(dumpstate, true);
        }*/
        for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
          final TIntHashSet preds = mTransitionRelation.getPredecessors(state, e);
          if (preds == null) {continue;}
          final TIntIterator it = preds.iterator();
          while (it.hasNext()) {
            final int pred = it.next();
            if (redirect.contains(pred)) {
              continue; //has or will be nulled;
            }
            if (e == mTau) {
              redirect.add(pred); stack.push(pred); continue; // is another dumpstate
            }
            final TIntHashSet succs = mTransitionRelation.getSuccessors(pred, e);
            final int[] arsuccs = succs.toArray();
            for (int i = 0; i < arsuccs.length; i++) {
              final int succ = arsuccs[i];
              if (succ == state) {continue;}
              mTransitionRelation.removeTransition(pred, e, succ);
            }
          }
        }
        mTransitionRelation.moveAllPredeccessors(state, dumpstate);
      }
      if (dumpstate == -1) {break;}
      MainLoop:
      for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
        if (mTransitionRelation.isMarked(s)) {continue;}
        for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
          final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
          if (succs == null) {continue;}
          if (succs.size() > 1 || !succs.contains(dumpstate)) {
            continue MainLoop;
          }
        }
        if (redirect.add(s)) {stack.push(s);}
      }
      MainLoop2:
      for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
        for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
          final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
          if (succs == null) {continue;}
          if (succs.contains(dumpstate)) {
            if (mTransitionRelation.removeEventFromAnnotations(e, s)) {
              if (redirect.add(s)) {stack.push(s);}
              continue MainLoop2;
            }
          }
        }
      }
      if (stack.size() == 0) {
        break;
      }
    }
    TIME += System.currentTimeMillis();
    return true;
  }
}

