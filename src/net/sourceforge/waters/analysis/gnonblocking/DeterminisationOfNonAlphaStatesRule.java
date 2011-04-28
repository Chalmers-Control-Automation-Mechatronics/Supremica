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
import java.util.List;

import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

class DeterminisationOfNonAlphaStatesRule extends AbstractionRule
{

  //#########################################################################
  //# Constructor
  DeterminisationOfNonAlphaStatesRule(final ProductDESProxyFactory factory,
                                      final KindTranslator translator)
  {
    this(factory, translator, null);

  }

  DeterminisationOfNonAlphaStatesRule(final ProductDESProxyFactory factory,
                                      final KindTranslator translator,
                                      final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions);
    mTransitionRemovalMode =
        ObservationEquivalenceTRSimplifier.TransitionRemoval.NONTAU;
    mTransitionLimit = Integer.MAX_VALUE;
  }


  //#########################################################################
  //# Rule Application
  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode
    (final ObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    mTransitionRemovalMode = mode;
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public ObservationEquivalenceTRSimplifier.TransitionRemoval
    getTransitionRemovalMode()
  {
    return mTransitionRemovalMode;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be constructed by the observation equivalence
   * algorithm. An attempt to store more transitions leads to an
   * {@link net.sourceforge.waters.model.analysis.OverflowException
   * OverflowException}.
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
   * @see {@link #setTransitionLimit(int) setTransitionLimit()}
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }


  //#########################################################################
  //# Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
    throws AnalysisException
  {
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      return autToAbstract;
    }
    mTau = tau;
    mAutToAbstract = autToAbstract;
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, translator, tau, getPropositions(),
                          EventEncoding.FILTER_PROPOSITIONS);
    final int alphaCode = eventEnc.getEventCode(mAlphaMarking);
    mInputEncoding = new StateEncoding(autToAbstract);
    mTr = new ListBufferTransitionRelation
            (autToAbstract, eventEnc,
             mInputEncoding, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(mTr);
    bisimulator.setTransitionRemovalMode(mTransitionRemovalMode);
    bisimulator.setTransitionLimit(mTransitionLimit);
    final NonAlphaDeterminisationTRSimplifier simplifier =
      new NonAlphaDeterminisationTRSimplifier(bisimulator, mTr);
    simplifier.setPropositions(alphaCode, -1);
    if (simplifier.run()) {
      mPartition = simplifier.getResultPartition();
      mTr.removeTauSelfLoops();
      mTr.removeProperSelfLoopEvents();
      mTr.removeRedundantPropositions();
      final ProductDESProxyFactory factory = getFactory();
      mOutputEncoding = new StateEncoding();
      return mTr.createAutomaton(factory, eventEnc, mOutputEncoding);
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    return checker.createDeterminisationOfNonAlphaStatesStep(abstractedAut,
                                                             mAutToAbstract,
                                                             mTau,
                                                             mInputEncoding,
                                                             mPartition,
                                                             mOutputEncoding);
  }

  public void cleanup()
  {
    mTr = null;
    mInputEncoding = null;
    mPartition = null;
    mOutputEncoding = null;
    mAutToAbstract = null;
  }


  //#########################################################################
  //# Data Members
  private ObservationEquivalenceTRSimplifier.TransitionRemoval
    mTransitionRemovalMode;
  private int mTransitionLimit;

  private AutomatonProxy mAutToAbstract;
  private EventProxy mAlphaMarking;
  private EventProxy mTau;
  private ListBufferTransitionRelation mTr;
  private StateEncoding mInputEncoding;
  private List<int[]> mPartition;
  private StateEncoding mOutputEncoding;

}
