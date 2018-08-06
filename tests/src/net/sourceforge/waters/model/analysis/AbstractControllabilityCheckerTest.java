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

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractControllabilityCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractControllabilityCheckerTest()
  {
  }

  public AbstractControllabilityCheckerTest(final String name)
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
    testTransferline__1();
    testEmpty();
    testTransferline__1();
    testSmallFactory2();
  }

  public void testOverflowException()
    throws Exception
  {
    try {
      final ModelVerifier verifier = getModelVerifier();
      verifier.setNodeLimit(2);
      testTransferline__1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- handwritten
  public void testDosingTankWithJellyEFA1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "DosingTankWithJellyEFA1.wmod");
    runModelVerifier(des, false);
  }

  public void testMachineBuffer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "machine_buffer.wmod");
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
  //# Test Cases --- hisc
  public void testHISCAIP0Sub1Patch0() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip0sub1p0.wmod");
    runModelVerifier(des, true);
  }

  public void testHISCAIP0Sub1Patch1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip0sub1p1.wmod");
    runModelVerifier(des, true);
  }

  public void testHISCAIP0Sub1Patch2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip0sub1p2.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- tests/ball_sorter
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
    runModelVerifier(des, false);
  }

  public void test_BallTSorter1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ball_sorter", "robis_ball_sorter_attempt1.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- tests/batchtank2005
  public void test_Batchtank2005_amk14() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "amk14.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_cjn5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "cjn5.wmod");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "cs37.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "ez1.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gb20.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gb21.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gjr5.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "grj3.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "imr1.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jbr2.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jmr30.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jpt10.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "kah18.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lsr1_1.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lsr1_2.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lz136_1.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lz136_2.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_rch11() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "rch11.wmod");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "ry27.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_scs10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "scs10.wmod");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "sjw41.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "smr26.wmod");
    runModelVerifier(des, true);
  }

  public void test_Batchtank2005_tk27() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "tk27.wmod");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_tp20() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "tp20.wmod");
    runModelVerifier(des, false);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "vl6.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- tests/ims
  public void test_IMS_ims() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ims", "ims.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- tests/nasty
  public void test_AmpleCandidateTrue() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_candidate_true.wmod");
    runModelVerifier(des, true);
  }

  public void test_AmpleHypercube222() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_222.wmod");
    runModelVerifier(des, false);
  }

  public void test_AmpleHypercube234() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_234.wmod");
    runModelVerifier(des, false);
  }

  public void test_AmpleHypercube333() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_333.wmod");
    runModelVerifier(des, false);
  }

  public void test_AmpleHypercube334() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_334.wmod");
    runModelVerifier(des, false);
  }

  public void test_AmpleHypercube344() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_344.wmod");
    runModelVerifier(des, false);
  }

  public void test_AmpleHypercube444() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_444.wmod");
    runModelVerifier(des, false);
  }

  public void test_AmpleHypercubeCont() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_hypercube_cont.wmod");
    runModelVerifier(des, true);
  }

  public void test_AmpleStutterTest() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "ample_stutter_test.wmod");
    runModelVerifier(des, false);
  }

  public void test_Nasty_EmptySpec() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "empty_spec.wmod");
    runModelVerifier(des, true);
  }

  public void testJpt10Counter() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "jpt10counter.wmod");
    runModelVerifier(des, false);
  }

  public void test_Nasty_JustProperty() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "just_property.wmod");
    runModelVerifier(des, true);
  }

  public void test_Nasty_Mx27() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "mx27.wdes");
    runModelVerifier(des, false);
  }

  public void test_Nasty_OnlyInitBad() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "only_init_bad.wmod");
    runModelVerifier(des, false);
  }

  public void testOrphanEvents() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "orphan_events.wmod");
    runModelVerifier(des, true);
  }

  public void test_Nasty_ReleaseAndBlow() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "release_and_blow.wmod");
    runModelVerifier(des, true);
  }

  public void test_Nasty_RhoneSubsystem1Robot() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "rhone_subsystem1_robot.wmod");
    runModelVerifier(des, false);
  }

  public void testSjw41Counter() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "sjw41counter.wmod");
    runModelVerifier(des, false);
  }

  public void test_Nasty_TwoInit2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "twoinit2.wmod");
    runModelVerifier(des, false);
  }

  public void testVerriegel4Counter2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "verriegel4counter2.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- tests/nondeterministic
  public void test_Nondet_MultiSepPlacesConflicting() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic",
                     "multiNondeterministicSepPlacesConflicting.wmod");
    runModelVerifier(des, false);
  }

  public void testNondeterministicExtension() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nondeterministic", "nondeterministicExtension.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Test Cases --- tests/profisafe
  public void testProfisafeI3HostEFA() throws Exception
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 4);
    bindings.add(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "tests", "profisafe",
                     "profisafe_ihost_efa_1.wmod");
    runModelVerifier(des, true);
  }

  public void testProfisafeI4SlaveEFA() throws Exception
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 4);
    bindings.add(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "tests", "profisafe",
                     "profisafe_islave_efa.wmod");
    runModelVerifier(des, true);
  }

  public void testProfisafeI4Host() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "profisafe", "profisafe_i4_host.wmod");
    runModelVerifier(des, true);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "profisafe", "profisafe_i4_slave.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- tests/trafficlights2006
  public void test_TrafficLights2006_plants() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "plants.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ac61() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ac61.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_al29() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "al29.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_asjc1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "asjc1.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_dal9() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "dal9.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_dmt10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "dmt10.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ejtrw1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ejtrw1.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_ekb2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ekb2.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_gat7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "gat7.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_jdm18() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jdm18.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_jlm39() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jlm39.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_jpg7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jpg7.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_jpm22() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jpm22.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_jrv2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jrv2.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_js173() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "js173.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_lz173() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "lz173.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_meb16() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "meb16.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_mjd29() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "mjd29.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_ncj3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ncj3.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_rjo6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "rjo6.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_rms33() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "rms33.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_sdh7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sdh7.wmod");
    runModelVerifier(des, true);
  }

  public void test_TrafficLights2006_sgc9_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sgc9_1.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_sgc9_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sgc9_2.wmod");
    runModelVerifier(des, false);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBigFactory() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "big_factory", "bfactory.wmod");
    runModelVerifier(des, false);
  }

  public void testBmw_fh() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "bmw_fh", "bmw_fh.wmod");
    runModelVerifier(des, true);
  }

  public void testBorder_cases() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "border_cases", "never_blow_up.wmod");
    runModelVerifier(des, false);
  }

  public void testDebounce() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "debounce", "debounce.wmod");
    runModelVerifier(des, true);
  }

  public void testFalko() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "falko", "falko.wmod");
    runModelVerifier(des, true);
  }

  public void testFischertechnik() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "incremental_suite", "ftechnik.wmod");
    runModelVerifier(des, false);
  }

  public void testFtuer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "ftuer.wmod");
    runModelVerifier(des, true);
  }

  public void testKoordwsp() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "koordwsp.wmod");
    runModelVerifier(des, true);
  }

  public void testMazes() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "mazes", "mazes.wmod");
    runModelVerifier(des, true);
  }

  public void testSafetydisplay() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "safetydisplay", "safetydisplay.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "small", "small.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactoryUncont() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "small", "small_uncont.wmod");
    runModelVerifier(des, false);
  }

  public void testSmd() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "smd", "smdreset.wmod");
    runModelVerifier(des, true);
  }

  public void testWeiche() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "vt", "weiche.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__1() throws Exception
  {
    checkTransferline("transferline.wmod", 1, true);
  }

  public void testTransferline__2() throws Exception
  {
    checkTransferline("transferline.wmod", 2, true);
  }

  public void testTransferline__3() throws Exception
  {
    checkTransferline("transferline.wmod", 3, true);
  }

  public void testTransferline__4() throws Exception
  {
    checkTransferline("transferline.wmod", 4, true);
  }

  public void testTransferline__5() throws Exception
  {
    checkTransferline("transferline.wmod", 5, true);
  }

  public void testTransferlineUncont2__5() throws Exception
  {
    checkTransferline("transferline_uncont2.wmod", 5, false);
  }

  public void checkTransferline(final String name,
                                final int n,
                                final boolean result)
    throws Exception
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", n);
    bindings.add(binding);
    final ProductDESProxy des = getCompiledDES(bindings, "handwritten", name);
   runModelVerifier(des, result);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
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
    final SafetyCounterExampleProxy castTest =
      (SafetyCounterExampleProxy) counter;
    final TraceProxy trace = castTest.getTrace();
    final List<EventProxy> eventlist = trace.getEvents();
    final int len = eventlist.size();
    assertTrue("Empty Counterexample!", len > 0);

    final EventProxy last = eventlist.get(len-1);
    final EventKind ekind = last.getKind();
    assertEquals(ekind, EventKind.UNCONTROLLABLE);

    final Collection<AutomatonProxy> automata = des.getAutomata();
    boolean rejected = false;
    for (final AutomatonProxy aut : automata){
      final ComponentKind akind = aut.getKind();
      final StateProxy state =
        checkCounterExample(aut, trace, akind == ComponentKind.SPEC);
      rejected |= state == null;
    }
    assertTrue("Counterexample not rejected by any spec!", rejected);
  }

}
