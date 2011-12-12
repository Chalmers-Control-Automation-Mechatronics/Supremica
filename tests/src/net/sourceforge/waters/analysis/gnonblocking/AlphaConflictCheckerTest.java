//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicConflictCheckerTest
//###########################################################################
//# $Id: MonolithicConflictCheckerTest.java 4630 2009-03-10 01:42:07Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.analysis.annotation.ProjectingNonBlockingChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;

public class AlphaConflictCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(AlphaConflictCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  protected void runModelVerifierWithBindings(final ProductDESProxy des,
                                              final boolean expect)
      throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    configureModelVerifier(des);
    final boolean result = getModelVerifier().run();
    //TraceProxy counterexample = null;
    /*if (!result) {
      counterexample = mModelVerifier.getCounterExample();
      precheckCounterExample(counterexample);
      saveCounterExample(counterexample);
    }*/
    assertEquals("Wrong result from model checker: got " + result
        + " but should have been " + expect + "!", expect, result);
    /*if (!expect) {
      checkCounterExample(des, counterexample);
    }*/
    getLogger().info("Done " + des.getName());
  }

  
  
  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ConflictChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new AlphaNonBlockingChecker(null, factory);
  }

}
