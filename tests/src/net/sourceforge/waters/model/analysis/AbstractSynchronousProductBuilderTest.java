//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
