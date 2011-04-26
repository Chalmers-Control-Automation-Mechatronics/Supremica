//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AbstractTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;


public abstract class AbstractTRSimplifier
  implements TransitionRelationSimplifier
{

  //#########################################################################
  //# Constructor
  public AbstractTRSimplifier()
  {
    this(null);
  }

  public AbstractTRSimplifier(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  public int getPreferredConfiguration()
  {
    return 0;
  }

  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  public List<int[]> getResultPartition()
  {
    return mResultPartition;
  }

  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }

  public void reset()
  {
    mTransitionRelation = null;
    mResultPartition = null;
  }


  //#########################################################################
  //# Algorithm Support
  protected void setUp()
    throws AnalysisException
  {
    mResultPartition = null;
  }

  protected void setResultPartitionList(final List<int[]> partition)
  {
    mResultPartition = partition;
  }

  protected void setResultPartitionArray(final int[][] partition)
  {
    if (partition == null) {
      mResultPartition = null;
    } else {
      mResultPartition = Arrays.asList(partition);
    }
  }

  protected void applyResultPartition()
    throws AnalysisException
  {
    mTransitionRelation.merge(mResultPartition);
  }


  //#########################################################################
  //# Data Members
  private ListBufferTransitionRelation mTransitionRelation;
  private List<int[]> mResultPartition;

}
