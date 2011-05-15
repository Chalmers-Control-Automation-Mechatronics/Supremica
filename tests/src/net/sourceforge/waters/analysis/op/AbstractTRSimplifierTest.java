//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AbstractTransitionRelationSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.io.File;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
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
    mIsomorphismChecker = new IsomorphismChecker(factory, false);
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
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/abstraction/
   * empty_1.wmod.
   * </P>
   *
   * <P>
   * All test modules contain up to two automata, named "before" and "after".
   * The automaton named "before" is required to be present, and defines the
   * input automaton for the abstraction rule. The automaton "after" defines the
   * expected result of abstraction. It may be missing, in which case the
   * abstraction should have no effect and return the unchanged input automaton
   * (the test expects the same object, not an identical copy).
   * </P>
   *
   * <P>
   * The names of critical events are expected to be "tau", ":alpha", and
   * ":accepting", respectively.
   * </P>
   *
   * <P>
   * After running the test, any automaton created by the rule is saved in
   * {supremica}/logs/results/analysis/op/{classname} as a .des file
   * (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_empty_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "empty_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runTransitionRelationSimplifier
    (final String group, final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runTransitionRelationSimplifier(groupdir, name, bindings);
  }

  protected void runTransitionRelationSimplifier
    (final String group, final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runTransitionRelationSimplifier(groupdir, subdir, name, bindings);
  }

  protected void runTransitionRelationSimplifier
    (final File groupdir, final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runTransitionRelationSimplifier(dir, name, bindings);
  }

  protected void runTransitionRelationSimplifier
    (final File dir, final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File filename = new File(dir, name);
    runTransitionRelationSimplifier(filename, bindings);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runTransitionRelationSimplifier(final String group,
                                                 final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runTransitionRelationSimplifier(groupdir, name);
  }

  protected void runTransitionRelationSimplifier(final String group,
                                                 final String subdir,
                                                 final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runTransitionRelationSimplifier(groupdir, subdir, name);
  }

  protected void runTransitionRelationSimplifier(final File groupdir,
                                                 final String subdir,
                                                 final String name)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runTransitionRelationSimplifier(dir, name);
  }

  protected void runTransitionRelationSimplifier(final File dir,
                                                 final String name)
  throws Exception
  {
    final File filename = new File(dir, name);
    runTransitionRelationSimplifier(filename);
  }

  protected void runTransitionRelationSimplifier(final File filename)
  throws Exception
  {
    final List<ParameterBindingProxy> empty = null;
    runTransitionRelationSimplifier(filename, empty);
  }

  protected void runTransitionRelationSimplifier
    (final File filename,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runTransitionRelationSimplifier(final ProductDESProxy des)
  throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final AutomatonProxy result = applySimplifier(des, before);
    checkResult(des, result);
    getLogger().info("Done " + des.getName());
  }

  private AutomatonProxy applySimplifier(final ProductDESProxy des,
                                         final AutomatonProxy aut)
  throws Exception
  {
    final EventEncoding eventEnc = createEventEncoding(des, aut);
    final StateEncoding inputStateEnc = new StateEncoding(aut);
    final int config = mSimplifier.getPreferredConfiguration();
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc, config);
    mSimplifier.setTransitionRelation(rel);
    configureTransitionRelationSimplifier();
    if (mSimplifier.run()) {
      rel.setName("result");
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
      rel.removeRedundantPropositions();
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
    createTransitionRelationSimplifier();

  /**
   * Creates an event encoding for use with the given product DES and
   * automaton. This method is called automatically and should be overridden
   * by subclasses that need more specific implementation.
   */
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getEvent(des, TAU);
    return new EventEncoding(aut, translator, tau);
  }

  /**
   * Creates an event encoding for use with the given product DES and
   * automaton, with alpha and omega propositions added. This method is
   * provided as a convenience to subclasses needing to override {@link
   * #createEventEncoding(ProductDESProxy, AutomatonProxy)
   * createEventEncoding()}.
   */
  protected EventEncoding createEventEncodingWithPropositions
    (final ProductDESProxy des, final AutomatonProxy aut)
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getEvent(des, TAU);
    final EventEncoding enc = new EventEncoding(aut, translator, tau);
    final EventProxy alpha = getEvent(des, ALPHA);
    mAlphaID = enc.getEventCode(alpha);
    if (alpha != null && mAlphaID < 0) {
      mAlphaID = enc.addEvent(alpha, translator, true);
    }
    final EventProxy omega = getEvent(des, OMEGA);
    mOmegaID = enc.getEventCode(omega);
    if (omega != null && mOmegaID < 0) {
      mOmegaID = enc.addEvent(omega, translator, true);
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
    simplifier.setPropositions(mAlphaID, mOmegaID);
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
  final String TAU = "tau";
  final String ALPHA = ":alpha";
  final String OMEGA = EventDeclProxy.DEFAULT_MARKING_NAME;

  private final String BEFORE = "before";
  private final String AFTER = "after";

}
