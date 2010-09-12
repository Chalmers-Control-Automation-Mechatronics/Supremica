//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityCheckerTest
//###########################################################################
//# $Id: ProjectingControllabilityCheckerTest.java 5900 2010-08-19 05:45:16Z cmjr1 $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
  AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;


public class Projecting3ControllabilityCheckerTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(Projecting3ControllabilityCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ProjectingControllabilityChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final ControllabilityChecker subchecker =
      new NativeControllabilityChecker(factory);
    final Projection3 projector = new Projection3(factory);
    projector.setOutputStream(true);
    final ProjectingControllabilityChecker checker =
      new ProjectingControllabilityChecker(factory, subchecker, projector);
    //checker.setMaxProjStates(2000);
    return checker;
  }

}
