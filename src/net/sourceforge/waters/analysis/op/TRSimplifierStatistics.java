//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TRSimplifierStatistics
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.io.PrintWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.base.ProxyTools;


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
  public TRSimplifierStatistics(final TransitionRelationSimplifier simplifier,
                                final boolean trans, final boolean markings)
  {
    mSimplifierClass = simplifier.getClass();
    mApplicationCount = mOverflowCount = mReductionCount = 0;
    if (trans) {
      mInputStates = mOutputStates = mUnchangedStates = 0;
      mInputTransitions = mOutputTransitions = mUnchangedTransitions = 0;
    } else {
      mInputStates = mOutputStates = mUnchangedStates = -1;
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
  public Class<? extends TransitionRelationSimplifier> getSimplifierClass()
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


  //#########################################################################
  //# Providing Statistics
  public void recordStart(final ListBufferTransitionRelation rel)
  {
    mApplicationCount++;
    if (mInputStates >= 0) {
      mInputStates += rel.getNumberOfReachableStates();
      mInputTransitions += rel.getNumberOfTransitions();
    }
    if (mInputMarkings >= 0) {
      mInputMarkings += rel.getNumberOfMarkings();
    }
  }

  public void recordFinish(final ListBufferTransitionRelation rel,
                           final boolean success)
  {
    if (success) {
      mReductionCount++;
      if (mOutputStates >= 0) {
        mOutputStates += rel.getNumberOfReachableStates();
        mOutputTransitions += rel.getNumberOfTransitions();
      }
      if (mOutputMarkings >= 0) {
        mOutputMarkings += rel.getNumberOfMarkings();
      }
    } else {
      if (mUnchangedStates >= 0) {
        mUnchangedStates += rel.getNumberOfReachableStates();
        mUnchangedTransitions += rel.getNumberOfTransitions();
      }
      if (mUnchangedMarkings >= 0) {
        mUnchangedMarkings += rel.getNumberOfMarkings();
      }
    }
  }

  public void recordOverflow(final ListBufferTransitionRelation rel)
  {
    mOverflowCount++;
    recordFinish(rel, false);
  }

  public void recordRunTime(final long runTime)
  {
    mRunTime += runTime;
  }

  public void merge(final TRSimplifierStatistics stats)
  {
    if (mSimplifierClass == stats.mSimplifierClass) {
      mApplicationCount += stats.mApplicationCount;
      mOverflowCount += stats.mOverflowCount;
      mReductionCount += stats.mReductionCount;
      mInputStates = AnalysisResult.mergeAdd(mInputStates, stats.mInputStates);
      mOutputStates =
        AnalysisResult.mergeAdd(mOutputStates, stats.mOutputStates);
      mUnchangedStates =
        AnalysisResult.mergeAdd(mUnchangedStates, stats.mUnchangedStates);
      mInputTransitions =
        AnalysisResult.mergeAdd(mInputTransitions, stats.mInputTransitions);
      mOutputTransitions =
        AnalysisResult.mergeAdd(mOutputTransitions, stats.mOutputTransitions);
      mUnchangedTransitions =
        AnalysisResult.mergeAdd(mUnchangedTransitions,
                                stats.mUnchangedTransitions);
      mInputMarkings =
        AnalysisResult.mergeAdd(mInputMarkings, stats.mInputMarkings);
      mOutputMarkings =
        AnalysisResult.mergeAdd(mOutputMarkings, stats.mOutputMarkings);
      mUnchangedMarkings =
        AnalysisResult.mergeAdd(mUnchangedMarkings, stats.mUnchangedMarkings);
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
    if (mInputMarkings >= 0) {
      mInputMarkings = mOutputMarkings = mUnchangedMarkings = 0;
    }
    mRunTime = 0;
  }


  //#########################################################################
  //# Printing
  public void print(final PrintWriter writer)
  {
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
  private final Class<? extends TransitionRelationSimplifier> mSimplifierClass;

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
