//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class ProjectingControllabilityChecker
  extends ProjectingSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ProjectingControllabilityChecker(final ProductDESProxyFactory factory,
                                          final SafetyVerifier checker)
  {
    this(null, factory, checker);
  }

  public ProjectingControllabilityChecker(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory,
                                          final SafetyVerifier checker)
  {
    super(model, ControllabilityKindTranslator.getInstance(),
          factory, checker);
  }

  public ProjectingControllabilityChecker(final ProductDESProxyFactory factory,
                                          final SafetyVerifier checker,
                                          final int projsize)
  {
    this(null, factory, checker, projsize);
  }

  public ProjectingControllabilityChecker(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory,
                                          final SafetyVerifier checker,
                                          final int projsize)
  {
    super(model, ControllabilityKindTranslator.getInstance(),
          factory, checker, projsize);
  }

}
