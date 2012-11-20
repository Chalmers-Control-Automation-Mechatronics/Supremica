//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSynchronousProductBuilderTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicSynthesizerTest
  extends AbstractSynchronousProductBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(MonolithicSynthesizerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  @Override
  protected MonolithicSynthesizer
    createAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    return new MonolithicSynthesizer(factory);
  }

  @Override
  protected MonolithicSynthesizer getAutomatonBuilder()
  {
    return (MonolithicSynthesizer) super.getAutomatonBuilder();
  }


  //#########################################################################
  //# Specific Test Cases


  public void testDeadlockPruning() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "deadlockPruning";
    runAutomatonBuilder(group, subdir, name);
  }


}
