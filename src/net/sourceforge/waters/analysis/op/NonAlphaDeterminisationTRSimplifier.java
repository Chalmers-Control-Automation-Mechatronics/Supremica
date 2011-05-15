//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   NonAlphaDeterminisationTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier.TransitionRemoval;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * @author Rachel Francis, Robi Malik
 */

public class NonAlphaDeterminisationTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public NonAlphaDeterminisationTRSimplifier
    (final ObservationEquivalenceTRSimplifier bisimulator)
  {
    this(bisimulator, null);
  }

  public NonAlphaDeterminisationTRSimplifier
    (final ObservationEquivalenceTRSimplifier bisimulator,
     final ListBufferTransitionRelation rel)
  {
    super(rel);
    mBisimulator = bisimulator;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier#TransitionRemoval
   */
  public void setTransitionRemovalMode(final TransitionRemoval mode)
  {
    mBisimulator.setTransitionRemovalMode(mode);
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier#TransitionRemoval
   */
  public TransitionRemoval getTransitionRemovalMode()
  {
    return mBisimulator.getTransitionRemovalMode();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  public boolean run()
    throws AnalysisException
  {
    if (!hasNonPreconditionMarkedStates()) {
      return false;
    }
    setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.reverse();
    try {
      mBisimulator.setTransitionRelation(rel);
      List<int[]> partition = createInitialPartition();
      if (partition == null) {
        return false;
      }
      mBisimulator.setInitialPartition(partition);
      mBisimulator.refineInitialPartitionBasedOnInitialStates();
      final boolean modified = mBisimulator.run();
      partition = mBisimulator.getResultPartition();
      setResultPartitionList(partition);
      return modified;
    } finally {
      mBisimulator.reset();
      rel.reverse();
    }
  }

  @Override
  public void reset()
  {
    mBisimulator.reset();
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
