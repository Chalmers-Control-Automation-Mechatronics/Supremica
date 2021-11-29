//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * Test case for {@link StateReorderingTRSimplifier}.
 * Currently only tests for integrity and isomorphism between input and
 * output, not whether a particular ordering is achieved.
 *
 * @author Robi Malik
 */

public class StateReorderingTRSimplifierTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public StateReorderingTRSimplifierTest()
  {
  }

  public StateReorderingTRSimplifierTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSimplifier = new StateReorderingTRSimplifier();
    mIntegrityChecker = ProductDESIntegrityChecker.getInstance();
    mIsomorphismChecker = new IsomorphismChecker(factory, false, true);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mSimplifier = null;
    mIntegrityChecker = null;
    mIsomorphismChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  public void test_empty_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "empty_1.wmod");
    testOrderings(des);
  }

  public void test_empty_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "empty_2.wmod");
    testOrderings(des);
  }

  public void test_basic_3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_3.wmod");
    testOrderings(des);
  }

  public void test_basic_4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_4.wmod");
    testOrderings(des);
  }

  public void test_basic_6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_6.wmod");
    testOrderings(des);
  }

  public void test_basic_7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_7.wmod");
    testOrderings(des);
  }

  // Unreachable states
  public void test_reordering_01() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "reordering_01.wmod");
    testOrderings(des);
  }

  // Two initial states
  public void test_woeq03() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "woeq03.wmod");
    testOrderings(des);
  }


  //#########################################################################
  //# Instantiating and Processing Modules
  private void testOrderings(final ProductDESProxy des)
    throws Exception
  {
    final EnumFactory<StateReorderingTRSimplifier.StateOrdering> factory =
      StateReorderingTRSimplifier.getStateOrderingEnumFactory();
    for (final StateReorderingTRSimplifier.StateOrdering ordering :
         factory.getEnumConstants()) {
      testOrdering(des, ordering);
    }
  }

  private void testOrdering
    (final ProductDESProxy des,
     final StateReorderingTRSimplifier.StateOrdering ordering)
    throws Exception
  {
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final AutomatonProxy aut = automata.iterator().next();
    final EventEncoding enc = createEventEncoding(des, aut);
    final ListBufferTransitionRelation rel =
      createTransitionRelation(aut, enc);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final AutomatonProxy before = rel.createAutomaton(factory, enc, null);
    reorder(rel, ordering);
    final AutomatonProxy after = rel.createAutomaton(factory, enc, null);
    checkResult(des, before, after, ordering);
  }


  //#########################################################################
  //# Auxiliary Methods
  private ListBufferTransitionRelation createTransitionRelation
    (final AutomatonProxy aut,
     final EventEncoding enc)
    throws OverflowException
  {
    final StateEncoding inputStateEnc = new StateEncoding(aut);
    final StateProxy dumpState = getState(aut, DUMP);
    ListBufferTransitionRelation rel;
    if (dumpState == null) {
      rel = new ListBufferTransitionRelation
        (aut, enc, inputStateEnc,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    } else {
      rel = new ListBufferTransitionRelation
        (aut, enc, inputStateEnc, dumpState,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }
    rel.checkReachability();
    return rel;
  }

  private void reorder
    (final ListBufferTransitionRelation rel,
     final StateReorderingTRSimplifier.StateOrdering ordering)
    throws AnalysisException
  {
    mSimplifier.setStateOrdering(ordering);
    mSimplifier.setTransitionRelation(rel);
    rel.checkIntegrity();
    @SuppressWarnings("unused")
    final boolean changed = mSimplifier.run();
//    assertEquals("Unexpected result from StateReorderingTRSimplifier",
//                 changeExpected, changed);
    rel.setName("result");
  }

  private void checkResult
    (final ProductDESProxy des,
     final AutomatonProxy aut,
     final AutomatonProxy result,
     final StateReorderingTRSimplifier.StateOrdering ordering)
    throws Exception
  {
    final String name = des.getName();
    final String basename =
      appendSuffixes(name, mBindings) + "-" + ordering.getConsoleName();
    final String comment =
      "Test output from " +
      ProxyTools.getShortClassName(mSimplifier) + '.';
    saveAutomaton(result, basename, comment);
    mIntegrityChecker.check(result, des);
    mIsomorphismChecker.checkIsomorphism(result, aut);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    compiler.setOptimizationEnabled(false);
  }

  @Override
  protected ProductDESProxy getCompiledDES
    (final List<ParameterBindingProxy> bindings, final String... path)
    throws Exception
  {
    mBindings = bindings;
    return super.getCompiledDES(bindings, path);
  }


  //#########################################################################
  //# Data Members
  private StateReorderingTRSimplifier mSimplifier;
  private ProductDESIntegrityChecker mIntegrityChecker;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;


  //#########################################################################
  //# Class Constants
  protected static final String DUMP = ":dump";

}
