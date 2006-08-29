//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControlLoopCheckerTest
//###########################################################################
//# $Id: ControlLoopCheckerTest.java,v 1.7 2006-08-29 03:33:00 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ControlLoopCheckerTest extends AbstractModelCheckerTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public static Test suite() { 
    TestSuite testSuite = new TestSuite(ControlLoopCheckerTest.class);     
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite()); 
  }


  //#########################################################################
  //# Test Cases --- handwritten
  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2.wdes";
    runModelChecker(group, name, true);
  }

  public void testTictactoe() throws Exception
  {
    final String group = "handwritten";
    final String name = "tictactoe.wdes";
    runModelChecker(group, name, true);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void test_Batchtank2005_amk14() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "amk14.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_cjn5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cjn5.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cs37.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ez1.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb20.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb21.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gjr5.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "grj3.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "imr1.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jbr2.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jmr30.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jpt10.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "kah18.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_1.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_2.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_1.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_2.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_rch11() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "rch11.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ry27.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_scs10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "scs10.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "sjw41.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "smr26.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_tk27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tk27.wdes";
    runModelChecker(group, dir, name, true); 
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tp20.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "vl6.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_Nasty_TheVicousLoop() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "the_vicious_loop.wdes";
    runModelChecker(group, dir, name, false);
  }

  public void test_TrafficLights2006_ac61() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ac61.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_plants() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "plants.wdes";
    runModelChecker(group, dir, name, false);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final String group = "valid";
    final String dir  = "big_factory";
    final String name = "bfactory.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testBmw_fh() throws Exception
  {
    final String group = "valid";
    final String dir  = "bmw_fh";
    final String name = "bmw_fh.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void testBorder_cases() throws Exception
  {
    final String group = "valid";
    final String dir  = "border_cases";
    final String name = "never_blow_up.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void testDebounce() throws Exception
  {
    final String group = "valid";
    final String dir = "debounce";
    final String name = "debounce.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testFalko() throws Exception
  {
    final String group = "valid";
    final String dir = "falko";
    final String name = "falko.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testFtuer() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "ftuer.wdes";
    runModelChecker(group, dir, name, false);
  } 

  /*
  public void testFischertechnik() throws Exception
  {
    final String group = "valid";
    final String dir = "fischertechnik";
    final String name = "fischertechnik.wdes";
    runModelChecker(group, dir, name, false);
  }
  */  
  
  public void testKoordwsp() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "koordwsp.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testSafetydisplay() throws Exception
  {
    final String group = "valid";
    final String dir = "safetydisplay";
    final String name = "safetydisplay.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testSmallFactory() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testSmallFactoryUncont() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small_uncont.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testSmd() throws Exception
  {
    final String group = "valid";
    final String dir = "smd";
    final String name = "smdreset.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void testTline_0() throws Exception
  {
    final String group = "valid";
    final String dir = "tline_0";
    final String name = "transferline_templ.wdes";
    runModelChecker(group, dir, name, true);
  }
  
  public void testTline_1() throws Exception
  {
    final String group = "valid";
    final String dir = "tline_1";
    final String name = "tline_1.wdes";
    runModelChecker(group, dir, name, true);
  }

  public void testWeiche() throws Exception
  {
    final String group = "valid";
    final String dir = "vt";
    final String name = "weiche.wdes";
    runModelChecker(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__1() throws Exception
  {
    checkTransferline(1);
  }

  public void testTransferline__2() throws Exception
  {
    checkTransferline(2);
  }

  public void testTransferline__3() throws Exception
  {
    checkTransferline(3);
  }

  public void testTransferline__4() throws Exception
  {
    checkTransferline(4);
  }

  public void testTransferline__5() throws Exception
  {
    checkTransferline(5);
  }

  public void checkTransferline(final int n) throws Exception
  {
    final String group = "handwritten";
    final String name = "transferline.wmod";
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", n);
    bindings.add(binding);
    runModelChecker(group, name, bindings, true);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelCheckerTest
  ModelChecker createModelChecker(final ProductDESProxy des,
                                  final ProductDESProxyFactory factory)
  {
    return new ControlLoopChecker(des, factory);
  }

  void checkCounterExample(final ProductDESProxy des,
                           final TraceProxy trace)
  {
    final LoopTraceProxy counterexample = (LoopTraceProxy) trace;
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final List<EventProxy> eventlist = counterexample.getEvents();
    final int len = eventlist.size();
    final int loopIndex = counterexample.getLoopIndex();
    
    // General: if counterexample is null
    assertNotNull(counterexample);
    
    // General: if counterexample is empty
    assertTrue("Empty Counterexample!", len > 0);
    
    // 1. All events in the loops are controllable
    for(int i = loopIndex; i < len; i++){
      assertTrue("Event " + eventlist.get(i).getName() +
                 "in loop is not controllable", 
                 eventlist.get(i).getKind() == EventKind.CONTROLLABLE);
    }
    
    // 2. Loop must not be empty
    assertTrue("Loop is empty", len - loopIndex > 0);
    
    // 3. Check trace is available in each automaton
    // 3.1. Check control loop is actually a loop
    for(final AutomatonProxy aProxy: automata){
      final boolean accepted = checkCounterExample(aProxy, eventlist, loopIndex);
      assertTrue("Counterexample not accepted by " + aProxy.getName(), accepted);
    }
  }
  
  private boolean checkCounterExample(final AutomatonProxy automaton,
                                  final List<EventProxy> counterexample,
                                  final int loopIndex)
  {
    final Collection<EventProxy> events = automaton.getEvents();
    final Collection<StateProxy> states = automaton.getStates();
    final Collection<TransitionProxy> transitions = automaton.getTransitions();
    
    // Get initial state to current state
    StateProxy currState = null;
    for(final StateProxy sProxy: states){
      if(sProxy.isInitial()){
        currState = sProxy;
        break;
      }
    }

    if(currState == null){
      return false;
    }

    int index = 1;
    StateProxy loopStart = null;
    for(final EventProxy eProxy: counterexample){
      if(index == loopIndex){
        loopStart = currState;
      }
      index++;

      if(events.contains(eProxy)){
        boolean found = false;
        for (final TransitionProxy trans : transitions) {
          if (trans.getSource() == currState && trans.getEvent() == eProxy) {
            currState = trans.getTarget();
            found = true;
            break;
          }
        }
        if(!found){
          return false;
        }
      }
    }

    // check there is a loop
    return (loopStart == currState);
  }

}
