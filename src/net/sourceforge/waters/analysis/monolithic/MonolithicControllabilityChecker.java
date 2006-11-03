//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicControllabilityChecker
//###########################################################################
//# $Id: MonolithicControllabilityChecker.java,v 1.1 2006-11-03 05:18:28 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
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
    super(model, IdenticalKindTranslator.getInstance(), factory);
  }

}
