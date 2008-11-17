//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicControlLoopCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractControlLoopCheckerTest;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicControlLoopCheckerTest
  extends AbstractControlLoopCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(MonolithicControlLoopCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ControlLoopChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new MonolithicControlLoopChecker(factory);
  }

}
