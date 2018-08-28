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
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


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

  public void testNotDiag10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "diagnosability", "notDiag_10.wmod");
    runModelVerifier(des, false);
  }

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
  {
    try {
    super.checkCounterExample(des, counter);
    }catch(final Exception e){

    }
    final DualCounterExampleProxy dual = (DualCounterExampleProxy) counter;
    final List<TraceProxy> traces = dual.getTraces();
    final TraceProxy traceA = traces.get(0);
    final TraceProxy traceB = traces.get(1);
    final List<TraceStepProxy> stepsA = traceA.getTraceSteps();
    final List<TraceStepProxy> stepsB = traceB.getTraceSteps();
    final int loopIndexA = traceA.getLoopIndex();
    final int loopIndexB = traceB.getLoopIndex();
    final int lenA = stepsA.size();
    final int lenB = stepsB.size();
    final Collection<AutomatonProxy> automata = des.getAutomata();

    // Both traces are not empty
    assertTrue("TraceA is empty", lenA > 0);
    assertTrue("TraceB is empty", lenB > 0);

    // Both loops are not empty
    assertTrue("TraceA loop is empty", lenA - loopIndexA > 0);
    assertTrue("TraceB loop is empty", lenB - loopIndexB > 0);

    // Both traces have valid loops
    final Map<AutomatonProxy,StateProxy> lastMapA
    = stepsA.get(lenA-1).getStateMap();
    final Map<AutomatonProxy,StateProxy> lastMapB
    = stepsB.get(lenB-1).getStateMap();
    final Map<AutomatonProxy,StateProxy> loopMapA
    = stepsA.get(loopIndexA).getStateMap();
    final Map<AutomatonProxy,StateProxy> loopMapB
    = stepsB.get(loopIndexB).getStateMap();
    for(final AutomatonProxy aut : automata) {
      System.out.println(loopMapA.get(aut).getName()+" "+lastMapA.get(aut).getName());
      System.out.println(loopMapB.get(aut).getName()+" "+lastMapB.get(aut).getName());
      System.out.println();
      assertTrue("TraceA does not have a valid loop",
                 lastMapA.get(aut)==loopMapA.get(aut));
      assertTrue("TraceB does not have a valid loop",
                 lastMapB.get(aut)==loopMapB.get(aut));
    }

    // traceA has fault event
    int iA=1;
    EventProxy eventA = null;
    final String[] split = dual.getComment().split(" ");
    final String faultClass = split[2];
    String eventFC;
    Map<String,String> attrib;
    boolean foundFault = false;
    while(iA < lenA) {
      eventA = stepsA.get(iA++).getEvent();
      attrib = eventA.getAttributes();
      eventFC = attrib.get("FAULT");
      System.out.println(eventA.getName());
      if(faultClass.equals(eventFC))
        foundFault = true;
    }
    assertTrue("TraceA has no fault event", foundFault);
    System.out.println();
    // traceB has no fault event
    int iB=1;
    EventProxy eventB = null;
    foundFault = false;
    while(iB < lenB) {
      eventB = stepsB.get(iB++).getEvent();
      attrib = eventB.getAttributes();
      eventFC = attrib.get("FAULT");
      System.out.println(eventB.getName());
      if(faultClass.equals(eventFC))
        foundFault = true;
    }
    assertFalse("TraceB has a fault event", foundFault);

    // Both traces have identical observable events
    iA=1;
    iB=1;
    eventA = null;
    eventB = null;
    while(iA<lenA||iA<lenB) {
      if(iA<lenA)
        eventA = stepsA.get(iA++).getEvent();
      if(iB<lenB)
        eventB = stepsB.get(iB++).getEvent();
      while(!eventA.isObservable()&&iA<lenA) {
        eventA = stepsA.get(iA++).getEvent();
      }
      while(!eventB.isObservable()&&iB<lenB) {
        eventB = stepsB.get(iB++).getEvent();
      }
      assertTrue("Traces do not have identical observable events"
                 , eventA.equals(eventB));
    }

    //both traces are possible
    for(final AutomatonProxy aut : automata) {
      final StateProxy lastA = lastMapA.get(aut);
      final StateProxy lastB = lastMapB.get(aut);
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
        assertTrue("TraceA is not possible in automaton  "+aut.getName(),
          lastA == super.checkTrace(aut, traceA));
        assertTrue("TraceB is not possible in automaton  "+aut.getName(),
          lastB == super.checkTrace(aut, traceB));
      case SPEC:
        assertTrue("TraceA is not possible in automaton  "+aut.getName(),
          lastA == super.checkTrace(aut, traceA));
        assertTrue("TraceB is not possible in automaton  "+aut.getName(),
          lastB == super.checkTrace(aut, traceB));
      default:
      }
    }
  }



}
