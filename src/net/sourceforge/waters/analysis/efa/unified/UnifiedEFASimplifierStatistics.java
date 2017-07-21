//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.efa.unified;

import java.io.PrintWriter;
import java.util.Formatter;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * A record holding performance statistics about the application of transition
 * relation simplifier during a verification run. The intended use is that a
 * rule will be applied multiple times during the composition of a model, and
 * this will hold accumulative data of each application of the rule.
 *
 * @author Sahar Mohajerani, Robi Malik
 */
public class UnifiedEFASimplifierStatistics
{

  //#########################################################################
  //# Constructors
  public UnifiedEFASimplifierStatistics(final Object simplifier,
                                        final boolean trans)
  {
    mSimplifierClass = simplifier.getClass();
    mApplicationCount = mOverflowCount = mReductionCount = 0;
    mInputStates = mOutputStates = mUnchangedStates = 0;
    if (trans) {
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = 0;
    } else {
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = -1;
    }
    mRunTime = 0;
    mPeakMemoryUsage = DefaultAnalysisResult.getCurrentMemoryUsage();
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the class of the simplifier these statistics are for.
   */
  public Class<?> getSimplifierClass()
  {
    return mSimplifierClass;
  }

  /**
   * Gets the accumulative time taken to run the abstraction rule.
   */
  public long getRunTime()
  {
    return mRunTime;
  }

  /**
   * Gets the number of times this abstraction rule is applied to the model.
   */
  public int getApplicationCount()
  {
    return mApplicationCount;
  }

  /**
   * Gets the number of times this abstraction rule reduced the size of the
   * model it was applied to.
   */
  public int getReductionCount()
  {
    return mReductionCount;
  }


  /**
   * Gets the sum of the number of input states for this rule.
   */
  public int getInputStates()
  {
    return mInputStates;
  }

  /**
   * Gets the sum of the number of output states for this rule when a reduction
   * occurred.
   */
  public int getOutputStates()
  {
    return mOutputStates;
  }

  /**
   * Gets the sum of the number of input states for this rule when no reduction
   * occurred.
   */
  public int getUnchangedStates()
  {
    return mUnchangedStates;
  }

  /**
   * Gets the sum of the total number of output states for this rule. This
   * includes output for when a reduction did occur and when a reduction did not
   * occur.
   */
  public int getTotalOutputStates()
  {
    return mOutputStates + mUnchangedStates;
  }

  /**
   * Gets the sum of the number of input transitions for this rule.
   */
  public int getInputTransitions()
  {
    return mInputTransitions;
  }

  /**
   * Gets the sum of the number of output transitions for this rule when a
   * reduction occurred.
   */
  public int getOutputTransitions()
  {
    return mOutputTransitions;
  }

  /**
   * Gets the sum of the number of input transitions for this rule when no
   * reduction occurred.
   */
  public int getUnchangedTransitions()
  {
    return mUnchangedTransitions;
  }

  /**
   * Gets the sum of the total number of output transitions for this rule. This
   * includes output for when a reduction did occur and when a reduction did not
   * occur.
   */
  public int getTotalOutputTransitions()
  {
    return mOutputTransitions + mUnchangedTransitions;
  }

  public long getPeakMemoryUsage()
  {
    return mPeakMemoryUsage;
  }

  public void updatePeakMemoryUsage(final long usage)
  {
    if (usage > mPeakMemoryUsage) {
      mPeakMemoryUsage = usage;
    }
  }

  /**
   * Updates the recorded memory usage.
   * This method checks whether the amount of memory currently in use by
   * the Java virtual machine exceeds the currently recorded memory usage,
   * and if so, updates the recorded usage.
   */
  public void updatePeakMemoryUsage()
  {
    final long usage = DefaultAnalysisResult.getCurrentMemoryUsage();
    updatePeakMemoryUsage(usage);
  }


  //#########################################################################
  //# Providing Statistics
  public void recordStart(final UnifiedEFATransitionRelation tr)
  {
    mApplicationCount++;
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    if (mInputStates >= 0) {
      mInputStates += rel.getNumberOfStates();
      mInputTransitions += rel.getNumberOfTransitions();
    }
  }

  public void recordStart(final UnifiedEFATransitionRelation tr1,
                          final UnifiedEFATransitionRelation tr2)
  {
    mApplicationCount++;
    if (mInputStates >= 0) {
      final ListBufferTransitionRelation rel1 = tr1.getTransitionRelation();
      final ListBufferTransitionRelation rel2 = tr2.getTransitionRelation();
      mInputStates += rel1.getNumberOfStates() * rel2.getNumberOfStates();
      mInputTransitions += rel1.getNumberOfTransitions() + rel2.getNumberOfTransitions();
    }
  }

  public void recordStart(final UnifiedEFAVariable var)
  {
    mApplicationCount++;
    if (mInputStates >= 0) {
      mInputStates += var.getRange().size();
    }
  }

  public void recordFinish(final UnifiedEFATransitionRelation tr,
                           final boolean success)
  {
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    if (success) {
      mReductionCount++;
      if (mOutputStates >= 0) {
        mOutputStates += rel.getNumberOfStates();
        mOutputTransitions += rel.getNumberOfTransitions();
      }

    } else {
      if (mUnchangedStates >= 0) {
        mUnchangedStates += rel.getNumberOfStates();
        mUnchangedTransitions += rel.getNumberOfTransitions();
      }
    }
  }

  public void recordFinish(final UnifiedEFAVariable var, final TRPartition partition)
  {
    if (partition != null) {
      mReductionCount++;
      if (mOutputStates >= 0) {
        mOutputStates += partition.getNumberOfClasses();
      }

    } else {
      if (mUnchangedStates >= 0) {
        mUnchangedStates += var.getRange().size();
      }
    }
  }

  public void recordOverflow()
  {
    mOverflowCount++;
  }

  public void recordRunTime(final long runTime)
  {
    mRunTime += runTime;
  }

  public void setRunTime(final long runTime)
  {
    mRunTime = runTime;
  }

  public void merge(final UnifiedEFASimplifierStatistics stats)
  {
    if (mSimplifierClass == stats.mSimplifierClass) {
      mApplicationCount += stats.mApplicationCount;
      mOverflowCount += stats.mOverflowCount;
      mReductionCount += stats.mReductionCount;

      mInputStates = DefaultAnalysisResult.mergeAdd(mInputStates,
                                                    stats.mInputStates);
      mOutputStates =
        DefaultAnalysisResult.mergeAdd(mOutputStates, stats.mOutputStates);
      mUnchangedStates =
        DefaultAnalysisResult.mergeAdd(mUnchangedStates,
                                       stats.mUnchangedStates);
      mInputTransitions =
        DefaultAnalysisResult.mergeAdd(mInputTransitions,
                                       stats.mInputTransitions);
      mOutputTransitions =
        DefaultAnalysisResult.mergeAdd(mOutputTransitions,
                                       stats.mOutputTransitions);
      mUnchangedTransitions =
        DefaultAnalysisResult.mergeAdd(mUnchangedTransitions,
                                       stats.mUnchangedTransitions);

      mRunTime += stats.mRunTime;
    } else {
      throw new ClassCastException
      ("Attempting to merge statistics for " +
        ProxyTools.getShortClassName(mSimplifierClass) +
        " with statistics for " +
        ProxyTools.getShortClassName(stats.mSimplifierClass) + "!");
    }
  }

  public void reset()
  {
    mApplicationCount = mOverflowCount = mReductionCount = 0;
    if (mInputStates >= 0) {
      mInputStates = mOutputStates = mUnchangedStates = 0;
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = 0;
    }
    mRunTime = 0;
  }


  //#########################################################################
  //# Printing
  public void print(final PrintWriter writer)
  {
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    writer.print("Name of rule: ");
    writer.println(ProxyTools.getShortClassName(mSimplifierClass));
    writer.print("Total number of times applied: ");
    writer.println(mApplicationCount);
    writer.print("Number of times a reduction occurred: ");
    writer.println(mReductionCount);
    if (mOverflowCount > 0) {
      writer.print("Number of times an overflow occurred: ");
      writer.println(mOverflowCount);
    }
    final float probability =
      (float) mReductionCount / (float) mApplicationCount;
    formatter.format("Probability of a reduction occurring: %.2f%%\n",
                     100.0f * probability);
    if (mInputStates >= 0) {
      writer.print("Number of input states: ");
      writer.println(mInputStates);
      writer.print("Number of output states: ");
      writer.println(getTotalOutputStates());
      writer.print("Number of input transitions: ");
      writer.println(mInputTransitions);
      writer.print("Number of output transitions: ");
      writer.println(getTotalOutputTransitions());
    }
    final float seconds = 0.001f * mRunTime;
    formatter.format("Total runtime: %.3fs\n", seconds);
  }

  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    writer.print(',');
    writer.print(ProxyTools.getShortClassName(mSimplifierClass));
    writer.print(",Applied");
    writer.print(",Reductions");
    writer.print(",Overflows");
    if (mInputStates >= 0) {
      writer.print(",InStates");
      writer.print(",OutStatesRed");
      writer.print(",OutStatesNoRed");
      writer.print(",InTrans");
      writer.print(",OutTransRed");
      writer.print(",OutTransNoRed");
    }
    writer.print(",RunTime");
  }

  public void printCSVHorizontal(final PrintWriter writer)
  {
    writer.print(",,");  // empty column under rule name
    writer.print(mApplicationCount);
    writer.print(',');
    writer.print(mReductionCount);
    writer.print(',');
    writer.print(mOverflowCount);
    if (mInputStates >= 0) {
      writer.print(',');
      writer.print(mInputStates);
      writer.print(',');
      writer.print(mOutputStates);
      writer.print(',');
      writer.print(mUnchangedStates);
      writer.print(',');
      writer.print(mInputTransitions);
      writer.print(',');
      writer.print(mOutputTransitions);
      writer.print(',');
      writer.print(mUnchangedTransitions);
    }
    writer.print(',');
    writer.print(mRunTime);
  }


  //#########################################################################
  //# Data Members
  private final Class<?> mSimplifierClass;

  private int mApplicationCount;
  private int mOverflowCount;
  private int mReductionCount;
  private int mInputStates;
  private int mOutputStates;
  private int mUnchangedStates;
  private int mInputTransitions;
  private int mOutputTransitions;
  private int mUnchangedTransitions;
  private long mRunTime;
  private long mPeakMemoryUsage;

}
