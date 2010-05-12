//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;


public class AnalysisResult
{

  //#########################################################################
  //# Constructors
  public AnalysisResult(final boolean satisfied)
  {
    mSatisfied = satisfied;
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
  }


  //#########################################################################
  //# Data Members
  private final boolean mSatisfied;
  private long mRunTime;

}
