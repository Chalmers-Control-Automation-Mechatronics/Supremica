//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TObjectIntHashMap;

import java.util.Collection;

import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Rachel Francis
 */

class RemovalOfAlphaMarkingsRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
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
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    final ObserverProjectionTransitionRelation tr =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = tr.getEventInt(tau);
    if (tauID == -1) {
      return autToAbstract;
    }
    final int numStates = tr.getNumberOfStates();
    boolean modified = false;
    int alphaID = tr.getEventInt(mAlphaMarking);
    if (alphaID == -1) {
      alphaID = tr.addProposition(mAlphaMarking, true);
    }

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();

    // creates a hash set of all states which can reach an alpha marked
    // state by tau transitions only
    for (int stateID = 0; stateID < numStates; stateID++) {
      if (tr.isMarked(stateID, alphaID) && !reachableStates.contains(stateID)) {
        unvisitedStates.push(stateID);
        // reachableStates.add(stateID);
        while (unvisitedStates.size() > 0) {
          final int newState = unvisitedStates.pop();
          final TIntHashSet predeccessors = tr.getPredecessors(newState, tauID);
          if (predeccessors != null) {
            final TIntIterator iter = predeccessors.iterator();
            while (iter.hasNext()) {
              final int predID = iter.next();
              if (!reachableStates.contains(predID)) {
                reachableStates.add(predID);
                unvisitedStates.push(predID);
              }
            }
          }
        }
      }
    }
    // removes alpha markings from found states
    for (int stateID = 0; stateID < numStates; stateID++) {
      if (reachableStates.contains(stateID) && tr.isMarked(stateID, alphaID)) {
        tr.markState(stateID, alphaID, false);
        modified = true;
      }
    }
    if (modified) {
      final AutomatonProxy convertedAut = tr.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  AutomatonProxy applyRuleOld(final AutomatonProxy autToAbstract,
                              final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    final ObserverProjectionTransitionRelation rel =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = rel.getEventInt(tau);
    if (tauID == -1) {
      return autToAbstract;
    }
    final int numStates = rel.getNumberOfStates();
    boolean modified = false;
    int alphaID = rel.getEventInt(mAlphaMarking);
    if (alphaID == -1) {
      alphaID = rel.addProposition(mAlphaMarking, true);
    }
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.hasPredecessors(sourceID) && rel.isMarked(sourceID, alphaID)) {
        final TIntHashSet successors = rel.getSuccessors(sourceID, tauID);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int targetID = iter.next();
            if (rel.isMarked(targetID, alphaID)) {
              if (targetID != sourceID) {
                rel.markState(sourceID, alphaID, false);
                modified = true;
                break;
              }
            }
          }
        }
      }
    }
    if (modified) {
      final AutomatonProxy convertedAut = rel.createAutomaton(getFactory());
      mOriginalIntToStateMap = rel.getOriginalIntToStateMap();
      mResultingStateToIntMap = rel.getResultingStateToIntMap();
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
  // # Data Members
  private EventProxy mAlphaMarking;
  private AutomatonProxy mAutToAbstract;
  private StateProxy[] mOriginalIntToStateMap;
  private TObjectIntHashMap<StateProxy> mResultingStateToIntMap;

}
