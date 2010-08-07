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
 * also can store a Boolean result and the time taken. More specific result data
 * can be added by subclasses.
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
  }

  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    writer.print("Result,Runtime [ms]");
  }


  //#########################################################################
  //# Data Members
  private boolean mFinished;
  private boolean mSatisfied;
  private long mRunTime;
  private AnalysisException mException;

}
