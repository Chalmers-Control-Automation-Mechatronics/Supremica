//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AlphaDeterminisationTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * @author Robi Malik
 */

public class AlphaDeterminisationTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public AlphaDeterminisationTRSimplifier()
  {
    this(null);
  }

  public AlphaDeterminisationTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setAppliesPartitionAutomatically(false);
    mBisimulator.setStatistics(null);
    mTransitionRemovalMode = mBisimulator.getTransitionRemovalMode();
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
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
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
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    if (getPreconditionMarkingID() < 0) {
      // If there are no alpha-markings,
      // other rules will have simplified this transition relation already.
      return false;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mBisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                                Equivalence.WEAK_OBSERVATION_EQUIVALENCE);
    mBisimulator.setTransitionRemovalMode(mTransitionRemovalMode);
    mBisimulator.setTransitionRelation(rel);
    long mask = 0;
    final int omega = getDefaultMarkingID();
    if (omega >= 0) {
      mask = rel.addMarking(mask, omega);
    }
    mBisimulator.setPropositionMask(mask);
    mBisimulator.setUpInitialPartitionBasedOnMarkings();
    boolean modified = mBisimulator.run();
    if (!modified) {
      return false;
    }
    TRPartition partition = mBisimulator.getResultPartition();
    mBisimulator.reset();
    rel.reverse();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    switch (mTransitionRemovalMode) {
    case NONTAU:
    case ALL:
      mBisimulator.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER);
      break;
    default:
      break;
    }
    mBisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                                Equivalence.OBSERVATION_EQUIVALENCE);
    mBisimulator.setTransitionRelation(rel);
    mBisimulator.setUpInitialPartition(partition);
    mBisimulator.refinePartitionBasedOnInitialStates();
    modified = mBisimulator.run();
    partition = mBisimulator.getResultPartition();
    setResultPartition(partition);
    mBisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                                Equivalence.WEAK_OBSERVATION_EQUIVALENCE);
    applyResultPartitionAutomatically();
    rel.reverse();
    return modified;
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    mBisimulator.applyResultPartition();
  }


  //#########################################################################
  //# Data Members
  private final ObservationEquivalenceTRSimplifier mBisimulator;

  private ObservationEquivalenceTRSimplifier.TransitionRemoval
    mTransitionRemovalMode;

}
