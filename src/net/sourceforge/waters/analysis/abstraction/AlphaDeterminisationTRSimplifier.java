//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
