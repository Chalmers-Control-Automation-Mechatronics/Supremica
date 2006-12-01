//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   NativeControllabilityCheckerTest
//###########################################################################
//# $Id: AllHeuristicTest.java,v 1.3 2006-12-01 02:16:42 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
//import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class AllHeuristicTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(AllHeuristicTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ModularControllabilityChecker createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker(null, factory,
                                             new NativeControllabilityChecker(null, factory),
                                             new AllHeuristic(HeuristicType.NOPREF), false);
  }
}
