//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ChainTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.log4j.Logger;


public class ChainTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ChainTRSimplifier()
  {
    mSteps = new LinkedList<TransitionRelationSimplifier>();
  }

  public ChainTRSimplifier(final List<TransitionRelationSimplifier> steps)
  {
    mSteps = steps;
  }

  public ChainTRSimplifier(final List<TransitionRelationSimplifier> steps,
                           final ListBufferTransitionRelation rel)
  {
    super(rel);
    mSteps = steps;
  }


  //#########################################################################
  //# Configuration
  List<TransitionRelationSimplifier> getSteps()
  {
    return mSteps;
  }

  void add(final TransitionRelationSimplifier step)
  {
    final int config = step.getPreferredInputConfiguration();
    setPreferredOutputConfiguration(config);
    mSteps.add(step);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
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

  public boolean run()
    throws AnalysisException
  {
    final Logger logger = getLogger();
    setUp();
    mIsObservationEquivalentAbstraction = true;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (logger.isDebugEnabled()) {
      logger.debug(rel.getName());
      logger.debug(rel.getNumberOfReachableStates() + " states, " +
                   rel.getNumberOfTransitions() + " transitions, " +
                   rel.getNumberOfMarkings() + " markings.");
    }
    final int numProps = rel.getNumberOfPropositions();
    mReducedMarkings = new boolean[numProps];
    boolean result = false;
    for (final TransitionRelationSimplifier step : mSteps) {
      try {
        if (logger.isDebugEnabled()) {
          logger.debug(ProxyTools.getShortClassName(step) + " ...");
        }
        step.setTransitionRelation(rel);
        if (step.run()) {
          if (logger.isDebugEnabled()) {
            logger.debug(rel.getNumberOfReachableStates() + " states, " +
                         rel.getNumberOfTransitions() + " transitions, " +
                         rel.getNumberOfMarkings() + " markings.");
          }
          result = true;
          mIsObservationEquivalentAbstraction &=
            step.isObservationEquivalentAbstraction();
          for (int prop = 0; prop < numProps; prop++) {
            mReducedMarkings[prop] |= step.isReducedMarking(prop);
          }
          final List<int[]> partition = step.getResultPartition();
          mergePartitions(partition);
        }
      } finally {
        step.reset();
      }
    }
    return result;
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
  public void reset()
  {
    super.reset();
    for (final TransitionRelationSimplifier step : mSteps) {
      step.reset();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void mergePartitions(final List<int[]> next)
  {
    if (next == null) {
      return;
    }
    final List<int[]> current = getResultPartition();
    if (current == null) {
      setResultPartitionList(next);
      return;
    }
    final int nextSize = next.size();
    final List<int[]> result = new ArrayList<int[]>(nextSize);
    final TIntArrayList clazz = new TIntArrayList();
    for (final int[] clazz2 : next) {
      for (final int state2 : clazz2) {
        final int[] clazz1 = current.get(state2);
        for (final int state1 : clazz1) {
          clazz.add(state1);
        }
      }
      result.add(clazz.toNativeArray());
      clazz.clear();
    }
    setResultPartitionList(result);
  }


  //#########################################################################
  //# Data Members
  private final List<TransitionRelationSimplifier> mSteps;
  private boolean mIsObservationEquivalentAbstraction;
  private boolean[] mReducedMarkings;

}
