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

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import junit.framework.Test;
import junit.framework.TestSuite;


public class MonolithicSynthesizerNormalityTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# To be Provided by Subclasses
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final MonolithicSynthesizerNormality synthesizer =
      new MonolithicSynthesizerNormality(factory);
    synthesizer.setSupervisorReductionEnabled(false);
    synthesizer.setSupervisorLocalizationEnabled(false);
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(MonolithicSynthesizerNormalityTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest
  @Override
  protected void checkResult(final ProductDESProxy des,
                             final ProductDESResult result,
                             final boolean expect)
                               throws Exception
  {
    super.checkResult(des, result, expect);
    if (result.isSatisfied()) {
      final Collection<? extends AutomatonProxy> computedSupervisors =
        result.getComputedAutomata();
      // Check whether any of the supervisors uses an unobservable event
      for (final AutomatonProxy sup : computedSupervisors) {
        final Set<EventProxy> events = sup.getEvents();
        for (final EventProxy event : events){
          if (!event.isObservable()) {
            fail("The supervisor '" + sup.getName() +
                 "' uses the unobservable event '" + event.getName() + "'!");
          }
        }
      }
    }
  }


  //#########################################################################
  //# Test cases with unobservable events
  public void testNormality01() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "normality_01.wmod");
    runSynthesizer(des, true);
  }

  public void testNormality02() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "normality_02.wmod");
    runSynthesizer(des, false);
  }

  public void testParrowNormality() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "parrow_normality.wmod");
    runSynthesizer(des, true);
  }

  public void testSoeContNormality() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "soe_cont_normality.wmod");
    runSynthesizer(des, false);
  }
}
