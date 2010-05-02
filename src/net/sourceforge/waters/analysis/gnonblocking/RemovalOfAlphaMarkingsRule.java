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
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = mTR.getEventInt(tau);
    if (tauID == -1) {
      return autToAbstract;
    }
    final int numStates = mTR.getNumberOfStates();
    boolean modified = false;
    int alphaID = mTR.getEventInt(mAlphaMarking);
    if (alphaID == -1) {
      alphaID = mTR.addProposition(mAlphaMarking, true);
    }

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();

    // performs a backwards search to remove alpha markings from states which
    // satisfy the rule conditions
    for (int stateID = 0; stateID < numStates; stateID++) {
      if (mTR.isMarked(stateID, alphaID)) {
        unvisitedStates.push(stateID);
        while (unvisitedStates.size() > 0) {
          final int newState = unvisitedStates.pop();
          final TIntHashSet predeccessors =
              mTR.getPredecessors(newState, tauID);
          if (predeccessors != null) {
            final TIntIterator iter = predeccessors.iterator();
            while (iter.hasNext()) {
              final int predID = iter.next();
              if (predID != stateID && predID != newState) {
                if (reachableStates.add(predID)) {
                  unvisitedStates.push(predID);
                }
                if (mTR.isMarked(predID, alphaID)) {
                  mTR.markState(predID, alphaID, false);
                  modified = true;
                }
              }
            }
          }
        }
      }
      reachableStates.clear();
    }
    if (modified) {
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
      mOriginalIntToStateMap = mTR.getOriginalIntToStateMap();
      mResultingStateToIntMap = mTR.getResultingStateToIntMap();
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
  ObserverProjectionTransitionRelation mTR = null;

}
