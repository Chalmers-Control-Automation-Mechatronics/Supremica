//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.BitSet;

import gnu.trove.TIntStack;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class HalfWaySynthesisTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public HalfWaySynthesisTRSimplifier()
  {
  }

  public HalfWaySynthesisTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }


  /**
   * Sets the code of the last local uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events. The
   * tau event code ({@link EventEncoding#TAU} is not used. Therefore, the
   * range of uncontrollable local events is from {@link EventEncoding#NONTAU}
   * to {@link #getLastLocalUncontrollableEvent()} inclusive.
   */
  public void setLastLocalUncontrollableEvent(final int event)
  {
    mLastLocalUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last local uncontrollable event.
   *
   * @see #setLastLocalUncontrollableEvent(int) setLastLocalUncontrollableEvent()
   */
  public int getLastLocalUncontrollableEvent()
  {
    return mLastLocalUncontrollableEvent;
  }

  /**
   * Sets the code of the last local controllable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events.
   * Therefore, the range of controllable local events is from
   * {@link #getLastLocalUncontrollableEvent()}+1 to
   * {@link #getLastLocalControllableEvent()} inclusive.
   */
  public void setLastLocalControllableEvent(final int event)
  {
    mLastLocalControllableEvent = event;
  }

  /**
   * Gets the code of the last local controllable event.
   *
   * @see #setLastLocalControllableEvent(int) setLastLocalControllableEvent()
   */
  public int getLastLocalControllableEvent()
  {
    return mLastLocalControllableEvent;
  }

  /**
   * Sets the code of the last shared uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable events.
   * Therefore, the range of uncontrollable shared events is from
   * {@link #getLastLocalControllableEvent()}+1 to
   * {@link #getLastSharedUncontrollableEvent()} inclusive.
   */
  public void setLastSharedUncontrollableEvent(final int event)
  {
    mLastSharedUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last shared uncontrollable event.
   *
   * @see #setLastSharedUncontrollableEvent(int) setLastSharedUncontrollableEvent()
   */
  public int getLastSharedUncontrollableEvent()
  {
    return mLastSharedUncontrollableEvent;
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    boolean hasAdded;
    BitSet badStates = new BitSet(numStates);
    do {
      final BitSet newBadStates = new BitSet(numStates);
      findCoreachableStates(newBadStates, badStates);
      newBadStates.flip(0, numStates);
      badStates = newBadStates;
      hasAdded = findMoreBadStates(badStates);
    } while (hasAdded);

    final int dumpState = badStates.nextSetBit(0);
    if(dumpState < 0){
      return false;
    }

    final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
    boolean dumpStateUsed = false;
    for (int state = badStates.nextSetBit(0); state >= 0;
         state = badStates.nextSetBit(state+1)) {
      iter.resetState(state);
      while(iter.advance()){
        final int source = iter.getCurrentSourceState();
        final int event = iter.getCurrentEvent();
        if(!badStates.get(source) && mLastLocalControllableEvent < event &&
          event<= mLastSharedUncontrollableEvent){
          if(state != dumpState){
            iter.remove();
            rel.addTransition(state, event, dumpState);
          }
          dumpStateUsed = true;
        } else iter.remove();
      }
      if(state != dumpState){
        rel.setReachable(state, false);
      }
    }

    if (!dumpStateUsed){
      rel.setReachable(dumpState, false);
    }
    return true;
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeUnreachableTransitions();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }

  private void findCoreachableStates(final BitSet coreachable,
                                     final BitSet badStates)
    throws AbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int defaultID = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    final TIntStack unvisited = new TIntStack();
    // Creates a hash set of all states which can reach a marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isMarked(sourceID, defaultID) && rel.isReachable(sourceID)
        && !badStates.get(sourceID)
          && ! coreachable.get(sourceID)) {
        checkAbort();
        coreachable.set(sourceID);
        unvisited.push(sourceID);
        while (unvisited.size() > 0) {
          final int newSource = unvisited.pop();
          iter.resetState(newSource);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (rel.isReachable(predID) && !badStates.get(predID)
              && !coreachable.get(predID)) {
              coreachable.set(predID);
              unvisited.push(predID);
            }
          }
        }
      }
    }
  }

  private boolean findMoreBadStates(final BitSet badStates)
    throws AbortException
  {
    boolean hasAdded = false;
    final BitSet oldBadStates = (BitSet) badStates.clone();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    iter.resetEvents(EventEncoding.NONTAU, mLastLocalUncontrollableEvent);
    final TIntStack unvisited = new TIntStack();
    for (int state = oldBadStates.nextSetBit(0); state >= 0;
         state = oldBadStates.nextSetBit(state+1)) {
      unvisited.push(state);
      while (unvisited.size()>0) {
        final int current = unvisited.pop();
        iter.resetState(current);
        while(iter.advance()){
          final int source = iter.getCurrentSourceState();
          if(rel.isReachable(source) && !badStates.get(source)){
            hasAdded = true;
            badStates.set(source);
            unvisited.push(source);
          }
        }
      }
    }
    return hasAdded;
  }


  //#########################################################################
  //# Data Members
  private int mLastLocalUncontrollableEvent;
  private int mLastLocalControllableEvent;
  private int mLastSharedUncontrollableEvent;

}
