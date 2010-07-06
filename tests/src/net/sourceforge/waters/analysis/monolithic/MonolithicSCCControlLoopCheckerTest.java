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

  public void test_Nasty_TheVicousLoop1() throws Exception
  {
    super.test_Nasty_TheVicousLoop1();
    checkNonLoopEvents(new String[0]);
  }

  public void test_Nasty_TheVicousLoop2() throws Exception
  {
    super.test_Nasty_TheVicousLoop2();
    checkNonLoopEvents(new String[]{"a", "b", "c"});
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    super.test_Batchtank2005_smr26();
    checkNonLoopEvents(new String[]{"open_out", "close_out", "stirrer_on", "stirrer_off"});
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    super.test_Batchtank2005_gjr5();
    checkNonLoopEvents(new String[0]);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    super.test_Batchtank2005_gb20();
    checkNonLoopEvents(new String[]{"open_out", "close_out", "stirrer_on", "stirrer_off"});
  }

  public void test_liquidControl() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "liquidControl.wmod";
    runModelVerifier(group, dir, name, false);
    checkNonLoopEvents(new String[]{"close_in", "open_in"});
  }


  //#########################################################################
  //# Auxiliary Methods
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
