//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   NormalisingCompilerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.efa.EFSMControllabilityException;
import net.sourceforge.waters.model.module.ModuleProxy;


public class NormalisingCompilerTest extends AbstractCompilerTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(NormalisingCompilerTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.model.compiler.AbstractCompilerTest
  @Override
  void configure(final ModuleCompiler compiler)
  {
    compiler.setMultiExceptionsEnabled(true);
    compiler.setNormalizationEnabled(true);
    compiler.setOptimizationEnabled(false);
    compiler.setSourceInfoEnabled(true);
    compiler.setUsingEventAlphabet(false);
  }

  @Override
  String[] getTestSuffices()
  {
    final String[] array = {"-norm", ""};
    return array;
  }


  //#########################################################################
  //# Specific Test Cases
  @Override
  public void testCompile_EFATransferLine() throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("efa", "transferline_efa");
    final String[] culprit1 = {"'bufferA[1].c'", "'acceptT[0]'"};
    final String[] culprit2 = {"'bufferA[1].c'", "'rejectT[1]'"};
    final String[] culprit3 = {"'bufferB[1].c'", "'finishM[1]'"};
    compileError(module, null, EFSMControllabilityException.class,
                 culprit1, culprit2, culprit3);
  }

  public void testCompile_controllability() throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "controllability");
    final String[] culprit = {"'x'", "'event'"};
    compileError(module, null, EFSMControllabilityException.class, culprit);
  }

  public void testCompile_normalise1() throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler", "efsm", "normalise1");
    testCompile(module);
  }

  public void testCompile_normalise2() throws IOException, WatersException
  {
    final ModuleProxy module =
      loadModule("tests", "compiler" ,"efsm", "normalise2");
    testCompile(module);
  }

}