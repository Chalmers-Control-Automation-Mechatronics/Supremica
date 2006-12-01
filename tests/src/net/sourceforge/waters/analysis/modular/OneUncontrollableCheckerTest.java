//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionCheckerTest
//###########################################################################
//# $Id: OneUncontrollableCheckerTest.java,v 1.1 2006-12-01 02:16:43 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import junit.framework.Test;
import junit.framework.TestSuite;

//import net.sourceforge.waters.analysis.monolithic.
//       MonolithicLanguageInclusionChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.
       AbstractLargeControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class OneUncontrollableCheckerTest
  extends AbstractLargeControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(OneUncontrollableCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected OneUncontrollableChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    return new OneUncontrollableChecker
      (null, factory,
       new ModularControllabilityChecker(null, factory,
                                         new NativeControllabilityChecker(null, factory),
                                         new MaxCommonEventsHeuristic(HeuristicType.NOPREF),
                                         false));
  }

}
