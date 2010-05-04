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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.op.StateEncoding;
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
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau) throws AnalysisException
  {
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
    bisimulator
        .setSuppressRedundantHiddenTransitions(mSuppressRedundantHiddenTransitions);
    bisimulator.setTransitionLimit(mTransitionLimit);
    final Collection<int[]> initPartition = createInitialPartition(eventEnc);
    bisimulator.setInitialPartition(initPartition);
    final int alphaCode = eventEnc.getEventCode(mAlphaMarking);
    bisimulator.refineInitialPartition(mTr, alphaCode);
    final boolean modified = bisimulator.run();
    if (modified) {
      mPartition = bisimulator.getResultPartition();
      mTr.merge(mPartition);
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
    return checker.createObservationEquivalenceStep(abstractedAut,
                                                    mAutToAbstract, mTau,
                                                    mInputEncoding, mPartition,
                                                    mOutputEncoding);
  }

  /**
   * Creates an initial partition. This includes a separate equivalence state
   * for every state marked alpha, an equivalence class for all initial states
   * and an equivalence class for the remaining states.
   *
   * @param eventEnc
   * @return A collection containing int[] of equivalence classes.
   */
  private Collection<int[]> createInitialPartition(final EventEncoding eventEnc)
  {
    final Collection<int[]> initialPartition = new HashSet<int[]>();
    final int[] stateCodes = mInputEncoding.getStateCodeMap().getValues();

    final TIntArrayList initialStates = new TIntArrayList();
    final TIntArrayList remainingStates = new TIntArrayList();
    final int alphaCode = eventEnc.getEventCode(mAlphaMarking);

    for (int i = 0; i < stateCodes.length; i++) {
      final int stateCode = stateCodes[i];
      if (mTr.isMarked(stateCode, alphaCode)) {
        // creates a separate equivalence class for every state marked alpha
        final int[] alphaClass = new int[1];
        alphaClass[0] = stateCode;
        initialPartition.add(alphaClass);
      } else if (mTr.isInitial(stateCode)) {
        // creates an equivalence class for all initial states
        initialStates.add(stateCode);
      } else {
        // creates an equivalence class for all states which don't fit into the
        // above two categories
        remainingStates.add(stateCode);
      }
    }
    final int[] initialStatesArray = initialStates.toNativeArray();
    final int[] remainingStatesArray = remainingStates.toNativeArray();

    initialPartition.add(initialStatesArray);
    initialPartition.add(remainingStatesArray);

    return initialPartition;
  }

  // #######################################################################
  // # Data Members
  private boolean mSuppressRedundantHiddenTransitions;
  private int mTransitionLimit;

  private AutomatonProxy mAutToAbstract;
  private EventProxy mAlphaMarking;
  private EventProxy mTau;
  private ListBufferTransitionRelation mTr;
  private StateEncoding mInputEncoding;
  private List<int[]> mPartition;
  private StateEncoding mOutputEncoding;
}
