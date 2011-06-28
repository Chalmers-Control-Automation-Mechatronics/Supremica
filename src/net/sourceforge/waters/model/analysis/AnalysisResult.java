//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.base.ProxyTools;


/**
 * A record containing the result of a {@link ModelAnalyser}.
 *
 * The analysis result contains all data returned from running an analysis
 * algorithm. This includes computed data such as counterexamples or generated
 * automata as well as runtime statistics.
 *
 * This base only provides basic information, so it can be determined whether
 * the analysis operation has terminated and if an exception has occurred. It
 * also can store a Boolean result and the time taken, and it records basic
 * statistics about automata sizes. More specific result data can be added
 * by subclasses.
 *
 * @author Robi Malik
 */

public class AnalysisResult
{

  //#########################################################################
  //# Constructors
  public AnalysisResult()
  {
    mFinished = false;
    mRunTime = -1;
    mException = null;
    mTotalNumberOfAutomata = -1;
    mTotalNumberOfStates = -1.0;
    mPeakNumberOfStates = -1.0;
    mTotalNumberOfTransitions = -1.0;
    mPeakNumberOfTransitions = -1.0;
    mPeakNumberOfNodes = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the termination status of this analysis result. An analysis result may
   * be created while an analysis operation is in progress.
   * @return <CODE>true</CODE> to confirm that the result represents a completed
   *         analysis run, <CODE>false</CODE> otherwise.
   */
  public boolean isFinished()
  {
    return mFinished;
  }

  /**
   * Gets the Boolean analysis result. The Boolean result typically indicates
   * whether model checking has found a property of interest to be true, or
   * whether synthesis or a similar has been successful.
   */
  public boolean isSatisfied()
  {
    return mSatisfied;
  }

  /**
   * Gets the exception produced by the analysis run. If analysis is aborted by
   * an exception, the exception should be stored on the analysis result. Note,
   * if an exception is set, the Boolean result and other information may not be
   * accurate.
   * @see #isSatisfied()
   */
  public AnalysisException getException()
  {
    return mException;
  }

  /**
   * Gets the runtime of the operation that produced this result.
   * @return Time taken, in milliseconds. A value of <CODE>-1</CODE> indicates
   *         that timing information is not available.
   */
  public long getRunTime()
  {
    return mRunTime;
  }

  /**
   * Sets the Boolean analysis result. Setting the result also marks the result
   * run as 'finished'.
   * @see #isSatisfied()
   * @see #isFinished()
   */
  public void setSatisfied(final boolean sat)
  {
    mFinished = true;
    mSatisfied = sat;
  }

  /**
   * Stores an exception on this analysis result. Setting the result also marks
   * the result run as 'finished'.
   * @see #isFinished()
   */
  public void setException(final AnalysisException exception)
  {
    mFinished = true;
    mException = exception;
  }

  /**
   * Sets a runtime for this result.
   * @param time
   *          Time to be stored, in milliseconds.
   */
  public void setRuntime(final long time)
  {
    mRunTime = time;
  }

  /**
   * Gets the total number of automata used by the analysis.
   * @return The number of automata, or <CODE>-1</CODE> if unknown.
   */
  public int getTotalNumberOfAutomata()
  {
    return mTotalNumberOfAutomata;
  }


  /**
   * Gets the total number of states constructed by the analysis.
   * @return The total number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getTotalNumberOfStates()
  {
    return mTotalNumberOfStates;
  }


  /**
   * Gets the maximum number of states constructed by the analysis. The peak
   * number of states should identify the size of the largest automaton
   * constructed. For monolithic algorithms, it will be equal to the total
   * number of states, but for compositional algorithms it may be different.
   * @return The peak number of states, or <CODE>-1</CODE> if unknown.
   */
  public double getPeakNumberOfStates()
  {
    return mPeakNumberOfStates;
  }


  /**
   * <P>
   * Gets the maximum number of nodes used during analysis.
   * </P>
   * <P>
   * A 'node' here represents a basic unit of memory such as a state in a
   * synchronous product or a BDD node.
   * </P>
   * <P>
   * <I>Note.</I> It does not make much sense to speak of the total number of
   * nodes in BDD-based algorithms, as the final number of nodes often is much
   * smaller than the size of interim BDDs. Therefore, no total number of nodes
   * will be computed.
   * </P>
   *
   * @return The peak number of nodes, or <CODE>-1</CODE> if unknown.
   */
  public int getPeakNumberOfNodes()
  {
    return mPeakNumberOfNodes;
  }


  /**
   * Gets the total number of transitions constructed by the analysis.
   * @return The total number of transitions, or <CODE>-1</CODE> if unknown.
   */
  public double getTotalNumberOfTransitions()
  {
    return mTotalNumberOfTransitions;
  }


  /**
   * Gets the maximum number of transitions constructed by the analysis. The
   * peak number of transitions should identify the size of the largest
   * automaton constructed. For monolithic algorithms, it will be equal to the
   * total number of transitions, but for compositional algorithms it may be
   * different.
   * @return The peak number of transitions, or <CODE>-1</CODE> if unknown.
   */
  public double getPeakNumberOfTransitions()
  {
    return mPeakNumberOfTransitions;
  }


  /**
   * Specifies a value for the total number of automata used by the analysis.
   */
  public void setNumberOfAutomata(final int numaut)
  {
    mTotalNumberOfAutomata = numaut;
  }


  /**
   * Specifies a value for both the peak and total number of states constructed
   * by the analysis.
   */
  public void setNumberOfStates(final double numstates)
  {
    setTotalNumberOfStates(numstates);
    setPeakNumberOfStates(numstates);
  }


  /**
   * Specifies a value for the total number of states constructed by the
   * analysis.
   */
  public void setTotalNumberOfStates(final double numstates)
  {
    mTotalNumberOfStates = numstates;
  }


  /**
   * Specifies a value for the peak number of states constructed by the
   * analysis.
   */
  public void setPeakNumberOfStates(final double numstates)
  {
    mPeakNumberOfStates = numstates;
  }


  /**
   * Specifies a value for both the peak and total number of transitions
   * constructed by the analysis.
   */
  public void setNumberOfTransitions(final double numtrans)
  {
    setTotalNumberOfTransitions(numtrans);
    setPeakNumberOfTransitions(numtrans);
  }


  /**
   * Specifies a value for the total number of transitions constructed by the
   * analysis.
   */
  public void setTotalNumberOfTransitions(final double numtrans)
  {
    mTotalNumberOfTransitions = numtrans;
  }


  /**
   * Specifies a value for the peak number of transitions constructed by the
   * analysis.
   */
  public void setPeakNumberOfTransitions(final double numtrans)
  {
    mPeakNumberOfTransitions = numtrans;
  }


  /**
   * Specifies the maximum number of nodes used during analysis. A 'node' here
   * represents a basic unit of memory such as a state in a synchronous product
   * or a BDD node.
   */
  public void setPeakNumberOfNodes(final int numnodes)
  {
    mPeakNumberOfNodes = numnodes;
  }


  //#########################################################################
  //# Merging
  /**
   * <P>Merges this result with another.</P>
   *
   * <P>This method destructively modifies the contents of this result record
   * by merging in the contents of the given other record.</P>
   *
   * <P>Merging is done for the purpose of accumulating statistics over
   * multiple runs of the same algorithm. The way how information is merged
   * depends on the type of data stored, e.g., run times and totals of state
   * numbers will be added, while for other values such as peak state number
   * the maximum value will be chosen. Not all result data makes sense for
   * multiple runs, so items that are only relevant for a single run (such
   * as counterexamples) may be removed by this method.</P>
   * @param other
   *          The record to be merged into this record.
   * @throws ClassCaseException
   *          to indicate that the two records merged are not of exactly the
   *          same type.
   */
  public void merge(final AnalysisResult other)
  {
    if (other.getClass() == getClass()) {
      mFinished &= other.mFinished;
      mSatisfied &= other.mSatisfied;
      mRunTime = mergeAdd(mRunTime, other.mRunTime);
      if (mException != null) {
        mException = other.mException;
      }
      mTotalNumberOfAutomata =
        mergeAdd(mTotalNumberOfAutomata, other.mTotalNumberOfAutomata);
      mTotalNumberOfStates =
        mergeAdd(mTotalNumberOfStates, other.mTotalNumberOfStates);
      mTotalNumberOfTransitions =
        mergeAdd(mTotalNumberOfTransitions, other.mTotalNumberOfTransitions);
      mPeakNumberOfStates =
        Math.max(mPeakNumberOfStates, other.mPeakNumberOfStates);
      mPeakNumberOfTransitions =
        Math.max(mPeakNumberOfTransitions, other.mPeakNumberOfTransitions);
      mPeakNumberOfNodes =
        Math.max(mPeakNumberOfNodes, other.mPeakNumberOfNodes);
    } else {
      throw new ClassCastException
        ("Attempting to merge " + ProxyTools.getShortClassName(this) +
         " with " + ProxyTools.getShortClassName(other) + "!");
    }
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

  public void print(final PrintStream stream)
  {
    final PrintWriter writer = new PrintWriter(stream);
    print(writer);
    writer.flush();
  }

  public void print(final PrintWriter writer)
  {
    if (mException != null) {
      writer.println("Exception: " + ProxyTools.getShortClassName(mException));
      final String msg = mException.getMessage();
      if (msg != null) {
        writer.println("           " + msg);
      }
    } else {
      writer.println("Verification result: " + mSatisfied);
    }
    if (mRunTime >= 0) {
      final Formatter formatter = new Formatter(writer);
      final float seconds = 0.001f * mRunTime;
      formatter.format("Total runtime: %.3fs\n", seconds);
    }
    if (mTotalNumberOfAutomata >= 0) {
      writer.println("Total number of automata: " + mTotalNumberOfAutomata);
    }
    final Formatter formatter = new Formatter(writer);
    if (mTotalNumberOfStates >= 0) {
      formatter.format("Total number of states: %.0f\n", mTotalNumberOfStates);
    }
    if (mTotalNumberOfTransitions >= 0) {
      formatter.format("Total number of transitions: %.0f\n",
                       mTotalNumberOfTransitions);
    }
    if (mPeakNumberOfStates >= 0) {
      formatter.format("Peak number of states: %.0f\n", mPeakNumberOfStates);
    }
    if (mPeakNumberOfTransitions >= 0) {
      formatter.format("Peak number of transitions: %.0f\n",
                       mPeakNumberOfTransitions);
    }
    if (mPeakNumberOfNodes >= 0) {
      writer.println("Peak number of nodes: " + mPeakNumberOfNodes);
    }
  }

  public void printCSVHorizontal(final PrintWriter writer)
  {
    if (mException != null) {
      writer.print(ProxyTools.getShortClassName(mException));
    } else {
      writer.print(mSatisfied);
    }
    writer.print(',');
    if (mRunTime >= 0) {
      writer.print(mRunTime);
    }
    writer.print(',');
    if (mTotalNumberOfAutomata >= 0) {
      writer.print(mTotalNumberOfAutomata);
    }
    writer.print(',');
    final Formatter formatter = new Formatter(writer);
    if (mTotalNumberOfStates >= 0) {
      formatter.format("%.0f", mTotalNumberOfStates);
    }
    writer.print(',');
    if (mTotalNumberOfTransitions >= 0) {
      formatter.format("%.0f", mTotalNumberOfTransitions);
    }
    writer.print(',');
    if (mPeakNumberOfStates >= 0) {
      formatter.format("%.0f", mPeakNumberOfStates);
    }
    writer.print(',');
    if (mPeakNumberOfTransitions >= 0) {
      formatter.format("%.0f", mPeakNumberOfTransitions);
    }
    writer.print(',');
    if (mPeakNumberOfNodes >= 0) {
      writer.print(mPeakNumberOfNodes);
    }
  }

  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    writer.print("Result,RunTime");
    writer.print(",TotAut");
    writer.print(",TotStates");
    writer.print(",TotTrans");
    writer.print(",PeakStates");
    writer.print(",PeakTrans");
    writer.print(",PeakNodes");
  }


  //#########################################################################
  //# Static Methods
  public static int mergeAdd(final int data1, final int data2)
  {
    if (data1 < 0) {
      return data2;
    } else if (data2 < 0) {
      return data1;
    } else {
      return data1 + data2;
    }
  }

  public static long mergeAdd(final long data1, final long data2)
  {
    if (data1 < 0) {
      return data2;
    } else if (data2 < 0) {
      return data1;
    } else {
      return data1 + data2;
    }
  }

  public static double mergeAdd(final double data1, final double data2)
  {
    if (data1 < 0.0) {
      return data2;
    } else if (data2 < 0.0) {
      return data1;
    } else {
      return data1 + data2;
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mFinished;
  private boolean mSatisfied;
  private long mRunTime;
  private AnalysisException mException;
  private int mTotalNumberOfAutomata;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;
  private int mPeakNumberOfNodes;

}
