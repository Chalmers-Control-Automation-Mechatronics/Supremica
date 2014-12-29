//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   OldCompilerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;


public class OldCompilerTest extends AbstractCompilerTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(OldCompilerTest.class);
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
    compiler.setOptimizationEnabled(false);
    compiler.setSourceInfoEnabled(true);
    compiler.setMultiExceptionsEnabled(true);
  }

  @Override
  String[] getTestSuffices()
  {
    final String[] array = {"", ""};
    return array;
  }
}