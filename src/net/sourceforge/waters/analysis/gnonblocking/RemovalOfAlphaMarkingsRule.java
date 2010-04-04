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
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      // TODO Is this correct? I don't see why not, this rule can only remove
      // alpha markings, if the automaton alphabet doesn't contain it why bother
      // running the algorithm (or atleast this is what I think I am
      // checking...)
      return autToAbstract;
    }
    boolean modified = false;
    final ObserverProjectionTransitionRelation rel =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int alphaID = rel.getEventInt(mAlphaMarking);
    final int tauID = rel.getEventInt(tau);
    final int numStates = rel.getNumberOfStates();
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
