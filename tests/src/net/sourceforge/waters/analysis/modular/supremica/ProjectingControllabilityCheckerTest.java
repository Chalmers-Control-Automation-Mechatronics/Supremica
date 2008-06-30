//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular.supremica
//# CLASS:   ProjectingControllabilityCheckerTest
//###########################################################################
//# $Id: ProjectingControllabilityCheckerTest.java,v 1.3 2008-06-30 01:50:57 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular.supremica;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.modular.HeuristicType;
import net.sourceforge.waters.analysis.modular.MaxCommonEventsHeuristic;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
  AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;


public class ProjectingControllabilityCheckerTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    TestSuite testSuite =
      new TestSuite(ProjectingControllabilityCheckerTest.class);
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
       new MonolithicControllabilityChecker(factory),
       new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT), false);
  }

}
