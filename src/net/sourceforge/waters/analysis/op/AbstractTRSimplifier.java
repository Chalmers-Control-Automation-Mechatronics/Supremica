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

import org.apache.log4j.Logger;


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
    mAppliesPartitionAutomatically = true;
    mPreferredOutputConfiguration = 0;
    mTransitionRelation = rel;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  public int getPreferredInputConfiguration()
  {
    return 0;
  }

  public void setPreferredOutputConfiguration(final int config)
  {
    mPreferredOutputConfiguration = config;
  }

  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  public void setDefaultMarkingID(final int defaultID)
  {
    setPropositions(-1, defaultID);
  }

  public void setPropositions(final int preconditionID, final int defaultID)
  {
  }

  public void setAppliesPartitionAutomatically(final boolean apply)
  {
    mAppliesPartitionAutomatically = apply;
  }

  public boolean getAppliesPartitionAutomatically()
  {
    return mAppliesPartitionAutomatically;
  }

  public List<int[]> getResultPartition()
  {
    return mResultPartition;
  }

  public boolean isObservationEquivalentAbstraction()
  {
    return mResultPartition == null;
  }

  public boolean isReducedMarking(final int propID)
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
    final int config = getPreferredInputConfiguration();
    if (config != 0) {
      mTransitionRelation.reconfigure(config);
    }
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

  protected void applyResultPartitionAutomatically()
    throws AnalysisException
  {
    if (mAppliesPartitionAutomatically) {
      applyResultPartition();
    }
  }

  protected void applyResultPartition()
    throws AnalysisException
  {
    if (mTransitionRelation.getConfiguration() ==
        ListBufferTransitionRelation.CONFIG_ALL) {
      final int config;
      if (mPreferredOutputConfiguration != 0) {
        config = mPreferredOutputConfiguration;
      } else {
        config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      }
      mTransitionRelation.reconfigure(config);
    }
    mTransitionRelation.merge(mResultPartition);
  }


  //#########################################################################
  //# Logging
  protected Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private boolean mAppliesPartitionAutomatically;
  private int mPreferredOutputConfiguration;
  private ListBufferTransitionRelation mTransitionRelation;
  private List<int[]> mResultPartition;

}
