//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.Formatter;


/**
 * A record containing the result of a {@link ModelAnalyser}.
 *
 * The analysis result contains all data returned from running an analysis
 * algorithm. This includes computed data such as counterexamples or generated
 * automata as well as runtime statistics.
 *
 * This base only provides basic information, namely the Boolean result of
 * an analysis run and the time taken. More specific result data can be added
 * by subclasses.
 *
 * @author Robi Malik
 */

public class AnalysisResult
{

  //#########################################################################
  //# Constructors
  public AnalysisResult(final boolean satisfied)
  {
    mSatisfied = satisfied;
    mRunTime = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the Boolean analysis result.
   * The Boolean result typically indicates whether model checking has found
   * a property of interest to be true, or whether synthesis or a similar has
   * been successful.
   */
  public boolean isSatisfied()
  {
    return mSatisfied;
  }

  /**
   * Gets the runtime of the operation that produced this result.
   * @return Time taken, in milliseconds. A value of <CODE>-1</CODE>
   *         indicates that timing information is not available.
   */
  public long getRunTime()
  {
    return mRunTime;
  }

  /**
   * Sets a runtime for this result.
   * @param time   Time to be stored, in milliseconds.
   */
  public void setRuntime(final long time)
  {
    mRunTime = time;
  }


  //#########################################################################
  //# Printing
  public void print(final PrintStream stream)
  {
    stream.println("Verification result: " + mSatisfied);
    if (mRunTime >= 0) {
      final Formatter formatter = new Formatter(stream);
      final float seconds = 0.001f * mRunTime;
      formatter.format("Total runtime: %.3fs\n", seconds);
    }
  }


  //#########################################################################
  //# Data Members
  private final boolean mSatisfied;
  private long mRunTime;

}
