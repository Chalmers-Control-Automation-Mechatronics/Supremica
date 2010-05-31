//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfNoncoreachableStatesRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntStack;
import gnu.trove.TObjectIntHashMap;

import java.util.BitSet;
import java.util.Collection;

import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.analysis.op.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * For a given Automaton applies an abstraction rule which removes states from
 * which neither an alpha or omega state can be reached.
 *
 * This is an alternative experimental implementation based on
 * {@link ListBufferTransitionRelation}.
 *
 * @author Rachel Francis, Robi Malik
 */

class AltRemovalOfNoncoreachableStatesRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  AltRemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  AltRemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory,
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

  EventProxy getDefaultMarking()
  {
    return mDefaultMarking;
  }

  void setDefaultMarking(final EventProxy defaultMarking)
  {
    mDefaultMarking = defaultMarking;
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

    // Set up transition relation containing all transitions, indexed
    // by predecessor. Only include declared propositions.
    final EventEncoding eventEnc =
      new EventEncoding(autToAbstract, tau,
                        getPropositions(), EventEncoding.FILTER_PROPOSITIONS);
    final int alphaID = eventEnc.getEventCode(mAlphaMarking);
    final int defaultID = eventEnc.getEventCode(mDefaultMarking);
    if (alphaID < 0 || defaultID < 0) {
      return autToAbstract;
    }
    final StateEncoding stateEnc = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation tr = new ListBufferTransitionRelation
      (autToAbstract, eventEnc, stateEnc,
       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TransitionIterator iter = tr.createPredecessorsReadOnlyIterator();

    // Collects in a bit vector all states which can reach an omega-marked
    // or alpha-marked state
    final int numStates = stateEnc.getNumberOfStates();
    int nonCoreachableCount = numStates;
    final BitSet coreachableStates = new BitSet(numStates);
    final TIntStack unvisitedStates = new TIntStack();
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if ((tr.isMarked(sourceID, defaultID) ||
           tr.isMarked(sourceID, alphaID)) &&
          !coreachableStates.get(sourceID)) {
        if (--nonCoreachableCount == 0) {
          return autToAbstract;
        }
        unvisitedStates.push(sourceID);
        coreachableStates.set(sourceID);
        while (unvisitedStates.size() > 0) {
          final int stateID = unvisitedStates.pop();
          iter.resetState(stateID);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (!coreachableStates.get(predID)) {
              if (--nonCoreachableCount == 0) {
                return autToAbstract;
              }
              unvisitedStates.push(predID);
              coreachableStates.set(predID);
            }
          }
        }
      }
    }

    // There are non-coreachable states.
    // Mark them as unreachable and build a restricted automaton.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (!coreachableStates.get(sourceID)) {
        tr.setReachable(sourceID, false);
      }
    }
    // After removal of states, some events may become selfloop-only,
    // so let us try to clean up.
    tr.removeTauSelfLoops();
    tr.removeProperSelfLoopEvents();
    tr.removeRedundantPropositions();
    mOriginalIntToStateMap = stateEnc.getStatesArray();
    stateEnc.clear();
    final ProductDESProxyFactory factory = getFactory();
    final AutomatonProxy convertedAut =
      tr.createAutomaton(factory, eventEnc, stateEnc);
    mResultingStateToIntMap = stateEnc.getStateCodeMap();
    return convertedAut;
  }

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               mOriginalIntToStateMap,
                                               mResultingStateToIntMap);
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  private AutomatonProxy mAutToAbstract;
  private StateProxy[] mOriginalIntToStateMap;
  private TObjectIntHashMap<StateProxy> mResultingStateToIntMap;

}
