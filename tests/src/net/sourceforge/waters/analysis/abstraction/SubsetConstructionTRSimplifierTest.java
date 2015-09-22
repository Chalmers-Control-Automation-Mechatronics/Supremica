//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * A test for the subset construction algorithm
 * ({@link SubsetConstructionTRSimplifier}).
 *
 * @author Robi Malik
 */

public class SubsetConstructionTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(SubsetConstructionTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    final SubsetConstructionTRSimplifier simplifier =
      new SubsetConstructionTRSimplifier();
    simplifier.setFailingEventsAsSelfloops(true);
    return simplifier;
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final EventEncoding enc = super.createEventEncoding(des, aut);
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = enc.getProperEvent(e);
      final String name = event.getName();
      if (name.startsWith(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        byte status = enc.getProperEventStatus(e);
        status |= EventStatus.STATUS_FAILING;
        status |= EventStatus.STATUS_ALWAYS_ENABLED;
        enc.setProperEventStatus(e, status);
      }
    }
    return enc;
  }

  @Override
  protected SubsetConstructionTRSimplifier getTransitionRelationSimplifier()
  {
    return (SubsetConstructionTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_determinisation_1()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_2()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_3()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_4()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_4.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_5()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_5.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_6()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_6.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_7()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_7.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_determinisation_8()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "determinisation_8.wmod");
    runTransitionRelationSimplifier(des);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_determinisation_4();
    test_determinisation_3();
    test_determinisation_2();
    test_determinisation_1();
    test_determinisation_2();
    test_determinisation_3();
    test_determinisation_4();
    test_determinisation_5();
    test_determinisation_6();
    test_determinisation_5();
    test_determinisation_4();
    test_determinisation_3();
    test_determinisation_2();
    test_determinisation_1();
  }

}









