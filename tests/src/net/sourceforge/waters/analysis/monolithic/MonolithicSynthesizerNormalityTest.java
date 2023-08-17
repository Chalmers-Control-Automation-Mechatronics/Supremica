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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
  /**
   * <P>In addition to standard synthesis results, for synthesis with
   * unobservable events it is also checked whether:</P>
   * <UL>
   * <LI>The supervisor refrains from using unobservable events;</LI>
   * <LI>The controlled behaviour is contained within the language defined
   *     by the specifications.</LI>
   * </UL>
   */
  @Override
  protected void checkResult(final ProductDESProxy des,
                             final ProductDESResult result,
                             final boolean expect)
    throws Exception
  {
    super.checkResult(des, result, expect);
    if (result.isSatisfied()) {
      final Collection<AutomatonProxy> desAutomata = des.getAutomata();
      final Collection<? extends AutomatonProxy> computedSupervisors =
        result.getComputedAutomata();
      final int numAutomata = desAutomata.size() + computedSupervisors.size();
      final Collection<AutomatonProxy> testingAutomata =
        new ArrayList<>(numAutomata);
      for (final AutomatonProxy aut : desAutomata) {
        switch (aut.getKind()) {
        case PLANT:
        case SPEC:
          testingAutomata.add(aut);
          break;
        default:
          break;
        }
      }
      for (final AutomatonProxy sup : computedSupervisors) {
        final Set<EventProxy> events = sup.getEvents();
        for (final EventProxy event : events){
          if (!event.isObservable()) {
            fail("The supervisor '" + sup.getName() +
                 "' uses the unobservable event '" + event.getName() + "'!");
          }
        }
        testingAutomata.add(sup);
      }
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      final String name = des.getName();
      final Collection<EventProxy> events = des.getEvents();
      final ProductDESProxy testingDES =
        factory.createProductDESProxy(name, null, null, events, testingAutomata);
      final KindTranslator translator =
        new SpecInclusionKindTranslator();
      verifySupervisor(testingDES, translator, "contained in the specification");
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

  public void testNormality03() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "normality_03.wmod");
    runSynthesizer(des, true);
  }

  public void testNormality04() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "normality_04.wmod");
    runSynthesizer(des, true);
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

  public void testTrafficLights() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "traffic_lights.wmod");
    runSynthesizer(des, true);
  }


  //#########################################################################
  //# Inner Class SpecInclusionKindTranslator
  private static class SpecInclusionKindTranslator
    implements KindTranslator
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      switch (aut.getKind()) {
      case PLANT:
      case SUPERVISOR:
        return ComponentKind.PLANT;
      case SPEC:
        return ComponentKind.SPEC;
      default:
        return null;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      switch (event.getKind()) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        return EventKind.UNCONTROLLABLE;
      case PROPOSITION:
        return EventKind.PROPOSITION;
      default:
        return null;
      }
    }
  }

}
