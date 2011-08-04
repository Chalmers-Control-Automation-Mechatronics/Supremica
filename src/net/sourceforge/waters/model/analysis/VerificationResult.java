//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record returned by a {@link ModelVerifier}. A verification result
 * contains the information on whether a property checked is true or false, and
 * in the latter case, it also contains a counterexample.
 *
 * @author Robi Malik
 */

public class VerificationResult extends AnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new verification result representing an incomplete run.
   */
  public VerificationResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the counter example computed by the model checker, or
   * <CODE>null</CODE> if the property checked was true.
   */
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }

  /**
   * Sets the counterexample obtained from verification. Setting the
   * counterexample also marks the verification result as completed and sets the
   * Boolean result to <CODE>false</CODE>.
   */
  public void setCounterExample(final TraceProxy counterexample)
  {
    super.setSatisfied(false);
    mCounterExample = counterexample;
  }

  @Override
  public void setSatisfied(final boolean sat)
  {
    super.setSatisfied(sat);
    if (sat) {
      mCounterExample = null;
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final VerificationResult result = (VerificationResult) other;
    if (mCounterExample == null) {
      mCounterExample = result.mCounterExample;
    }
  }


  //#########################################################################
  //# Data Members
  private TraceProxy mCounterExample;

}
