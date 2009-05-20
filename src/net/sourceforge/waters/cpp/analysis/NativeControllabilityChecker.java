//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class NativeControllabilityChecker
  extends NativeSafetyVerifier
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
    super(model, ControllabilityKindTranslator.getInstance(), factory);
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.cpp.analysis.NativeModelVerifier
  public String getTraceName()
  {
    return getModel().getName() + ":uncontrollable";
  }

}
