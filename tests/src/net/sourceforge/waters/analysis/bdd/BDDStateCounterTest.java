//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.bdd;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class BDDStateCounterTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(BDDStateCounterTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mMonolithicChecker = new NativeControllabilityChecker(factory);
    mBDDChecker = new BDDControllabilityChecker(factory);
    mBDDChecker.setReorderingEnabled(true);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mMonolithicChecker = null;
    mBDDChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases --- handwritten
  public void testMachineBuffer() throws Exception
  {
    final String group = "handwritten";
    final String name = "machine_buffer.wmod";
    countStates(group, name);
  }

  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2.wmod";
    countStates(group, name);
  }


  //#########################################################################
  //# Test Cases --- hisc
  public void testHISCRhoneSubsystem1Patch0() throws Exception
  {
    final String group = "tests";
    final String dir = "hisc";
    final String name = "rhone_subsystem1_patch0.wmod";
    countStates(group, dir, name);
  }

  public void testHISCRhoneSubsystem1Patch1() throws Exception
  {
    final String group = "tests";
    final String dir = "hisc";
    final String name = "rhone_subsystem1_patch1.wmod";
    countStates(group, dir, name);
  }

  public void testHISCRhoneSubsystem1Patch2() throws Exception
  {
    final String group = "tests";
    final String dir = "hisc";
    final String name = "rhone_subsystem1_patch2.wmod";
    countStates(group, dir, name);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void test_BallTimer() throws Exception
  {
    final String group = "tests";
    final String dir = "ball_sorter";
    final String name = "ball_timer.wmod";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_amk14() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "amk14.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_cs37() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "cs37.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_ez1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ez1.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_gb20() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb20.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_gb21() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gb21.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_gjr5() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "gjr5.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_grj3() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "grj3.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_imr1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "imr1.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_jbr2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jbr2.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_jmr30() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jmr30.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_jpt10() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "jpt10.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_kah18() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "kah18.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_lsr1_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_1.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_lsr1_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lsr1_2.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_lz136_1() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_1.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_lz136_2() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "lz136_2.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_ry27() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "ry27.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_sjw41() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "sjw41.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_smr26() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "smr26.wdes";
    countStates(group, dir, name);
  }

  public void test_Batchtank2005_vl6() throws Exception
  {
    final String group = "tests";
    final String dir = "batchtank2005";
    final String name = "vl6.wdes";
    countStates(group, dir, name);
  }

  public void test_IMS_ims() throws Exception
  {
    final String group = "tests";
    final String dir = "ims";
    final String name = "ims.wmod";
    countStates(group, dir, name);
  }

  public void test_Nasty_ReleaseAndBlow() throws Exception
  {
    final String group = "tests";
    final String dir  = "nasty";
    final String name = "release_and_blow.wmod";
    countStates(group, dir, name);
  }

  public void testProfisafeI4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    countStates(group, dir, name);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_slave.wmod";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_dal9() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dal9.wdes";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_dmt10() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "dmt10.wdes";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_jlm39() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "jlm39.wdes";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_mjd29() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "mjd29.wdes";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_ncj3() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "ncj3.wdes";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_sdh7() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sdh7.wdes";
    countStates(group, dir, name);
  }

  public void test_TrafficLights2006_yip1() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "yip1.wdes";
    countStates(group, dir, name);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBmw_fh() throws Exception
  {
    final String group = "valid";
    final String dir  = "bmw_fh";
    final String name = "bmw_fh.wdes";
    countStates(group, dir, name);
  }

  public void testDebounce() throws Exception
  {
    final String group = "valid";
    final String dir = "debounce";
    final String name = "debounce.wdes";
    countStates(group, dir, name);
  }

  public void testFtuer() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "ftuer.wdes";
    countStates(group, dir, name);
  }

  public void testKoordwsp() throws Exception
  {
    final String group = "valid";
    final String dir  = "central_locking";
    final String name = "koordwsp.wdes";
    countStates(group, dir, name);
  }

  public void testSafetydisplay() throws Exception
  {
    final String group = "valid";
    final String dir = "safetydisplay";
    final String name = "safetydisplay.wdes";
    countStates(group, dir, name);
  }

  public void testSmallFactory() throws Exception
  {
    final String group = "valid";
    final String dir = "small";
    final String name = "small.wdes";
    countStates(group, dir, name);
  }

  public void testSmd() throws Exception
  {
    final String group = "valid";
    final String dir = "smd";
    final String name = "smdreset.wdes";
    countStates(group, dir, name);
  }

  public void testWeiche() throws Exception
  {
    final String group = "valid";
    final String dir = "vt";
    final String name = "weiche.wdes";
    countStates(group, dir, name);
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
    countStates(group, name, bindings);
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  private void countStates(final String group,
                           final String name,
                           final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    countStates(groupdir, name, bindings);
  }

  @SuppressWarnings("unused")
  private void countStates(final String group,
                           final String subdir,
                           final String name,
                           final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    countStates(groupdir, subdir, name, bindings);
  }

  private void countStates(final File groupdir,
                           final String subdir,
                           final String name,
                           final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    countStates(dir, name, bindings);
  }

  private void countStates(final File dir,
                           final String name,
                           final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File filename = new File(dir, name);
    countStates(filename, bindings);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  private void countStates(final String group,
                           final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    countStates(groupdir, name);
  }

  private void countStates(final String group,
                           final String subdir,
                           final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    countStates(groupdir, subdir, name);
  }

  private void countStates(final File groupdir,
                           final String subdir,
                           final String name)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    countStates(dir, name);
  }

  private void countStates(final File dir,
                           final String name)
  throws Exception
  {
    final File filename = new File(dir, name);
    countStates(filename, (List<ParameterBindingProxy>) null);
  }

  private void countStates(final File filename,
                           final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    countStates(des);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void countStates(final ProductDESProxy des)
  throws Exception
  {
    getLogger().info("Counting " + des.getName() + " ...");
    mBDDChecker.setModel(des);
    mBDDChecker.run();
    final AnalysisResult bdd = mBDDChecker.getAnalysisResult();
    assertTrue("Unexpected FALSE result from BDD checker!",
               bdd.isSatisfied());
    final double count = bdd.getTotalNumberOfStates();
    mMonolithicChecker.setModel(des);
    mMonolithicChecker.run();
    final AnalysisResult mono = mMonolithicChecker.getAnalysisResult();
    assertTrue("Unexpected FALSE result from monolithic checker!",
               mono.isSatisfied());
    final double expect = mono.getTotalNumberOfStates();
    assertEquals("Unexpected number of states!", expect, count, 0.1);
    getLogger().info("Done " + des.getName());
  }


  //#########################################################################
  //# Data Members
  private ControllabilityChecker mMonolithicChecker;
  private BDDControllabilityChecker mBDDChecker;

}
