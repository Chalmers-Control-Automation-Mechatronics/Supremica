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
    mSteps.add(step);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredConfiguration()
  {
    for (final TransitionRelationSimplifier step : mSteps) {
      final int result = step.getPreferredConfiguration();
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  public boolean run()
    throws AnalysisException
  {
    setUp();
    mIsObservationEquivalentAbstraction = true;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    boolean result = false;
    for (final TransitionRelationSimplifier step : mSteps) {
      try {
        step.setTransitionRelation(rel);
        if (step.run()) {
          result = true;
          mIsObservationEquivalentAbstraction &=
            step.isObservationEquivalentAbstraction();
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

}
