//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   ControllabilityCounterExampleCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
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
    final SafetyTraceProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start1", "finish1");
    checkCounterExample(des, trace);
  }

  public void test_SmallFactory2u_missingLast() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyTraceProxy trace =
      createSafetyTrace(des, "start1", "finish1");
    checkCounterExample(des, trace,
                        "is accepted by all specifications");
  }

  public void test_SmallFactory2u_endsControllable() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyTraceProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start1");
    checkCounterExample(des, trace,
                        "ends with controllable event 'start1'");
  }

  public void test_SmallFactory2u_notAcceptedByPlant() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyTraceProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start2", "finish1");
    checkCounterExample(des, trace,
                        "is rejected by plant 'machine1' in step 4");
  }

  public void test_SmallFactory2u_notAcceptedBySpec() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyTraceProxy trace =
      createSafetyTrace(des, "start1", "finish1", "start1", "finish1", "finish1");
    checkCounterExample(des, trace,
                        "is rejected by plant 'machine1' in step 5");
  }

  public void test_SmallFactory2u_null() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyTraceProxy trace =
      createSafetyTrace(des, null, "start1", "finish1", "start1", "finish1");
    checkCounterExample(des, trace,
                        "contains NULL event in step 1");
  }

  public void test_SmallFactory2u_proposition() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    final SafetyTraceProxy trace =
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
    final SafetyTraceProxy trace = mFactory.createSafetyTraceProxy(des, events);
    checkCounterExample(des, trace,
                        "contains unknown event 'finish1' in step 4");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkCounterExample(final ProductDESProxy des,
                                   final SafetyTraceProxy trace)
    throws AnalysisException
  {
    checkCounterExample(des, trace, null);
  }

  private void checkCounterExample(final ProductDESProxy des,
                                   final SafetyTraceProxy trace,
                                   final String expectedDiagnostics)
    throws AnalysisException
  {
    final boolean expected = expectedDiagnostics == null;
    final boolean actual = mChecker.checkCounterExample(des, trace);
    assertEquals("Unexpected result from ControllabilityCounterExampleChecker!",
                 expected, actual);
    if (!actual && !expected) {
      final String actualDiagnostics = mChecker.getDiagnostics();
      assertTrue("Unexpected diagnostics message: " + actualDiagnostics,
                 actualDiagnostics.endsWith(expectedDiagnostics + "."));
    }
  }

  private SafetyTraceProxy createSafetyTrace(final ProductDESProxy des,
                                             final String... names)
  {
    final List<EventProxy> events = createEventList(des, names);
    return mFactory.createSafetyTraceProxy(des, events);
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
