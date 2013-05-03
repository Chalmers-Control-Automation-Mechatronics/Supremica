//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class SDControllabilityChecker
  extends ModularControllabilityChecker
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public SDControllabilityChecker(final ProductDESProxyFactory factory,
                                  final SafetyVerifier checker)
  {
    this(null,factory,checker,false);
  }

  public SDControllabilityChecker(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final SafetyVerifier checker,
                                  final boolean least)
  {super(model, factory,checker, false);

  }

}
