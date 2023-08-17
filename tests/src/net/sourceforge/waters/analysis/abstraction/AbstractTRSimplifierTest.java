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

package net.sourceforge.waters.analysis.abstraction;

import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * @author Robi Malik
 */

public abstract class AbstractTRSimplifierTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractTRSimplifierTest()
  {
  }

  public AbstractTRSimplifierTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSimplifier = createTransitionRelationSimplifier();
    mIntegrityChecker = ProductDESIntegrityChecker.getInstance();
    mIsomorphismChecker = new IsomorphismChecker(factory, false, true);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mSimplifier = null;
    mIntegrityChecker = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  /**
   * <P>Tests the model in file {supremica}/examples/waters/tests/abstraction/
   * empty_1.wmod.</P>
   *
   * <P>All test modules contain up to two automata, named "before" and "after".
   * The automaton named "before" is required to be present, and defines the
   * input automaton for the abstraction rule. The automaton "after" defines the
   * expected result of abstraction. It may be missing, in which case the
   * abstraction should have no effect and return the unchanged input automaton
   * (the test expects the same object, not an identical copy).</P>
   *
   * <P>The names of critical events are expected to be "tau", ":alpha", and
   * ":accepting", respectively. In addition, a state called ":dump" is set
   * to be the dump state of the input transition relation.</P>
   *
   * <P>After running the test, any automaton created by the rule is saved in
   * {supremica}/logs/results/analysis/op/{classname} as a .des file
   * (for text viewing) and as a .wmod file (to load into the IDE).</P>
   */
  public void test_empty_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "empty_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_empty_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "empty_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_4.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_5.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_6.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_basic_7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "basic_7.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runTransitionRelationSimplifier(final String... path)
    throws Exception
  {
    runTransitionRelationSimplifier(null, path);
  }

  protected void runTransitionRelationSimplifier
    (final List<ParameterBindingProxy> bindings, final String... path)
    throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(bindings, path);
    runTransitionRelationSimplifier(des);
  }

  protected void runTransitionRelationSimplifier(final ProductDESProxy des)
    throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final AutomatonProxy resultSucc = applySimplifier
      (des, before, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    checkResult(des, resultSucc);
    final AutomatonProxy resultPred = applySimplifier
      (des, before, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    checkResult(des, resultPred);
    getLogger().info("Done " + des.getName());
  }


  //#########################################################################
  //# Auxiliary Methods
  private AutomatonProxy applySimplifier(final ProductDESProxy des,
                                         final AutomatonProxy aut,
                                         final int config)
  throws Exception
  {
    final EventEncoding eventEnc = createEventEncoding(des, aut);
    final StateEncoding inputStateEnc = new StateEncoding(aut);
    final StateProxy dumpState = getState(aut, DUMP);
    ListBufferTransitionRelation rel;
    if (dumpState == null) {
      rel = new ListBufferTransitionRelation
        (aut, eventEnc, inputStateEnc,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    } else {
      rel = new ListBufferTransitionRelation
        (aut, eventEnc, inputStateEnc, dumpState,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }
    rel.checkReachability();
    rel.reconfigure(config);
    mSimplifier.setTransitionRelation(rel);
    configureTransitionRelationSimplifier();
    if (mSimplifier.run()) {
      rel = mSimplifier.getTransitionRelation();
      rel.setName("result");
      rel.checkIntegrity();
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      return rel.createAutomaton(factory, eventEnc, null);
    } else {
      return null;
    }
  }

  private void checkResult(final ProductDESProxy des,
                           final AutomatonProxy result)
  throws Exception
  {
    final AutomatonProxy expected = getAutomaton(des, AFTER);
    if (result == null) {
      assertNull("Simplifier reports no change, " +
                 "but the input can be simplified!", expected);
    } else {
      final String name = des.getName();
      final String basename = appendSuffixes(name, mBindings);
      final String comment =
        "Test output from " +
        ProxyTools.getShortClassName(mSimplifier) + '.';
      saveAutomaton(result, basename, comment);
      mIntegrityChecker.check(result, des);
      if (expected == null) {
        assertNull("Test expects no change, " +
                   "but the simplifier reports some change!",
                   result);
      } else {
        mIsomorphismChecker.checkIsomorphism(result, expected);
      }
    }
  }


  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the transition simplifier to be tested.
   */
  protected abstract TransitionRelationSimplifier
    createTransitionRelationSimplifier() throws AnalysisException;

  /**
   * Creates an event encoding for use with the given product DES and
   * automaton. This method is called automatically and should be overridden
   * by subclasses that need a more specific implementation.
   */
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding enc = new EventEncoding();
    EventProxy tau = getEvent(aut, TAU);
    if (tau != null) {
      enc.addSilentEvent(tau);
    } else {
      tau = getEvent(des, TAU);
      if (tau != null) {
        enc.setTauEvent(tau);
      }
    }
    for (final EventProxy event : aut.getEvents()) {
      final byte status = getEventStatusFromAttributes(event);
      enc.addEvent(event, translator, status);
    }
    final EventProxy alpha = getEvent(des, ALPHA);
    if (alpha != null) {
      mAlphaID = enc.addEvent(alpha, translator, EventStatus.STATUS_UNUSED);
    } else {
      mAlphaID = -1;
    }
    final EventProxy omega = getEvent(des, OMEGA);
    if (omega != null) {
      mOmegaID = enc.addEvent(omega, translator, EventStatus.STATUS_UNUSED);
    } else {
      mOmegaID = -1;
    }
    return enc;
  }

  /**
   * Provides any additional parameters needed to the transition relation
   * simplifier. This method is called automatically and should be overridden
   * by subclasses that need more specific implementation.
   */
  protected void configureTransitionRelationSimplifier()
  {
  }

  /**
   * Provides the IDs of alpha and omega propositions to the transition
   * relation simplifier. This method is provided as a convenience to
   * subclasses needing to override {@link
   * #configureTransitionRelationSimplifier()}.
   * @throws ClassCastException to indicate that the transition simplifier
   *         is not of type {@link AbstractMarkingTRSimplifier}.
   */
  protected void configureTransitionRelationSimplifierWithPropositions()
  {
    final AbstractMarkingTRSimplifier simplifier =
      (AbstractMarkingTRSimplifier) mSimplifier;
    simplifier.setMarkings(mAlphaID, mOmegaID);
  }

  /**
   * Retrieves the transition relation simplifier used by this test.
   */
  protected TransitionRelationSimplifier getTransitionRelationSimplifier()
  {
    return mSimplifier;
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


  //#########################################################################
  //# Data Members
  private TransitionRelationSimplifier mSimplifier;
  private ProductDESIntegrityChecker mIntegrityChecker;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;

  private int mAlphaID;
  private int mOmegaID;


  //#########################################################################
  //# Class Constants
  protected static final String TAU = "tau";
  protected static final String ALPHA = ":alpha";
  protected static final String OMEGA = EventDeclProxy.DEFAULT_MARKING_NAME;
  protected static final String DUMP = ":dump";

  protected static final String BEFORE = "before";
  protected static final String AFTER = "after";

}
