//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.analysis.coobs.CoobservabilityDiagnostics;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * Abstract base class to test the interface {@link CoobservabilityChecker}.
 *
 * @author Robi Malik
 */

public abstract class AbstractCoobservabilityCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractCoobservabilityCheckerTest()
  {
  }

  public AbstractCoobservabilityCheckerTest(final String name)
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
    testSmallFactory2();
    testAbp1();
    testAbp3();
    testOverflowException();
    testSmallFactory2();
    testAbp3();
    testAbp1();
    testAbp3();
  }

  public void testOverflowException()
    throws Exception
  {
    final ModelVerifier verifier = getModelVerifier();
    try {
      verifier.setNodeLimit(2);
      testAbp1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      final OverflowKind kind = exception.getOverflowKind();
      assertTrue("Unexpected overflow kind!",
                 kind == OverflowKind.STATE || kind == OverflowKind.NODE);
      final AnalysisResult result = verifier.getAnalysisResult();
      assertNotNull("Got NULL analysis result after exception!", result);
      assertNotNull("No exception in analysis result after caught exception!",
                    result.getException());
      assertSame("Unexpected exception in analysis result!",
                 exception, result.getException());
    } finally {
      verifier.setNodeLimit(Integer.MAX_VALUE);
    }
  }


  //#########################################################################
  //# Test Cases --- specific tests for coobservability
  public void testAbp1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp1.wmod");
    runModelVerifier(des, true);
  }

  public void testAbp2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp2.wmod");
    runModelVerifier(des, true);
  }

  public void testAbp3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp3.wmod");
    runModelVerifier(des, false);
  }

  public void testAbp4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp4.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory2Coobs1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "small_factory_2_coobs1.wmod");
    runModelVerifier(des, false);
  }

  public void testSmallFactory2Coobs2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "small_factory_2_coobs2.wmod");
    runModelVerifier(des, false);
  }

  public void testSmallFactory2Coobs3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "small_factory_2_coobs3.wmod");
    runModelVerifier(des, true);
  }


  public void testParManEgLoCoobs2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("despot", "parallelManufacturingExample",
                     "parManEg_lo_coobs2.wmod");
    runModelVerifier(des, false);
  }

  public void testParManEgLoCoobs3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("despot", "parallelManufacturingExample",
                     "parManEg_lo_coobs3.wmod");
    runModelVerifier(des, true);
  }


  public void testTrafficlightsYip1Coobs1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1coobs1.wmod");
    runModelVerifier(des, false);
  }

  public void testTrafficlightsYip1Coobs2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1coobs2.wmod");
    runModelVerifier(des, false);
  }

  public void testTrafficlightsYip1Coobs3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1coobs3.wmod");
    runModelVerifier(des, true);
  }

  public void testTrafficlightsYip1Coobs4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1coobs4.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- controllability plus observability via coobservability
  public void testSimpleCoobs() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "simple_coobs.wmod");
    runModelVerifier(des, true);
  }

  public void testTrafficlightsAc61Coobs() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ac61coobs.wmod");
    runModelVerifier(des, false);
  }

  public void testTrafficlightsYip1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- controllability via coobservability
  public void testEmptySpec() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "empty_spec.wmod");
    runModelVerifier(des, true);
  }

  public void testParManEgLoCoobs1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("despot", "parallelManufacturingExample",
                     "parManEg_lo_coobs1.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory2u() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    runModelVerifier(des, false);
  }

  public void testTictactoe() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "tictactoe.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final Collection<String> empty = Collections.emptyList();
    compiler.setEnabledPropositionNames(empty);
    compiler.setEnabledPropertyNames(empty);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws Exception
  {
    super.checkCounterExample(des, counter);
    final List<TraceProxy> traces = counter.getTraces();
    final Iterator<TraceProxy> iter = traces.iterator();
    final TraceProxy referenceTrace = iter.next();
    assertEquals("The first trace of the counterexample is not labelled " +
                 "as reference trace!",
                 CoobservabilityDiagnostics.REFERENCE_SITE_NAME,
                 referenceTrace.getName());
    final Map<String,TraceProxy> traceMap = new LinkedHashMap<>(traces.size() - 1);
    final Set<String> remainingNames = new THashSet<>(traces.size() - 1);
    while (iter.hasNext()) {
      final TraceProxy trace = iter.next();
      final String name = trace.getName();
      traceMap.put(name, trace);
      remainingNames.add(name);
    }

    final EventProxy lastEvent = getLastEvent(referenceTrace);
    final String lastEventName = lastEvent.getName();
    final Map<String,String> attribs = lastEvent.getAttributes();
    boolean siteFound = false;
    for (final Map.Entry<String,String> entry : attribs.entrySet()) {
      final String key = entry.getKey();
      if (key.startsWith(CoobservabilityAttributeFactory.CONTROLLABITY_KEY)) {
        final String name = entry.getValue();
        checkExpectedController(name, traceMap, remainingNames, lastEventName);
        siteFound = true;
      }
    }
    if (!siteFound && lastEvent.getKind() == EventKind.CONTROLLABLE) {
      checkExpectedController(CoobservabilityAttributeFactory.DEFAULT_SITE_NAME,
                              traceMap, remainingNames, lastEventName);
    }
    if (!remainingNames.isEmpty()) {
      final String name = remainingNames.iterator().next();
      fail("The counterexample contains a trace named '" + name +
           "' that does not correspond to any supervisor site "+
           "that can disable its last event '" + lastEventName + "'!");
    }

    checkTrace(des, referenceTrace, true);
    for (final TraceProxy trace : traceMap.values()) {
      final EventProxy traceLastEvent = getLastEvent(trace);
      assertSame("The last event of the counterexample trace '" +
                 trace.getName() + "' is '" + traceLastEvent.getName() +
                 "', which is different from the last event '" +
                 lastEventName + "' of the reference trace!",
                 lastEvent, traceLastEvent);
      checkTrace(des, trace, false);
      checkObservability(referenceTrace, trace);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventProxy getLastEvent(final TraceProxy trace)
  {
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int numSteps = steps.size();
    assertTrue("The counterexample trace '" + trace.getName() +
               "'is missing a failing event at the end!",
               numSteps >= 2);
    final TraceStepProxy lastStep = steps.get(numSteps - 1);
    return lastStep.getEvent();
  }

  private void checkTrace(final ProductDESProxy des,
                          final TraceProxy trace,
                          final boolean reference)
  {
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (aut.getKind()) {
      case PLANT:
        checkTrace(aut, trace);
        break;
      case SPEC:
      case SUPERVISOR:
        checkTrace(aut, trace, reference);
        break;
      default:
        break;
      }
    }
  }

  private void checkExpectedController(final String siteName,
                                       final Map<String,TraceProxy> traceMap,
                                       final Set<String> remainingNames,
                                       final String lastEventName)
  {
    if (!remainingNames.remove(siteName)) {
      if (traceMap.containsKey(siteName)) {
        fail("The counterexample contains more than one trace named '" +
             siteName + "'!");
      } else {
        fail("The counterexample contains no trace for the supervisor site '" +
             siteName + "' that disables its last event '" + lastEventName + "'!");
      }
    }
  }

  private void checkObservability(final TraceProxy referenceTrace,
                                  final TraceProxy trace)
  {
    final String siteName = trace.getName();
    final List<EventProxy> referenceEvents = referenceTrace.getEvents();
    final Iterator<EventProxy> referenceIter = referenceEvents.iterator();
    EventProxy referenceEvent = advanceToObservable(referenceIter, siteName);
    final List<EventProxy> traceEvents = trace.getEvents();
    final Iterator<EventProxy> traceIter = traceEvents.iterator();
    EventProxy traceEvent = advanceToObservable(traceIter, siteName);
    while (referenceEvent != null && traceEvent != null) {
      assertSame("The counterexample trace '" + siteName +
                 "' + contains a step with observable event '" +
                 traceEvent.getName() +
                 "', while the reference trace expects a step with '" +
                 referenceEvent.getName() + "' instead!",
                 referenceEvent, traceEvent);
      referenceEvent = advanceToObservable(referenceIter, siteName);
      traceEvent = advanceToObservable(traceIter, siteName);
    }
    if (referenceEvent != null) {
      fail("The counterexample trace '" + siteName +
           "' does not contain any step matching the observable event '" +
           referenceEvent.getName() + "' at the end of the reference trace!");
    }
    if (traceEvent != null) {
      fail("The counterexample trace '" + siteName +
           "' contains an observable event '" + traceEvent.getName() +
           "' at the end that does not match anything in the reference trace!");
    }
  }

  private EventProxy advanceToObservable(final Iterator<EventProxy> iter,
                                         final String siteName)
  {
    while (iter.hasNext()) {
      final EventProxy event = iter.next();
      final Map<String,String> attribs = event.getAttributes();
      boolean siteFound = false;
      for (final Map.Entry<String,String> entry : attribs.entrySet()) {
        final String key = entry.getKey();
        if (key.startsWith(CoobservabilityAttributeFactory.OBSERVABITY_KEY)) {
          final String name = entry.getValue();
          if (siteName.equals(name)) {
            return event;
          }
        }
        siteFound = true;
      }
      if (!siteFound && event.isObservable() &&
          siteName.equals(CoobservabilityAttributeFactory.DEFAULT_SITE_NAME)) {
        return event;
      }
    }
    return null;
  }

}
