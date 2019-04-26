//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * Contains statistics about the application of an abstraction rule to one
 * model. Intended use is that a rule will be applied multiple times during the
 * composition of a model, and this will hold accumulative data of each
 * application of the rule.
 *
 * @author Rachel Francis
 */

public class AbstractionRuleStatistics
{

  //#########################################################################
  //# Constructors
  public AbstractionRuleStatistics(final Class<? extends AbstractionRule> clazz)
  {
    mRuleClass = clazz;
    mRunTime = 0;
    mAppliedCount = 0;
    mReductionCount = 0;
    mInputStates = 0;
    mOutputStates = 0;
    mInputTransitions = 0;
    mOutputTransitions = 0;
    mUnchangedStates = 0;
    mUnchangedTransitions = 0;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the class of the abstraction rule these statistics are for.
   */
  public Class<? extends AbstractionRule> getRuleClass()
  {
    return mRuleClass;
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
    return mAppliedCount;
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
   * Gets the sum of the number of input states for this rule when a reduction
   * occurred.
   */
  public int getInputStatesSum()
  {
    return mInputStates;
  }

  /**
   * Gets the sum of the number of output states for this rule when a reduction
   * occurred.
   */
  public int getOutputStatesSum()
  {
    return mOutputStates;
  }

  /**
   * Gets the sum of the number of input transitions for this rule when a
   * reduction occurred.
   */
  public int getInputTransitionsSum()
  {
    return mInputTransitions;
  }

  /**
   * Gets the sum of the number of output transitions for this rule when a
   * reduction occurred.
   */
  public int getOutputTransitionsSum()
  {
    return mOutputTransitions;
  }

  /**
   * Gets the sum of the number of input states for this rule when a reduction
   * didn't occur (therefore, output count = this input count).
   */
  public int getUnchangedStatesSum()
  {
    return mUnchangedStates;
  }

  /**
   * Gets the sum of the number of input transitions for this rule when a
   * reduction didn't occur (therefore, output count = this input count).
   */
  public int getUnchangedTransitionsSum()
  {
    return mUnchangedTransitions;
  }

  /**
   * Gets the sum of the total number of input states for this rule. This
   * includes input for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalInputStates()
  {
    return mInputStates + mUnchangedStates;
  }

  /**
   * Gets the sum of the total number of input transitions for this rule. This
   * includes input for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalInputTransitions()
  {
    return mInputTransitions + mUnchangedTransitions;
  }

  /**
   * Gets the sum of the total number of output states for this rule. This
   * includes output for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalOutputStates()
  {
    return mOutputStates + mUnchangedStates;
  }

  /**
   * Gets the sum of the total number of output transitions for this rule. This
   * includes output for when a reduction did occur and when a reduction didn't
   * occur.
   */
  public int getTotalOutputTransitions()
  {
    return mOutputTransitions + mUnchangedTransitions;
  }


  //#########################################################################
  //# Providing Statistics
  /**
   * Sets the number of times this abstraction rule reduced the size of the
   * model it was applied to.
   */
  public void setReductionCount(final int reduced)
  {
    mReductionCount = reduced;
  }

  /**
   * Sets the time taken for the abstraction rule to run.
   */
  public void setRunTime(final long runTime)
  {
    mRunTime = runTime;
  }

  /**
   * Sets the number of times this abstraction rule is applied to the model.
   */
  public void setApplicationCount(final int count)
  {
    mAppliedCount = count;
  }

  /**
   * Sets the sum of the number of input states for this rule when a reduction
   * occurred.
   */
  public void setInputStates(final int sum)
  {
    mInputStates = sum;
  }

  /**
   * Sets the sum of the number of output states for this rule when a reduction
   * occurred.
   */
  public void setOutputStates(final int sum)
  {
    mOutputStates = sum;
  }

  /**
   * Sets the sum of the number of input transitions for this rule when a
   * reduction occurred.
   */
  public void setInputTransitions(final int sum)
  {
    mInputTransitions = sum;
  }

  /**
   * Sets the sum of the number of output transitions for this rule when a
   * reduction occurred.
   */
  public void setOutputTransitions(final int sum)
  {
    mOutputTransitions = sum;
  }

  /**
   * Sets the sum of the number of input states for this rule when a reduction
   * didn't occur.
   */
  public void setUnchangedStates(final int sum)
  {
    mUnchangedStates = sum;
  }

  /**
   * Sets the sum of the number of input transitions for this rule when a
   * reduction didn't occur.
   */
  public void setUnchangedTransitions(final int sum)
  {
    mUnchangedTransitions = sum;
  }


  //#########################################################################
  //# Merging
  public void merge(final AbstractionRuleStatistics stats)
  {
    if (mRuleClass == stats.mRuleClass) {
      mAppliedCount += stats.mAppliedCount;
      mReductionCount += stats.mReductionCount;
      mInputStates =
        DefaultAnalysisResult.mergeAdd(mInputStates, stats.mInputStates);
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
         ProxyTools.getShortClassName(mRuleClass) +
         " with statistics for " +
         ProxyTools.getShortClassName(stats.mRuleClass) + "!");
    }
  }


  //#########################################################################
  //# Printing
  public void print(final PrintWriter writer)
  {
    writer.println("Name of rule: " + ProxyTools.getShortClassName(mRuleClass));
    writer.println("Total number of times applied: " + mAppliedCount);

    writer.println("Total run time: " + mRunTime);
    writer.println("Total number of times a reduction occurred: " +
                   mReductionCount);
    final double probability =
        (double) mReductionCount / (double) mAppliedCount;
    writer.println("Probability of a reduction occurring: " + probability);
    writer.println("Total number of input states: " + getTotalInputStates());
    writer.println("Total number of input transitions: " +
                   getTotalInputTransitions());
    writer.println("Total number of output states: " + getTotalOutputStates());
    writer.println("Total number of output transitions: " +
                   getTotalOutputTransitions());
    writer.println("Sum of input states with a reduction: " + mInputStates);
    writer.println("Sum of input transitions with a reduction: " +
                   mInputTransitions);
    writer.println("Sum of output states with a reduction: " + mOutputStates);
    writer.println("Sum of output transitions with a reduction: " +
                   mOutputTransitions);
    writer.println("Sum of input states with no reduction: " +
                   mUnchangedStates);
    writer.println("Sum of input transitions with no reduction: " +
                   mUnchangedTransitions);

  }

  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    writer.print(",Rule Name");
    writer.print(",Applied");
    writer.print(",RunTime");
    writer.print(",Reduction");
    writer.print(",Prob Reduction");
    writer.print(",State Red %");
    writer.print(",Trans Red %");
    writer.print(",Tot In States");
    writer.print(",Tot In Trans");
    writer.print(",Tot Out States");
    writer.print(",Tot Out Trans");
    writer.print(",Sum In S Reduction");
    writer.print(",Sum In T Reduction");
    writer.print(",Sum Out S Reduction");
    writer.print(",Sum Out T Reduction");
    writer.print(",Sum S NoRed");
    writer.print(",Sum T NoRed");
  }

  public void printCSVHorizontal(final PrintWriter writer)
  {
    writer.print("," + ProxyTools.getShortClassName(mRuleClass));
    writer.print("," + mAppliedCount);
    writer.print("," + mRunTime);
    writer.print("," + mReductionCount );
    double probability = (double) mReductionCount / (double) mAppliedCount;
    writer.print("," + probability);
    probability = (double) (getTotalInputStates() - getTotalOutputStates()) /
                  (double) getTotalInputStates();
    writer.print("," + probability);
    probability =
      (double) (getTotalInputTransitions() - getTotalOutputTransitions()) /
      (double) getTotalInputTransitions();
    writer.print("," + probability);
    writer.print("," + getTotalInputStates());
    writer.print("," + getTotalInputTransitions());
    writer.print("," + getTotalOutputStates());
    writer.print("," + getTotalOutputTransitions());
    writer.print("," + mInputStates);
    writer.print("," + mInputTransitions);
    writer.print("," + mOutputStates);
    writer.print("," + mOutputTransitions);
    writer.print("," + mUnchangedStates);
    writer.print("," + mUnchangedTransitions);
  }


  //#########################################################################
  //# Data Members
  private final Class<? extends AbstractionRule> mRuleClass;
  private long mRunTime;
  private int mAppliedCount;
  private int mReductionCount;
  private int mInputStates;
  private int mOutputStates;
  private int mInputTransitions;
  private int mOutputTransitions;
  private int mUnchangedStates;
  private int mUnchangedTransitions;

}
