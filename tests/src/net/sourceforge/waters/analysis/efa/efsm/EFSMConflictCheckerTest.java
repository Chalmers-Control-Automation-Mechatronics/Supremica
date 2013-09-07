//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EFSMConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

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
 * A test for the {@link EFSMConflictChecker}.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class EFSMConflictCheckerTest
  extends AbstractAnalysisTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(EFSMConflictCheckerTest.class);
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


  public void testPhilosophers5()
    throws IOException, WatersException
  {
    checkPhilosophers("dining_philosophers", 5, false);
  }

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

  public void testProfisafeIHost4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efsm", 4 ,true);
  }

  public void testProfisafeISlave4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_islave_efsm", 4 ,true);
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

  /*
   * This file is not in the repository :-(
  public void testPslWithResetTrans()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslWithResetTrans");
    checkConflict(module, false);
  }
   */


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


  //#########################################################################
  //# Customisation
  void configure(final EFSMConflictChecker checker)
  {
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
    final EFSMConflictChecker conflictChecker =
      new EFSMConflictChecker(module, factory);
    configure(conflictChecker);
    conflictChecker.setBindings(bindings);
    final boolean result = conflictChecker.run();
    assertEquals("Unexpected result from conflict check!", expected, result);
    return result;
  }

}