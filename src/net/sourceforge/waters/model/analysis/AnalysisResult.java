//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisResult
//###########################################################################
//# $Id: AnalysisResult.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
  public boolean getSatisfied()
  {
    return mSatisfied;
  }


  //#########################################################################
  //# Data Members
  private final boolean mSatisfied;

}
