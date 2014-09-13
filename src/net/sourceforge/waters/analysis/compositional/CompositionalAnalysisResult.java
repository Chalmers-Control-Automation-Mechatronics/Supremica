//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalAnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * A result record that can be returned by a compositional analysis
 * algorithms.
 *
 * In addition to a standard analysis result ({@link AnalysisResult}),
 * the compositional verification result contains detailed statistics about
 * individual abstraction rules and the final (monolithic) verification.
 *
 * @author Rachel Francis, Robi Malik
 */

public class CompositionalAnalysisResult
  extends DefaultAnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public CompositionalAnalysisResult()
  {
    mCompileTime = -1;
    mTotalCompositionsCount = 0;
    mUnsuccessfulCompositionsCount = 0;
    mRedundantEventsCount = mFailingEventsCount = 0;
    mNumberOfSplitAttempts = 0;
    mNumberOfSplits = 0;
    mSplitTime = 0;
    mSimplifierStatistics = null;
    mNumberOfSyncProducts = 0;
    mSynchronousProductStats = null;
    mNumberOfMonolithicRuns = 0;
    mMonolithicStats = null;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the compile time recorded for this analysis result.
   * The compile time measures the time spent by the {@link ModuleCompiler}
   * to compile the input {@link ModuleProxy} to a {@link ProductDESProxy},
   * in milliseconds.
   */
  public long getCompileTime()
  {
    return mCompileTime;
  }

  /**
   * Gets the total number of times a candidate is chosen and composition and
   * abstraction is attempted. The attempts may or may not have been successful
   * in producing a reduced size model with no overflow exceptions occurring.
   */
  public int getTotalCompositionsCount()
  {
    return mTotalCompositionsCount;
  }

  /**
   * Gets the number of times an unsuccessful candidate is chosen and an
   * overflow exception occurs during composition or abstraction.
   */
  public int getUnsuccessfulCompositionsCount()
  {
    return mUnsuccessfulCompositionsCount;
  }

  /**
   * Gets the number of events that were found to be redundant and have
   * been removed. Events are removed if they are found to be globally
   * disabled, i.e., always blocked by one automaton, or globally selflooped,
   * i.e., appear only in selfloops in all automata.
   */
  public int getRedundantEventsCount()
  {
    return mRedundantEventsCount;
  }

  /**
   * Gets the number of events that were found to be failing and have
   * been redirected to dump states.
   */
  public int getFailingEventsCount()
  {
    return mFailingEventsCount;
  }

  /**
   * Gets the statistics for the individual abstraction rules used.
   */
  public List<TRSimplifierStatistics> getSimplifierStatistics()
  {
    return mSimplifierStatistics;
  }

  /**
   * Gets the statistics of the intermediate synchronous product steps.
   */
  public AnalysisResult getSynchronousProductAnalysisResult()
  {
    return mSynchronousProductStats;
  }

  /**
   * Gets the statistics for the final (monolithic) verification steps.
   */
  public AnalysisResult getMonolithicAnalysisResult()
  {
    return mMonolithicStats;
  }


  //#########################################################################
  //# Providing Statistics
  public void setCompileTime(final long time)
  {
    mCompileTime = time;
  }

  public void addCompositionAttempt()
  {
    mTotalCompositionsCount++;
  }

  public void addUnsuccessfulComposition()
  {
    mUnsuccessfulCompositionsCount++;
  }

  public void addSplitAttempt(final boolean success, final long time)
  {
    mNumberOfSplitAttempts++;
    if (success) {
      mNumberOfSplits++;
    }
    mSplitTime += time;
  }

  public void addRedundantEvents(final int count)
  {
    mRedundantEventsCount += count;
  }

  public void addFailingEvents(final int count)
  {
    mFailingEventsCount += count;
  }

  public void setSimplifierStatistics
    (final List<? extends TRSimplifierStatistics> stats)
  {
    final int size = stats.size();
    mSimplifierStatistics = new ArrayList<TRSimplifierStatistics>(size);
    mSimplifierStatistics.addAll(stats);
  }

  public void setSimplifierStatistics
    (final TransitionRelationSimplifier simplifier)
  {
    mSimplifierStatistics = new LinkedList<TRSimplifierStatistics>();
    simplifier.collectStatistics(mSimplifierStatistics);
  }

  public void addSynchronousProductAnalysisResult(final AnalysisResult result)
  {
    if (result == null || result.isFinished()) {
      mNumberOfSyncProducts++;
    }
    if (mSynchronousProductStats == null) {
      mSynchronousProductStats = result;
    } else if (result != null) {
      mSynchronousProductStats.merge(result);
    }
    if (result != null) {
      final long usage = result.getPeakMemoryUsage();
      updatePeakMemoryUsage(usage);
    }
  }

  public void addMonolithicAnalysisResult(final AnalysisResult result)
  {
    if (result == null || result.isFinished()) {
      mNumberOfMonolithicRuns++;
    }
    if (mMonolithicStats == null) {
      mMonolithicStats = result;
    } else if (result != null) {
      mMonolithicStats.merge(result);
    }
    if (result != null) {
      final long usage = result.getPeakMemoryUsage();
      updatePeakMemoryUsage(usage);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final CompositionalAnalysisResult result =
      (CompositionalAnalysisResult) other;
    mCompileTime = mergeAdd(mCompileTime, result.mCompileTime);
    mTotalCompositionsCount += result.mTotalCompositionsCount;
    mUnsuccessfulCompositionsCount += result.mUnsuccessfulCompositionsCount;
    mNumberOfSplitAttempts += result.mNumberOfSplitAttempts;
    mNumberOfSplits += result.mNumberOfSplits;
    mSplitTime += result.mSplitTime;
    if (mSimplifierStatistics != null && result.mSimplifierStatistics != null) {
      final Iterator<TRSimplifierStatistics> iter1 =
        mSimplifierStatistics.iterator();
      final Iterator<TRSimplifierStatistics> iter2 =
        result.mSimplifierStatistics.iterator();
      while (iter1.hasNext() && iter2.hasNext()) {
        final TRSimplifierStatistics stats1 = iter1.next();
        final TRSimplifierStatistics stats2 = iter2.next();
        stats1.merge(stats2);
      }
    }
    mNumberOfSyncProducts += result.mNumberOfSyncProducts;
    if (mSynchronousProductStats == null) {
      mSynchronousProductStats = result.mSynchronousProductStats;
    } else if (result.mSynchronousProductStats != null) {
      mSynchronousProductStats.merge(result.mSynchronousProductStats);
    }
    mNumberOfMonolithicRuns += result.mNumberOfMonolithicRuns;
    if (mMonolithicStats == null) {
      mMonolithicStats = result.mMonolithicStats;
    } else if (result.mMonolithicStats != null) {
      mMonolithicStats.merge(result.mMonolithicStats);
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    formatter.format("Compile time: %.3fs\n",
                     0.001f * mCompileTime);
    writer.print("Total number of compositions: ");
    writer.println(mTotalCompositionsCount);
    writer.print("Number of unsuccessful compositions: ");
    writer.println(mUnsuccessfulCompositionsCount);
    writer.print("Number of attempts to split off subsystems: ");
    writer.println(mNumberOfSplitAttempts);
    writer.print("Number of subsystems split off: ");
    writer.println(mNumberOfSplits);
    formatter.format("Time consumed by split attempts: %.3fs\n",
                     0.001f * mSplitTime);
    writer.print("Number of redundant events: ");
    writer.println(mRedundantEventsCount);
    writer.print("Number of failing events: ");
    writer.println(mFailingEventsCount);
    if (mUnsuccessfulCompositionsCount > 0) {
      final float probability =
        (float) (mTotalCompositionsCount - mUnsuccessfulCompositionsCount) /
        (float) mTotalCompositionsCount;
      formatter.format
        ("Probability of a candidate selection being successful: %.2f%%\n",
         100.0f * probability);
    }
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        writer.println("--------------------------------------------------");
        ruleStats.print(writer);
      }
    }
    if (mSynchronousProductStats != null) {
      writer.println("--------------------------------------------------");
      writer.print("Number of synchronous products computed: ");
      writer.println(mNumberOfSyncProducts);
      mSynchronousProductStats.print(writer);
    }
    writer.println("--------------------------------------------------");
    writer.print("Number of monolithic analysis runs: ");
    writer.println(mNumberOfMonolithicRuns);
    if (mMonolithicStats != null) {
      mMonolithicStats.print(writer);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",CompileTime");
    writer.print(",Compositions");
    writer.print(",Overflows");
    writer.print(",SplitAttempts");
    writer.print(",Splits");
    writer.print(",SplitTime");
    writer.print(",RedundantEvents");
    writer.print(",FailingEvents");
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontalHeadings(writer);
      }
    }
    if (mSynchronousProductStats != null) {
      writer.print(",SyncProd,");
      mSynchronousProductStats.printCSVHorizontalHeadings(writer);
    }
    writer.print(",Monolithic");
    if (mMonolithicStats != null) {
      writer.print(',');
      mMonolithicStats.printCSVHorizontalHeadings(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mCompileTime);
    writer.print(',');
    writer.print(mTotalCompositionsCount);
    writer.print(',');
    writer.print(mUnsuccessfulCompositionsCount);
    writer.print(',');
    writer.print(mNumberOfSplitAttempts);
    writer.print(',');
    writer.print(mNumberOfSplits);
    writer.print(',');
    writer.print(mSplitTime);
    writer.print(',');
    writer.print(mRedundantEventsCount);
    writer.print(',');
    writer.print(mFailingEventsCount);
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontal(writer);
      }
    }
    if (mSynchronousProductStats != null) {
      writer.print(',');
      writer.print(mNumberOfSyncProducts);
      writer.print(',');
      mSynchronousProductStats.printCSVHorizontal(writer);
    }
    writer.print(',');
    writer.print(mNumberOfMonolithicRuns);
    if (mMonolithicStats != null) {
      writer.print(',');
      mMonolithicStats.printCSVHorizontal(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private long mCompileTime;
  private int mTotalCompositionsCount;
  private int mUnsuccessfulCompositionsCount;
  private int mNumberOfSplitAttempts;
  private int mNumberOfSplits;
  private long mSplitTime;
  private int mRedundantEventsCount;
  private int mFailingEventsCount;
  private List<TRSimplifierStatistics> mSimplifierStatistics;
  private int mNumberOfSyncProducts;
  private AnalysisResult mSynchronousProductStats;
  private int mNumberOfMonolithicRuns;
  private AnalysisResult mMonolithicStats;

}
