//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id: VerificationResult.java,v 1.1 2006-08-15 01:43:06 robi Exp $
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
  }


  //#########################################################################
  //# Simple Access Methods
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }


  //#########################################################################
  //# Data Members
  private final TraceProxy mCounterExample;

}
