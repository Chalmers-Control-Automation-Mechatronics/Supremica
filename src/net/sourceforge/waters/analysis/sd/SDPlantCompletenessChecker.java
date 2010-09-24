//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDPlantCompletenessChecker
//###########################################################################
//# $Id: MonolithicControllabilityChecker.java 5118 2010-01-17 06:28:22Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.monolithic.MonolithicSafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Mahvash Baloch
 */

public class SDPlantCompletenessChecker
  extends MonolithicSafetyVerifier
  //implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public SDPlantCompletenessChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SDPlantCompletenessChecker(final ProductDESProxy model,
                                    final ProductDESProxyFactory factory)
  {
    super(model,
          SDPlantCompletenessKindTranslator.getInstance(),
          SDPlantCompletenessDiagnostics.getInstance(),
          factory);
  }

}
