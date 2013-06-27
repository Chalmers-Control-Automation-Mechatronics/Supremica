//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynthesizerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class MonolithicSynthesizerTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# To be Provided by Subclasses
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final MonolithicSynthesizer synthesizer =
      new MonolithicSynthesizer(factory);
    synthesizer.setSupervisorReductionEnabled(false);
    synthesizer.setSupervisorLocalizationEnabled(false);
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(MonolithicSynthesizerTest.class);
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
      final Collection<AutomatonProxy> computedSupervisors =
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
      assertTrue(builder.run());
      final AutomatonProxy expectedSupervisor = builder.getComputedAutomaton();
      final IsomorphismChecker checker =
        new IsomorphismChecker(factory, false, true);
      checker.checkIsomorphism(computedSupervisor, expectedSupervisor);
    }
  }

}
