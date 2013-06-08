//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMCompilerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;



public class EFSMConflictCheckerTest
  extends AbstractWatersTest
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
    final String name = "empty";
    final ModuleProxy module = mModuleFactory.createModuleProxy
      (name, null, null, null, null, null, null);
    checkConflict(module, true);
  }


  //#########################################################################
  //# Successful Test Cases using EFA
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
    checkConflict(module, true);
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

  public void testTransferLine22Block()
    throws IOException, WatersException
  {
    checkTransferLine("transferline_efsm_block", 2, 2, false);
  }

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

  public void testPrimeSieve4fail()
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

  public void testPslBigWithManyRestartTrans()
    throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "psl", "pslBigWithManyRestartTrans");
    checkConflict(module, false);
  }


  //#########################################################################
  //# Paremeterised Tests
  private void checkTransferLine(final String name,
                                 final int n, final int m,
                                 final boolean expect)
    throws IOException, WatersException
  {
    final List<ParameterBindingProxy> bindings =
      new ArrayList<ParameterBindingProxy>(2);
    final IntConstantProxy constN = mModuleFactory.createIntConstantProxy(n);
    final ParameterBindingProxy bindingN =
      mModuleFactory.createParameterBindingProxy("N", constN);
    bindings.add(bindingN);
    final IntConstantProxy constM = mModuleFactory.createIntConstantProxy(m);
    final ParameterBindingProxy bindingM =
      mModuleFactory.createParameterBindingProxy("M", constM);
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
  private ModuleProxy loadModule(final String dirname, final String subdirname, final String name)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final File subdir = new File(dir, subdirname);
    final String extname = name + mModuleMarshaller.getDefaultExtension();
    final File filename = new File(subdir, extname);
    return loadModule(filename);
  }

  private ModuleProxy loadModule(final String dirname, final String name)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final String extname = name + mModuleMarshaller.getDefaultExtension();
    final File filename = new File(dir, extname);
    return loadModule(filename);
  }

  private ModuleProxy loadModule(final File filename)
    throws IOException, WatersException
  {
    final URI uri = filename.toURI();
    return mModuleMarshaller.unmarshal(uri);
  }

  private boolean checkConflict(final ModuleProxy module, final boolean expected)
    throws EvalException, AnalysisException
  {
    return checkConflict(module, null, expected);
  }

  private boolean checkConflict(final ModuleProxy module,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expected)
    throws EvalException, AnalysisException
  {
    final EFSMConflictChecker conflictChecker =
      new EFSMConflictChecker(module, mModuleFactory);
    configure(conflictChecker);
    final boolean result = conflictChecker.run();
    assertEquals("Unexpected result from conflict check!", expected, result);
    return result;
  }

  @SuppressWarnings("unused")
  private ParameterBindingProxy createBinding(final String name,
                                              final int value)
  {
    final IntConstantProxy expr = mModuleFactory.createIntConstantProxy(value);
    return mModuleFactory.createParameterBindingProxy(name, expr);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mModuleFactory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(mModuleFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mModuleFactory = null;
    mModuleMarshaller = null;
    mDocumentManager = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mModuleFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;

}



