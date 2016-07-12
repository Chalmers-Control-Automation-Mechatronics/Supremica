//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2016 Robi Malik
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

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.StateCounter;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

import junit.framework.Test;
import junit.framework.TestSuite;


public abstract class AbstractStateCounterTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(AbstractStateCounterTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mStateCounter = createStateCounter(factory);
    setNodeLimit(mStateCounter);
  }


  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the model analyser under test. This method
   * instantiates the class of the model verifier tested by the particular
   * subclass of this test, and configures it as needed.
   *
   * @param factory
   *          The factory used by the model verifier for trace construction.
   * @return An instance of the model verifier
   */
  protected abstract StateCounter createStateCounter
    (ProductDESProxyFactory factory);

  /**
   * Configures the model analyser under test for a given product DES. This
   * method is called just before the model verifier is started for each model
   * to be tested. Subclasses that override this method should call the
   * superclass method first.
   *
   * @param des
   *          The model to be verified for the current test case.
   */
  protected void configureStateCounter(final ProductDESProxy des)
  {
    mStateCounter.setModel(des);
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy des = factory.createProductDESProxy("empty");
    countStates(des, 1.0);
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
      mStateCounter.setNodeLimit(2);
      testTransferline__1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- handwritten
  public void testBadFactory() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "bad_factory.wmod");
    countStates(des, 15.0);
  }

  public void testMachine_buffer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "machine_buffer.wmod");
    countStates(des, 4.0);
  }

  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2.wmod");
    countStates(des, 12.0);
  }

  public void testSmallFactory2u() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    countStates(des, 18.0);
  }


  //#########################################################################
  //# Test Cases --- hisc
  public void testAip0sub1p0() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip0sub1p0.wmod");
    countStates(des, 24386.0);
  }

  public void testAip0sub1p1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip0sub1p1.wmod");
    countStates(des, 34998.0);
  }

  public void testAip0sub1p2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip0sub1p2.wmod");
    countStates(des, 29289.0);
  }


  //#########################################################################
  //# Test Cases --- tests
  public void testBall_timer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ball_sorter", "ball_timer.wmod");
    countStates(des, 8.0);
  }

  public void testBatchtank2005Amk14() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "amk14.wdes");
    countStates(des, 40.0);
  }

  public void testBatchtank2005Cs37() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "cs37.wdes");
    countStates(des, 64.0);
  }

  public void testBatchtank2005Ez1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "ez1.wdes");
    countStates(des, 104.0);
  }

  public void testBatchtank2005Gb20() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gb20.wdes");
    countStates(des, 34.0);
  }

  public void testBatchtank2005Gb21() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gb21.wdes");
    countStates(des, 60.0);
  }

  public void testBatchtank2005Gjr5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "gjr5.wdes");
    countStates(des, 30.0);
  }

  public void testBatchtank2005Grj3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "grj3.wdes");
    countStates(des, 77.0);
  }

  public void testBatchtank2005Imr1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "imr1.wdes");
    countStates(des, 75.0);
  }

  public void testBatchtank2005Jbr2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jbr2.wdes");
    countStates(des, 38.0);
  }

  public void testBatchtank2005Jmr30() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jmr30.wdes");
    countStates(des, 74.0);
  }

  public void testBatchtank2005Jpt10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "jpt10.wdes");
    countStates(des, 105.0);
  }

  public void testBatchtank2005Kah18() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "kah18.wdes");
    countStates(des, 62.0);
  }

  public void testBatchtank2005Lsr1_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lsr1_1.wdes");
    countStates(des, 55.0);
  }

  public void testBatchtank2005Lsr1_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lsr1_2.wdes");
    countStates(des, 55.0);
  }

  public void testBatchtank2005Lz136_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lz136_1.wdes");
    countStates(des, 55.0);
  }

  public void testBatchtank2005Lz136_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "lz136_2.wdes");
    countStates(des, 55.0);
  }

  public void testBatchtank2005Ry27() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "ry27.wdes");
    countStates(des, 37.0);
  }

  public void testBatchtank2005Sjw41() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "sjw41.wdes");
    countStates(des, 80.0);
  }

  public void testBatchtank2005Smr26() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "smr26.wdes");
    countStates(des, 42.0);
  }

  public void testBatchtank2005Vl6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "batchtank2005", "vl6.wdes");
    countStates(des, 62.0);
  }

  public void testIms() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ims", "ims.wmod");
    countStates(des, 12960.0);
  }

  public void testRelease_and_blow() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "release_and_blow.wmod");
    countStates(des, 1.0);
  }

  public void testProfisafe_i4_slave() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "profisafe", "profisafe_i4_slave.wmod");
    countStates(des, 8067.0);
  }

  public void testTrafficlights2006Dal9() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "dal9.wdes");
    countStates(des, 81.0);
  }

  public void testTrafficlights2006Dmt10() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "dmt10.wdes");
    countStates(des, 90.0);
  }

  public void testTrafficlights2006Jlm39() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "jlm39.wdes");
    countStates(des, 83.0);
  }

  public void testTrafficlights2006Mjd29() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "mjd29.wdes");
    countStates(des, 86.0);
  }

  public void testTrafficlights2006Ncj3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "ncj3.wdes");
    countStates(des, 88.0);
  }

  public void testTrafficlights2006Sdh7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "sdh7.wdes");
    countStates(des, 98.0);
  }

  public void testTrafficlights2006Yip1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "trafficlights2006", "yip1.wdes");
    countStates(des, 99.0);
  }


  //#########################################################################
  //# Test Cases --- valid
  public void testBmw_fh() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "bmw_fh", "bmw_fh.wdes");
    countStates(des, 7672.0);
  }

  public void testDebounce() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "debounce", "debounce.wdes");
    countStates(des, 6.0);
  }

  public void testFtuer() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "ftuer.wdes");
    countStates(des, 195.0);
  }

  public void testKoordwsp() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "central_locking", "koordwsp.wdes");
    countStates(des, 465648.0);
  }

  public void testSafetydisplay() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "safetydisplay", "safetydisplay.wdes");
    countStates(des, 81.0);
  }

  public void testSmdreset() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("valid", "smd", "smdreset.wdes");
    countStates(des, 31.0);
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__1() throws Exception
  {
    checkTransferline(1, 28.0);
  }

  public void testTransferline__2() throws Exception
  {
    checkTransferline(2, 410.0);
  }

  public void testTransferline__3() throws Exception
  {
    checkTransferline(3, 5992.0);
  }

  public void testTransferline__4() throws Exception
  {
    checkTransferline(4, 87578.0);
  }

  public void testTransferline__5() throws Exception
  {
    checkTransferline(5, 1280020.0);
  }

  public void checkTransferline(final int n, final double expected)
    throws Exception
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", n);
    bindings.add(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "handwritten", "transferline.wmod");
    countStates(des, expected);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void countStates(final ProductDESProxy des, final double expected)
    throws Exception
  {
    getLogger().info("Counting " + des.getName() + " ...");
    configureStateCounter(des);
    mStateCounter.run();
    final AnalysisResult result = mStateCounter.getAnalysisResult();
    assertTrue("Unexpected FALSE result from state counter!",
               result.isSatisfied());
    final double count = result.getTotalNumberOfStates();
    assertEquals("Unexpected number of states!", expected, count, 0.1);
    getLogger().info("Done " + des.getName());
  }


  //#########################################################################
  //# Data Members
  private StateCounter mStateCounter;

}
