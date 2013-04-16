//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObservationEquivalenceRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

class ObservationEquivalenceRule extends TRSimplifierAbstractionRule
{

  //#######################################################################
  //# Constructor
  ObservationEquivalenceRule(final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    this(factory, translator, null);

  }

  ObservationEquivalenceRule(final ProductDESProxyFactory factory,
                             final KindTranslator translator,
                             final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions,
          new ObservationEquivalenceTRSimplifier());
    mTransitionRemovalMode =
        ObservationEquivalenceTRSimplifier.TransitionRemoval.NONTAU;
    mTransitionLimit = Integer.MAX_VALUE;
  }


  //#########################################################################
  //# Configuration
  @Override
  ObservationEquivalenceTRSimplifier getSimplifier()
  {
    return (ObservationEquivalenceTRSimplifier) super.getSimplifier();
  }

  /**
   * Sets the mode which redundant transitions are to be removed.
   *
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode
    (final ObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    mTransitionRemovalMode = mode;
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   *
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


  //#######################################################################
  //# Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws AnalysisException
  {
    mTau = tau;
    mAutToAbstract = autToAbstract;
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, translator, tau, getPropositions(),
            EventEncoding.FILTER_PROPOSITIONS);
    // final int codeOfTau = eventEnc.getEventCode(tau);
    mInputEncoding = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (autToAbstract, eventEnc, mInputEncoding,
       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final ObservationEquivalenceTRSimplifier bisimulator = getSimplifier();
    bisimulator.setTransitionRelation(rel);
    try {
      bisimulator.setTransitionRemovalMode(mTransitionRemovalMode);
      bisimulator.setTransitionLimit(mTransitionLimit);
      final boolean modified = bisimulator.run();
      if (modified) {
        mPartition = bisimulator.getResultPartition();
        final ProductDESProxyFactory factory = getFactory();
        rel.removeRedundantPropositions();
        mOutputEncoding = new StateEncoding();
        return rel.createAutomaton(factory, eventEnc, mOutputEncoding);
      } else {
        return autToAbstract;
      }
    } catch (final OutOfMemoryError error) {
      bisimulator.reset();
      throw new OverflowException(error);
    } finally {
      bisimulator.reset();
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createObservationEquivalenceStep(abstractedAut,
                                                    mAutToAbstract, mTau,
                                                    mInputEncoding, mPartition,
                                                    mOutputEncoding);
  }

  public void cleanup()
  {
    mInputEncoding = null;
    mPartition = null;
    mOutputEncoding = null;
    mAutToAbstract = null;
  }


  //#######################################################################
  //# Data Members
  private ObservationEquivalenceTRSimplifier.TransitionRemoval
    mTransitionRemovalMode;
  private int mTransitionLimit;

  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private StateEncoding mInputEncoding;
  private List<int[]> mPartition;
  private StateEncoding mOutputEncoding;

}
