//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class TRMonolithicSynthesizerTest
  extends AbstractSupervisorSynthesizerTest
  {

  //#########################################################################
  //# To be Provided by Subclasses
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final TRMonolithicSynthesizer synthesizer =
      new TRMonolithicSynthesizer(factory);
    synthesizer.setSupervisorLocalizationEnabled(false);
    synthesizer.setKindTranslator(ConflictSynthesisKindTranslator.getInstance());
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(TRMonolithicSynthesizerTest.class);
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
      // For monolithic synthesis, check whether the computed supervisor is
      // isomorphic (including markings) to the synchronous product of the
      // expected result and the plants and specs in the system.
      final Collection<? extends AutomatonProxy> computedSupervisors =
        result.getComputedAutomata();
      assertEquals("Monolithic synthesis did not return exactly one supervisor!",
                   1, computedSupervisors.size());
      final AutomatonProxy computedSupervisor =
        computedSupervisors.iterator().next();
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      final MonolithicSynchronousProductBuilder builder =
        new MonolithicSynchronousProductBuilder(des, factory);
      builder.setOutputName(des.getName());
      builder.setOutputKind(ComponentKind.SUPERVISOR);
      builder.setRemovingSelfloops(true);
      final EventProxy marking =
        AbstractConflictChecker.findMarkingProposition(des);
      final Collection<EventProxy> props = Collections.singletonList(marking);
      builder.setPropositions(props);
      assertTrue(builder.run());
      final AutomatonProxy expectedSupervisor = builder.getComputedAutomaton();
      final IsomorphismChecker checker =
        new IsomorphismChecker(factory, false, true);
      checker.checkIsomorphism(computedSupervisor, expectedSupervisor);
    }
  }

  @Override
  protected ProductDESResult runSynthesizer(final ProductDESProxy des,
                                            final List<ParameterBindingProxy> bindings,
                                            final boolean expect)
    throws Exception
  {
    final Set<AutomatonProxy> auts = new HashSet<>();
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch(aut.getKind()) {
      case PLANT:
      case SPEC:
        auts.add(aut);
      }
    }

    final ProductDESProxy properDes = getProductDESProxyFactory().createProductDESProxy("properDes", des.getEvents(), auts);

    final MonolithicSynthesizer synthesizer = new MonolithicSynthesizer(des, getProductDESProxyFactory(),
                                                                  ConflictSynthesisKindTranslator.getInstance());
    final boolean newExpect = synthesizer.run();
    ProductDESProxy supDes = null;
    final ProductDESProxy newDes;
    if (newExpect) {
      supDes = synthesizer.getComputedProxy();
      auts.add(supDes.getAutomata().iterator().next());
      newDes = getProductDESProxyFactory().createProductDESProxy(des.getName(), des.getEvents(), auts);
    }
    else {
      newDes = properDes;
    }



    return super.runSynthesizer(newDes, bindings, newExpect);

  }

  @Override
  protected void verifySupervisorControllability(final ProductDESProxy des)
    throws Exception
  {
    // TODO Auto-generated method stub

  }


  private static class ConflictSynthesisKindTranslator implements KindTranslator {

    private static ConflictSynthesisKindTranslator mInstance = new ConflictSynthesisKindTranslator();

    private static ConflictSynthesisKindTranslator getInstance() {
      return mInstance;
    }

    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
      case SPEC:
        return ComponentKind.PLANT;
      case SUPERVISOR:
      default:
        return null;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final EventKind kind = event.getKind();
      switch (kind) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        return EventKind.CONTROLLABLE;
      default:
        return kind;
      }
    }

  }

  @Override
  public void testFTechnik() throws Exception
  {
    // Too large, skip
  }

  @Override
  public void testBallProcess() throws Exception
  {
    // Invalid model
  }


}

