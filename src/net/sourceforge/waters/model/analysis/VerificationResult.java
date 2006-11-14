//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id: VerificationResult.java,v 1.3 2006-11-14 03:32:30 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.TraceProxy;


public class VerificationResult extends AnalysisResult
{

  //#########################################################################
  //# Constructors
  public VerificationResult()
  {
    this(true, null);
  }

  public VerificationResult(final TraceProxy counterexample)
  {
    this(false, counterexample);
  }
  
  public VerificationResult(final boolean satisfied,
                            final TraceProxy counterexample)
  {
    super(satisfied);
    mCounterExample = counterexample;
    mTotalNumberOfStates = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }
  
  public int getTotalNumberOfStates()
  {
    return mTotalNumberOfStates;
  }


  //#########################################################################
  //# Providing Statistics
  public void setNumberOfStates(final int numstates)
  {
    if (mTotalNumberOfStates < 0) {
      mTotalNumberOfStates = numstates;
    } else {
      throw new IllegalStateException
	("Trying to overwrite previously set total number of states " +
	 "in verification result!");
    }
  }


  //#########################################################################
  //# Data Members
  private final TraceProxy mCounterExample;

  private int mTotalNumberOfStates;

}
