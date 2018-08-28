//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * A record holding performance statistics about the application of transition
 * relation simplifier during a verification run. The intended use is that a
 * rule will be applied multiple times during the composition of a model, and
 * this will hold accumulative data of each application of the rule.
 *
 * @author Rachel Francis, Robi Malik
 */
public class TRSimplifierStatistics
{

  //#########################################################################
  //# Constructors
  public TRSimplifierStatistics(final Object simplifier,
                                final boolean trans,
                                final boolean markings)
  {
    this(simplifier, trans, trans, markings);
  }

  public TRSimplifierStatistics(final Object simplifier,
                                final boolean states,
                                final boolean trans,
                                final boolean markings)
  {
    mSimplifierClass = simplifier.getClass();
    mApplicationCount = mOverflowCount = mReductionCount = 0;
    if (states) {
      mInputStates = mOutputStates = mUnchangedStates = 0;
    } else {
      mInputStates = mOutputStates = mUnchangedStates = -1;
    }
    if (trans) {
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = 0;
    } else {
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = -1;
    }
    if (markings) {
      mInputMarkings = mOutputMarkings = mUnchangedMarkings = 0;
    } else {
      mInputMarkings = mOutputMarkings = mUnchangedMarkings = -1;
    }
    mRunTime = 0;
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
   * Gets the number of times this abstraction rule has been unsuccessful
   * and resulted in an overflow.
   */
  public int getOverflowCount()
  {
    return mOverflowCount;
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
   * Gets the sum of the number of input markings for this rule.
   */
  public int getInputMarkings()
  {
    return mInputMarkings;
  }

  /**
   * Gets the sum of the number of output markings for this rule when a
   * reduction occurred.
   */
  public int getOutputMarkings()
  {
    return mOutputMarkings;
  }

  /**
   * Gets the sum of the number of output markings for this rule when no
   * reduction occurred.
   */
  public int getUnchangedMarkings()
  {
    return mUnchangedMarkings;
  }

  /**
   * Gets the sum of the total number of output markings for this rule. This
   * includes output for when a reduction did occur and when a reduction did not
   * occur.
   */
  public int getTotalOutputMarkings()
  {
    return mOutputMarkings + mUnchangedMarkings;
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
  public int getChangedOutputStates()
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
   * includes output for when a reduction did occur and when a reduction did
   * not occur.
   */
  public int getOutputStates()
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
  public int getChangedOutputTransitions()
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
   * Gets the sum of the total number of output transitions for this rule.
   * This includes output for when a reduction did occur and when a reduction
   * did not occur.
   */
  public int getOutputTransitions()
  {
    return mOutputTransitions + mUnchangedTransitions;
  }


  //#########################################################################
  //# Providing Statistics
  public void recordStart(final ListBufferTransitionRelation rel)
  {
    mApplicationCount++;
    if (mInputStates >= 0) {
      mInputStates += rel.getNumberOfReachableStates();
    }
    if (mInputTransitions >= 0) {
      mInputTransitions += rel.getNumberOfTransitions();
    }
    if (mInputMarkings >= 0) {
      mInputMarkings += rel.getNumberOfMarkings(false);
    }
  }

  public void recordStart(final AutomatonProxy aut)
  {
    mApplicationCount++;
    if (mInputStates >= 0) {
      mInputStates += aut.getStates().size();
    }
    if (mInputTransitions >= 0) {
      mInputTransitions += aut.getTransitions().size();
    }
    if (mInputMarkings >= 0) {
      for (final StateProxy state : aut.getStates()) {
        mInputMarkings += state.getPropositions().size();
      }
    }
  }

  public void recordFinish(final ListBufferTransitionRelation rel,
                           final boolean success)
  {
    if (success) {
      mReductionCount++;
      if (mOutputStates >= 0) {
        mOutputStates += rel.getNumberOfReachableStates();
      }
      if (mInputTransitions >= 0) {
        mOutputTransitions += rel.getNumberOfTransitions();
      }
      if (mOutputMarkings >= 0) {
        mOutputMarkings += rel.getNumberOfMarkings(false);
      }
    } else {
      if (mUnchangedStates >= 0) {
        mUnchangedStates += rel.getNumberOfReachableStates();
      }
      if (mUnchangedTransitions >= 0) {
        mUnchangedTransitions += rel.getNumberOfTransitions();
      }
      if (mUnchangedMarkings >= 0) {
        mUnchangedMarkings += rel.getNumberOfMarkings(false);
      }
    }
  }

  public void recordFinish(final AutomatonProxy aut, final boolean success)
  {
    if (success) {
      mReductionCount++;
      if (mOutputStates >= 0) {
        mOutputStates += aut.getStates().size();
      }
      if (mOutputTransitions >= 0) {
        mOutputTransitions += aut.getTransitions().size();
      }
      if (mOutputMarkings >= 0) {
        for (final StateProxy state : aut.getStates()) {
          mOutputMarkings += state.getPropositions().size();
        }
      }
    } else {
      if (mUnchangedStates >= 0) {
        mUnchangedStates += aut.getStates().size();
      }
      if (mUnchangedTransitions >= 0) {
        mUnchangedTransitions += aut.getTransitions().size();
      }
      if (mUnchangedMarkings >= 0) {
        for (final StateProxy state : aut.getStates()) {
          mUnchangedMarkings += state.getPropositions().size();
        }
      }
    }
  }

  public void recordOverflow(final ListBufferTransitionRelation rel)
  {
    mOverflowCount++;
    recordFinish(rel, false);
  }

  public void recordOverflow(final AutomatonProxy aut)
  {
    mOverflowCount++;
    recordFinish(aut, false);
  }

  public void recordRunTime(final long runTime)
  {
    mRunTime += runTime;
  }

  public void setRunTime(final long runTime)
  {
    mRunTime = runTime;
  }

  public void merge(final TRSimplifierStatistics stats)
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
      mInputMarkings =
        DefaultAnalysisResult.mergeAdd(mInputMarkings, stats.mInputMarkings);
      mOutputMarkings =
        DefaultAnalysisResult.mergeAdd(mOutputMarkings,
                                       stats.mOutputMarkings);
      mUnchangedMarkings =
        DefaultAnalysisResult.mergeAdd(mUnchangedMarkings,
                                       stats.mUnchangedMarkings);
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
    }
    if (mInputTransitions >= 0) {
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = 0;
    }
    if (mInputMarkings >= 0) {
      mInputMarkings = mOutputMarkings = mUnchangedMarkings = 0;
    }
    mRunTime = 0;
  }


  //#########################################################################
  //# Printing
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter stream = new PrintWriter(writer);
    print(stream);
    return writer.toString();
  }

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
      writer.println(getOutputStates());
    }
    if (mInputTransitions >= 0) {
      writer.print("Number of input transitions: ");
      writer.println(mInputTransitions);
      writer.print("Number of output transitions: ");
      writer.println(getOutputTransitions());
    }
    if (mInputMarkings >= 0) {
      writer.print("Number of input markings: ");
      writer.println(mInputMarkings);
      writer.print("Number of output markings: ");
      writer.println(getTotalOutputMarkings());
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
    }
    if (mInputTransitions >= 0) {
      writer.print(",InTrans");
      writer.print(",OutTransRed");
      writer.print(",OutTransNoRed");
    }
    if (mInputMarkings >= 0) {
      writer.print(",InMarkings");
      writer.print(",OutMarkingsRed");
      writer.print(",OutMarkingsNoRed");
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
    }
    if (mInputTransitions >= 0) {
      writer.print(',');
      writer.print(mInputTransitions);
      writer.print(',');
      writer.print(mOutputTransitions);
      writer.print(',');
      writer.print(mUnchangedTransitions);
    }
    if (mInputMarkings >= 0) {
      writer.print(',');
      writer.print(mInputMarkings);
      writer.print(',');
      writer.print(mOutputMarkings);
      writer.print(',');
      writer.print(mUnchangedMarkings);
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
  private int mInputMarkings;
  private int mOutputMarkings;
  private int mUnchangedMarkings;
  private long mRunTime;

}
