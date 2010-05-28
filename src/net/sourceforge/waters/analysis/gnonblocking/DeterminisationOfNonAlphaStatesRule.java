//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObservationEquivalenceRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier.TransitionRemoval;
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
    mTransitionRemovalMode =
      ObservationEquivalenceTRSimplifier.TransitionRemoval.NONTAU;
    mTransitionLimit = Integer.MAX_VALUE;
  }

  // #######################################################################
  // # Rule Application
  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode(final TransitionRemoval mode)
  {
    mTransitionRemovalMode = mode;
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public TransitionRemoval getTransitionRemovalMode()
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

  // #######################################################################
  // # Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws AnalysisException
  {
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      return autToAbstract;
    }
    mTau = tau;
    mAutToAbstract = autToAbstract;
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, tau, getPropositions(),
            EventEncoding.FILTER_PROPOSITIONS);
    mInputEncoding = new StateEncoding(autToAbstract);
    mTr =
        new ListBufferTransitionRelation(autToAbstract, eventEnc,
            mInputEncoding, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mTr.reverse();
    final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(mTr);
    bisimulator.setTransitionRemovalMode(mTransitionRemovalMode);
    bisimulator.setTransitionLimit(mTransitionLimit);
    final Collection<int[]> initPartition = createInitialPartition(eventEnc);
    if (initPartition == null) {
      return autToAbstract;
    }
    bisimulator.setInitialPartition(initPartition);
    bisimulator.refineInitialPartitionBasedOnInitialStates();
    bisimulator.run();
    final boolean modified = bisimulator.applyResultPartition();
    if (modified) {
      mPartition = bisimulator.getResultPartition();
      mTr.removeTauSelfLoops();
      mTr.removeProperSelfLoopEvents();
      final ProductDESProxyFactory factory = getFactory();
      mOutputEncoding = new StateEncoding();
      mTr.reverse();
      return mTr.createAutomaton(factory, eventEnc, mOutputEncoding);
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createDeterminisationOfNonAlphaStatesStep(abstractedAut,
                                                             mAutToAbstract,
                                                             mTau,
                                                             mInputEncoding,
                                                             mPartition,
                                                             mOutputEncoding);
  }

  /**
   * Creates an initial partition. This includes a separate equivalence class
   * for every state marked alpha, and an equivalence class which contains all
   * the remaining states.
   *
   * @param eventEnc
   * @return A collection containing int[] of equivalence classes.
   */
  private Collection<int[]> createInitialPartition(final EventEncoding eventEnc)
  {
    final List<int[]> initialPartition = new ArrayList<int[]>();
    final int numStates = mTr.getNumberOfStates();

    final TIntArrayList remainingStates = new TIntArrayList();
    final int alphaCode = eventEnc.getEventCode(mAlphaMarking);

    for (int stateCode = 0; stateCode < numStates; stateCode++) {
      if (mTr.isMarked(stateCode, alphaCode)) {
        // creates a separate equivalence class for every state marked alpha
        final int[] alphaClass = new int[1];
        alphaClass[0] = stateCode;
        initialPartition.add(alphaClass);
      } else {
        // creates an equivalence class for all states which don't fit into the
        // above two categories
        remainingStates.add(stateCode);
      }
    }
    if (remainingStates.size() == 0) {
      return null;
    }
    final int[] remainingStatesArray = remainingStates.toNativeArray();
    initialPartition.add(remainingStatesArray);

    return initialPartition;
  }

  // #######################################################################
  // # Data Members
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
