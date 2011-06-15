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

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;

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
    mIsAborting = false;
    mAppliesPartitionAutomatically = true;
    mPreferredOutputConfiguration = 0;
    mStatistics = createStatistics();
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

  public boolean run()
  throws AnalysisException
  {
    final long start = System.currentTimeMillis();
    boolean completed = false;
    try {
      setUp();
      mStatistics.recordStart(mTransitionRelation);
      final boolean success = runSimplifier();
      mStatistics.recordFinish(mTransitionRelation, success);
      completed = true;
      return success;
    } catch (final OutOfMemoryError error) {
      tearDown();
      System.gc();
      throw new OverflowException(error);
    } finally {
      tearDown();
      if (!completed) {
        mStatistics.recordOverflow(mTransitionRelation);
      }
      final long stop = System.currentTimeMillis();
      mStatistics.recordRunTime(stop - start);
    }
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

  public TRSimplifierStatistics getStatistics()
  {
    return mStatistics;
  }

  public void reset()
  {
    mTransitionRelation = null;
    mResultPartition = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  public void requestAbort()
  {
    mIsAborting = true;
  }

  public boolean isAborting()
  {
    return mIsAborting;
  }


  //#########################################################################
  //# Algorithm Support
  protected abstract TRSimplifierStatistics createStatistics();

  protected void setUp()
    throws AnalysisException
  {
    checkAbort();
    mResultPartition = null;
    final int config = getPreferredInputConfiguration();
    if (config != 0) {
      mTransitionRelation.reconfigure(config);
    }
  }

  protected abstract boolean runSimplifier() throws AnalysisException;

  protected void tearDown()
  {
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

  /**
   * Checks whether this simplifier has been requested to abort,
   * and if so, performs the abort by throwing an {@link AbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  protected void checkAbort()
    throws AbortException
  {
    if (mIsAborting) {
      final AbortException exception = new AbortException();
      throw exception;
    }
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
  private boolean mIsAborting;
  private boolean mAppliesPartitionAutomatically;
  private int mPreferredOutputConfiguration;
  private final TRSimplifierStatistics mStatistics;
  private ListBufferTransitionRelation mTransitionRelation;
  private List<int[]> mResultPartition;

}
