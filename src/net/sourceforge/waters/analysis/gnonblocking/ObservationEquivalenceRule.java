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
  ObservationEquivalenceRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);

  }

  ObservationEquivalenceRule(final ProductDESProxyFactory factory,
                             final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
    mSuppressRedundantHiddenTransitions = false;
  }

  // #######################################################################
  // # Rule Application
  /**
   * Sets whether redundant hidden transitions can be suppressed in the
   * output automaton. If this is set to <CODE>true</CODE>, the transition
   * minimisation algorithm will attempt to remove tau-transitions that
   * can be replaced by a sequence of two or more other tau-transitions.
   * This only works if the input automaton is already tau-loop free, so it
   * should only be set in this case. The default is <CODE>false</CODE>, which
   * guarantees a correct but not necessarily minimal result for all inputs.
   */
  public void setSuppressRedundantHiddenTransitions(final boolean suppress)
  {
    mSuppressRedundantHiddenTransitions = suppress;
  }

  /**
   * Gets whether redundant hidden transitions can be suppressed in the
   * output automaton.
   * @see {@link #setSuppressRedundantHiddenTransitions(boolean)
   *      setSuppressHiddenEvent()}
   */
  public boolean getSuppressRedundantHiddenTransitions()
  {
    return mSuppressRedundantHiddenTransitions;
  }


  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mTau = tau;
    mAutToAbstract = autToAbstract;
    mTr =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    mBiSimulator =
        new ObservationEquivalenceTRSimplifier(mTr, mTr.getEventInt(tau));
    mBiSimulator.setSuppressRedundantHiddenTransitions
      (mSuppressRedundantHiddenTransitions);
    final boolean modified = mBiSimulator.run();
    if (modified) {
      final AutomatonProxy convertedAut = mTr.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createObservationEquivalenceStep(abstractedAut,
                                                    mAutToAbstract, mTau, mTr,
                                                    mBiSimulator);
  }


  // #######################################################################
  // # Data Members
  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private ObserverProjectionTransitionRelation mTr;
  private ObservationEquivalenceTRSimplifier mBiSimulator;
  private boolean mSuppressRedundantHiddenTransitions;

}
