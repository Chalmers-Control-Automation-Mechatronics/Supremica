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
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class UnifiedEFAConflictCheckerTest extends AbstractAnalysisTest
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

  /*---------------------------- EFA --------------------------------------*/

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

  public void testJDEDS2014()
    throws IOException, WatersException
  {
    checkJDEDS2014(2, 1);
  }

  public void testProductionCell()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efa", "production_cell");
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


  /*--------------------------- Goran -------------------------------------*/
  /*
  public void testGoranSimpleTestSystem()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "goran", "goran-SimpleTestSystem");
    checkConflict(module, true);
  }

  public void testGoranSMB()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "goran", "goran-sm-b");
    checkConflict(module, false);
  }

  public void testGoranKulbanaNB()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "goran", "goran-Kulbana");
    checkConflict(module, true);
  }

  /*-------------------------- Transfer Line ------------------------------*/

  public void testTransferLineEFA131()
    throws IOException, WatersException
  {
    checkTransferLineEFA("transferline_norm", 1, 3, 1, true);
  }

  public void testTransferLineEFA131Block()
    throws IOException, WatersException
  {
    checkTransferLineEFA("transferline_efa_block", 1, 3, 1, false);
  }

  public void testTransferLineEFA10Block()
    throws IOException, WatersException
  {
    checkTransferLineEFA("transferline_efa_block", 3, 10, 10, false);
  }

  // TODO Argh! This one is much too slow!
  public void testTransferLineEFA20Block()
    throws IOException, WatersException
  {
    checkTransferLineEFA("transferline_efa_block", 3, 20, 20, false);
  }

  public void testTransferLineEFSM21()
    throws IOException, WatersException
  {
    checkTransferLineEFSM("transferline_efsm", 2, 1, true);
  }

  public void testTransferLineEFSM22()
    throws IOException, WatersException
  {
    checkTransferLineEFSM("transferline_efsm", 2, 2, true);
  }

  public void testTransferLineEFSM12Block()
    throws IOException, WatersException
  {
    checkTransferLineEFSM("transferline_efsm_block", 1, 2, false);
  }

  public void testTransferLineEFSM21Block()
    throws IOException, WatersException
  {
    checkTransferLineEFSM("transferline_efsm_block", 2, 1, false);
  }

  public void testTransferLineEFSM22Block()
    throws IOException, WatersException
  {
    checkTransferLineEFSM("transferline_efsm_block", 2, 2, false);
  }

  /*---------------------------- PROFIsafe --------------------------------*/

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

  public void testProfisafeISlaveEFA4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_islave_efa", 4 ,true);
  }

  public void testProfisafeIHostEFABlock3()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efa_0b", 3 ,false);
  }

  public void testProfisafeIHostEFAv0()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efa_0", 4 ,true);
  }

  public void testProfisafeIHostEFAv1()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efa_1", 4 ,true);
  }

  public void testProfisafeIHostEFAv2()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efa_2", 4 ,true);
  }

  public void testProfisafeIHostEFSM4()
    throws IOException, WatersException
  {
    checkProfisafe("profisafe_ihost_efsm", 4 ,true);
  }

  /*--------------------------- Prime Sieve -------------------------------*/

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

  /*--------------------------- Dynamic Sieve -----------------------------*/

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

  /*
  public void testDynamicPrimeSieve6()
    throws IOException, WatersException
  {
    checkPrimeSieve("dynamic_prime_sieve", 6, 288, true);
  }

  public void testDynamicPrimeSieve7()
    throws IOException, WatersException
  {
    checkPrimeSieve("dynamic_prime_sieve", 7, 360, true);
  }
  */

  /*----------------------------- PSL -------------------------------------*/
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

  public void testPslSmallNonblockingTest()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslSmallNonblockingTest");
    checkConflict(module, true);
  }

  public void testPslSmallBlockingTest()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslSmallBlocking");
    checkConflict(module, false);
  }

  public void testPslTest()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslTest");
    checkConflict(module, true);
  }

  public void testPsl()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "psl");
    checkConflict(module, false);
  }

  public void testPslSmallNonblocking()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslSmallNonblocking");
    checkConflict(module, true);
  }

  public void testPslBig()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "psl", "pslBig");
    checkConflict(module, false);
  }

  public void testPslBigWithManyRestartTrans()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslBigWithManyRestartTrans");
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


  //#########################################################################
  //# Parametrised Tests
  void checkJDEDS2014(final int c, final int n)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final ModuleProxy module = loadModule("efa", "jdeds2014");
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(3);
    final IntConstantProxy constC = factory.createIntConstantProxy(c);
    final ParameterBindingProxy bindingC =
      factory.createParameterBindingProxy("C", constC);
    bindings.add(bindingC);
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
    final IntConstantProxy constW = factory.createIntConstantProxy(c);
    final ParameterBindingProxy bindingW =
      factory.createParameterBindingProxy("W", constW);
    bindings.add(bindingW);
    checkConflict(module, bindings, false);
  }

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

  void checkPML(final String name,
                final int c, final int n,
                final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final ModuleProxy module = loadModule("efa", name);
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(2);
    final IntConstantProxy constC = factory.createIntConstantProxy(c);
    final ParameterBindingProxy bindingC =
      factory.createParameterBindingProxy("C", constC);
    bindings.add(bindingC);
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
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

  void checkTransferLineEFA(final String name,
                            final int n, final int a, final int b,
                            final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(3);
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
    final IntConstantProxy constA = factory.createIntConstantProxy(a);
    final ParameterBindingProxy bindingA =
      factory.createParameterBindingProxy("A", constA);
    bindings.add(bindingA);
    final IntConstantProxy constB = factory.createIntConstantProxy(b);
    final ParameterBindingProxy bindingB =
      factory.createParameterBindingProxy("B", constB);
    bindings.add(bindingB);
    final ModuleProxy module = loadModule("efa", name);
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

  void checkTransferLineEFSM(final String name,
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

  void checkTransferLineRework(final String name,
                               final int r, final int n,
                               final boolean expect)
    throws IOException, WatersException
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final ModuleProxy module = loadModule("efa", name);
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(2);
    final IntConstantProxy constR = factory.createIntConstantProxy(r);
    final ParameterBindingProxy bindingR =
      factory.createParameterBindingProxy("R", constR);
    bindings.add(bindingR);
    final IntConstantProxy constN = factory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      factory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
    checkConflict(module, bindings, expect);
  }


  //#########################################################################
  //# Customisation
  void configure(final UnifiedEFAConflictChecker checker)
  {
    // TODO Configuration:
    final DocumentManager manager = getDocumentManager();
    checker.setDocumentManager(manager);
    checker.setUsesLocalVariable(true);
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
