//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   OldOptimisingCompilerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;


public class OldOptimisingCompilerTest extends AbstractCompilerTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(OldOptimisingCompilerTest.class);
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
    compiler.setNormalizationEnabled(false);
    compiler.setOptimizationEnabled(true);
    compiler.setSourceInfoEnabled(true);
    compiler.setMultiExceptionsEnabled(true);
  }

  @Override
  String[] getTestSuffices()
  {
    final String[] array = {"opt"};
    return array;
  }
}