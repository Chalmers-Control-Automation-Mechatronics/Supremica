//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSCCControlLoopCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractControlLoopCheckerTest;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicSCCControlLoopCheckerTest
  extends AbstractControlLoopCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(MonolithicSCCControlLoopCheckerTest.class);
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
    return new MonolithicSCCControlLoopChecker(factory);
  }


  //#########################################################################
  //# Auxiliary Methods
  @SuppressWarnings("unused")
  private void checkNonLoopEvents(final String[] expectedNames)
  {
    final ControlLoopChecker checker = (ControlLoopChecker) getModelVerifier();
    final ProductDESProxy model = checker.getModel();
    final Set<EventProxy> expectedEvents =
      new THashSet<EventProxy>(expectedNames.length);
    for (final String name : expectedNames) {
      final EventProxy event = findEvent(model, name);
      expectedEvents.add(event);
    }
    final Collection<EventProxy> nonLoopEvents = checker.getNonLoopEvents();
    assertEquals("Incorrect set of non-loop events!",
                 expectedEvents, nonLoopEvents);
  }

}
