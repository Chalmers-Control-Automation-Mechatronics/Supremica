//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.abstraction.SimpleSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SmallCliqueSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Jordan Schroder
 */
public class MonolithicSmallCliqueUnionSupervisorReductionTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final MonolithicSynthesizer synthesizer =
      new MonolithicSynthesizer(factory);
    final SmallCliqueSupervisorReductionTRSimplifier simplifier =
      new SmallCliqueSupervisorReductionTRSimplifier();
    simplifier.setMode
      (SmallCliqueSupervisorReductionTRSimplifier.Mode.GREEDY_UNION);
    final SupervisorReductionFactory reduction =
      new SimpleSupervisorReductionFactory(simplifier);
    synthesizer.setSupervisorReductionFactory(reduction);
    synthesizer.setSupervisorLocalizationEnabled(true);
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(MonolithicSmallCliqueUnionSupervisorReductionTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Too Big :-(
  @Override
  public void testAip0Sub1P1() throws Exception
  {
  }

  @Override
  public void testCatMouseUnsup2() throws Exception
  {
    // out of memory checking result ...
  }

  @Override
  public void testIPC() throws Exception
  {
  }

  @Override
  public void testIPCcswitch() throws Exception
  {
  }

  @Override
  public void testIPClswitch() throws Exception
  {
  }

  @Override
  public void testIMS() throws Exception
  {
  }

  @Override
  public void testTransferLine3() throws Exception
  {
  }

  @Override
  public void test2LinkAlt() throws Exception
  {
  }

  @Override
  public void test2LinkAltBatch() throws Exception
  {
  }
}
