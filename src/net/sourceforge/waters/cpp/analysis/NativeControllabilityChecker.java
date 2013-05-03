//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A monolithic controllability checker implementation, written in C++.</P>
 *
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
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public SafetyDiagnostics getDiagnostics()
  {
    return LanguageInclusionDiagnostics.getInstance();
  }

}
