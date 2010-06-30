package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongHashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class EquivalentIncoming
{
  private final TransitionRelation mTransitionRelation;
  private TLongHashSet[] mIncomings = null;
  public static int STATESMERGED = 0;
  public static int ANNOTIONSSUBSET = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESMERGED = 0;
    ANNOTIONSSUBSET = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "EquivalentIncoming: STATESMERGED = " + STATESMERGED + " ANNOTIONSSUBSET = " + ANNOTIONSSUBSET +
            " TIME = " + TIME;
  }

  public EquivalentIncoming(final TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
  }

  public long mergeIntoLong(final int state, final int event)
  {
    long merge = state;
    merge <<= 32;
    merge |= event;
    return merge;
  }

  public TLongHashSet getIncoming(final int state)
  {
    if (mIncomings[state] != null) {return mIncomings[state];}
    final TLongHashSet incoming = new TLongHashSet();
    final TIntHashSet[] preds = mTransitionRelation.getAllPredecessors(state);
    for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
      if (preds[e] == null) {continue;}
      final int[] predarray = preds[e].toArray();
      for (int i = 0; i < predarray.length; i++) {
        final int pred = predarray[i];
        incoming.add(mergeIntoLong(pred, e));
      }
    }
    if (mTransitionRelation.isInitial(state)) {
      incoming.add(mergeIntoLong(-1, -1));
    }
    mIncomings[state] = incoming;
    return incoming;
  }

  public Collection<TIntHashSet> getIncomingEquivalentStates()
  {
    final Map<TLongHashSet, TIntHashSet> incomingTransitionsMap =
      new THashMap<TLongHashSet, TIntHashSet>();
    mIncomings = new TLongHashSet[mTransitionRelation.numberOfStates()];
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TLongHashSet incoming = getIncoming(s);
      if (!incoming.isEmpty()) {
        TIntHashSet equiv = incomingTransitionsMap.get(incoming);
        if (equiv == null) {
          equiv = new TIntHashSet();
          incomingTransitionsMap.put(incoming, equiv);
        }
        equiv.add(s);
      }
    }
    final Iterator<TIntHashSet> it = incomingTransitionsMap.values().iterator();
    while (it.hasNext()) {
      if (it.next().size() <= 1) {it.remove();}
    }
    return incomingTransitionsMap.values();
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    while(true) {
      boolean ruleactivated = false;
      final Collection<TIntHashSet> incequiv = getIncomingEquivalentStates();
      for (final TIntHashSet equiv : incequiv) {
        final int[] array = equiv.toArray();
        for (int i = 0; i < array.length; i++) {
          final int state1 = array[i];
          if (!equiv.contains(state1)) {continue;}
          for (int j = 0; j < array.length; j++) {
            final int state2 = array[j];
            if (state1 == state2) {continue;}
            ANNOTIONSSUBSET += mTransitionRelation.getAnnotations2(state1).size();
            ANNOTIONSSUBSET += mTransitionRelation.getAnnotations2(state2).size();
            mTransitionRelation.mergewithannotations(new int[] {state1, state2}); equiv.remove(state2);
            ANNOTIONSSUBSET -= mTransitionRelation.getAnnotations2(state1).size();
            STATESMERGED++;
            ruleactivated = true;
          }
          equiv.remove(state1);
        }
      }
      if (!ruleactivated) {break;}
    }
    TIME += System.currentTimeMillis();
  }

  /*public void run()
  {
    TIME -= System.currentTimeMillis();
    boolean[] onstack = new boolean[mTransitionRelation.numberOfStates()];
    TIntStack stack = new TIntStack(mTransitionRelation.numberOfStates());
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if (mTransitionRelation.hasPredecessors(s)) {
        stack.push(s); onstack[s] = true;
      }
    }
    while(stack.size() != 0) {
      int state = stack.pop(); onstack[state] = false;
      if (!mTransitionRelation.hasPredecessors(state)) {continue;}
      for (int other = 0; other < mTransitionRelation.numberOfStates(); other++) {
        if (other == state) {continue;}
        if (!mTransitionRelation.equivalentIncoming(other, state)) {continue;}
        ANNOTIONSSUBSET += mTransitionRelation.getAnnotation(state) == null ?
                            1 : mTransitionRelation.getAnnotation(state).size();
        ANNOTIONSSUBSET += mTransitionRelation.getAnnotation(other) == null ?
                            1 : mTransitionRelation.getAnnotation(other).size();
        mTransitionRelation.mergewithannotations(new int[] {state, other});
        STATESMERGED++;
        ANNOTIONSSUBSET -= mTransitionRelation.getAnnotation(state).size();
      }
    }
    TIME += System.currentTimeMillis();
  }*/
}
