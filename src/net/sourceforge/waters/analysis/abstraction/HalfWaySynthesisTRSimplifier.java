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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class HalfWaySynthesisTRSimplifier
  extends AbstractSynthesisTRSimplifier
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
   * Sets the set of renamed event indexes.
   * Renamed controllable events are treated specially for the benefit
   * of compositional synthesis. Renamed controllable transitions to a
   * dump state are not removed in the synthesised supervisor to facilitate
   * composition with distinguishers; these transitions are only removed in
   * the abstraction.
   * @param renamedEventIndexes Set of proper events indexes to be considered
   *                            as renamed.
   */
  public void setRenamedEvents(final TIntHashSet renamedEventIndexes)
  {
    mRenamedEvents = renamedEventIndexes;
  }

  /**
   * Gets the set of renamed event indexes.
   * @see #setRenamedEvents(TIntHashSet) setRenamedEvents()
   */
  public TIntHashSet getRenamedEvents()
  {
    return mRenamedEvents;
  }

  /**
   * Gets the supervisor computed by the last run of this simplifier.
   * The supervisor returned may contain transitions to a dump state,
   * and therefore is referred to as a pseudo-supervisor. Transitions
   * with <I>renamed</I> controllable events leading to blocking states
   * are not removed from the supervisor to facilitate composition with
   * distinguishers.
   * @return Transition relation representing supervisor, or <CODE>null</CODE>
   *         if no controllable events need to be disabled.
   * @see #setRenamedEvents(TIntHashSet) setRenamedEvents()
   */
  public ListBufferTransitionRelation getPseudoSupervisor()
  {
    return mPseudoSupervisor;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public void reset(){
    super.reset();
    mPseudoSupervisor = null;
  }

  @Override
  protected void setUp() throws AnalysisException{
    super.setUp();
    mPseudoSupervisor = null;
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

    // 1. Do synthesis --- find bad states
    BitSet badStates = new BitSet(numStates);
    do {
      final BitSet newBadStates = new BitSet(numStates);
      findCoreachableStates(newBadStates, badStates);
      newBadStates.flip(0, numStates);
      badStates = newBadStates;
    } while (findMoreBadStates(badStates));
    // If there are no bad states, no need to synthesise
    final int dumpState = badStates.nextSetBit(0);
    if (dumpState < 0) {
      return false;
    }

    // 2. Check transitions from safe states to bad states.
    //  If there are controllable transitions, we need to construct
    //  an abstraction and a supervisor.
    //  If there are only uncontrollable transitions, we do not need a
    //  supervisor; we may need an abstraction if states are merged or
    //  transitions deleted.
    //  While testing, merge bad states into dump state and delete outgoing
    //  transitions from bad states,
    boolean needAbstraction = false;
    boolean needSupervisor = false;
    final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
    for (int state = badStates.nextSetBit(0); state >= 0;
         state = badStates.nextSetBit(state+1)) {
      if (rel.isInitial(state)) {
        // some initial state is bad --- supervisor cannot exist.
        setAllStatesUnreachable();
        return true;
      } else if (rel.isReachable(state)) {
        iter.resetState(state);
        while (iter.advance()) {
          final int source = iter.getCurrentSourceState();
          if (badStates.get(source)) {
            needAbstraction = true;
            iter.remove();
            continue;
          }
          final int event = iter.getCurrentEvent();
          if (isControllable(event)) {
            needAbstraction = needSupervisor = true;
          }
        }
        if (state != dumpState) {
          needAbstraction = true;
          rel.moveIncomingTransitions(state, dumpState);
        }
      }
    }
    if (!needAbstraction) {
      return false;
    }
    rel.setMarked(dumpState, defaultID, false);

    // 3. Create the pseudo-supervisor.
    //  The supervisor is obtained by redirecting all renamed controllable
    //  transitions that lead to a bad state to the dump state.
    //  Other transitions leading to a bad state are deleted.
    if (needSupervisor) {
      mPseudoSupervisor = new ListBufferTransitionRelation
        (rel, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mPseudoSupervisor.removeOutgoingTransitions(dumpState);
      iter.resetState(dumpState);
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
        if (!isRenamedControllable(event)) {
          final int source = iter.getCurrentSourceState();
          mPseudoSupervisor.removeTransition(source, event, dumpState);
        }
      }
      mPseudoSupervisor.checkReachability();
      mPseudoSupervisor.removeProperSelfLoopEvents();
      mPseudoSupervisor.removeRedundantPropositions();
    }

    // 4. Create the abstraction.
    //  The abstraction is obtained by redirecting all uncontrollable
    //  transitions that lead to a bad state to the dump state.
    //  Controllable transitions leading to a bad state are deleted.
    iter.resetState(dumpState);
    while (iter.advance()) {
      final int event = iter.getCurrentEvent();
      if (isControllable(event)) {
        iter.remove();
      }
    }
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.removeOutgoingTransitions(dumpState);
    rel.checkReachability();
    rel.removeProperSelfLoopEvents();
    rel.removeRedundantPropositions();
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
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
    iterateLocalUncontrollable(iter);
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

  private void setAllStatesUnreachable()
  {
    try {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final String name = rel.getName();
      final int numStates = rel.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        rel.setReachable(state, false);
      }
      final int numEvents = rel.getNumberOfProperEvents();
      for (int event = 0; event < numEvents; event++) {
        rel.setUsedEvent(event, false);
      }
      rel.removeRedundantPropositions();
      final ComponentKind kind = rel.getKind();
      final ListBufferTransitionRelation abstraction =
        new ListBufferTransitionRelation(name, kind, 0, 0, 0,
                                         ListBufferTransitionRelation.
                                         CONFIG_SUCCESSORS);
      setTransitionRelation(abstraction);
      mPseudoSupervisor =
        new ListBufferTransitionRelation(name, ComponentKind.SUPERVISOR,
                                         0, 0, 0,
                                         ListBufferTransitionRelation.
                                         CONFIG_SUCCESSORS);
    } catch (final OverflowException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  private boolean isRenamedControllable(final int event)
  {
    return
      isControllable(event) &&
      mRenamedEvents != null &&
      mRenamedEvents.contains(event);
  }


  //#########################################################################
  //# Data Members
  private TIntHashSet mRenamedEvents;

  private ListBufferTransitionRelation mPseudoSupervisor;

}
