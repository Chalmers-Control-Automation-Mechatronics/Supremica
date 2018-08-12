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

package net.sourceforge.waters.analysis.monolithic;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

public class MonolithicCliqueBasedSupervisorReductionTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the synthesiser under test. This method
   * instantiates the class of the synthesiser tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the synthesiser to create its output.
   * @return An instance of the synthesiser.
   */
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final MonolithicSynthesizer synthesizer =
      new MonolithicSynthesizer(factory);
    final CliqueBasedSupervisorReductionTRSimplifier simplifier = new CliqueBasedSupervisorReductionTRSimplifier();
    simplifier.setIsFindFirst(false);
    simplifier.setHeuristicCoverStrategy(HeuristicCoverStrategy.NONE);
    synthesizer.setSupervisorReductionSimplifier(simplifier);
    synthesizer.setSupervisorLocalizationEnabled(true);
    return synthesizer;
  }

  @Override
  protected ProductDESResult runSynthesizer(final ProductDESProxy des,
                                            final List<ParameterBindingProxy> bindings,
                                            final boolean expect)
    throws Exception
  {
    final MonolithicSynthesisResult result = (MonolithicSynthesisResult)super.runSynthesizer(des, bindings, expect);


    final List<TRSimplifierStatistics> allStatistics = result.getSimplifierStatistics();
    TRSimplifierStatistics reductionStatistics = null;
    for (int i = 0; i < allStatistics.size(); i++) {
      final TRSimplifierStatistics statisticsForSimplifier = allStatistics.get(i);
      if (statisticsForSimplifier.getSimplifierClass().equals(CliqueBasedSupervisorReductionTRSimplifier.class)) {
        reductionStatistics = statisticsForSimplifier;
        break;
      }
    }

    if (reductionStatistics != null) {
      System.out.println("Overall stats for " + des.getName());
      //a System.out.println(reductionStatistics.toString()) just gives me the default .toString() implementation
      System.out.println(reductionStatistics.toString());
    }
    return result;
  }

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(MonolithicCliqueBasedSupervisorReductionTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //92 states, 56 events, 336 transitions
  @Override
  public void testCatMouseUnsup1() throws Exception
  {
  }

  //698 states, 72 events, 4272 transitions
  @Override
  public void testCatMouseUnsup2() throws Exception
  {
  }

  //410 states, 12 events, 1494 transitions
  @Override
  public void testTransferLine2() throws Exception
  {
  }

  //5992 states, 17 events, 29749 transitions
  @Override
  public void testTransferLine3() throws Exception
  {
  }

  //672 states, 15 events, 2248 transitions
  @Override
  public void testCellSwitch() throws Exception
  {
  }

  //830 states, 16 events, 8432 transitions
  @Override
  public void testIPC() throws Exception
  {
  }

  //18432 states, 18 events, 83316 transitions
  @Override
  public void testIPCcswitch() throws Exception
  {
  }

  //4374 states, 20 events, 14293 transitions
  @Override
  public void testIPClswitch() throws Exception
  {
  }

  //852 states, 18 events, 2826 transitions
  @Override
  public void testIPCuswicth() throws Exception
  {
  }

  //2394 states, 35 events, 4381 transitions
  @Override
  public void testTictactoe() throws Exception
  {
  }

  //419 states, 22 events, 972 transitions
  @Override
  public void testCT3() throws Exception
  {
  }

  //4675 states, 36 events, 20752 transitions (can be reduced to 2 states?)
  @Override
  public void testRobotAssemblyCell() throws Exception
  {
  }

  //6288 states, 26 events, 35308 transitions
  @Override
  public void test2LinkAlt() throws Exception
  {
  }

  //33712 states, 26 events, 186444 transitions
  @Override
  public void test2LinkAltBatch() throws Exception
  {
  }

  //12960 states, 17 events, 49968 transitions
  @Override
  public void testIMS() throws Exception
  {
  }
}
