//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import gnu.trove.set.hash.THashSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;


public class ChainTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ChainTRSimplifier()
  {
    mIsPartitioning = true;
    mSteps = new LinkedList<TransitionRelationSimplifier>();
  }

  public ChainTRSimplifier(final List<TransitionRelationSimplifier> steps)
  {
    this();
    for (final TransitionRelationSimplifier step : steps) {
      add(step);
    }
  }

  public ChainTRSimplifier(final List<TransitionRelationSimplifier> steps,
                           final ListBufferTransitionRelation rel)
  {
    super(rel);
    mIsPartitioning = true;
    mSteps = new LinkedList<TransitionRelationSimplifier>();
    for (final TransitionRelationSimplifier step : steps) {
      add(step);
    }
  }


  //#########################################################################
  //# Configuration
  public int size()
  {
    return mSteps.size();
  }

  public TransitionRelationSimplifier getStep(final int index)
  {
    return mSteps.get(index);
  }

  public int add(final TransitionRelationSimplifier step)
  {
    final int index = size();
    final int config = step.getPreferredInputConfiguration();
    setPreferredOutputConfiguration(config);
    mSteps.add(step);
    mIsPartitioning &= step.isPartitioning();
    return index;
  }

  public void blacklistOption(final String id)
  {
    if (mBlackListedOptions == null) {
      mBlackListedOptions = new THashSet<>();
    }
    mBlackListedOptions.add(id);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.options.Configurable
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    for (final TransitionRelationSimplifier step : mSteps) {
      options.addAll((step.getOptions(db)));
    }
    return options.stream()
      .distinct()
      .filter(o -> mBlackListedOptions == null
        || !mBlackListedOptions.contains(o.getID()))
      .collect(Collectors.toList());
  }

  @Override
  public void setOption(final Option<?> option)
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      step.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      final int result = step.getPreferredInputConfiguration();
      if (result != 0) {
        return result;
      }
    }
    return getPreferredOutputConfiguration();
  }

  @Override
  public void setPreferredOutputConfiguration(final int config)
  {
    super.setPreferredOutputConfiguration(config);
    if (!mSteps.isEmpty()) {
      final int end = mSteps.size() - 1;
      final TransitionRelationSimplifier last = mSteps.get(end);
      last.setPreferredOutputConfiguration(config);
    }
  }

  @Override
  public void setPropositions(final int preconditionID, final int defaultID)
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      step.setPropositions(preconditionID, defaultID);
    }
  }

  @Override
  public void setStateLimit(final int limit)
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      step.setStateLimit(limit);
    }
  }

  @Override
  public int getStateLimit()
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      final int limit = step.getStateLimit();
      if (limit != Integer.MAX_VALUE) {
        return limit;
      }
    }
    return Integer.MAX_VALUE;
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      step.setTransitionLimit(limit);
    }
  }

  @Override
  public int getTransitionLimit()
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      final int limit = step.getTransitionLimit();
      if (limit != Integer.MAX_VALUE) {
        return limit;
      }
    }
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean isPartitioning()
  {
    return mIsPartitioning;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return mIsObservationEquivalentAbstraction;
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      if (step.isAlwaysEnabledEventsSupported()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isReducedMarking(final int propID)
  {
    return mReducedMarkings[propID];
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    if (mSteps != null) {
      for (final TransitionRelationSimplifier step : mSteps) {
        step.createStatistics();
      }
    }
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, true);
    return setStatistics(stats);
  }

  @Override
  public void collectStatistics(final List<TRSimplifierStatistics> list)
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      step.collectStatistics(list);
    }
  }

  @Override
  protected void logStart()
  {
  }

  @Override
  protected void logFinish(final boolean success)
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    for (final TransitionRelationSimplifier step : mSteps) {
      step.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    for (final TransitionRelationSimplifier step : mSteps) {
      step.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.
  //# AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    // Do not call super to avoid reconfiguration of TR ahead of time.
    setResultPartition(null);
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    mIsObservationEquivalentAbstraction = true;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numProps = rel.getNumberOfPropositions();
    mReducedMarkings = new boolean[numProps];
    boolean result = false;
    for (final TransitionRelationSimplifier step : mSteps) {
      try {
        checkAbort();
        step.setTransitionRelation(rel);
        if (step.run()) {
          result = true;
          mIsObservationEquivalentAbstraction &=
            step.isObservationEquivalentAbstraction();
          for (int prop = 0; prop < numProps; prop++) {
            mReducedMarkings[prop] |= step.isReducedMarking(prop);
          }
        }
        // rel.checkIntegrity();
        if (isPartitioning()) {
          final TRPartition currentPartition = getResultPartition();
          final TRPartition newPartition = step.getResultPartition();
          final TRPartition combinedPartition =
            TRPartition.combine(currentPartition, newPartition);
          setResultPartition(combinedPartition);
        }
      } finally {
        step.reset();
      }
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final List<TransitionRelationSimplifier> mSteps;
  private boolean mIsPartitioning;
  private boolean mIsObservationEquivalentAbstraction;
  private boolean[] mReducedMarkings;
  private Set<String> mBlackListedOptions;

}
