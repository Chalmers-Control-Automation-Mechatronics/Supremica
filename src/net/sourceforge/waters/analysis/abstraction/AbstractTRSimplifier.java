//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AbstractTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;

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
    mTransitionRelation = rel;
    createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return 0;
  }

  @Override
  public void setPreferredOutputConfiguration(final int config)
  {
    mPreferredOutputConfiguration = config;
  }

  public int getPreferredOutputConfiguration()
  {
    return mPreferredOutputConfiguration;
  }

  @Override
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  @Override
  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  /**
   * Sets the marking IDs used by this simplifier.
   * This method also clears any precondition marking that may have been set.
   * @param defaultID
   *          ID of default marking proposition,
   *          or <CODE>-1</CODE> if unused or not present.
   */
  @Override
  public void setDefaultMarkingID(final int defaultID)
  {
    setPropositions(-1, defaultID);
  }

  /**
   * Sets the marking IDs used by this simplifier.
   * The default implementation does nothing but subclasses may override it.
   * @param preconditionID
   *          ID of precondition marking proposition,
   *          or <CODE>-1</CODE> if unused or not present.
   * @param defaultID
   *          ID of default marking proposition,
   *          or <CODE>-1</CODE> if unused or not present.
   */
  @Override
  public void setPropositions(final int preconditionID, final int defaultID)
  {
  }

  @Override
  public void setAppliesPartitionAutomatically(final boolean apply)
  {
    mAppliesPartitionAutomatically = apply;
  }

  @Override
  public boolean getAppliesPartitionAutomatically()
  {
    return mAppliesPartitionAutomatically;
  }

  @Override
  public boolean run()
  throws AnalysisException
  {
    final long start = System.currentTimeMillis();
    boolean completed = false;
    try {
      logStart();
      setUp();
      recordStart();
      final boolean success = runSimplifier();
      recordFinish(success);
      logFinish(success);
      completed = true;
      return success;
    } catch (final OutOfMemoryError error) {
      tearDown();
      getLogger().debug("<out of memory>");
      System.gc();
      throw new OverflowException(error);
    } catch (final StackOverflowError error) {
      throw new OverflowException(error);
    } finally {
      tearDown();
      final long stop = System.currentTimeMillis();
      recordRunTime(stop - start);
      if (!completed) {
        recordOverflow();
      }
    }
  }

  @Override
  public List<int[]> getResultPartition()
  {
    return mResultPartition;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return mResultPartition == null;
  }

  @Override
  public boolean isReducedMarking(final int propID)
  {
    return false;
  }

  @Override
  public TRSimplifierStatistics getStatistics()
  {
    return mStatistics;
  }

  @Override
  public void collectStatistics(final List<TRSimplifierStatistics> list)
  {
    final TRSimplifierStatistics stats = getStatistics();
    list.add(stats);
  }

  @Override
  public void reset()
  {
    mTransitionRelation = null;
    mResultPartition = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#########################################################################
  //# Algorithm Support
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

  /**
   * Cleans up temporary data from the current {@link #run()}.
   * The difference between this method and {@link #reset()} is that
   * tearDown() deletes temporary data structures that are only needed
   * during simplification, whereas {@link #reset()} cleans up results.
   * The tearDown() method is called automatically by {@link #run()} in
   * a <CODE>finally</CODE> block, while {@link #reset()} must be called
   * by the user after retrieving all results.
   */
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

  /**
   * Applies the computed partition to the transition relation.
   * This method is called automatically at the end of {@link #run()}
   * if the simplifier is configured to apply the result partition.
   * Otherwise it is up to the user to partition the transition
   * relation manually.
   * @see #setAppliesPartitionAutomatically(boolean) setAppliesPartitionAutomatically()
   */
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
    checkAbort();
    mTransitionRelation.merge(mResultPartition);
  }

  /**
   * Stores the given statistics record to be used for any further
   * statistics recording.
   * @param  statistics  The new statistics record.
   * @return The given statistics record.
   */
  protected TRSimplifierStatistics setStatistics
    (final TRSimplifierStatistics statistics)
  {
    return mStatistics = statistics;
  }

  /**
   * Initiates recording of statistics by storing the current transition
   * relation data as a new input automaton.
   */
  protected void recordStart()
  {
    if (mStatistics != null) {
      mStatistics.recordStart(mTransitionRelation);
    }
  }

  /**
   * Completes recording of statistics by storing the current transition
   * relation data as a new output automaton.
   * @param  success  Whether or not the simplifier has been able to
   *                  actually simplify the transition relation.
   */
  protected void recordFinish(final boolean success)
  {
    if (mStatistics != null) {
      mStatistics.recordFinish(mTransitionRelation, success);
    }
  }

  /**
   * Completes recording of statistics by storing a failure of the current
   * run due to an exception.
   */
  protected void recordOverflow()
  {
    if (mStatistics != null) {
      mStatistics.recordOverflow(mTransitionRelation);
    }
  }

  /**
   * Adds the given runtime to the simplifier's statistics record.
   */
  protected void recordRunTime(final long runtime)
  {
    if (mStatistics != null) {
      mStatistics.recordRunTime(runtime);
    }
  }

  /**
   * Prints a message to the current logger indicating that this simplifier
   * has just started.
   */
  protected void logStart()
  {
    if (mAppliesPartitionAutomatically) {
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        logger.debug(ProxyTools.getShortClassName(this) + " ...");
      }
    }
  }

  /**
   * Prints a message to the current logger indicating that this simplifier
   * has just completed.
   * @param  success  Whether or not the simplifier has been able to
   *                  actually simplify the transition relation.
   */
  protected void logFinish(final boolean success)
  {
    if (success && mAppliesPartitionAutomatically) {
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        logger.debug
          (mTransitionRelation.getNumberOfReachableStates() + " states, " +
           mTransitionRelation.getNumberOfTransitions() + " transitions, " +
           mTransitionRelation.getNumberOfMarkings() + " markings.");
      }
    }
  }

  /**
   * Checks whether this simplifier has been requested to abort,
   * and if so, performs the abort by throwing an {@link AnalysisAbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  protected void checkAbort()
    throws AnalysisAbortException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
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
  private TRSimplifierStatistics mStatistics;
  private ListBufferTransitionRelation mTransitionRelation;
  private List<int[]> mResultPartition;

}
