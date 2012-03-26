//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDPlantCompletenessChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Mahvash Baloch
 */

public class SDPlantCompletenessChecker
  extends ModularControllabilityChecker
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public SDPlantCompletenessChecker(final ProductDESProxyFactory factory,
                                    final SafetyVerifier checker)
  {
    this(null,factory,checker,false);
  }

  public SDPlantCompletenessChecker(final ProductDESProxy model,
                                    final ProductDESProxyFactory factory,
                                    final SafetyVerifier checker,
                                    final boolean least)
  {
    super(model, factory,checker, false);
    setKindTranslator(SDPlantCompletenessKindTranslator.getInstance());


  }

}
