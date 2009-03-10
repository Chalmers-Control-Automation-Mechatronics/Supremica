//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicConflictCheckerTest
  extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(MonolithicConflictCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ConflictChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new MonolithicConflictChecker(factory);
  }

}
