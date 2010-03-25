//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.List;
import java.util.LinkedList;

import net.sourceforge.waters.model.module.ParameterBindingProxy;


public abstract class AbstractSynchronousProductBuilderTest
  extends AbstractAutomatonBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractSynchronousProductBuilderTest()
  {
  }

  public AbstractSynchronousProductBuilderTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  protected String getExpectedName(final String desname)
  {
    return desname + "-sync";
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testReentrant()
    throws Exception
  {
    testSmallFactory2();
    testTransferline__1();
    testTransferline__1();
    testSmallFactory2();
  }

  public void testOverflowException()
    throws Exception
  {
    try {
      final AutomatonBuilder builder = getAutomatonBuilder();
      builder.setNodeLimit(2);
      testTransferline__1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- handwritten
  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2";
    runAutomatonBuilder(group, name);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void testNondeterministicCombinations() throws Exception
  {
    final String group = "tests";
    final String dir = "nondeterministic";
    final String name = "NondeterministicCombinations";
    runAutomatonBuilder(group, dir, name);
  }

  /*
  public void testTictactoe() throws Exception
  {
    final String group = "handwritten";
    final String name = "tictactoe";
    runAutomatonBuilder(group, name);
  }


  //#########################################################################
  //# Test Cases --- nasty
  public void testJpt10Counter() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "jpt10counter";
    runAutomatonBuilder(group, dir, name);
  }

  public void testOrphanEvents() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "orphan_events";
    runAutomatonBuilder(group, dir, name);
  }

  public void testVerriegel4Counter2() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "verriegel4counter2";
    runAutomatonBuilder(group, dir, name);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void test_BallTimer() throws Exception
  {
    final String group = "tests";
    final String dir = "ball_sorter";
    final String name = "ball_timer";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_BallTimerUncont() throws Exception
  {
    final String group = "tests";
    final String dir = "ball_sorter";
    final String name = "ball_timer_uncont";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_BallTSorter1() throws Exception
  {
    final String group = "tests";
    final String dir = "ball_sorter";
    final String name = "robis_ball_sorter_attempt1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_amk14() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "amk14";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_cjn5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cjn5";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cs37";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ez1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb20";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb21";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gjr5";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "grj3";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "imr1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jbr2";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jmr30";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jpt10";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "kah18";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_2";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_2";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_rch11() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "rch11";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ry27";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_scs10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "scs10";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "sjw41";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "smr26";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_tk27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tk27";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "tp20";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "vl6";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_IMS_ims() throws Exception
  {
    final String group = "tests";
    final String dir = "ims";
    final String name = "ims";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Nasty_JustProperty() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "just_property";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Nasty_Mx27() throws Exception
  {
    final String group = "tests";
    final String dir  = "nasty";
    final String name = "mx27";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Nasty_OnlyInitBad() throws Exception
  {
    final String group = "tests";
    final String dir  = "nasty";
    final String name = "only_init_bad";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_Nasty_ReleaseAndBlow() throws Exception
  {
    final String group = "tests";
    final String dir  = "nasty";
    final String name = "release_and_blow";
    runAutomatonBuilder(group, dir, name);
  }

  public void testProfisafeI3HostEFA() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_ihost_efa";
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    bindings.add(binding);
    runAutomatonBuilder(group, dir, name, bindings);
  }

  public void testProfisafeI4SlaveEFA() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa";
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 4);
    bindings.add(binding);
    runAutomatonBuilder(group, dir, name, bindings);
  }

  public void testProfisafeI4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host";
    runAutomatonBuilder(group, dir, name);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_slave";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_plants() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "plants";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_ac61() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ac61";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_al29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "al29";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_asjc1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "asjc1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_dal9() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dal9";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_dmt10() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dmt10";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_ejtrw1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ejtrw1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_ekb2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ekb2";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_gat7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "gat7";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_jdm18() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jdm18";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_jlm39() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jlm39";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_jpg7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpg7";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_jpm22() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jpm22";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_jrv2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jrv2";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_js173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "js173";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_lz173() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "lz173";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_meb16() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "meb16";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_mjd29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "mjd29";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_ncj3() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ncj3";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_rjo6() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "rjo6";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_rms33() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "rms33";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_sdh7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sdh7";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_sgc9_1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_1";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_sgc9_2() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sgc9_2";
    runAutomatonBuilder(group, dir, name);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "yip1";
    runAutomatonBuilder(group, dir, name);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final String group = "valid";
    final String dir  = "big_factory";
    final String name = "bfactory";
    runAutomatonBuilder(group, dir, name);
  }

  public void testBmw_fh() throws Exception
  {
    final String group = "valid";
    final String dir  = "bmw_fh";
    final String name = "bmw_fh";
    runAutomatonBuilder(group, dir, name);
  }

  public void testBorder_cases() throws Exception
  {
    final String group = "valid";
    final String dir  = "border_cases";
    final String name = "never_blow_up";
    runAutomatonBuilder(group, dir, name);
  }

  public void testDebounce() throws Exception
  {
    final String group = "valid";
    final String dir = "debounce";
    final String name = "debounce";
    runAutomatonBuilder(group, dir, name);
  }

  public void testFalko() throws Exception
  {
    final String group = "valid";
    final String dir = "falko";
    final String name = "falko";
    runAutomatonBuilder(group, dir, name);
  }

  public void testFischertechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik";
    runAutomatonBuilder(group, dir, name);
  }

  public void testFtuer() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "ftuer";
    runAutomatonBuilder(group, dir, name);
  }

  public void testKoordwsp() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "koordwsp";
    runAutomatonBuilder(group, dir, name);
  }

  public void testMazes() throws Exception
  {
    final String group = "valid";
    final String dir = "mazes";
    final String name = "mazes";
    runAutomatonBuilder(group, dir, name);
  }

  public void testSafetydisplay() throws Exception
  {
    final String group = "valid";
    final String dir = "safetydisplay";
    final String name = "safetydisplay";
    runAutomatonBuilder(group, dir, name);
  }

  public void testSmallFactory() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small";
    runAutomatonBuilder(group, dir, name);
  }

  public void testSmallFactoryUncont() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small_uncont";
    runAutomatonBuilder(group, dir, name);
  }

  public void testSmd() throws Exception
  {
    final String group = "valid";
    final String dir = "smd";
    final String name = "smdreset";
    runAutomatonBuilder(group, dir, name);
  }

  public void testWeiche() throws Exception
  {
    final String group = "valid";
    final String dir = "vt";
    final String name = "weiche";
    runAutomatonBuilder(group, dir, name);
  }
  */

  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__1() throws Exception
  {
    checkTransferline(1);
  }

  public void checkTransferline(final int n) throws Exception
  {
    final String group = "handwritten";
    final String name = "transferline";
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", n);
    bindings.add(binding);
    runAutomatonBuilder(group, name, bindings);
  }

}
