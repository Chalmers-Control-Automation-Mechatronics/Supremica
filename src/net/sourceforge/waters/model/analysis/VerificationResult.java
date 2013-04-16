//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   VerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.analysis.des.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record returned by a {@link ModelVerifier}. A verification result
 * contains the information on whether a property checked is true or false,
 * and in the latter case, it also contains a counterexample.
 *
 * @author Robi Malik
 */

public interface VerificationResult extends AnalysisResult
{

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the counter example computed by the model checker, or
   * <CODE>null</CODE> if the property checked was true.
   */
  public TraceProxy getCounterExample();

  /**
   * Sets the counterexample obtained from verification. Setting the
   * counterexample also marks the verification result as completed and sets the
   * Boolean result to <CODE>false</CODE>.
   */
  public void setCounterExample(final TraceProxy trace);

}
