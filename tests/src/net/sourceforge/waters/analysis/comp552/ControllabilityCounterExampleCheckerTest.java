//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.comp552;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

public class ControllabilityCounterExampleCheckerTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public ControllabilityCounterExampleCheckerTest()
  {
  }

  public ControllabilityCounterExampleCheckerTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mFactory = ProductDESElementFactory.getInstance();
    mChecker = new ControllabilityCounterExampleChecker();
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    mFactory = null;
    mChecker = null;
  }


  //#########################################################################
  //# Test Cases
  public void test_SmallFactory2u_correct() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start1", "finish1");
    checkCounterExample(des, trace);
  }

  public void test_SmallFactory2u_missingLast() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, "start1", "finish1");
    checkCounterExample(des, trace,
                        "is accepted by all specifications");
  }

  public void test_SmallFactory2u_endsControllable() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start1");
    checkCounterExample(des, trace,
                        "ends with controllable event 'start1'");
  }

  public void test_SmallFactory2u_notAcceptedByPlant() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start2", "finish1");
    checkCounterExample(des, trace,
                        "is rejected by plant 'machine1' in step 4");
  }

  public void test_SmallFactory2u_notAcceptedBySpec() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start1", "finish1", "finish1");
    checkCounterExample(des, trace,
                        "is rejected by plant 'machine1' in step 5");
  }

  public void test_SmallFactory2u_null() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, null, "start1", "finish1", "start1", "finish1");
    checkCounterExample(des, trace,
                        "contains NULL event in step 1");
  }

  public void test_SmallFactory2u_proposition() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyCounterExampleProxy trace =
      createSafetyTrace(des, "start1", ":accepting", "finish1", "start1", "finish1");
    checkCounterExample(des, trace,
                        "contains proposition ':accepting' in step 2");
  }

  public void test_SmallFactory2u_badEvent() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final List<EventProxy> events = createEventList(des, "start1", "finish1", "start1");
    final EventProxy badEvent =
      mFactory.createEventProxy("finish1", EventKind.UNCONTROLLABLE);
    events.add(badEvent);
    final SafetyCounterExampleProxy trace =
      mFactory.createSafetyCounterExampleProxy(des, events);
    checkCounterExample(des, trace,
                        "contains unknown event 'finish1' in step 4");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkCounterExample(final ProductDESProxy des,
                                   final SafetyCounterExampleProxy counter)
    throws AnalysisException
  {
    checkCounterExample(des, counter, null);
  }

  private void checkCounterExample(final ProductDESProxy des,
                                   final SafetyCounterExampleProxy counter,
                                   final String expectedDiagnostics)
    throws AnalysisException
  {
    final boolean expected = expectedDiagnostics == null;
    final boolean actual = mChecker.checkCounterExample(des, counter);
    assertEquals("Unexpected result from ControllabilityCounterExampleChecker!",
                 expected, actual);
    if (!actual && !expected) {
      final String actualDiagnostics = mChecker.getDiagnostics();
      assertTrue("Unexpected diagnostics message: " + actualDiagnostics,
                 actualDiagnostics.endsWith(expectedDiagnostics + "."));
    }
  }

  private SafetyCounterExampleProxy createSafetyTrace(final ProductDESProxy des,
                                                      final String... names)
  {
    final List<EventProxy> events = createEventList(des, names);
    return mFactory.createSafetyCounterExampleProxy(des, events);
  }

  private List<EventProxy> createEventList(final ProductDESProxy des,
                                           final String... names)
  {
    final List<EventProxy> trace = new ArrayList<>(names.length);
    for (final String name : names) {
      if (name == null) {
        trace.add(null);
      } else {
        final EventProxy event = findEvent(des, name);
        trace.add(event);
      }
    }
    return trace;
  }


  //#########################################################################
  //# Data Members
  ProductDESProxyFactory mFactory;
  ControllabilityCounterExampleChecker mChecker;

}
