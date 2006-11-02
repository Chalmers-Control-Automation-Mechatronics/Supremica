//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeControllabilityChecker
//###########################################################################
//# $Id: NativeControllabilityChecker.java,v 1.2 2006-11-02 22:40:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * @author Robi Malik
 */

public class NativeControllabilityChecker
  extends NativeModelVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public NativeControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeControllabilityChecker(final ProductDESProxy model,
				      final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ControllabilityChecker
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Native Methods
  native VerificationResult runNativeAlgorithm();

}
