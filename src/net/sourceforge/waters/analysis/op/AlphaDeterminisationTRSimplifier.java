//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AlphaDeterminisationTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * @author Robi Malik
 */

public class AlphaDeterminisationTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public AlphaDeterminisationTRSimplifier
    (final AbstractObservationEquivalenceTRSimplifier bisimulator)
  {
    this(bisimulator, null);
  }

  public AlphaDeterminisationTRSimplifier
    (final AbstractObservationEquivalenceTRSimplifier bisimulator,
     final ListBufferTransitionRelation rel)
  {
    super(rel);
    mBisimulator = bisimulator;
    mTransitionRemovalMode =
      AbstractObservationEquivalenceTRSimplifier.TransitionRemoval.NONTAU;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode
    (final AbstractObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    mTransitionRemovalMode = mode;
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public AbstractObservationEquivalenceTRSimplifier.TransitionRemoval
    getTransitionRemovalMode()
  {
    return mTransitionRemovalMode;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
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
    if (getPreconditionMarkingID() < 0) {
      // If there are no alpha-markings,
      // other rules will have simplified this transition relation already.
      return false;
    }
    final AbstractObservationEquivalenceTRSimplifier.Equivalence eq =
      mBisimulator.getEquivalence();
    final AbstractObservationEquivalenceTRSimplifier.TransitionRemoval mode =
      mBisimulator.getTransitionRemovalMode();
    final boolean apply = mBisimulator.getAppliesPartitionAutomatically();
    final TRSimplifierStatistics stats = mBisimulator.getStatistics();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    try {
      mBisimulator.setEquivalence(AbstractObservationEquivalenceTRSimplifier.
                                  Equivalence.OBSERVATION_EQUIVALENCE);
      mBisimulator.setTransitionRemovalMode(mTransitionRemovalMode);
      mBisimulator.setAppliesPartitionAutomatically(false);
      mBisimulator.setStatistics(null);
      mBisimulator.setTransitionRelation(rel);
      long mask = 0;
      final int omega = getDefaultMarkingID();
      if (omega >= 0) {
        mask = rel.addMarking(mask, omega);
      }
      mBisimulator.setUpInitialPartitionBasedOnMarkings(mask);
      boolean modified = mBisimulator.run();
      if (!modified) {
        return false;
      }
      List<int[]> partition = mBisimulator.getResultPartition();
      mBisimulator.reset();
      rel.reverse();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      switch (mTransitionRemovalMode) {
      case NONTAU:
      case ALL:
        mBisimulator.setTransitionRemovalMode
          (AbstractObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER);
        break;
      default:
        break;
      }
      mBisimulator.setTransitionRelation(rel);
      mBisimulator.setUpInitialPartition(partition);
      mBisimulator.refinePartitionBasedOnInitialStates();
      modified = mBisimulator.run();
      partition = mBisimulator.getResultPartition();
      setResultPartitionList(partition);
      applyResultPartitionAutomatically();
      rel.reverse();
      return modified;
    } finally {
      mBisimulator.setEquivalence(eq);
      mBisimulator.setTransitionRemovalMode(mode);
      mBisimulator.setAppliesPartitionAutomatically(apply);
      mBisimulator.setStatistics(stats);
    }
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    mBisimulator.applyResultPartition();
  }


  //#########################################################################
  //# Data Members
  private final AbstractObservationEquivalenceTRSimplifier mBisimulator;

  private AbstractObservationEquivalenceTRSimplifier.TransitionRemoval
    mTransitionRemovalMode;

}
