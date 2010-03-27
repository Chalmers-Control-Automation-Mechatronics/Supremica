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
public class ObservationEquivalenceRule extends AbstractionRule
{
  // #######################################################################
  // # Constructor
  public ObservationEquivalenceRule(final ProductDESProxyFactory factory,
                                    final AutomatonProxy autToAbstract,
                                    final EventProxy tau,
                                    final Collection<EventProxy> propositions)
  {
    super(factory, autToAbstract, tau, propositions);

  }

  // #######################################################################
  // # Rule Application
  public AutomatonProxy applyRule()
  {
    final ObserverProjectionTransitionRelation tr =
        new ObserverProjectionTransitionRelation(getAutomaton(),
            getPropositions());
    final ObservationEquivalenceTRSimplifier biSimulator =
        new ObservationEquivalenceTRSimplifier(tr, tr.getEventInt(getTau()));
    final boolean modified = biSimulator.run();
    if (modified) {
      final AutomatonProxy convertedAut = tr.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return getAutomaton();
    }
  }
}
