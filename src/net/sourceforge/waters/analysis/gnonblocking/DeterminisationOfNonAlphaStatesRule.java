//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObservationEquivalenceRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntObjectHashMap;

import java.util.Collection;

import net.sourceforge.waters.analysis.op.DeterminisationOfNonAlphaStatesTRSimplifier;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

class DeterminisationOfNonAlphaStatesRule extends AbstractionRule
{
  // #######################################################################
  // # Constructor
  DeterminisationOfNonAlphaStatesRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);

  }

  DeterminisationOfNonAlphaStatesRule(final ProductDESProxyFactory factory,
                                      final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
    mSuppressRedundantHiddenTransitions = false;
    mTransitionLimit = Integer.MAX_VALUE;
  }

  // #######################################################################
  // # Rule Application
  /**
   * Sets whether redundant hidden transitions can be suppressed in the output
   * automaton. If this is set to <CODE>true</CODE>, the transition minimisation
   * algorithm will attempt to remove tau-transitions that can be replaced by a
   * sequence of two or more other tau-transitions. This only works if the input
   * automaton is already tau-loop free, so it should only be set in this case.
   * The default is <CODE>false</CODE>, which guarantees a correct but not
   * necessarily minimal result for all inputs.
   */
  public void setSuppressRedundantHiddenTransitions(final boolean suppress)
  {
    mSuppressRedundantHiddenTransitions = suppress;
  }

  /**
   * Gets whether redundant hidden transitions can be suppressed in the output
   * automaton.
   *
   * @see {@link #setSuppressRedundantHiddenTransitions(boolean)
   *      setSuppressHiddenEvent()}
   */
  public boolean getSuppressRedundantHiddenTransitions()
  {
    return mSuppressRedundantHiddenTransitions;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be constructed by the observation equivalence
   * algorithm. An attempt to store more transitions leads to an
   * {@link net.sourceforge.waters.model.analysis.OverflowException
   * OverflowException}.
   *
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   *
   * @see {@link #setTransitionLimit(int) setTransitionLimit()}
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau) throws AnalysisException
  {
    mTau = tau;
    mAutToAbstract = autToAbstract;
    mTr =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int codeOfTau = mTr.getEventInt(tau);
    final DeterminisationOfNonAlphaStatesTRSimplifier bisimulator =
        new DeterminisationOfNonAlphaStatesTRSimplifier(mTr, codeOfTau);
    bisimulator
        .setSuppressRedundantHiddenTransitions(mSuppressRedundantHiddenTransitions);
    bisimulator.setTransitionLimit(mTransitionLimit);
    final boolean modified = bisimulator.run();
    if (modified) {
      final Collection<int[]> partition = bisimulator.getResultPartition();
      final int size = partition.size();
      mClassMap = new TIntObjectHashMap<int[]>(size);
      mTr.mergePartition(partition, codeOfTau, mClassMap);
      final int suppress = mSuppressRedundantHiddenTransitions ? codeOfTau : -1;
      mTr.removeSelfLoopEvents(suppress);
      return mTr.createAutomaton(getFactory());
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
                                                    mClassMap);
  }

  // #######################################################################
  // # Data Members
  private boolean mSuppressRedundantHiddenTransitions;
  private int mTransitionLimit;

  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private ObserverProjectionTransitionRelation mTr;
  private TIntObjectHashMap<int[]> mClassMap;

}
