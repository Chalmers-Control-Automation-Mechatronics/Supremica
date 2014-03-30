//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A result record that can be returned by a compositional verification
 * algorithms such as {@link CompositionalConflictChecker}.
 *
 * @author Rachel Francis, Robi Malik
 */

class CompositionalVerificationResult
  extends CompositionalAnalysisResult
  implements VerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new verification result representing an incomplete run.
   */
  public CompositionalVerificationResult()
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
  //# Interface net.sourceforge.waters.model.analysis.ProxyResult<TraceProxy>
  @Override
  public TraceProxy getComputedProxy()
  {
    return getCounterExample();
  }

  @Override
  public void setComputedProxy(final TraceProxy counterexample)
  {
    setCounterExample(counterexample);
  }

  @Override
  public String getResultDescription()
  {
    return "counterexample";
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.VerificationResult
  @Override
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }

  @Override
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
