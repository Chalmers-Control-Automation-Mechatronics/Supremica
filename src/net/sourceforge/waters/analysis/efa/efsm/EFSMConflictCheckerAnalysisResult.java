//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
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

public class EFSMConflictCheckerAnalysisResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public EFSMConflictCheckerAnalysisResult()
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
  public void setEFSMSystem (final EFSMSystem system) {
    final int numTR = system.getTransitionRelations().size();
    final int numVar = system.getVariables().size();
    setNumberOfAutomata(numTR + numVar);
    for (final EFSMTransitionRelation efsmTR : system.getTransitionRelations()) {
      addEFSMTransitionRelation(efsmTR);
    }
  }

  public void addEFSMTransitionRelation(final EFSMTransitionRelation efsmTR)
  {
    final ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
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

  public void setSimplifierStatistics(final EFSMTRSimplifier simplifier)
  {
    mSimplifierStatistics = new LinkedList<TRSimplifierStatistics>();
    simplifier.collectStatistics(mSimplifierStatistics);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final EFSMConflictCheckerAnalysisResult result =
      (EFSMConflictCheckerAnalysisResult) other;
    mTotalCompositionsCount += result.mTotalCompositionsCount;
    mUnsuccessfulCompositionsCount += result.mUnsuccessfulCompositionsCount;
    mNumberOfSplitAttempts += result.mNumberOfSplitAttempts;
    mNumberOfSplits += result.mNumberOfSplits;
    mSplitTime += result.mSplitTime;
    if (mSynchronisationStatistics == null) {
      mSynchronisationStatistics = result.mSynchronisationStatistics;
    } else if (result.mSynchronisationStatistics != null) {
      mSynchronisationStatistics.merge(result.mSynchronisationStatistics);
    }
    if (mPartitioningStatistics == null) {
      mPartitioningStatistics = result.mPartitioningStatistics;
    } else if (result.mPartitioningStatistics != null) {
      mPartitioningStatistics.merge(result.mPartitioningStatistics);
    }
    if (mUnfoldingStatistics == null) {
      mUnfoldingStatistics = result.mUnfoldingStatistics;
    } else if (result.mUnfoldingStatistics != null) {
      mUnfoldingStatistics.merge(result.mUnfoldingStatistics);
    }
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
  private int mNumberOfSplitAttempts;
  private int mNumberOfSplits;
  private long mSplitTime;

}
