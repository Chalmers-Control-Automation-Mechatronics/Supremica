//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.TraceProxy;


/**
 * The standard implementation of the {@link VerificationResult} interface.
 * The default analysis provides read/write access to all the data provided
 * by the interface.
 *
 * @author Robi Malik
 */

public class DefaultVerificationResult
  extends DefaultAnalysisResult
  implements VerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new verification result representing an incomplete run.
   */
  public DefaultVerificationResult()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void setSatisfied(final boolean sat)
  {
    super.setSatisfied(sat);
    if (sat) {
      mCounterExample = null;
    }
  }

  
  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.VerificationResult
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }

  public void setCounterExample(final TraceProxy counterexample)
  {
    super.setSatisfied(false);
    mCounterExample = counterexample;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    if (mCounterExample == null) {
      final VerificationResult result = (VerificationResult) other;
      mCounterExample = result.getCounterExample();
    }
  }


  //#########################################################################
  //# Data Members
  private TraceProxy mCounterExample;

}
