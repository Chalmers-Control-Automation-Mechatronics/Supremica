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
 * For a given Automaton applies an abstraction rule which removes states from
 * which neither an alpha or omega state can be reached.
 *
 * @author Rachel Francis
 */

class RemovalOfNoncoreachableStatesRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory,
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
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    if (!autToAbstract.getEvents().contains(mAlphaMarking)
        && !autToAbstract.getEvents().contains(mDefaultMarking)) {
      return autToAbstract;
    }
    boolean modified = false;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int alphaID = mTR.getEventInt(mAlphaMarking);
    final int defaultID = mTR.getEventInt(mDefaultMarking);
    final int numStates = mTR.getNumberOfStates();

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();

    // creates a hash set of all states which can reach an omega marked or alpha
    // marked state
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if ((mTR.isMarked(sourceID, defaultID) || mTR.isMarked(sourceID, alphaID))
          && !reachableStates.contains(sourceID)) {
        unvisitedStates.push(sourceID);
        reachableStates.add(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          // TODO don't put unreachable states on the stack in the first place.
          if (mTR.hasPredecessors(newSource)) {
            final TIntHashSet[] predecessors =
                mTR.getAllPredecessors(newSource);
            for (int e = 0; e < predecessors.length; e++) {
              final TIntHashSet preds = predecessors[e];
              if (preds != null) {
                final TIntIterator iter = preds.iterator();
                while (iter.hasNext()) {
                  final int predID = iter.next();
                  // TODO do not add if the state is already in reachable
                  reachableStates.add(predID);
                  unvisitedStates.push(predID);
                }
              }
            }
          }
        }
      }
    }
    // removes states which can not reach a state marked alpha or omega
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (!reachableStates.contains(sourceID)) {
        // TODO don't visit unreachable states in the first place.
        if (mTR.hasPredecessors(sourceID)) {
          final TIntHashSet[] predecessors = mTR.getAllPredecessors(sourceID);
          for (int e = 0; e < predecessors.length; e++) {
            final TIntHashSet preds = predecessors[e];
            if (preds != null) {
              final TIntIterator iter = preds.iterator();
              while (iter.hasNext()) {
                final int predID = iter.next();
                // TODO why move successors? Try removeAllIncoming() ...
                // Then the loop below should not be needed.
                mTR.moveAllSuccessors(sourceID, predID);
                modified = true;
              }
            }
          }
        }
        final TIntHashSet[] successors = mTR.getAllSuccessors(sourceID);
        if (successors.length > 0) {
          for (int e = 0; e < successors.length; e++) {
            final TIntHashSet targets = successors[e];
            if (targets != null) {
              final TIntIterator iter = targets.iterator();
              while (iter.hasNext()) {
                final int targetID = iter.next();
                mTR.moveAllPredeccessors(sourceID, targetID);
                modified = true;
              }
            }
          }
        }
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
    return null;
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  @SuppressWarnings("unused")
  private AutomatonProxy mAutToAbstract;
  private ObserverProjectionTransitionRelation mTR;
}
