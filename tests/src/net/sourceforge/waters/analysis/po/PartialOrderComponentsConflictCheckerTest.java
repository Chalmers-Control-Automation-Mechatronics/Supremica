//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicControllabilityCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.po;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class PartialOrderComponentsConflictCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(PartialOrderComponentsConflictCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ConflictChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new PartialOrderComponentsConflictChecker(factory);
  }
}
