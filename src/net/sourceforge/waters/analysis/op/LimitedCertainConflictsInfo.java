//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   LimitedCertainConflictsInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A record containing information about states of certain conflicts
 * computed by a {@link LimitedCertainConflictsTRSimplifier}.
 *
 * @author Robi Malik
 */

public class LimitedCertainConflictsInfo
{

  //#########################################################################
  //# Constructors
  LimitedCertainConflictsInfo(final ListBufferTransitionRelation rel)
  throws OverflowException
  {
    mNumberOfStates = rel.getNumberOfStates();
    mNumberOfEvents = rel.getNumberOfProperEvents();
    mBlockingStates = new BitSet(mNumberOfStates);
    mCertainConflictStates = new BitSet(mNumberOfStates);
    mCertainConflictTransitions =
      new OutgoingTransitionListBuffer(mNumberOfEvents, mNumberOfStates);
    mIterator = mCertainConflictTransitions.createReadOnlyIterator();
  }


  //#########################################################################
  //# Read Access
  public boolean isBlockingState(final int state)
  {
    return mBlockingStates.get(state);
  }

  public boolean isCertainConflictState(final int state)
  {
    return mCertainConflictStates.get(state);
  }

  public int getCertainConflictsSuccessor(final int state, final int event)
  {
    mIterator.reset(state, event);
    if (mIterator.advance()) {
      return mIterator.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  public boolean canSilentlyReachBlockingState(final int state)
  {
    if (isBlockingState(state)) {
      return true;
    }
    final int tauSucc = getCertainConflictsSuccessor(state, EventEncoding.TAU);
    if (tauSucc < 0) {
      return false;
    } else {
      return canSilentlyReachBlockingState(tauSucc);
    }
  }

  public ListBufferTransitionRelation createTestAutomaton
    (final ListBufferTransitionRelation rel,
     final String name,
     final int init)
  throws OverflowException
  {
    final int numStates = mNumberOfStates + 1;
    final ListBufferTransitionRelation result = new ListBufferTransitionRelation
      (name, ComponentKind.SPEC, mNumberOfEvents, 0, numStates,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    for (int state = 0; state < numStates; state++) {
      result.setReachable(state, false);
    }
    result.setInitial(init, true);
    final int tau = EventEncoding.TAU;
    final int dump = mNumberOfStates;
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final BitSet uncontrollable = new BitSet(mNumberOfEvents);
    final TIntStack open = new TIntStack();
    final TIntHashSet visited = new TIntHashSet();
    open.push(init);
    visited.add(init);
    while (open.size() > 0) {
      final int state = open.pop();
      result.setReachable(state, true);
      iter.resetState(state);
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
        if (event != tau && getCertainConflictsSuccessor(state, event) >= 0) {
          uncontrollable.set(event);
          continue;
        }
        final int target = iter.getCurrentTargetState();
        if (isCertainConflictState(target)) {
          result.addTransition(state, event, target);
          if (visited.add(target)) {
            open.push(target);
          }
        }
      }
    }
    final int nontau = EventEncoding.NONTAU;
    final BitSet todump = new BitSet(mNumberOfEvents);
    todump.or(uncontrollable);
    for (int state = 0; state < mNumberOfStates; state++) {
      if (result.isReachable(state)) {
        iter.resetState(state);
        while (iter.advance()) {
          final int event = iter.getCurrentEvent();
          todump.clear(event);
        }
        int event = todump.nextSetBit(nontau);
        if (event < mNumberOfEvents) {
          do {
            result.addTransition(state, event, dump);
            event = todump.nextSetBit(event + 1);
          } while (event < mNumberOfEvents);
          result.setReachable(dump, true);
          todump.or(uncontrollable);
        }
      }
    }
    for (int event = uncontrollable.nextSetBit(nontau);
         event < mNumberOfEvents;
         event = uncontrollable.nextSetBit(event + 1)) {
      result.addTransition(dump, event, dump);
    }
    return result;
  }

  public Collection<EventProxy> getUncontrollableEvents
    (final ListBufferTransitionRelation testrel, final EventEncoding enc)
  {
    final int numEvents = testrel.getNumberOfProperEvents();
    final Collection<EventProxy> uncontrollables =
      new ArrayList<EventProxy>(numEvents);
    final int dump = testrel.getNumberOfStates() - 1;
    final TransitionIterator iter =
      testrel.createSuccessorsReadOnlyIterator(dump);
    while (iter.advance()) {
      final int code = iter.getCurrentEvent();
      final EventProxy event = enc.getProperEvent(code);
      uncontrollables.add(event);
    }
    return uncontrollables;
  }


  //#########################################################################
  //# Write Access
  void setBlockingStates(final BitSet coreachable)
  {
    for (int state = coreachable.nextClearBit(0);
         state < mNumberOfStates;
         state = coreachable.nextClearBit(state + 1)) {
      mBlockingStates.set(state);
    }
  }

  void setCertainConflictStates(final BitSet coreachable)
  {
    for (int state = coreachable.nextClearBit(0);
         state < mNumberOfStates;
         state = coreachable.nextClearBit(state + 1)) {
      mCertainConflictStates.set(state);
    }
  }

  void addCertainConflictTransition(final int from,
                                    final int event,
                                    final int to)
  {
    mCertainConflictTransitions.addTransition(from, event, to);
  }


  //#########################################################################
  //# Data Members
  private final int mNumberOfStates;
  private final int mNumberOfEvents;
  private final BitSet mBlockingStates;
  private final BitSet mCertainConflictStates;
  private final TransitionListBuffer mCertainConflictTransitions;
  private final TransitionIterator mIterator;

}
