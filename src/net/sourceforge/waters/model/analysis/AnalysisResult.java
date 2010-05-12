//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;


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
  public boolean isSatisfied()
  {
    return mSatisfied;
  }

  public long getRunTime()
  {
    return mRunTime;
  }

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
      final double seconds = 0.001 * mRunTime;
      final NumberFormat formatter = new DecimalFormat("0.00");
      stream.println("Total runtime: " + formatter.format(seconds) + "s");
    }
  }


  //#########################################################################
  //# Data Members
  private final boolean mSatisfied;
  private long mRunTime;

}
