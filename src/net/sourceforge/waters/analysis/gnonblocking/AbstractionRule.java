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

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;


/**
 * @author Rachel Francis
 */

abstract class AbstractionRule
{
  // #######################################################################
  // # Constructor
  AbstractionRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  AbstractionRule(final ProductDESProxyFactory factory,
                  final Collection<EventProxy> propositions)
  {
    mFactory = factory;
    mPropositions = propositions;
  }


  // #######################################################################
  // # Configuration
  ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }

  void setPropositions(final Collection<EventProxy> props)
  {
    mPropositions = props;
  }


  // #######################################################################
  // # Invocation
  CompositionalGeneralisedConflictChecker.Step applyRuleAndCreateStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy autToAbstract,
     final EventProxy tau)
    throws AnalysisException
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
                                    final EventProxy tau)
    throws AnalysisException;

  abstract CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut);


  //#########################################################################
  //# Logging
  Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  // #######################################################################
  // # Data Members
  private final ProductDESProxyFactory mFactory;
  private Collection<EventProxy> mPropositions;

}
