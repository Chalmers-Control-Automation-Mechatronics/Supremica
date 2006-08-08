//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControlLoopCheckerTest
//###########################################################################
//# $Id: ControlLoopCheckerTest.java,v 1.1 2006-08-08 22:32:37 yip1 Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.io.File;
import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ControlLoopCheckerTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public void setUp() throws Exception
  {    
    mFactory = ProductDESElementFactory.getInstance();
    mTraceMarshaller = new JAXBTraceMarshaller(mFactory);
    mDESMarshaller = new JAXBProductDESMarshaller(mFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mDESMarshaller);
  } 
  
  public static Test suite() { 
    TestSuite testSuite = new TestSuite(ControlLoopCheckerTest.class);     
    return testSuite;
  }


  //#########################################################################
  //# Test Cases --- handwritten
  /*
  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2.wdes";
    runControlLoopChecker(group, name, true|false);
  }
  */

  /*
  public void testTictactoe() throws Exception
  {
    final String group = "handwritten";
    final String name = "tictactoe.wdes";
    runControlLoopChecker(group, name, true|false);
  }
  */

  //#########################################################################
  //# Test Cases --- tests
  public void test_Batchtank2005_amk14() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "amk14.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_cjn5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cjn5.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cs37.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ez1.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb20.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb21.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gjr5.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "grj3.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "imr1.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jbr2.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jmr30.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jpt10.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "kah18.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_1.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_2.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_1.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_2.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_rch11() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "rch11.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ry27.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_Batchtank2005_scs10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "scs10.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "sjw41.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "smr26.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_tk27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tk27.wdes";
    runControlLoopChecker(group, dir, name, true); 
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tp20.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "vl6.wdes";
    runControlLoopChecker(group, dir, name, false);
  }

  /*
  public void testMx27() throws Exception
  {
    final String group = "tests";
    final String dir  = "nasty";
    final String name = "mx27.wdes";
    runControlLoopChecker(group, dir, name, true|false);
  }
  */
  /*
  public void test_TrafficLights2006_ac61() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ac61.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_al29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "al29.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_asjc1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "asjc1.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_dal9() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dal9.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_dmt10() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dmt10.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_ejtrw1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ejtrw1.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_ekb2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ekb2.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_gat7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "gat7.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_jdm18() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jdm18.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_jlm39() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jlm39.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_jpg7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpg7.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_jpm22() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpm22.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_jrv2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jrv2.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_js173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "js173.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_lz173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "lz173.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_meb16() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "meb16.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_mjd29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "mjd29.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_ncj3() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ncj3.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_rjo6() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "rjo6.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_rms33() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "rms33.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_sdh7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sdh7.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_sgc9_1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_1.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_sgc9_2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_2.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "yip1.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  */
  //#########################################################################
  //# Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final String group = "valid";
    final String dir  = "big_factory";
    final String name = "bfactory.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  public void testBmw_fh() throws Exception
  {
    final String group = "valid";
    final String dir  = "bmw_fh";
    final String name = "bmw_fh.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void testBorder_cases() throws Exception
  {
    final String group = "valid";
    final String dir  = "border_cases";
    final String name = "never_blow_up.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void testDebounce() throws Exception
  {
    final String group = "valid";
    final String dir = "debounce";
    final String name = "debounce.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  public void testFalko() throws Exception
  {
    final String group = "valid";
    final String dir = "falko";
    final String name = "falko.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  public void testFtuer() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "ftuer.wdes";
    runControlLoopChecker(group, dir, name, false);
  } 

  /*
  public void testFischertechnik() throws Exception
  {
    final String group = "valid";
    final String dir = "fischertechnik";
    final String name = "fischertechnik.wdes";
    runControlLoopChecker(group, dir, name, false);
  }
  */
  /*
  public void testKoordwsp() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "koordwsp.wdes";
    runControlLoopChecker(group, dir, name, true);
  } 
  */
  
  public void testSafetydisplay() throws Exception
  {
    final String group = "valid";
    final String dir = "safetydisplay";
    final String name = "safetydisplay.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  public void testSmallFactory() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  public void testSmallFactoryUncont() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small_uncont.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  /*
  public void testSmd() throws Exception
  {
    final String group = "valid";
    final String dir = "smd";
    final String name = "smdreset.wdes";
    runControlLoopChecker(group, dir, name, true|false);
  }
  */

  public void testTline_0() throws Exception
  {
    final String group = "valid";
    final String dir = "tline_0";
    final String name = "transferline_templ.wdes";
    runControlLoopChecker(group, dir, name, true);
  }
  
  public void testTline_1() throws Exception
  {
    final String group = "valid";
    final String dir = "tline_1";
    final String name = "tline_1.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  public void testWeiche() throws Exception
  {
    final String group = "valid";
    final String dir = "vt";
    final String name = "weiche.wdes";
    runControlLoopChecker(group, dir, name, true);
  }

  //#########################################################################
  //# Auxiliary Methods
  private void runControlLoopChecker(final String group,
                                     final String name,
                                     final boolean expect)
    throws Exception
  {
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runControlLoopChecker(groupdir, name, expect);
  }

  private void runControlLoopChecker(final String group,
                                     final String subdir,
                                     final String name,
                                     final boolean expect)
    throws Exception
  {
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runControlLoopChecker(groupdir, subdir, name, expect);
  }

  private void runControlLoopChecker(final File groupdir,
                                     final String subdir,
                                     final String name,
                                     final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runControlLoopChecker(dir, name, expect);
  }

  private void runControlLoopChecker(final File dir,
                                     final String name,
                                     final boolean expect)
    throws Exception
  {
    final File filename = new File(dir, name);
    runControlLoopChecker(filename, expect);
  }

    
  private void runControlLoopChecker(final File filename,
                                     final boolean expect)
    throws Exception
  {
    final ProductDESProxy des =
      (ProductDESProxy) mDocumentManager.load(filename);
    final ControlLoopChecker checker =
      new ControlLoopChecker(des, mFactory);
    final boolean result = checker.run();
    LoopTraceProxy counterexample = null;
    if (!result) {
      counterexample = checker.getCounterExample();
      saveCounterExample(counterexample);
    }
    assertEquals("Wrong result from control loop checker: got " +
                 result + " but should have been " + expect + "!",
                 result, expect);
    if (!expect) {
      checkCounterExample(des, counterexample);
    }
  }

  private void saveCounterExample(final LoopTraceProxy counterexample)
    throws Exception
  {
    final String name = counterexample.getName();
    final String ext = mTraceMarshaller.getDefaultExtension();
    final File dir = getOutputDirectory();
    final File filename = new File(dir, name + ext);
    ensureParentDirectoryExists(filename);
    mTraceMarshaller.marshal(counterexample, filename);
  }

  private void checkCounterExample(final ProductDESProxy des,
				   final LoopTraceProxy counterexample)
  {
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
      assertTrue("Event " + eventlist.get(i).getName() + "in loop is not controllable", 
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
        for(final TransitionProxy trans: transitions){
          if(trans.getSource().equals(currState) && trans.getEvent().equals(eProxy)){
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


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mFactory;
  private JAXBProductDESMarshaller mDESMarshaller;
  private JAXBTraceMarshaller mTraceMarshaller;
  private DocumentManager mDocumentManager;	

}
