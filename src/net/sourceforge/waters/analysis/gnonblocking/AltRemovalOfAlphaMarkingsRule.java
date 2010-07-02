//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AltRemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.MemStateProxy;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.analysis.op.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An alternative implementation of alpha-removal that uses
 * {@link ListBufferTransitionRelation}. Experimental.
 *
 * @author Rachel Francis, Robi Malik
 */

class AltRemovalOfAlphaMarkingsRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  AltRemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  AltRemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
                                final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
  }

  // #######################################################################
  // # Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws OverflowException
  {
    mAutToAbstract = autToAbstract;
    mOriginalIntToStateMap = null;
    mResultingStateToIntMap = null;

    // If the automaton does not use the silent event tau, return it unchanged.
    final Collection<EventProxy> events = autToAbstract.getEvents();
    if (tau == null || !events.contains(tau)) {
      return autToAbstract;
    }

    // Build a transition relation that contains only tau transitions
    // (indexed by predecessor only) and alpha markings. This is enough
    // to do the search, but we cannot use it to build a converted
    // automaton later.
    final Collection<EventProxy> filter = new ArrayList<EventProxy>(2);
    filter.add(tau);
    filter.add(mAlphaMarking);
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, tau, filter, EventEncoding.FILTER_ALL);
    final int tauID = EventEncoding.TAU;
    int alphaID = eventEnc.getEventCode(mAlphaMarking);
    if (alphaID < 0) {
      alphaID = eventEnc.addEvent(mAlphaMarking, true);
    }
    final StateEncoding stateEnc = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation tr =
        new ListBufferTransitionRelation(autToAbstract, eventEnc, stateEnc,
            ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TransitionIterator iter = tr.createPredecessorsReadOnlyIterator();

    // Visit all alpha-marked states. For each of them, to a depth-first
    // search, removing alpha-markings from all states other than the start
    // state of the search.
    final int numStates = tr.getNumberOfStates();
    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();
    boolean modified = false;
    for (int stateID = 0; stateID < numStates; stateID++) {
      if (tr.isMarked(stateID, alphaID)) {
        unvisitedStates.push(stateID);
        reachableStates.add(stateID);
        while (unvisitedStates.size() > 0) {
          final int newStateID = unvisitedStates.pop();
          if (newStateID != stateID && tr.isMarked(newStateID, alphaID)) {
            tr.setMarked(newStateID, alphaID, false);
            modified = true;
          }
          iter.reset(newStateID, tauID);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (reachableStates.add(predID)) {
              unvisitedStates.push(predID);
            }
          }
        }
        reachableStates.clear();
      }
    }

    // If there was a change, build a modified automaton.
    if (modified) {
      mOriginalIntToStateMap = stateEnc.getStatesArray();
      final AutomatonProxy convertedAut =
          createAutomaton(autToAbstract, tr, eventEnc, stateEnc);
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               mOriginalIntToStateMap,
                                               mResultingStateToIntMap);
  }

  // #######################################################################
  // # Auxiliary Methods
  private AutomatonProxy createAutomaton(final AutomatonProxy autToAbstract,
                                         final ListBufferTransitionRelation tr,
                                         final EventEncoding eventEnc,
                                         final StateEncoding stateEnc)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String name = autToAbstract.getName();
    final ComponentKind kind = autToAbstract.getKind();

    final Collection<EventProxy> oldEvents = autToAbstract.getEvents();
    final int numEvents = oldEvents.size();
    final Collection<EventProxy> newEvents =
        new ArrayList<EventProxy>(numEvents + 1);
    final Collection<EventProxy> props = getPropositions();
    boolean containsAlpha = false;
    for (final EventProxy event : oldEvents) {
      if (event.getKind() != EventKind.PROPOSITION) {
        newEvents.add(event);
      } else if (event == mAlphaMarking) {
        containsAlpha = true;
        newEvents.add(event);
      } else if (props.contains(event)) {
        newEvents.add(event);
      }
    }
    if (!containsAlpha) {
      newEvents.add(mAlphaMarking);
    }
    final int alphaID = eventEnc.getEventCode(mAlphaMarking);

    final int numStates = stateEnc.getNumberOfStates();
    final StateProxy[] newStates = new StateProxy[numStates];
    mResultingStateToIntMap = new TObjectIntHashMap<StateProxy>(numStates);
    for (int stateID = 0; stateID < numStates; stateID++) {
      final StateProxy oldState = stateEnc.getState(stateID);
      final boolean init = oldState.isInitial();
      final Collection<EventProxy> oldProps = oldState.getPropositions();
      final boolean oldMarked = oldProps.contains(mAlphaMarking);
      final boolean newMarked = tr.isMarked(stateID, alphaID);
      final StateProxy newState;
      if (oldMarked == newMarked) {
        newState = new MemStateProxy(stateID, init, oldProps);
      } else {
        final int oldNumProps = oldProps.size();
        final Collection<EventProxy> newProps;
        if (oldMarked) {
          newProps = new ArrayList<EventProxy>(oldNumProps - 1);
          for (final EventProxy prop : oldProps) {
            if (prop != mAlphaMarking) {
              newProps.add(prop);
            }
          }
        } else {
          newProps = new ArrayList<EventProxy>(oldNumProps + 1);
          newProps.addAll(oldProps);
          newProps.add(mAlphaMarking);
        }
        newState = new MemStateProxy(stateID, init, newProps);
      }
      newStates[stateID] = newState;
      mResultingStateToIntMap.put(newState, stateID);
    }

    final Collection<TransitionProxy> oldTransitions =
        autToAbstract.getTransitions();
    final int numTrans = oldTransitions.size();
    final Collection<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>(numTrans);
    for (final TransitionProxy oldTrans : oldTransitions) {
      final EventProxy event = oldTrans.getEvent();
      final StateProxy oldSource = oldTrans.getSource();
      final int sourceID = stateEnc.getStateCode(oldSource);
      final StateProxy newSource = newStates[sourceID];
      final StateProxy oldTarget = oldTrans.getTarget();
      final int targetID = stateEnc.getStateCode(oldTarget);
      final StateProxy newTarget = newStates[targetID];
      final TransitionProxy newTrans =
          factory.createTransitionProxy(newSource, event, newTarget);
      newTransitions.add(newTrans);
    }

    return factory.createAutomatonProxy(name, kind, newEvents, Arrays
        .asList(newStates), newTransitions);
  }

  public void cleanup()
  {
    mAutToAbstract = null;
    mOriginalIntToStateMap = null;
    mResultingStateToIntMap = null;
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private AutomatonProxy mAutToAbstract;
  private StateProxy[] mOriginalIntToStateMap;
  private TObjectIntHashMap<StateProxy> mResultingStateToIntMap;

}
