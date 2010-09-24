//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityDiagnostics;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class MonolithicControllabilityChecker
  extends MonolithicSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public MonolithicControllabilityChecker(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory)
  {
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
  }

}
