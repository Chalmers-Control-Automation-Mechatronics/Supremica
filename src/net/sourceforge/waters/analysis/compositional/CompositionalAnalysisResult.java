//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;


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
   * Creates an analysis result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public CompositionalAnalysisResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates an analysis result representing an incomplete run.
   * @param  clazz    The class of the model analyser creating this result.
   */
  public CompositionalAnalysisResult(final Class<?> clazz)
  {
    super(clazz);
    mTotalCompositionsCount = 0;
    mUnsuccessfulCompositionsCount = 0;
    mBlockedEventsCount = mFailingEventsCount =
      mSelfloopOnlyEventsCount = mAlwaysEnabledEventsCount = -1;
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
   * Gets the number of automata that have been simplified by removing
   * a blocked event. This may include the number of globally selfloop-only
   * events (if the implementation does not count selfloop-only events
   * separately).
   */
  public int getBlockedEventsCount()
  {
    return mBlockedEventsCount;
  }

  /**
   * Gets the number of automata that have been simplified by redirecting
   * transitions with a failing event to dump states.
   */
  public int getFailingEventsCount()
  {
    return mFailingEventsCount;
  }

  /**
   * Gets the number of automata that have been simplified by deleting
   * a selfloop-only event.
   */
  public int getSelfloopOnlyEventsCount()
  {
    return mSelfloopOnlyEventsCount;
  }

  /**
   * Gets the number of automata that have been simplified using an
   * always enabled event.
   */
  public int getAlwaysEnabledEventsCount()
  {
    return mAlwaysEnabledEventsCount;
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

  public void addBlockedEvents(final int count)
  {
    if (mBlockedEventsCount < 0) {
      mBlockedEventsCount = 0;
    }
    mBlockedEventsCount += count;
  }

  public void addFailingEvents(final int count)
  {
    if (mFailingEventsCount < 0) {
      mFailingEventsCount = 0;
    }
    mFailingEventsCount += count;
  }

  public void addSelfloopOnlyEvents(final int count)
  {
    if (mSelfloopOnlyEventsCount < 0) {
      mSelfloopOnlyEventsCount = 0;
    }
    mSelfloopOnlyEventsCount += count;
  }

  public void addAlwaysEnabledEvents(final int count)
  {
    if (mAlwaysEnabledEventsCount < 0) {
      mAlwaysEnabledEventsCount = 0;
    }
    mAlwaysEnabledEventsCount += count;
  }

  public void setSimplifierStatistics
    (final List<? extends TRSimplifierStatistics> stats)
  {
    final int size = stats.size();
    mSimplifierStatistics = new ArrayList<TRSimplifierStatistics>(size);
    mSimplifierStatistics.addAll(stats);
  }

  public void addSimplifierStatistics
    (final TransitionRelationSimplifier simplifier)
  {
    if (mSimplifierStatistics == null) {
      mSimplifierStatistics = new LinkedList<TRSimplifierStatistics>();
    }
    simplifier.collectStatistics(mSimplifierStatistics);
  }

  public void addSynchronousProductAnalysisResult
    (final SynchronousProductResult result)
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
    if (mMonolithicStats == null || !mMonolithicStats.isFinished()) {
      mMonolithicStats = result;
    } else if (result != null) {
      mMonolithicStats.merge(result);
    }
    if (result != null && result.isFinished()) {
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
    mTotalCompositionsCount += result.mTotalCompositionsCount;
    mUnsuccessfulCompositionsCount += result.mUnsuccessfulCompositionsCount;
    mNumberOfSplitAttempts += result.mNumberOfSplitAttempts;
    mNumberOfSplits += result.mNumberOfSplits;
    mSplitTime += result.mSplitTime;
    mBlockedEventsCount =
      mergeAdd(mBlockedEventsCount, result.mBlockedEventsCount);
    mFailingEventsCount =
      mergeAdd(mFailingEventsCount, result.mFailingEventsCount);
    mSelfloopOnlyEventsCount =
      mergeAdd(mSelfloopOnlyEventsCount, result.mSelfloopOnlyEventsCount);
    mAlwaysEnabledEventsCount =
      mergeAdd(mAlwaysEnabledEventsCount, result.mAlwaysEnabledEventsCount);
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
  public final void print(final PrintWriter writer)
  {
    super.print(writer);
    printPart1(writer);
    printPart2(writer);
  }

  protected void printPart1(final PrintWriter writer)
  {
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    writer.print("Total number of compositions: ");
    writer.println(mTotalCompositionsCount);
    writer.print("Number of compositions with overflow: ");
    writer.println(mUnsuccessfulCompositionsCount);
    if (mUnsuccessfulCompositionsCount > 0) {
      final float probability =
        (float) (mTotalCompositionsCount - mUnsuccessfulCompositionsCount) /
        (float) mTotalCompositionsCount;
      formatter.format
        ("Probability of a candidate selection being successful: %.2f%%\n",
         100.0f * probability);
    }
    writer.print("Number of attempts to split off subsystems: ");
    writer.println(mNumberOfSplitAttempts);
    writer.print("Number of subsystems split off: ");
    writer.println(mNumberOfSplits);
    formatter.format("Time consumed by split attempts: %.3fs\n",
                     0.001f * mSplitTime);
    if (mBlockedEventsCount >= 0) {
      writer.print("Number of blocked events: ");
      writer.println(mBlockedEventsCount);
    }
    if (mFailingEventsCount >= 0) {
      writer.print("Number of failing events: ");
      writer.println(mFailingEventsCount);
    }
    if (mSelfloopOnlyEventsCount >= 0) {
      writer.print("Number of selfloop-only events: ");
      writer.println(mSelfloopOnlyEventsCount);
    }
    if (mAlwaysEnabledEventsCount >= 0) {
      writer.print("Number of always enabled events: ");
      writer.println(mAlwaysEnabledEventsCount);
    }
  }

  protected void printPart2(final PrintWriter writer)
  {
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
    if (mMonolithicStats != null && mNumberOfMonolithicRuns > 0) {
      mMonolithicStats.print(writer);
    }
  }

  @Override
  public final void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    printCSVHorizontalHeadingsPart1(writer);
    printCSVHorizontalHeadingsPart2(writer);
  }

  protected void printCSVHorizontalHeadingsPart1(final PrintWriter writer)
  {
    writer.print(",Compositions");
    writer.print(",Overflows");
    writer.print(",SplitAttempts");
    writer.print(",Splits");
    writer.print(",SplitTime");
    writer.print(",BlockedEvents");
    writer.print(",FailingEvents");
    writer.print(",SelfloopOnlyEvents");
    writer.print(",AlwaysEnabledEvents");
  }

  protected void printCSVHorizontalHeadingsPart2(final PrintWriter writer)
  {
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
  public final void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    printCSVHorizontalPart1(writer);
    printCSVHorizontalPart2(writer);
  }

  protected void printCSVHorizontalPart1(final PrintWriter writer)
  {
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
    writer.print(mBlockedEventsCount);
    writer.print(',');
    writer.print(mFailingEventsCount);
    writer.print(',');
    writer.print(mSelfloopOnlyEventsCount);
    writer.print(',');
    writer.print(mAlwaysEnabledEventsCount);
  }

  protected void printCSVHorizontalPart2(final PrintWriter writer)
  {
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
  private int mTotalCompositionsCount;
  private int mUnsuccessfulCompositionsCount;
  private int mNumberOfSplitAttempts;
  private int mNumberOfSplits;
  private long mSplitTime;
  private int mBlockedEventsCount;
  private int mFailingEventsCount;
  private int mSelfloopOnlyEventsCount;
  private int mAlwaysEnabledEventsCount;
  private List<TRSimplifierStatistics> mSimplifierStatistics;
  private int mNumberOfSyncProducts;
  private AnalysisResult mSynchronousProductStats;
  private int mNumberOfMonolithicRuns;
  private AnalysisResult mMonolithicStats;

}
