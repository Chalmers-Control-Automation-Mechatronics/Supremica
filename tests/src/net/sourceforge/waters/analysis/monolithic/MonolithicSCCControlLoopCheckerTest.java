//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractControlLoopCheckerTest;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
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
    checkNonLoopEvents(new String[]{"open_out"});
  }

  public void test_liquidControl2() throws Exception
  {
    test_liquidControl();
    test_liquidControl();
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
