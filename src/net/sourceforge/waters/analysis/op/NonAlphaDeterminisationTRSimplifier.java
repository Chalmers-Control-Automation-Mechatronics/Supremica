//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   NonAlphaDeterminisationTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * @author Rachel Francis, Robi Malik
 */

public class NonAlphaDeterminisationTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public NonAlphaDeterminisationTRSimplifier()
  {
    this(null);
  }

  public NonAlphaDeterminisationTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                                Equivalence.OBSERVATION_EQUIVALENCE);
    mBisimulator.setAppliesPartitionAutomatically(false);
    mBisimulator.setStatistics(null);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode
    (final ObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    mBisimulator.setTransitionRemovalMode(mode);
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public ObservationEquivalenceTRSimplifier.TransitionRemoval
    getTransitionRemovalMode()
  {
    return mBisimulator.getTransitionRemovalMode();
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mBisimulator.setTransitionLimit(limit);
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mBisimulator.getTransitionLimit();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public void reset()
  {
    mBisimulator.reset();
    super.reset();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    if (!hasNonPreconditionMarkedStates()) {
      return false;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.reverse();
    mBisimulator.setTransitionRelation(rel);
    List<int[]> partition = createInitialPartition();
    if (partition == null) {
      return false;
    }
    mBisimulator.setUpInitialPartition(partition);
    mBisimulator.refinePartitionBasedOnInitialStates();
    final boolean modified = mBisimulator.run();
    partition = mBisimulator.getResultPartition();
    setResultPartitionList(partition);
    applyResultPartitionAutomatically();
    rel.reverse();
    return modified;
  }

  @Override
  public void applyResultPartition()
  throws AnalysisException
  {
    mBisimulator.applyResultPartition();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether the transition relation has at least two states <I>not</I>
   * marked with the precondition marking. If all but one states are marked,
   * there is no need to try and simplify.
   */
  private boolean hasNonPreconditionMarkedStates()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int alphaID = getPreconditionMarkingID();
    final int numStates = rel.getNumberOfStates();
    int numNonAlphaStates = 0;
    for (int state = 0; state < numStates; state++) {
      if (!rel.isMarked(state, alphaID)) {
        if (++numNonAlphaStates >= 2) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates an initial partition. This includes a separate equivalence class
   * for every state marked alpha, and an equivalence class which contains all
   * the remaining states.
   * @return A list containing int[] representing equivalence classes.
   */
  private List<int[]> createInitialPartition()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final List<int[]> initialPartition = new ArrayList<int[]>();
    final TIntArrayList remainingStates = new TIntArrayList();
    final int alphaCode = getPreconditionMarkingID();
    for (int stateCode = 0; stateCode < numStates; stateCode++) {
      if (rel.isMarked(stateCode, alphaCode)) {
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


  //#########################################################################
  //# Data Members
  private final ObservationEquivalenceTRSimplifier mBisimulator;

}
