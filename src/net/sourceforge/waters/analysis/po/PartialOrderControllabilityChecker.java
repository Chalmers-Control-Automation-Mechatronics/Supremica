//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.po;

import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Adrian Shaw
 */

public class PartialOrderControllabilityChecker
  extends PartialOrderSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public PartialOrderControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public PartialOrderControllabilityChecker(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory)
  {
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
  }

}
