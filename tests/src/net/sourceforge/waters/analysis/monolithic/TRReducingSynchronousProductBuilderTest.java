//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRReducingSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class TRReducingSynchronousProductBuilderTest
  extends AbstractTRSynchronousProductBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(TRReducingSynchronousProductBuilderTest.class);
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
  protected TRReducingSynchronousProductBuilder
    createAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    return new TRReducingSynchronousProductBuilder();
  }


  //#########################################################################
  //# Reducing Test Cases
  public void testReducing01() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_01.wmod");
  }

  public void testReducing02() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_02.wmod");
  }

  public void testReducing03() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_03.wmod");
  }

  public void testReducing04() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_04.wmod");
  }

}
