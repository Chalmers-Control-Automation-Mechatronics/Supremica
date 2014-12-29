//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;


public abstract class AbstractSynchronousProductBuilderTest
  extends AbstractAutomatonBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractSynchronousProductBuilderTest()
  {
  }

  public AbstractSynchronousProductBuilderTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  @Override
  protected String getExpectedAutomatonName()
  {
    return EXPECTED_NAME;
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testReentrant()
    throws Exception
  {
    testSmallFactory2();
    testTransferline1();
    testTransferline1();
    testSmallFactory2u();
  }

  public void testStateOverflowException()
    throws Exception
  {
    final AutomatonBuilder builder = getAutomatonBuilder();
    try {
      builder.setNodeLimit(2);
      testTransferline1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      assertEquals("Unexpected overflow kind!",
                   OverflowKind.STATE, exception.getOverflowKind());
      final AnalysisResult result = builder.getAnalysisResult();
      assertNotNull("Got NULL analysis result after exception!", result);
      assertNotNull("No exception in analysis result after caught exception!",
                    result.getException());
      assertSame("Unexpected exception in analysis result!",
                 exception, result.getException());
    }
  }

  public void testTransitionOverflowException()
    throws Exception
  {
    final AutomatonBuilder builder = getAutomatonBuilder();
    try {
      builder.setTransitionLimit(3);
      testTransferline1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      assertEquals("Unexpected overflow kind!",
                   OverflowKind.TRANSITION, exception.getOverflowKind());
      final AnalysisResult result = builder.getAnalysisResult();
      assertNotNull("Got NULL analysis result after exception!", result);
      assertNotNull("No exception in analysis result after caught exception!",
                    result.getException());
      assertSame("Unexpected exception in analysis result!",
                 exception, result.getException());
    }
  }


  //#########################################################################
  //# Test Cases
  public void testNondeterministicCombinations() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod",
                        "nondeterministic_combinations.wmod");
  }

  public void testOneEvent() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "one_event.wmod");
  }

  public void testOrder() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "order.wmod");
  }

  public void testSmallFactory2() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "small_factory_2.wmod");
  }

  public void testSmallFactory2u() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "small_factory_2u.wmod");
  }

  public void testTransferline1() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "transferline_1.wmod");
  }


  //#########################################################################
  //# Class Constants
  private static final String EXPECTED_NAME = "sync";

}
