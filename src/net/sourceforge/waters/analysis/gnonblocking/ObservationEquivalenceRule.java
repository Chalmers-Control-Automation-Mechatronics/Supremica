//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObservationEquivalenceRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

class ObservationEquivalenceRule extends AbstractionRule
{
  // #######################################################################
  // # Constructor
  ObservationEquivalenceRule(final ProductDESProxyFactory factory,
                             final Collection<EventProxy> propositions)
  {
    super(factory, propositions);

  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    final ObserverProjectionTransitionRelation tr =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final ObservationEquivalenceTRSimplifier biSimulator =
        new ObservationEquivalenceTRSimplifier(tr, tr.getEventInt(tau));
    final boolean modified = biSimulator.run();
    if (modified) {
      final AutomatonProxy convertedAut = tr.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
