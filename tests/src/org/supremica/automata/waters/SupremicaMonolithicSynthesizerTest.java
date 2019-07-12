//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.waters;

import java.util.Collection;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class SupremicaMonolithicSynthesizerTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# To be Provided by Subclasses
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final SupervisorSynthesizer synthesizer =
      new SupremicaMonolithicSynthesizer(factory);
    synthesizer.setSupervisorReductionFactory(null);
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(SupremicaMonolithicSynthesizerTest.class);
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

}
