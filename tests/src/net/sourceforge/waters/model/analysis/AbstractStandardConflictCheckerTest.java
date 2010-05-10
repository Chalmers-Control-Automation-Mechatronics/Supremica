//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractGeneralisedConflictCheckerTest
//###########################################################################
//# $Id: AbstractGeneralisedConflictCheckerTest.java 4768 2009-10-09 03:16:33Z robi $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractStandardConflictCheckerTest extends
    AbstractConflictCheckerTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public AbstractStandardConflictCheckerTest()
  {
  }

  public AbstractStandardConflictCheckerTest(final String name)
  {
    super(name);
  }

  // #########################################################################
  // # Test Cases --- handcrafted
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

  public void testReentrant() throws Exception
  {
    testEmpty();
    testSmallFactory2();
    testTransferline__1();
    testEmpty();
    testTransferline__1();
    testSmallFactory2();
  }

  public void testOverflowException() throws Exception
  {
    try {
      final ModelVerifier verifier = getModelVerifier();
      verifier.setNodeLimit(2);
      testBigFactory();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }

  // #########################################################################
  // # Test Cases --- nondeterministic

  public void testNondeterministicCombiniations() throws Exception
  {
    final String group = "tests/nondeterministic";
    final String name = "NondeterministicCombinations.wmod";
    runModelVerifier(group, name, true);
  }

  public void testNondeterministicConflicting() throws Exception
  {
    final String group = "tests/nondeterministic";
    final String name = "NondeterministicConflicting.wmod";
    runModelVerifier(group, name, false);
  }

  public void testNondeterministicNonconflicting() throws Exception
  {
    final String group = "tests/nondeterministic";
    final String name = "NondeterministicNonconflicting.wmod";
    runModelVerifier(group, name, true);
  }

  public void testMultiNondeterministicConflicting() throws Exception
  {
    final String group = "tests/nondeterministic";
    final String name = "multiNondeterministicConflicting.wmod";
    runModelVerifier(group, name, false);
  }

  public void testMultiNondeterministicNonconflicting() throws Exception
  {
    final String group = "tests/nondeterministic";
    final String name = "multiNondeterministicNonconflicting.wmod";
    runModelVerifier(group, name, true);
  }

  public void testMultiNondeterministicSepPlacesConflicting() throws Exception
  {
    // the two deterministic automata don't have non determinism at the same
    // time
    final String group = "tests/nondeterministic";
    final String name = "multiNondeterministicSepPlacesConflicting.wmod";
    runModelVerifier(group, name, false);
  }

  // #########################################################################
  // # Test Cases --- handwritten
  public void testDosingTankWithJellyEFA1() throws Exception
  {
    final String group = "handwritten";
    final String name = "DosingTankWithJellyEFA1.wmod";
    runModelVerifier(group, name, false);
  }

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
    runModelVerifier(group, name, true);
  }

  // #########################################################################
  // # Test Cases --- hisc
  public void testHISCRhoneSubsystem1Patch0() throws Exception
  {
    final String group = "tests";
    final String dir = "hisc";
    final String name = "rhone_subsystem1_patch0.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testHISCRhoneSubsystem1Patch1() throws Exception
  {
    final String group = "tests";
    final String dir = "hisc";
    final String name = "rhone_subsystem1_patch1.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testHISCRhoneSubsystem1Patch2() throws Exception
  {
    final String group = "tests";
    final String dir = "hisc";
    final String name = "rhone_subsystem1_patch2.wmod";
    runModelVerifier(group, dir, name, true);
  }

  // #########################################################################
  // # Test Cases --- nasty
  public void testJpt10Counter() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "jpt10counter.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testNeverMarked() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "never_marked.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testOrphanEvents() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "orphan_events.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testVerriegel4Counter2() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "verriegel4counter2.wmod";
    runModelVerifier(group, dir, name, false);
  }

  // #########################################################################
  // # Test Cases --- tests
  public void test_BallTimer() throws Exception
  {
    final String group = "tests";
    final String dir = "ball_sorter";
    final String name = "ball_timer.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void test_BallTimerUncont() throws Exception
  {
    final String group = "tests";
    final String dir = "ball_sorter";
    final String name = "ball_timer_uncont.wmod";
    runModelVerifier(group, dir, name, true);
  }

  /*
   * public void test_BallTSorter1() throws Exception { final String group =
   * "tests"; final String dir = "ball_sorter"; final String name =
   * "robis_ball_sorter_attempt1.wmod"; runModelVerifier(group, dir, name,
   * false); }
   */

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
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, false);
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
    runModelVerifier(group, dir, name, false);
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
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tp20.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "vl6.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_IMS_ims() throws Exception
  {
    final String group = "tests";
    final String dir = "ims";
    final String name = "ims.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void test_Nasty_JustProperty() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "just_property.wdes";
    runModelVerifier(group, dir, name, true);
  }

  /*
   * This test seems to be too much for compositional methods in Eclipse. Move
   * to 'large' test suite? public void testProfisafeI3HostEFA() throws
   * Exception { final String group = "tests"; final String dir = "profisafe";
   * final String name = "profisafe_ihost_efa.wmod"; final ParameterBindingProxy
   * binding = createBinding("MAXSEQNO", 3); final List<ParameterBindingProxy>
   * bindings = Collections.singletonList(binding); runModelVerifier(group, dir,
   * name, bindings, true); }
   */

  public void testProfisafeI3HostEFABlock() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_ihost_efa_block.wmod";
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    final List<ParameterBindingProxy> bindings =
        Collections.singletonList(binding);
    runModelVerifier(group, dir, name, bindings, false);
  }

  /*
   * public void testProfisafeI4Host() throws Exception { final String group =
   * "tests"; final String dir = "profisafe"; final String name =
   * "profisafe_i4_host.wmod"; runModelVerifier(group, dir, name, true); }
   */

  public void testProfisafeI4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_slave.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeI4SlaveEFA() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 4);
    final List<ParameterBindingProxy> bindings =
        Collections.singletonList(binding);
    runModelVerifier(group, dir, name, bindings, true);
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
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_al29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "al29.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_asjc1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "asjc1.wdes";
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_ekb2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ekb2.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_gat7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "gat7.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_jdm18() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jdm18.wdes";
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_jpm22() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpm22.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_jrv2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jrv2.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_js173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "js173.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_lz173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "lz173.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_meb16() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "meb16.wdes";
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, true);
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
    runModelVerifier(group, dir, name, false);
  }

  public void test_TrafficLights2006_sgc9_1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_1.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_sgc9_2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_2.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "yip1.wdes";
    runModelVerifier(group, dir, name, true);
  }

  // #########################################################################
  // # Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final String group = "valid";
    final String dir = "big_factory";
    final String name = "bfactory.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testBmw_fh() throws Exception
  {
    final String group = "valid";
    final String dir = "bmw_fh";
    final String name = "bmw_fh.wdes";
    runModelVerifier(group, dir, name, true);
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

  /*
   * public void testFischertechnik() throws Exception { final String group =
   * "tests"; final String dir = "incremental_suite"; final String name =
   * "ftechnik.wmod"; runModelVerifier(group, dir, name, false); }
   */

  public void testFtuer() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "ftuer.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testKoordwsp() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "koordwsp.wdes";
    runModelVerifier(group, dir, name, true);
  }

  public void testKoordwspBlock() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "koordwsp_block.wdes";
    runModelVerifier(group, dir, name, false);
  }

  public void testWspTimer() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "wsp_timer.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testWspTimerNoreset() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "wsp_timer_noreset.wmod";
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
    runModelVerifier(group, dir, name, true);
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

  // #########################################################################
  // # Test Cases -- Parameterised
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

  /*
   * public void testTransferline__5() throws Exception { checkTransferline(5);
   * }
   */

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

}
