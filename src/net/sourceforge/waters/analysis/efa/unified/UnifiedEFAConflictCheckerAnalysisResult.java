//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   UnifiedEFAConflictCheckerAnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.efa.base.EFASimplifierStatistics;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;


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

public class UnifiedEFAConflictCheckerAnalysisResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public UnifiedEFAConflictCheckerAnalysisResult()
  {
    mTotalCompositionsCount = 0;
    mUnsuccessfulCompositionsCount = 0;
    mNumberOfSplitAttempts = 0;
    mNumberOfSplits = 0;
    mSplitTime = 0;
    mSynchronisationStatistics = null;
    mPartitioningStatistics = null;
    mUnfoldingStatistics = null;
    mSimplifierStatistics = null;
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
   * Gets the statistics for the individual abstraction rules used.
   */
  public List<TRSimplifierStatistics> getSimplifierStatistics()
  {
    return mSimplifierStatistics;
  }

  /**
   * Gets the statistics of the intermediate synchronous product steps.
   */
  public EFASimplifierStatistics getSynchronousProductAnalysisResult()
  {
    return mSynchronisationStatistics;
  }


  //#########################################################################
  //# Providing Statistics
  public void setUnifiedSystem(final UnifiedEFAConflictChecker checker)
  {
    setNumberOfAutomata(checker.getNumberOfAutomata());
    for (final UnifiedEFATransitionRelation tr :
      checker.getTransitionRelations()) {
      addUnifiedEFATRTransitionRelation(tr);
    }
  }

  public void addUnifiedEFATRTransitionRelation(final UnifiedEFATransitionRelation tr)
  {
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numTrans = rel.getNumberOfTransitions();
    final double totalStates = getTotalNumberOfStates() + numStates;
    setTotalNumberOfStates(totalStates);
    final double peakStates =
      Math.max(getPeakNumberOfStates(), numStates);
    setPeakNumberOfStates(peakStates);
    final double totalTrans = getTotalNumberOfTransitions() + numTrans;
    setTotalNumberOfTransitions(totalTrans);
    final double peakTrans =
      Math.max(getPeakNumberOfTransitions(), numTrans);
    setPeakNumberOfTransitions(peakTrans);
    updatePeakMemoryUsage();
  }

  public void addCompositionAttempt()
  {
    mTotalCompositionsCount++;
  }

  public void addUnsuccessfulComposition()
  {
    mUnsuccessfulCompositionsCount++;
  }

  public void addSynchronousProductStatistics(final EFASimplifierStatistics result)
  {
    if (mSynchronisationStatistics == null) {
      mSynchronisationStatistics = result;
    } else if (result != null) {
      mSynchronisationStatistics.merge(result);
    }
    if (result != null) {
      final long usage = result.getPeakMemoryUsage();
      updatePeakMemoryUsage(usage);
    }
  }

  public void addPartitioningStatistics(final EFASimplifierStatistics result)
  {
    if (mPartitioningStatistics == null) {
      mPartitioningStatistics = result;
    } else if (result != null) {
      mPartitioningStatistics.merge(result);
    }
    if (result != null) {
      final long usage = result.getPeakMemoryUsage();
      updatePeakMemoryUsage(usage);
    }
  }

  public void addUnfoldingStatistics(final EFASimplifierStatistics result)
  {
    if (mUnfoldingStatistics == null) {
      mUnfoldingStatistics = result;
    } else if (result != null) {
      mUnfoldingStatistics.merge(result);
    }
    if (result != null) {
      final long usage = result.getPeakMemoryUsage();
      updatePeakMemoryUsage(usage);
    }
  }

  public void setSimplifierStatistics
    (final List<? extends TRSimplifierStatistics> stats)
  {
    final int size = stats.size();
    mSimplifierStatistics = new ArrayList<TRSimplifierStatistics>(size);
    mSimplifierStatistics.addAll(stats);
  }

  public void setSimplifierStatistics(final UnifiedEFASimplifier simplifier)
  {
    mSimplifierStatistics = new LinkedList<TRSimplifierStatistics>();
    simplifier.collectStatistics(mSimplifierStatistics);
  }

  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
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
    writer.print("Time consumed by split attempts: ");
    writer.println(mNumberOfSplits);
    writer.print("Number of redundant events: ");
    if (mUnsuccessfulCompositionsCount > 0) {
      final float probability =
        (float) (mTotalCompositionsCount - mUnsuccessfulCompositionsCount) /
        (float) mTotalCompositionsCount;
      formatter.format
        ("Probability of a candidate selection being successful: %.2f%%\n",
         100.0f * probability);
    }
    if (mSynchronisationStatistics != null) {
      writer.println("--------------------------------------------------");
      mSynchronisationStatistics.print(writer);
    }
    if (mPartitioningStatistics != null) {
      writer.println("--------------------------------------------------");
      mPartitioningStatistics.print(writer);
    }
    if (mUnfoldingStatistics != null) {
      writer.println("--------------------------------------------------");
      mUnfoldingStatistics.print(writer);
    }
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        writer.println("--------------------------------------------------");
        ruleStats.print(writer);
      }
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Compositions");
    writer.print(",Overflows");
    writer.print(",SplitAttempts");
    writer.print(",Splits");
    writer.print(",SplitTime");
    if (mSynchronisationStatistics != null) {
      mSynchronisationStatistics.printCSVHorizontalHeadings(writer);
    }
    if (mPartitioningStatistics != null) {
      mPartitioningStatistics.printCSVHorizontalHeadings(writer);
    }
    if (mUnfoldingStatistics != null) {
      mUnfoldingStatistics.printCSVHorizontalHeadings(writer);
    }
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontalHeadings(writer);
      }
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
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
    if (mSynchronisationStatistics != null) {
      mSynchronisationStatistics.printCSVHorizontal(writer);
    }
    if (mPartitioningStatistics != null) {
      mPartitioningStatistics.printCSVHorizontal(writer);
    }
    if (mUnfoldingStatistics != null) {
      mUnfoldingStatistics.printCSVHorizontal(writer);
    }
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontal(writer);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private EFASimplifierStatistics mSynchronisationStatistics;
  private EFASimplifierStatistics mPartitioningStatistics;
  private EFASimplifierStatistics mUnfoldingStatistics;
  private List<TRSimplifierStatistics> mSimplifierStatistics;

  private int mTotalCompositionsCount;
  private int mUnsuccessfulCompositionsCount;
  private final int mNumberOfSplitAttempts;
  private final int mNumberOfSplits;
  private final long mSplitTime;

}


