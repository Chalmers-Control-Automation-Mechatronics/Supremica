//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractStandardConflictCheckerTest
  extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractStandardConflictCheckerTest()
  {
  }

  public AbstractStandardConflictCheckerTest(final String name)
  {
    super(name);
  }




  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty() throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final EventProxy marking =
        factory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                 EventKind.PROPOSITION);
    final Collection<EventProxy> events = Collections.singletonList(marking);
    final ProductDESProxy des =
        factory.createProductDESProxy("empty", events, null);
    runModelVerifier(des, true);
  }

  public void testReentrant1()
  throws Exception
  {
    testEmpty();
    testSmallFactory2();
    testWspTimer();
    testTransferline__1();
    testEmpty();
    testWspTimer();
    testTransferline__1();
    testSmallFactory2();
  }

  public void testReentrant2()
  throws Exception
  {
    testDiningPhilosophers__2();
    testDiningPhilosophers__3();
    testDiningPhilosophers__2();
    testDirtyPhilosophers__2();
    testDiningPhilosophers__3();
  }

  public void testOverflowException() throws Exception
  {
    try {
      final ModelVerifier verifier = getModelVerifier();
      verifier.setNodeLimit(4);
      testCell();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- nondeterministic
  public void testNondeterministicCombinations() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "NondeterministicCombinations.wmod");
    runModelVerifier(des, true);
  }

  public void testNondeterministicConflicting() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "NondeterministicConflicting.wmod");
    runModelVerifier(des, false);
  }

  public void testNondeterministicNonconflicting() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "NondeterministicNonconflicting.wmod");
    runModelVerifier(des, true);
  }

  public void testMultiNondeterministicConflicting() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "multiNondeterministicConflicting.wmod");
    runModelVerifier(des, false);
  }

  public void testMultiNondeterministicNonconflicting() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "multiNondeterministicNonconflicting.wmod");
    runModelVerifier(des, true);
  }

  public void testMultiNondeterministicSepPlacesConflicting() throws Exception
  {
    // The two deterministic automata don't have nondeterminism at the same
    // time.
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "multiNondeterministicSepPlacesConflicting.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- handwritten
  public void testCell() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "cell.wmod");
    runModelVerifier(des, true);
  }

  public void testCellBlock() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "cell_block.wmod");
    runModelVerifier(des, false);
  }

  public void testDosingTankWithJellyEFA1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "DosingTankWithJellyEFA1.wmod");
    runModelVerifier(des, false);
  }

  public void testElevatorSafety() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "elevator_safety.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2.wdes");
    runModelVerifier(des, true);
  }

  public void testTictactoe() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "tictactoe.wdes");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- hisc
  public void testHISCRhoneSubsystem1Patch0() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "rhone_subsystem1_patch0.wmod");
    runModelVerifier(des, false);
  }

  public void testHISCRhoneSubsystem1Patch1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "rhone_subsystem1_patch1.wmod");
    runModelVerifier(des, false);
  }

  public void testHISCRhoneSubsystem1Patch2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "rhone_subsystem1_patch2.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- nasty
  public void testAgvbPart1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "agvb_part1.wmod");
    runModelVerifier(des, false);
  }

  public void testAmpleStutterTest() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_stutter_test.wmod");
    runModelVerifier(des, false);
  }

  public void testCertainConf1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf1.wmod");
    runModelVerifier(des, false);
  }

  public void testCertainConf2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf2.wmod");
    runModelVerifier(des, false);
  }

  public void testCertainConf3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf3.wmod");
    runModelVerifier(des, false);
  }

  public void testCertainConf4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf4.wmod");
    runModelVerifier(des, true);
  }

  public void testCertainConf5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf5.wmod");
    runModelVerifier(des, false);
  }

  public void testCertainConf6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "certainconf6.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjoint1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint1.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjoint2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint2.wmod");
    runModelVerifier(des, false);
  }

  public void testDisjoint3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "disjoint3.wmod");
    runModelVerifier(des, false);
  }

  public void testEmptySpec() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "empty_spec.wmod");
    runModelVerifier(des, true);
  }

  public void testFailingEvent() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "failingEvent.wmod");
    runModelVerifier(des, false);
  }

  public void testFTechnikConflict() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ftechnik_conflict.wmod");
    runModelVerifier(des, false);
  }

  public void testJpt10Counter() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "jpt10counter.wmod");
    runModelVerifier(des, true);
  }

  public void testJustProperty() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "just_property.wdes");
    runModelVerifier(des, true);
  }

  public void testNeverMarked() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "never_marked.wmod");
    runModelVerifier(des, false);
  }

  public void testOneState() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "onestate.wmod");
    runModelVerifier(des, true);
  }

  public void test_onlySelfLoop01() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "onlySelfLoop01.wmod");
    runModelVerifier(des, true);
  }

  public void test_onlySelfLoop02() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "onlySelfLoop02.wmod");
    runModelVerifier(des, true);
  }

  public void testOrphanEvents() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "orphan_events.wmod");
    runModelVerifier(des, true);
  }

  public void testPrimeSieve2b() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "prime_sieve2b.wmod");
    runModelVerifier(des, false);
  }

  public void testRhoneTU34() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "rhone_tu34.wmod");
    runModelVerifier(des, true);
  }

  public void testSilentContinuation() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "silent_continuation.wmod");
    runModelVerifier(des, true);
  }

  public void testTwoInit() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "twoinit.wmod");
    runModelVerifier(des, true);
  }

  public void testTwoInitBlock() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "twoinit_block.wmod");
    runModelVerifier(des, false);
  }

  public void testVerriegel4Counter2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "verriegel4counter2.wmod");
    runModelVerifier(des, false);
  }

  public void testWickedCounting() throws Exception
  {
    checkWickedCounting(15);
  }

  protected void checkWickedCounting(final int digits) throws Exception
  {
    final ParameterBindingProxy binding = createBinding("DIGITS", digits);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "tests", "nasty", "wicked_counting.wmod");
    runModelVerifier(des, bindings, true);
  }


  //#########################################################################
  //# Test Cases --- efa
  public void test_CaseStudyNonblocking() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("efa", "caseStudy-nonblocking.wmod");
    runModelVerifier(des, true);
  }

  public void test_CaseStudyOriginal() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("efa", "caseStudy-original.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void test_BallTimer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ball_sorter", "ball_timer.wmod");
    runModelVerifier(des, true);
  }

  public void test_BallTimerUncont() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ball_sorter", "ball_timer_uncont.wmod");
    runModelVerifier(des, true);
  }

  public void test_BallTSorter1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ball_sorter", "robis_ball_sorter_attempt1.wmod");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_amk14() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "amk14.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_cjn5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "cjn5.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "cs37.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "ez1.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gb20.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gb21.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gjr5.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "grj3.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "imr1.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jbr2.wdes");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jmr30.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jpt10.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "kah18.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lsr1_1.wdes");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lsr1_2.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lz136_1.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lz136_2.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_rch11() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "rch11.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "ry27.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_scs10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "scs10.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "sjw41.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "smr26.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_tk27() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "tk27.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "tp20.wdes");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "vl6.wdes");
    runModelVerifier(des, true);
  }

  public void test_IMS_ims() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ims", "ims.wmod");
    runModelVerifier(des, true);
  }

  public void testProfisafeI3HostEFABlock() throws Exception
  {
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    final List<ParameterBindingProxy> bindings =
        Collections.singletonList(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "tests", "profisafe",
                     "profisafe_ihost_efa_block.wmod");
    runModelVerifier(des, bindings, false);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "profisafe", "profisafe_i4_slave.wmod");
    runModelVerifier(des, true);
  }

  public void testProfisafeI4SlaveEFA() throws Exception
  {
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 4);
    final List<ParameterBindingProxy> bindings =
        Collections.singletonList(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "tests", "profisafe",
                     "profisafe_islave_efa.wmod");
    runModelVerifier(des, bindings, true);
  }

  public void test_TrafficLights2006_plants() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "plants.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ac61() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ac61.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_al29() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "al29.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_asjc1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "asjc1.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_dal9() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "dal9.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_dmt10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "dmt10.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ejtrw1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ejtrw1.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ekb2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ekb2.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_gat7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "gat7.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_jdm18() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jdm18.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_jlm39() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jlm39.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_jpg7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jpg7.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_jpm22() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jpm22.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_jrv2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jrv2.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_js173() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "js173.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_lz173() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "lz173.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_meb16() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "meb16.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_mjd29() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "mjd29.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ncj3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ncj3.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_rjo6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "rjo6.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_rms33() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "rms33.wdes");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_sdh7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sdh7.wdes");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_sgc9_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sgc9_1.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_sgc9_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sgc9_2.wdes");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1.wdes");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "big_factory", "bfactory.wdes");
    runModelVerifier(des, true);
  }

  public void testBmw_fh() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "bmw_fh", "bmw_fh.wdes");
    runModelVerifier(des, true);
  }

  public void testDebounce() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "debounce", "debounce.wdes");
    runModelVerifier(des, true);
  }

  public void testFalko() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "falko", "falko.wdes");
    runModelVerifier(des, true);
  }

  public void testFtuer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "ftuer.wdes");
    runModelVerifier(des, true);
  }

  public void testKoordwsp() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "koordwsp.wdes");
    runModelVerifier(des, true);
  }

  public void testKoordwspBlock() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "koordwsp_block.wdes");
    runModelVerifier(des, false);
  }

  public void testWspTimer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "wsp_timer.wmod");
    runModelVerifier(des, false);
  }

  public void testWspTimerNoreset() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "wsp_timer_noreset.wmod");
    runModelVerifier(des, true);
  }

  public void testSafetydisplay() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "safetydisplay", "safetydisplay.wdes");
    runModelVerifier(des, true);
  }

  public void testSmallFactoryUncont() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "small", "small_uncont.wdes");
    runModelVerifier(des, true);
  }

  public void testSmd() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "smd", "smdreset.wdes");
    runModelVerifier(des, true);
  }

  public void testWeiche() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "vt", "weiche.wdes");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testControlledPhilosophers__2() throws Exception
  {
    checkPhilosophers("controlled_philosophers.wmod", 2, true);
  }

  public void testControlledPhilosophers__3() throws Exception
  {
    checkPhilosophers("controlled_philosophers.wmod", 3, true);
  }

  public void testDiningPhilosophers__2() throws Exception
  {
    checkPhilosophers("dining_philosophers.wmod", 2, false);
 }

  public void testDiningPhilosophers__3() throws Exception
  {
    checkPhilosophers("dining_philosophers.wmod", 3, false);
  }

  public void testDirtyPhilosophers__2() throws Exception
  {
    checkPhilosophers("dirty_philosophers.wmod", 2, false);
  }

  public void testDirtyPhilosophers__3() throws Exception
  {
    checkPhilosophers("dirty_philosophers.wmod", 2, false);
  }

  public void testOrderedPhilosophers__2() throws Exception
  {
    checkPhilosophers("ordered_philosophers.wmod", 2, true);
  }

  public void testOrderedPhilosophers__4() throws Exception
  {
    checkPhilosophers("ordered_philosophers.wmod", 4, true);
  }

  public void testRoundRobin__2() throws Exception
  {
    checkRoundRobin(2);
  }

  public void testRoundRobin__5() throws Exception
  {
    checkRoundRobin(5);
  }

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

  public void testDynamicSieve__2() throws Exception
  {
    checkDynamicSieve("dynamic_prime_sieve.wmod", 2, 24, true);
  }


  protected void checkDynamicSieve(final String name,
                                   final int s,
                                   final int n,
                                   final boolean result)
  throws Exception
  {
    final ParameterBindingProxy bindingS = createBinding("S", s);
    final ParameterBindingProxy bindingN = createBinding("N", n);
    final List<ParameterBindingProxy> bindings = new ArrayList<>(2);
    bindings.add(bindingS);
    bindings.add(bindingN);
    final ProductDESProxy des = getCompiledDES(bindings, "efa", name);
    runModelVerifier(des, bindings, result);
  }

  protected void checkPhilosophers(final String name,
                                   final int n,
                                   final boolean result)
  throws Exception
  {
    final ParameterBindingProxy binding = createBinding("N", n);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final ProductDESProxy des = getCompiledDES(bindings, "handwritten", name);
    runModelVerifier(des, bindings, result);
  }

  protected void checkRoundRobin(final int n)
  throws Exception
  {
    final ParameterBindingProxy binding = createBinding("N", n);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "efa", "round_robin_efa.wmod");
    runModelVerifier(des, bindings, false);
  }

  protected void checkTransferline(final int n)
  throws Exception
  {
    final ParameterBindingProxy binding = createBinding("N", n);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "handwritten", "transferline.wmod");
    runModelVerifier(des, bindings, true);
  }

}
