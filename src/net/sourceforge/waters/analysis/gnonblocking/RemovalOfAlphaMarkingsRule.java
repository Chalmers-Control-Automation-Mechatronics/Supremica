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
public class RemovalOfAlphaMarkingsRule extends AbstractionRule
{
  // #######################################################################
  // # Constructor
  public RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
                                    final AutomatonProxy autToAbstract,
                                    final EventProxy tau,
                                    final Collection<EventProxy> propositions,
                                    final EventProxy alphaMarking)
  {
    super(factory, autToAbstract, tau, propositions);
    mAlphaMarking = alphaMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule()
  {
    final ObserverProjectionTransitionRelation tr =
        new ObserverProjectionTransitionRelation(getAutomaton(),
            getPropositions());

    final int alphaID = tr.getEventInt(mAlphaMarking);
    final int tauID = tr.getEventInt(getTau());

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
    final AutomatonProxy convertedAut = tr.createAutomaton(getFactory());
    return convertedAut;
  }

  // #######################################################################
  // # Data Members
  private final EventProxy mAlphaMarking;
}
