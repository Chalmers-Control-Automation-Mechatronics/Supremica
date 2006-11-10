//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicControllabilityCheckerTest
//###########################################################################
//# $Id: LateNotAcceptHeuristicTest.java,v 1.1 2006-11-10 23:58:25 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class LateNotAcceptHeuristicTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(LateNotAcceptHeuristicTest.class);
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
                                             new MonolithicControllabilityChecker(null, factory),
                                             new LateNotAcceptHeuristic(false));
  }
}
