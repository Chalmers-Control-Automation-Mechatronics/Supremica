//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


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
  //# Data Members
  private final boolean mSatisfied;

}
