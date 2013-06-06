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
    final boolean result = checkConflict(module);
    assertTrue("Empty system is reported as blocking!", result);
  }


  //#########################################################################
  //# Successful Test Cases using EFA
  public void testEFSMCompiler1()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm1", true);
  }

  public void testEFSMCompiler2()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm2", true);
  }

  public void testEFSMCompiler5()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm5", true);
  }

  public void testEFSMCompiler8()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm8", true);
  }

  public void testEFSMCompiler10()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm10", true);
  }

  public void testEFSMCompiler11()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm11", true);
  }

  public void testEFSMCompiler12()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm12", true);
  }

  public void testEFSMCompiler13()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm13", false);
  }

  public void testEFSMCompiler14()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm14", false);
  }

  public void testEFSMCompiler15()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm15", true);
  }

  public void testEFSMCompiler16()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm16", true);
  }

  public void testEFSMCompiler17()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm17", false);
  }

  public void testEFSMCompiler18()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "efsm18", true);
  }

  public void testEFSMConflict1()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "conflict01", false);
  }

  public void testEFSMConflict2()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "conflict02", true);
  }

  public void testPslBig()
    throws IOException, WatersException
  {
    checkConflict("tests", "efsm", "pslBig", true);
  }

  public void testPrimeSieve2a()
    throws IOException, WatersException
  {
    checkConflict("efa", "prime_sieve2a", true);
  }

  public void testPrimeSieve2()
    throws IOException, WatersException
  {
    checkConflict("efa", "prime_sieve2", true);
  }

  public void testPrimeSieve3()
    throws IOException, WatersException
  {
    checkConflict("efa", "prime_sieve3", true);
  }

  public void testPrimeSieve4()
    throws IOException, WatersException
  {
    checkConflict("efa", "prime_sieve4", true);
  }

  public void testPrimeSieve5()
    throws IOException, WatersException
  {
    checkConflict("efa", "prime_sieve5", true);
  }

  public void testPrimeSieve6()
    throws IOException, WatersException
  {
    checkConflict("efa", "prime_sieve6", true);
  }

  public void pslBigWithManyRestartTrans()
    throws IOException, WatersException
  {
    checkConflict("efa", "pslBigWithManyRestartTrans", false);
  }

  //#########################################################################
  //# Customisation
  void configure(final EFSMConflictChecker checker)
  {

  }


  //#########################################################################
  //# Utilities
  private void checkConflict(final String dirname, final String name, final boolean expected)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    checkConflict(dir, name, null, expected);
  }

  private void checkConflict(final String dirname,
                             final String subdirname,
                             final String name,
                             final boolean expected)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final File subdir = new File(dir, subdirname);
    final boolean result = checkConflict(subdir, name, null, false);
    assertEquals("Unexpected result from conflict check!", expected, result);
  }

  private boolean checkConflict(final File dir,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean appendToName)
    throws IOException, WatersException
  {
    final String inextname = name + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(dir, inextname);
    return checkConflict(infilename, bindings);
  }

  private boolean checkConflict(final File infilename,
                             final List<ParameterBindingProxy> bindings)
    throws IOException, WatersException
  {
    final URI uri = infilename.toURI();
    final ModuleProxy inputModule = mModuleMarshaller.unmarshal(uri);
    return checkConflict(inputModule, bindings);
  }


  private boolean checkConflict(final ModuleProxy module,
                                final List<ParameterBindingProxy> bindings)
    throws EvalException, AnalysisException
  {
    final EFSMConflictChecker conflictChecker =
      new EFSMConflictChecker(module, mModuleFactory);
    configure(conflictChecker);
//    conflictChecker.setInternalTransitionLimit(5000000);
    return conflictChecker.run();
  }

  private boolean checkConflict(final ModuleProxy module)
    throws EvalException, AnalysisException
  {
    return checkConflict(module, null);
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



