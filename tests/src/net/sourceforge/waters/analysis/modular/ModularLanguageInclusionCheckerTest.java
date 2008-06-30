//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionCheckerTest
//###########################################################################
//# $Id: ModularLanguageInclusionCheckerTest.java,v 1.6 2008-06-30 01:50:57 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
       AbstractLanguageInclusionCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ModularLanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(ModularLanguageInclusionCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ModularLanguageInclusionChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    return createLanguageInclusionChecker(factory);
  }
  
  public ModularControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker
      (null,
       factory,
       new NativeControllabilityChecker(factory),
       new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT),
       false);
  }

  public ModularLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker(
       null, factory,
       createControllabilityChecker(factory),
       new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT));
  }

}
