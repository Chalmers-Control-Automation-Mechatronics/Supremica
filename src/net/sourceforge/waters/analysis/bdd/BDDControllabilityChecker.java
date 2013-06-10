//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   BDDControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class BDDControllabilityChecker
  extends BDDSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public BDDControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public BDDControllabilityChecker(final ProductDESProxy model,
                                   final ProductDESProxyFactory factory)
  {
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
  }

}
