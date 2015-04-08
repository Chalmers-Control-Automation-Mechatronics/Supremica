//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class TRSynchronousProductBuilderTest
  extends AbstractTRSynchronousProductBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(TRSynchronousProductBuilderTest.class);
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
  protected TRSynchronousProductBuilder
    createAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    return new TRSynchronousProductBuilder();
  }

}
