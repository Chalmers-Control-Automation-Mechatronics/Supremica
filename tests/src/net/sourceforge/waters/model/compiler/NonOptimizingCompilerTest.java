//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   NonOptimizingCompilerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;


public class NonOptimizingCompilerTest
  extends AbstractCompilerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(NonOptimizingCompilerTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.model.compiler.AbstractCompilerTest
  void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
    compiler.setSourceInfoEnabled(true);
  }

  String getTestSuffix()
  {
    return "noopt";
  }

}
