//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifier
//###########################################################################
//# $Id: AbstractModelVerifier.java,v 1.4 2006-11-15 01:26:40 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * An abstract base class that can be used for all model verifier
 * implementations. In addition to the model and factory members inherited
 * from {@link AbstractModelAnalyser}, this class provides access to a
 * verification result member, and uses this to implement access to the
 * Boolean result value and the counterexample.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelVerifier
  extends AbstractModelAnalyser
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractModelVerifier(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractModelVerifier(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mStateLimit = Integer.MAX_VALUE;
    mResult = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  public boolean isSatisfied()
  {
    if (mResult != null) {
      return mResult.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  public TraceProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      return mResult.getCounterExample();
    }
  }

  public VerificationResult getAnalysisResult()
  {
    return mResult;
  }

  public void clearAnalysisResult()
  {
    mResult = null;
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores a verification result indicating that the property checked
   * is satisfied.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    mResult = new VerificationResult();
    addStatistics(mResult);
    return true;
  }

  /**
   * Stores a verification result indicating that the property checked
   * is not satisfied.
   * @param  counterexample The counterexample obtained by verification.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final TraceProxy counterexample)
  {
    mResult = new VerificationResult(counterexample);
    addStatistics(mResult);
    return false;
  }

  /**
   * Gets the total number of states constructed by this verifier.
   * @return The number of states constructed, or <CODE>-1</CODE> to
   *         indicate that this figure is not available.
   */
  protected void addStatistics(VerificationResult result)
  {
  }


  //#########################################################################
  //# Data Members
  private int mStateLimit;
  private VerificationResult mResult;

}
