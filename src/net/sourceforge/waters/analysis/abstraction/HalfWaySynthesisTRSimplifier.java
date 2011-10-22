//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifier
//###########################################################################
//# $Id: 44365f9ce27545868ec37b61ed041ded9c304a22 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.BitSet;
import gnu.trove.TIntHashSet;
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

  //#########################################################################
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


  //#########################################################################
  //# Configuration
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

  public void setRenamedEvents(final TIntHashSet renamedEventIndexes)
  {
    mRenamedEvents = renamedEventIndexes;
  }

  public TIntHashSet getRenamedEvents()
  {
    return mRenamedEvents;
  }

  public ListBufferTransitionRelation getSupervisor(){
    return mSupervisor;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public void reset(){
    super.reset();
    mSupervisor = null;
  }

  @Override
  protected void setUp() throws AnalysisException{
    super.setUp();
    mSupervisor = null;
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int defaultID = getDefaultMarkingID();
    if (defaultID < 0){
      return false;
    }
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
    if (dumpState < 0) {
      return false;
    }

    final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
    boolean dumpStateUsed = false;
    boolean changed = false;
    boolean addSupervisor = false;
    for (int state = badStates.nextSetBit(0); state >= 0;
         state = badStates.nextSetBit(state+1)) {
      if(rel.isReachable(state)) {
        iter.resetState(state);
        while (iter.advance()) {
          final int source = iter.getCurrentSourceState();
          final int event = iter.getCurrentEvent();
          if (badStates.get(source)) {
            iter.remove();
            changed = true;
          } else if (mLastLocalControllableEvent < event &&
            event <= mLastSharedUncontrollableEvent) {
            // shared uncontrollable
            if (state != dumpState) {
              iter.remove();
              rel.addTransition(source, event, dumpState);
              changed = true;
            }
            dumpStateUsed = true;
          } else if (mRenamedEvents != null &&
                     mRenamedEvents.contains(event)) {
            // local or shared controllable, renamed
            if (state != dumpState) {
              iter.remove();
              rel.addTransition(source, event, dumpState);
              changed = true;
            }
            dumpStateUsed = true;
            addSupervisor = true;
          } else {
            // local or shared controllable, not renamed
            // (cannot be local uncontrollable, otherwise source would be bad)
            iter.remove();
            changed = true;
            addSupervisor = true;
          }
        }
        if (state != dumpState) {
          rel.setReachable(state, false);
          changed = true;
        }
        if(rel.isInitial(state)){
          rel.setReachable(state, false);
          changed = true;
          addSupervisor = true;
        }

      }
    }

    if(addSupervisor){
      mSupervisor = new ListBufferTransitionRelation
          (rel, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (dumpStateUsed) {
        mSupervisor.removeOutgoingTransitions(dumpState);
      } else {
        mSupervisor.setReachable(dumpState, false);
      }
      mSupervisor.checkReachability();
      mSupervisor.removeProperSelfLoopEvents();
    }

    dumpStateUsed = true;
    iter.resetState(dumpState);
    while (iter.advance()) {
      final int event = iter.getCurrentEvent();
      if ((event > mLastLocalUncontrollableEvent &&
           event <= mLastLocalControllableEvent) ||
          (event > mLastSharedUncontrollableEvent)) {
        iter.remove();
        dumpStateUsed = false;
      }
    }

    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (dumpStateUsed) {
      changed |= rel.removeOutgoingTransitions(dumpState);
    } else {
      rel.setReachable(dumpState, false);
      changed = true;
    }
    changed |= rel.checkReachability();
    changed |= rel.removeProperSelfLoopEvents();

    return changed;
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
  private TIntHashSet mRenamedEvents;

  private ListBufferTransitionRelation mSupervisor;

}
