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

import java.util.Collection;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * For a given Automaton applies an abstraction rule which removes the default
 * marking proposition from states which are not reachable from any state with
 * an alpha marking.
 *
 * @author Rachel Francis
 */

class RemovalOfDefaultMarkingsRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfDefaultMarkingsRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfDefaultMarkingsRule(final ProductDESProxyFactory factory,
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
  {
    mAutToAbstract = autToAbstract;
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      return autToAbstract;
    }
    boolean modified = false;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int alphaID = mTR.getEventInt(mAlphaMarking);
    int defaultID = mTR.getEventInt(mDefaultMarking);
    if (defaultID == -1) {
      defaultID = mTR.addProposition(mDefaultMarking, true);
    }
    final int numStates = mTR.getNumberOfStates();

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();

    // creates a hash set of all states which are reachable from an alpha marked
    // state
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (mTR.isMarked(sourceID, alphaID)
          && !reachableStates.contains(sourceID)) {
        unvisitedStates.push(sourceID);
        reachableStates.add(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          final TIntHashSet[] successors = mTR.getAllSuccessors(newSource);
          if (successors != null) {
            for (int e = 0; e < successors.length; e++) {
              final TIntHashSet targets = successors[e];
              if (targets != null) {
                final TIntIterator iter = targets.iterator();
                while (iter.hasNext()) {
                  final int targetID = iter.next();
                  if (!reachableStates.contains(targetID)) {
                    reachableStates.add(targetID);
                    unvisitedStates.push(targetID);
                  }
                }
              }
            }
          }
        }
      }
    }
    // removes default marking from all states which were not found as reachable
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (!reachableStates.contains(sourceID)
          && mTR.isMarked(sourceID, defaultID)) {
        mTR.markState(sourceID, defaultID, false);
        modified = true;
      }
    }
    if (modified) {
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
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
                                               mTR.getOriginalIntToStateMap(),
                                               mTR.getResultingStateToIntMap());
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  private AutomatonProxy mAutToAbstract;
  private ObserverProjectionTransitionRelation mTR;
}
