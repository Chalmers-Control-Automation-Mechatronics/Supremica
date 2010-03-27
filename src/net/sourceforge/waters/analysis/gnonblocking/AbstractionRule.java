//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AbstractionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

abstract class AbstractionRule
{
  // #######################################################################
  // # Constructor
  AbstractionRule(final ProductDESProxyFactory factory,
                  final Collection<EventProxy> propositions)
  {
    mFactory = factory;
    mPropositions = propositions;
  }


  // #######################################################################
  // # Simple Access
  ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }


  // #######################################################################
  // # Invocation
  CompositionalGeneralisedConflictChecker.Step applyRuleAndCreateStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy autToAbstract,
     final EventProxy tau)
  {
    final AutomatonProxy abstractedAut = applyRule(autToAbstract, tau);
    if (abstractedAut != autToAbstract) {
      return createStep(checker, abstractedAut);
    } else {
      return null;
    }
  }


  // #######################################################################
  // # Rule Application
  abstract AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                                    final EventProxy tau);

  abstract CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut);


  // #######################################################################
  // # Data Members
  private final ProductDESProxyFactory mFactory;
  private final Collection<EventProxy> mPropositions;

}
