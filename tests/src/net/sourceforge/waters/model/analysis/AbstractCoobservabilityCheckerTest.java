//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * Abstract base class to test the interface {@link CoobservabilityChecker}.
 *
 * @author Robi Malik
 */

public abstract class AbstractCoobservabilityCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractCoobservabilityCheckerTest()
  {
  }

  public AbstractCoobservabilityCheckerTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy des = factory.createProductDESProxy("empty");
    runModelVerifier(des, true);
  }

  public void testReentrant()
    throws Exception
  {
    testEmpty();
    testSmallFactory2();
    testAbp1();
    testEmpty();
    testOverflowException();
    testSmallFactory2();
    testAbp1();
  }

  public void testOverflowException()
    throws Exception
  {
    final ModelVerifier verifier = getModelVerifier();
    try {
      verifier.setNodeLimit(2);
      testAbp1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      final OverflowKind kind = exception.getOverflowKind();
      assertTrue("Unexpected overflow kind!",
                 kind == OverflowKind.STATE || kind == OverflowKind.NODE);
      final AnalysisResult result = verifier.getAnalysisResult();
      assertNotNull("Got NULL analysis result after exception!", result);
      assertNotNull("No exception in analysis result after caught exception!",
                    result.getException());
      assertSame("Unexpected exception in analysis result!",
                 exception, result.getException());
    } finally {
      verifier.setNodeLimit(Integer.MAX_VALUE);
    }
  }


  //#########################################################################
  //# Test Cases --- specific tests for coobservability
  public void testAbp1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp1.wmod");
    runModelVerifier(des, true);
  }

  public void testAbp2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp2.wmod");
    runModelVerifier(des, true);
  }

  public void testAbp3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp3.wmod");
    runModelVerifier(des, false);
  }

  public void testAbp4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "coobservability", "abp4.wmod");
    runModelVerifier(des, true);
  }


  //#########################################################################
  //# Test Cases --- controllability via coobservability
  public void testEmptySpec() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "empty_spec.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2.wmod");
    runModelVerifier(des, true);
  }

  public void testSmallFactory2u() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "small_factory_2u.wmod");
    runModelVerifier(des, false);
  }

  public void testTictactoe() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("handwritten", "tictactoe.wmod");
    runModelVerifier(des, false);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final Collection<String> empty = Collections.emptyList();
    compiler.setEnabledPropositionNames(empty);
    compiler.setEnabledPropertyNames(empty);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws Exception
  {
    // TODO implement this
  }

}
