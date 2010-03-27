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
public abstract class AbstractionRule
{
  // #######################################################################
  // # Constructor
  public AbstractionRule(final ProductDESProxyFactory factory,
                         final AutomatonProxy autToAbstract,
                         final EventProxy tau,
                         final Collection<EventProxy> propositions)
  {
    mFactory = factory;
    mAutToAbstract = autToAbstract;
    mTau = tau;
    mPropositions = propositions;
  }

  // #######################################################################
  // # Simple Access
  protected ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  protected AutomatonProxy getAutomaton()
  {
    return mAutToAbstract;
  }

  protected EventProxy getTau()
  {
    return mTau;
  }

  protected Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }

  // #######################################################################
  // # Auxiliary Methods

  // #######################################################################
  // # Rule Application
  abstract AutomatonProxy applyRule();

  // #######################################################################
  // # Data Members
  private final ProductDESProxyFactory mFactory;
  private final Collection<EventProxy> mPropositions;
  private final AutomatonProxy mAutToAbstract;
  private final EventProxy mTau;

}
