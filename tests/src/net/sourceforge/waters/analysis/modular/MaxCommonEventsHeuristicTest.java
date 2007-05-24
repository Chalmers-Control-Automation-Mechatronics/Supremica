//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   NativeControllabilityCheckerTest
//###########################################################################
//# $Id: MaxCommonEventsHeuristicTest.java,v 1.6 2007-05-24 04:03:56 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.analysis.modular.supremica.ProjectingControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.
  AbstractLargeControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.
  AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MaxCommonEventsHeuristicTest
  extends AbstractLargeControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(MaxCommonEventsHeuristicTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ControllabilityChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    return
      new ProjectingControllabilityChecker
            (null, factory,
             new NativeControllabilityChecker(factory),
             new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT), false);
  }
}
