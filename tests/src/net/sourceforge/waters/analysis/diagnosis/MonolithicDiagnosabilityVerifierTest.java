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

package net.sourceforge.waters.analysis.diagnosis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


public class MonolithicDiagnosabilityVerifierTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public MonolithicDiagnosabilityVerifierTest()
  {
  }

  public MonolithicDiagnosabilityVerifierTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy des = factory.createProductDESProxy("empty");
    runModelVerifier(des, true);
  }

  public void testReentrant()
    throws Exception
  {
    testEmpty();
    testDiag1();
    testNotDiag1();
    testEmpty();
    testDiag1();
    testNotDiag1();
  }

  public void testOverflowException()
    throws Exception
  {
    try {
      final ModelVerifier verifier = getModelVerifier();
      verifier.setNodeLimit(2);
      testDiag1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- diagnosable
  public void testDiag1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "Diag_1.wmod");
    runModelVerifier(des, true);
  }
  public void testDiag2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "Diag_2.wmod");
    runModelVerifier(des, true);
  }
  public void testDiag3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "Diag_3.wmod");
    runModelVerifier(des, true);
  }
  public void testDiag4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "Diag_4.wmod");
    runModelVerifier(des, true);
  }
  public void testDiag5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "Diag_5.wmod");
    runModelVerifier(des, true);
  }
  public void testDiag6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "Diag_6.wmod");
    runModelVerifier(des, true);
  }


  public void testSmallFactory2d() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "small_factory_2d.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- not diagnosable
  public void testNotDiag1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_1.wmod");
    runModelVerifier(des, false);
  }
  public void testNotDiag2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_2.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_3.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_4.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_5.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_6.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_7.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag8() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_8.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag9() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_9.wmod");
    runModelVerifier(des, false);
  }

  /*
   * Test case has unobservable loop and gives wrong counterexample.
  public void testNotDiag10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_10.wmod");
    runModelVerifier(des, false);
  }
  */

  public void testNotDiag11() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_11.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag12() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_12.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag13() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_13.wmod");
    runModelVerifier(des, false);
  }

  public void testNotDiag14() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_14.wmod");
    runModelVerifier(des, false);
  }


  public void testDistributing1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "distributing1.wmod");
    runModelVerifier(des, false);
  }

  public void testSmallFactory2nd() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "small_factory_2nd.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ModelVerifier createModelVerifier(final ProductDESProxyFactory factory)
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    return new MonolithicDiagnosabilityVerifier(factory, translator);
  }

  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws Exception
  {
    super.checkCounterExample(des, counter);

    final DualCounterExampleProxy dual = (DualCounterExampleProxy) counter;
    final List<TraceProxy> traces = dual.getTraces();
    assertEquals(2, traces.size());

    final Collection<AutomatonProxy> automata = des.getAutomata();
    TraceProxy faultyTrace = null;
    TraceProxy nonFaultyTrace = null;
    traces:
    for (final TraceProxy trace : traces) {
      // Check whether traces are accepted by all automata
      for (final AutomatonProxy aut : automata) {
        super.checkTrace(aut, trace);
      }
      // Identify faulty and non-faulty traces
      for (final EventProxy event : trace.getEvents()) {
        if (isFaultEvent(event, dual)) {
          faultyTrace = trace;
          continue traces;
        }
      }
      nonFaultyTrace = trace;
    }
    assertNotNull("Neither trace includes the fault!", faultyTrace);
    assertNotNull("Both traces include the fault!", nonFaultyTrace);

    // Check for presence of loops
    final int faultyLoopIndex = faultyTrace.getLoopIndex();
    assertTrue("Faulty trace has no loop!", faultyLoopIndex >= 0);
    final int faultySteps = faultyTrace.getTraceSteps().size();
    assertTrue("Faulty trace has empty loop!", faultyLoopIndex < faultySteps);
    int nonFaultyLoopIndex = nonFaultyTrace.getLoopIndex();
    final int nonFaultySteps = nonFaultyTrace.getTraceSteps().size();
    if (nonFaultyLoopIndex < 0) {
      nonFaultyLoopIndex = nonFaultySteps;
    } else {
      assertTrue("Non-faulty trace has empty loop!",
                 nonFaultyLoopIndex < nonFaultySteps);
    }

    // Check whether traces agree on observable events
    int faultyIndex = 0;
    final Iterator<EventProxy> faultyIter =
      faultyTrace.getEvents().iterator();
    int nonFaultyIndex = 0;
    final Iterator<EventProxy> nonFaultyIter =
      nonFaultyTrace.getEvents().iterator();
    beforeLoop:
    while (faultyIndex++ < faultyLoopIndex) {
      final EventProxy faultyEvent = faultyIter.next();
      if (faultyEvent.isObservable()) {
        while (nonFaultyIndex++ < nonFaultyLoopIndex) {
          final EventProxy nonFaultyEvent = nonFaultyIter.next();
          if (faultyEvent == nonFaultyEvent) {
            continue beforeLoop;
          }
        }
        fail("The non-faulty trace does not use the observable event '" +
             faultyEvent.getName() + "' found in the faulty trace!");
      }
    }
    while (nonFaultyIndex++ < nonFaultyLoopIndex) {
      final EventProxy nonFaultyEvent = nonFaultyIter.next();
      assertFalse("The faulty trace does not use the observable event '" +
                  nonFaultyEvent.getName() + "' found in the non-faulty trace!",
                  nonFaultyEvent.isObservable());
    }
    withinLoop:
    while (faultyIter.hasNext()) {
      final EventProxy faultyEvent = faultyIter.next();
      if (faultyEvent.isObservable()) {
        while (nonFaultyIter.hasNext()) {
          final EventProxy nonFaultyEvent = nonFaultyIter.next();
          if (faultyEvent == nonFaultyEvent) {
            continue withinLoop;
          }
        }
        fail("The non-faulty trace does not use the observable event '" +
             faultyEvent.getName() + "' found in the faulty trace!");
      }
    }
    while (nonFaultyIter.hasNext()) {
      final EventProxy nonFaultyEvent = nonFaultyIter.next();
      assertFalse("The faulty trace does not use the observable event '" +
                  nonFaultyEvent.getName() + "' found in the non-faulty trace!",
                  nonFaultyEvent.isObservable());
    }
  }

  private boolean isFaultEvent(final EventProxy event,
                               final DualCounterExampleProxy dual)
  {
    final Map<String,String> attribs = event.getAttributes();
    final String value = attribs.get(DiagnosabilityAttributeFactory.FAULT_KEY);
    if (value == null) {
      return false;
    }
    final String comment = dual.getComment();
    return comment.contains("fault-class " + value);
  }

}
