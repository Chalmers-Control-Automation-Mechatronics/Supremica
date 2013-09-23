//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFAConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * A test for the {@link UnifiedEFAConflictChecker}.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class UnifiedEFAConflictCheckerTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(UnifiedEFAConflictCheckerTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Handcrafting Test Cases
  public void testEmpty()
    throws EvalException, AnalysisException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final String name = "empty";
    final ModuleProxy module = factory.createModuleProxy
      (name, null, null, null, null, null, null);
    checkConflict(module, true);
  }


  //#########################################################################
  //# Test Cases using EFA
  public void testEFSMCompiler1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm1");
    checkConflict(module, true);
  }

  public void testEFSMCompiler2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm2");
    checkConflict(module, false);
  }

  public void testEFSMCompiler5()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm5");
    checkConflict(module, true);
  }

  public void testEFSMCompiler8()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm8");
    checkConflict(module, true);
  }

  public void testEFSMCompiler10()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm10");
    checkConflict(module, true);
  }

  public void testEFSMCompiler11()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm11");
    checkConflict(module, true);
  }

  public void testEFSMCompiler12()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm12");
    checkConflict(module, true);
  }

  public void testEFSMCompiler13()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm13");
    checkConflict(module, false);
  }

  public void testEFSMCompiler14()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm14");
    checkConflict(module, false);
  }

  public void testEFSMCompiler15()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm15");
    checkConflict(module, true);
  }

  public void testEFSMCompiler16()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm16");
    checkConflict(module, true);
  }

  public void testEFSMCompiler17()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm17");
    checkConflict(module, false);
  }

  public void testEFSMCompiler18()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm18");
    checkConflict(module, true);
  }

  public void testEFSMConflict1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "conflict01");
    checkConflict(module, false);
  }

  public void testEFSMConflict2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "conflict02");
    checkConflict(module, true);
  }

  public void testEFSMConflict3()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "conflict03");
    checkConflict(module, false);
  }

  public void testEFSMConflict4()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "conflict04");
    checkConflict(module, true);
  }


  public void testPhilosophers5()
    throws IOException, WatersException
  {
    checkPhilosophers("dining_philosophers", 5, false);
  }

  /*---------------------------- EFA ---------------------------------------*/

  public void testCaseStudy()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "caseStudy-original");
    checkConflict(module, false);
  }

  public void testCaseStudyNonblocking()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "caseStudy-nonblocking");
    checkConflict(module, true);
  }

  public void testDosingTankWithJellyEFA1()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("handwritten", "DosingTankWithJellyEFA1");
    checkConflict(module, false);
  }

  public void testRoundRobin2()
    throws IOException, WatersException
  {
    checkRoundRobin(2);
  }

  public void testRoundRobin5()
    throws IOException, WatersException
  {
    checkRoundRobin(5);
  }

  /*--------------------------- Goran --------------------------------------*/

//  public void testGoranSimpleTestSystem()
//    throws IOException, WatersException
//  {
//    final ModuleProxy module =
//      loadModule("tests", "goran", "goran-SimpleTestSystem");
//    checkConflict(module, true);
//  }
//
//  public void testGoranSMB()
//    throws IOException, WatersException
//  {
//    final ModuleProxy module = loadModule("tests", "goran", "goran-sm-b");
//    checkConflict(module, false);
//  }
//
//  public void testGoranKulbanaNB()
//    throws IOException, WatersException
//  {
//    final ModuleProxy module = loadModule("tests", "goran", "goran-Kulbana-nb");
//    checkConflict(module, true);
//  }

  /*--------------------------- Transfer Line ------------------------------*/

  public void testTransferLine12()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm", 1, 2, true);
  }

  public void testTransferLine21()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm", 2, 1, true);
  }

  public void testTransferLine22()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm", 2, 2, true);
  }

  public void testTransferLine12Block()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm_block", 1, 2, false);
  }

  public void testTransferLine21Block()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm_block", 2, 1, false);
  }

  public void testTransferLine22Block()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm_block", 2, 2, false);
  }

  /*---------------------------- PROFIsafe ---------------------------------*/

  public void testProfisafeISlaveEFSM1()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_islave_efsm", 1 ,true);
  }

  public void testProfisafeISlaveEFSM4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_islave_efsm", 4 ,true);
  }

  /* TODO These tests require group nodes ...
  public void testProfisafeISlaveEFA4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_i4_slave_efa", 4 ,true);
  }

  public void testProfisafeIHostEFABlock3()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efa_block", 3 ,false);
  }*/

  public void testProfisafeIHostEFSM4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efsm", 4 ,true);
  }

  /*--------------------------- Prime Sieve --------------------------------*/

  public void testPrimeSieve2a()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve2a");
    checkConflict(module, true);
  }

  public void testPrimeSieve2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve2");
    checkConflict(module, true);
  }

  public void testPrimeSieve3()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve3");
    checkConflict(module, true);
  }

  public void testPrimeSieve4()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve4");
    checkConflict(module, true);
  }

  public void testPrimeSieve4b()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve4b");
    checkConflict(module, false);
  }

  public void testPrimeSieve5()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve5");
    checkConflict(module, true);
  }

  public void testPrimeSieve6()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "prime_sieve6");
    checkConflict(module, true);
  }

  /*--------------------------- Dynamic Sieve ------------------------------*/

  public void testDynamicPrimeSieve2()
    throws IOException, WatersException
  {
    checkPrimeSieve("dynamic_prime_sieve", 2, 24, true);
  }

  public void testDynamicPrimeSieve3()
    throws IOException, WatersException
  {
    checkPrimeSieve("dynamic_prime_sieve", 3, 48, true);
  }

  public void testDynamicPrimeSieve4()
    throws IOException, WatersException
  {
    checkPrimeSieve("dynamic_prime_sieve", 4, 120, true);
  }

  public void testDynamicPrimeSieve5()
    throws IOException, WatersException
  {
    checkPrimeSieve("dynamic_prime_sieve", 5, 168, true);
  }

  /*----------------------------- PSL --------------------------------------*/

  public void testPsl()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "psl");
    checkConflict(module, false);
  }

  public void testPslBig()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslBig");
    checkConflict(module, false);
  }

  public void testPslBigNonblocking()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslBigNonblocking");
    checkConflict(module, true);
  }

  public void testPslBigBlocking()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslBigBlocking");
    checkConflict(module, false);
  }

  public void testPslBigWithManyRestartTrans()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslBigWithManyRestartTrans");
    checkConflict(module, false);
  }

  public void testPslSmallNonblocking()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslSmallNonblocking");
    checkConflict(module, true);
  }

  public void testPslVerySmallNonblocking()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslVerySmallNonblocking");
    checkConflict(module, true);
  }

  public void testPslVerySmallBlocking()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslVerySmallBlocking");
    checkConflict(module, false);
  }


  public void testPslTest()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslTest");
    checkConflict(module, true);
  }

  public void testPslTest_100times()
    throws IOException, WatersException
  {
    for (int i=0; i< 100; i++) {
      final ModuleProxy module =
        loadModule("tests", "psl", "pslTest");
      checkConflict(module, true);
    }
  }

  //#########################################################################
  //# Parametrised Tests
  void checkPhilosophers(final String name,
                         final int n,
                         final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(bindingN);
    final ModuleProxy module = loadModule("efa", name);
    checkConflict(module, bindings, expect);
  }

  void checkPrimeSieve(final String name,
                       final int s, final int n,
                       final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(2);
    final IntConstantProxy constS = factory.createIntConstantProxy(s);
    final ParameterBindingProxy bindingS =
      factory.createParameterBindingProxy("S", constS);
    bindings.add(bindingS);
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
    final ModuleProxy module = loadModule("efa", name);
    checkConflict(module, bindings, expect);
  }

  void checkProfisafe(final String name,
                      final int maxseqno,
                      final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final IntConstantProxy constMAXSEQNO =
      factory.createIntConstantProxy(maxseqno);
    final ParameterBindingProxy bindingMAXSEQNO =
      factory.createParameterBindingProxy("MAXSEQNO", constMAXSEQNO);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(bindingMAXSEQNO);
    final ModuleProxy module = loadModule("tests", "profisafe", name);
    checkConflict(module, bindings, expect);
  }

  void checkTransferLine(final String name,
                         final int n, final int m,
                         final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(2);
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
    final IntConstantProxy constM = factory.createIntConstantProxy(m);
    final ParameterBindingProxy bindingM =
      factory.createParameterBindingProxy("M", constM);
    bindings.add(bindingM);
    final ModuleProxy module = loadModule("tests", "efsm", name);
    checkConflict(module, bindings, expect);
  }

  void checkRoundRobin(final int n)
    throws IOException, WatersException
  {
    final ParameterBindingProxy binding = createBinding("N", n);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final ModuleProxy module = loadModule("efa", "round_robin_efa.wmod");
    checkConflict(module, bindings, false);
  }


  //#########################################################################
  //# Customisation
  void configure(final UnifiedEFAConflictChecker checker)
  {
    // TODO Configure here ...
    checker.setPrefersAutomataCandidates(false);
  }


  //#########################################################################
  //# Utilities
  boolean checkConflict(final ModuleProxy module,
                        final boolean expected)
    throws EvalException, AnalysisException
  {
    return checkConflict(module, null, expected);
  }

  boolean checkConflict(final ModuleProxy module,
                        final List<ParameterBindingProxy> bindings,
                        final boolean expected)
    throws EvalException, AnalysisException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final UnifiedEFAConflictChecker conflictChecker =
      new UnifiedEFAConflictChecker(module, factory);
    configure(conflictChecker);
    conflictChecker.setBindings(bindings);
    final boolean result = conflictChecker.run();
    assertEquals("Unexpected result from conflict check!", expected, result);
    return result;
  }

}