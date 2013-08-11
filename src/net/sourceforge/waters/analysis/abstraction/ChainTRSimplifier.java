//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ChainTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
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
    mIsPartitioning = true;
    mSteps = new LinkedList<TransitionRelationSimplifier>();
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
    return 0;
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
  public void reset()
  {
    super.reset();
    for (final TransitionRelationSimplifier step : mSteps) {
      step.reset();
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
  protected boolean runSimplifier()
  throws AnalysisException
  {
    mIsObservationEquivalentAbstraction = true;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numProps = rel.getNumberOfPropositions();
    mReducedMarkings = new boolean[numProps];
    boolean result = false;
    for (final TransitionRelationSimplifier step : mSteps) {
      checkAbort();
      final int config = step.getPreferredInputConfiguration();
      if (config != 0) {
        rel.reconfigure(config);
      }
      step.setTransitionRelation(rel);
      if (step.run()) {
        final ListBufferTransitionRelation rel1 = step.getTransitionRelation();
        setTransitionRelation(rel1);
        result = true;
        mIsObservationEquivalentAbstraction &=
          step.isObservationEquivalentAbstraction();
        for (int prop = 0; prop < numProps; prop++) {
          mReducedMarkings[prop] |= step.isReducedMarking(prop);
        }
      }
      if (isPartitioning()) {
        final List<int[]> currentPartition = getResultPartition();
        final List<int[]> newPartition = step.getResultPartition();
        final List<int[]> combinedPartition =
          mergePartitions(currentPartition, newPartition);
        setResultPartitionList(combinedPartition);
      }
    }
    return result;
  }


  //#########################################################################
  //# Merging Partitions
  public static List<int[]> mergePartitions(final List<int[]> part1,
                                            final List<int[]> part2)
  {
    if (part1 == null) {
      return part2;
    } else if (part2 == null) {
      return part1;
    } else {
      final int size2 = part2.size();
      final List<int[]> result = new ArrayList<int[]>(size2);
      final TIntArrayList clazz = new TIntArrayList();
      for (final int[] clazz2 : part2) {
        if (clazz2 != null) {
          for (final int state2 : clazz2) {
            final int[] clazz1 = part1.get(state2);
            if (clazz1 != null) {
              for (final int state1 : clazz1) {
                clazz.add(state1);
              }
            }
          }
        }
        if (clazz.size() > 0) {
          result.add(clazz.toArray());
          clazz.clear();
        } else {
          result.add(null);
        }
      }
      return result;
    }
  }


  //#########################################################################
  //# Data Members
  private final List<TransitionRelationSimplifier> mSteps;
  private boolean mIsPartitioning;
  private boolean mIsObservationEquivalentAbstraction;
  private boolean[] mReducedMarkings;

}

