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


  //#########################################################################
  //# Printing
  public void print(PrintStream stream)
  {
    stream.println("Verification: " + mSatisfied);
  }


  //#########################################################################
  //# Data Members
  private final boolean mSatisfied;

}
