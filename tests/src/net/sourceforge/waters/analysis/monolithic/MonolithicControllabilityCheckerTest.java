//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicControllabilityCheckerTest
//###########################################################################
//# $Id: MonolithicControllabilityCheckerTest.java,v 1.1 2006-11-03 05:18:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class MonolithicControllabilityCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(MonolithicControllabilityCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
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


  //#########################################################################
  //# Test Cases --- handwritten
  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2.wdes";
    runModelVerifier(group, name, true);
  }

  public void testTictactoe() throws Exception
  {
    final String group = "handwritten";
    final String name = "tictactoe.wdes";
    runModelVerifier(group, name, false);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void test_Batchtank2005_amk14() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "amk14.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_cjn5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cjn5.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cs37.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ez1.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb20.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb21.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gjr5.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "grj3.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "imr1.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jbr2.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jmr30.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jpt10.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "kah18.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_1.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_2.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_1.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_2.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_rch11() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "rch11.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ry27.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_scs10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "scs10.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "sjw41.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "smr26.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_tk27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tk27.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tp20.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "vl6.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testMx27() throws Exception
  {
    final String group = "tests";
    final String dir  = "nasty";
    final String name = "mx27.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void testProfisafeI4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_slave.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_slave.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_plants() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "plants.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_ac61() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ac61.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_al29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "al29.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_asjc1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "asjc1.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_dal9() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dal9.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_dmt10() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dmt10.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_ejtrw1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ejtrw1.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_ekb2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ekb2.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_gat7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "gat7.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_jdm18() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jdm18.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_jlm39() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jlm39.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_jpg7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpg7.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_jpm22() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpm22.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_jrv2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jrv2.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_js173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "js173.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_lz173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "lz173.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_meb16() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "meb16.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_mjd29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "mjd29.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_ncj3() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ncj3.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_rjo6() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "rjo6.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_rms33() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "rms33.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_sdh7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sdh7.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_sgc9_1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_1.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_sgc9_2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_2.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "yip1.wdes";
    runModelVerifier(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final String group = "valid";
    final String dir  = "big_factory";
    final String name = "bfactory.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void testBmw_fh() throws Exception
  {
    final String group = "valid";
    final String dir  = "bmw_fh";
    final String name = "bmw_fh.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testBorder_cases() throws Exception
  {
    final String group = "valid";
    final String dir  = "border_cases";
    final String name = "never_blow_up.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void testDebounce() throws Exception
  {
    final String group = "valid";
    final String dir = "debounce";
    final String name = "debounce.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testFalko() throws Exception
  {
    final String group = "valid";
    final String dir = "falko";
    final String name = "falko.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testFtuer() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "ftuer.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testFischertechnik() throws Exception
  {
    final String group = "valid";
    final String dir = "fischertechnik";
    final String name = "fischertechnik.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void testKoordwsp() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "koordwsp.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testMazes() throws Exception
  {
    final String group = "valid";
    final String dir = "mazes";
    final String name = "mazes.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testSafetydisplay() throws Exception
  {
    final String group = "valid";
    final String dir = "safetydisplay";
    final String name = "safetydisplay.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testSmallFactory() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testSmallFactoryUncont() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small_uncont.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void testSmd() throws Exception
  {
    final String group = "valid";
    final String dir = "smd";
    final String name = "smdreset.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testWeiche() throws Exception
  {
    final String group = "valid";
    final String dir = "vt";
    final String name = "weiche.wdes";
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, name, bindings, true);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ModelVerifier
    createModelVerifier(final ProductDESProxy des,
                        final ProductDESProxyFactory factory)
  {
    return new MonolithicControllabilityChecker(des, factory);
  }

  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace)
  {
    assertNotNull(trace);
    final SafetyTraceProxy counterexample = (SafetyTraceProxy) trace;
  	
    final List<EventProxy> eventlist = counterexample.getEvents();
    final int len = eventlist.size();
    assertTrue("Empty Counterexample!", len > 0);
  	  	
    final EventProxy last = eventlist.get(len-1);
    final EventKind ekind = last.getKind();
    assertEquals(ekind, EventKind.UNCONTROLLABLE);
  	
    final Collection<AutomatonProxy> automata = des.getAutomata();
    boolean rejected = false;
    for (final AutomatonProxy aut : automata){
      final ComponentKind akind = aut.getKind();
      final int accepted = checkCounterExample(aut, eventlist);
      if (akind.equals(ComponentKind.PLANT)){
	assertTrue("Counterexample not accepted by plant " +
		   aut.getName() + "!", accepted == len);
      } else if (akind.equals(ComponentKind.SPEC)) {
	assertFalse("Counterexample rejected too early (step " + accepted +
		    ") by spec " + aut.getName() + "!",
		    accepted < len - 1);
	rejected |= (accepted == len - 1);
      }
    }
    assertTrue("Counterexample not rejected by any spec!", rejected);
  }

  private int checkCounterExample(final AutomatonProxy aut,
                                  final List<EventProxy> counterexample)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();

    int steps = -1;
    StateProxy current = null;
    for (final StateProxy state : states){
      if (state.isInitial()){
        current = state;
        break;
      }
    }
    if (current == null){
      return steps;
    }
    for (final EventProxy event : counterexample){
      steps++;
      if (events.contains(event)){
        boolean found = false;
        for(final TransitionProxy trans : transitions){
          if (trans.getSource()== current && trans.getEvent() == event){
            current = trans.getTarget();
            found = true;
            break;
          }
        }
        if(!found){        	
          return steps;
        }
      }
    }
    return steps + 1;
  }

}
