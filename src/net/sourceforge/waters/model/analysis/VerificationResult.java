//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id: VerificationResult.java,v 1.2 2006-11-13 03:03:24 siw4 Exp $
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
  
  public VerificationResult(int states)
  {
    this(true, null, states);
  }

  public VerificationResult(final TraceProxy counterexample, int states)
  {
    this(false, counterexample, states);
  }

  public VerificationResult(final boolean satisfied,
			    final TraceProxy counterexample)
  {
    this(satisfied, counterexample, -1);
  }
  
  public VerificationResult(final boolean satisfied,
                            final TraceProxy counterexample,
                            final int states)
  {
    super(satisfied);
    mCounterExample = counterexample;
    mStates = states;
  }


  //#########################################################################
  //# Simple Access Methods
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }
  
  public int getStates()
  {
    return mStates;
  }


  //#########################################################################
  //# Data Members
  private final TraceProxy mCounterExample;
  private final int mStates;
}
