//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelVerifier
//###########################################################################
//# $Id: NativeModelVerifier.java,v 1.1 2006-08-15 01:43:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * @author Robi Malik
 */

public abstract class NativeModelVerifier
  extends NativeModelAnalyser
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public NativeModelVerifier(final ProductDESProxy input,
			     final ProductDESProxyFactory factory)
  {
    super(input, factory);
    mResult = null;
  }


  //#########################################################################
  //# Invocation
  public boolean run()
  {
    mResult = null;
    mResult = runNativeAlgorithm();
    return mResult.isSatisfied();
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


  //#########################################################################
  //# Native Methods
  abstract VerificationResult runNativeAlgorithm();


  //#########################################################################
  //# Data Members
  private VerificationResult mResult;

}
