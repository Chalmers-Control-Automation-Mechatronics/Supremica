//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier that implements a an experimental
 * supervisor reduction algorithm based on reverse languages.</P>
 *
 * @author Robi Malik
 */

public class ReverseLanguageSupervisorReductionTRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ReverseLanguageSupervisorReductionTRSimplifier()
  {
    this(null);
  }

  public ReverseLanguageSupervisorReductionTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mReverseLanguageStep = new ReverseLanguageStep1TRSimplifier();
    mSubsetConstructionStep = new SubsetConstructionTRSimplifier();
    mSubsetConstructionStep.setDumpStateAware(true);
    mSynchronisationStep = new ReverseLanguageStep3TRSimplifier();
    mMinimizationStep = new ObservationEquivalenceTRSimplifier();
    mMinimizationStep.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE);
    final List<TransitionRelationSimplifier> chain = new ArrayList<>(7);
    mSuWonhamStep = new SuWonhamSupervisorReductionTRSimplifier();
    chain.add(mReverseLanguageStep);
    chain.add(mSubsetConstructionStep);
    chain.add(mSynchronisationStep);
    chain.add(new SelfloopSupervisorReductionTRSimplifier());
    chain.add(mMinimizationStep);
    chain.add(mSuWonhamStep);
    chain.add(new SelfloopSupervisorReductionTRSimplifier());
    mChain = new ChainTRSimplifier(chain, rel);
    createStatistics();
  }


  //#########################################################################
  //# Configuration
  public void setEnablingSupervisor(final boolean enabling)
  {
    mReverseLanguageStep.setEnablingSupervisor(enabling);
  }

  public boolean getEnablingSupervisor()
  {
    return mReverseLanguageStep.getEnablingSupervisor();
  }

  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of states that will be created.
   * @param limit
   *          The new state limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of states.
   */
  @Override
  public void setStateLimit(final int limit)
  {
    mReverseLanguageStep.setStateLimit(limit);
    mSubsetConstructionStep.setStateLimit(limit);
    mSynchronisationStep.setStateLimit(limit);
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  @Override
  public int getStateLimit()
  {
    return mReverseLanguageStep.getStateLimit();
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions that will be created.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
   */
  @Override
  public void setTransitionLimit(final int limit)
  {
    mReverseLanguageStep.setTransitionLimit(limit);
    mSubsetConstructionStep.setTransitionLimit(limit);
    mMinimizationStep.setTransitionLimit(limit);
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  @Override
  public int getTransitionLimit()
  {
    return mReverseLanguageStep.getStateLimit();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.
  //# AbstractSupervisorReductionTRSimplifier
  @Override
  public void setSupervisedEvent(final int event)
  {
    super.setSupervisedEvent(event);
    mReverseLanguageStep.setSupervisedEvent(event);
    mSuWonhamStep.setSupervisedEvent(event);
  }

  @Override
  public boolean isSupervisedEventRequired()
  {
    return true;
  }

  @Override
  public boolean isMinimizationIncluded()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.
  //# AbstractMarkingTRSimplifier
  @Override
  public void setPropositions(final int preconditionID, final int defaultID)
  {
    super.setPropositions(preconditionID, defaultID);
    mChain.setPropositions(preconditionID, defaultID);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    super.setTransitionRelation(rel);
    mChain.setTransitionRelation(rel);
  }

  @Override
  public void setPreferredOutputConfiguration(final int config)
  {
    super.setPreferredOutputConfiguration(config);
    mChain.setPreferredOutputConfiguration(config);
  }

  @Override
  public boolean isPartitioning()
  {
    return false;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return mChain.isObservationEquivalentAbstraction();
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    return mChain.isAlwaysEnabledEventsSupported();
  }

  @Override
  public boolean isReducedMarking(final int propID)
  {
    return mChain.isReducedMarking(propID);
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    if (mChain != null) {
      final TRSimplifierStatistics stats = mChain.createStatistics();
      return setStatistics(stats);
    } else {
      return null;
    }
  }

  @Override
  public void collectStatistics(final List<TRSimplifierStatistics> list)
  {
    mChain.collectStatistics(list);
  }

  @Override
  public void reset()
  {
    super.reset();
    mChain.reset();
  }

  @Override
  protected void logStart()
  {
    mChain.logStart();
  }

  @Override
  protected void logFinish(final boolean success)
  {
    mChain.logFinish(success);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mChain.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mChain.resetAbort();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mChain.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mSynchronisationStep.setOriginalSupervisor(rel);
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    return mChain.runSimplifier();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mChain.tearDown();
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mChain;
  private final ReverseLanguageStep1TRSimplifier mReverseLanguageStep;
  private final SubsetConstructionTRSimplifier mSubsetConstructionStep;
  private final ReverseLanguageStep3TRSimplifier mSynchronisationStep;
  private final ObservationEquivalenceTRSimplifier mMinimizationStep;
  private final SuWonhamSupervisorReductionTRSimplifier mSuWonhamStep;
}
