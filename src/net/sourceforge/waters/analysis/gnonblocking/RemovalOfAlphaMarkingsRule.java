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

import java.util.Collection;

import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

class RemovalOfAlphaMarkingsRule extends AbstractionRule
{
  // #######################################################################
  // # Constructor
  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
                             final Collection<EventProxy> propositions,
                             final EventProxy alphaMarking)
  {
    super(factory, propositions);
    mAlphaMarking = alphaMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    final ObserverProjectionTransitionRelation tr =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int alphaID = tr.getEventInt(mAlphaMarking);
    final int tauID = tr.getEventInt(tau);
    final int numStates = tr.getNumberOfStates();
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (tr.hasPredecessors(sourceID) && tr.isMarked(sourceID, alphaID)) {
        final TIntHashSet successors = tr.getSuccessors(sourceID, tauID);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int targetID = iter.next();
            if (tr.isMarked(targetID, alphaID)) {
              if (targetID != sourceID) {
                tr.markState(sourceID, false, alphaID);
                break;
              }
            }
          }
        }
      }
    }
    // TODO Check if there was no change, and suppress automaton construction
    // in this case.
    final AutomatonProxy convertedAut = tr.createAutomaton(getFactory());
    return convertedAut;
  }

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    // TODO Auto-generated method stub
    return null;
  }


  // #######################################################################
  // # Data Members
  private final EventProxy mAlphaMarking;
}
