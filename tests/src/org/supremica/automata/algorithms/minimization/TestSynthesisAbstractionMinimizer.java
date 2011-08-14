//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Supremica Algorithms
//# PACKAGE: org.supremica.automata.algorithms.minimization
//# CLASS:   TestSynthesisAbstractionMinimizer
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.minimization;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.algorithms.EquivalenceRelation;


/**
 * A test for synthesis abstraction.
 * This class tests the {@link AutomatonMinimizer} in configuration
 * {@link EquivalenceRelation#SYNTHESISABSTRACTION}.
 *
 * @author Robi Malik
 */

public class TestSynthesisAbstractionMinimizer
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(TestSynthesisAbstractionMinimizer.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final DocumentManager docman = getDocumentManager();
    mAutomatonBuilder = new ProjectBuildFromWaters(docman);
    mMinimizationOptions = MinimizationOptions.getDefaultMinimizationOptions();
    mMinimizationOptions.setAlsoTransitions(true);
    mMinimizationOptions.setMinimizationType
      (EquivalenceRelation.SYNTHESISABSTRACTION);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mIsomorphismChecker = new IsomorphismChecker(factory, true);
    setNodeLimit();
  }

  protected void tearDown()
    throws Exception
  {
    mAutomatonBuilder = null;
    mAutomatonMinimizer = null;
    mMinimizationOptions = null;
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
   * After running the test, any automaton created by the rule is saved in
   * {supremica}/logs/results/algorithms/minimization/{classname} as a .des file
   * (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_empty_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "empty_1.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  /*
   * Commenting this one out for now---causes VM crash :-(
  public void test_empty_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "empty_2.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }
   */

  public void test_basic_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_1.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_basic_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_2.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_basic_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_3.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_basic_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_4.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_basic_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_5.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_basic_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_6.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_basic_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_basic_7.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_synthesisAbstraction_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_1.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_synthesisAbstraction_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_2.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_synthesisAbstraction_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_3.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }

  public void test_synthesisAbstraction_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_4.wmod";
    runAutomatonMinimizer(group, subdir, name);
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runAutomatonMinimizer
    (final String group,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonMinimizer(groupdir, name, bindings);
  }

  protected void runAutomatonMinimizer
    (final String group,
     final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonMinimizer(groupdir, subdir, name, bindings);
  }

  protected void runAutomatonMinimizer
    (final File groupdir,
     final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runAutomatonMinimizer(dir, name, bindings);
  }

  protected void runAutomatonMinimizer
    (final File dir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File filename = new File(dir, name);
    runAutomatonMinimizer(filename, bindings);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runAutomatonMinimizer(final String group,
                                       final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonMinimizer(groupdir, name);
  }

  protected void runAutomatonMinimizer(final String group,
                                       final String subdir,
                                       final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runAutomatonMinimizer(groupdir, subdir, name);
  }

  protected void runAutomatonMinimizer(final File groupdir,
                                       final String subdir,
                                       final String name)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runAutomatonMinimizer(dir, name);
  }

  protected void runAutomatonMinimizer(final File dir, final String name)
  throws Exception
  {
    final File filename = new File(dir, name);
    runAutomatonMinimizer(filename);
  }

  protected void runAutomatonMinimizer(final File filename)
  throws Exception
  {
    runAutomatonMinimizer(filename, (List<ParameterBindingProxy>) null);
  }

  protected void runAutomatonMinimizer
    (final File filename,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runAutomatonMinimizerWithBindings(des);
  }

  protected void runAutomatonMinimizer(final ProductDESProxy des)
  throws Exception
  {
    runAutomatonMinimizer(des, null);
  }

  protected void runAutomatonMinimizer
    (final ProductDESProxy des,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    mBindings = bindings;
    runAutomatonMinimizerWithBindings(des);
  }

  protected MinimizationOptions getMinimizationOptions()
  {
    return mMinimizationOptions;
  }

  protected IsomorphismChecker getIsomorphismChecker()
  {
    return mIsomorphismChecker;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void runAutomatonMinimizerWithBindings(final ProductDESProxy des)
  throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    final AutomatonProxy beforeW = findAutomaton(des, BEFORE);
    final Automaton beforeS = mAutomatonBuilder.build(beforeW);
    mAutomatonMinimizer = new AutomatonMinimizer(beforeS);
    final Alphabet alphabet = beforeS.getAlphabet();
    final Alphabet hidden = new Alphabet();
    for (final LabeledEvent event : alphabet) {
      if (!event.isObservable()) {
        event.setObservable(true);
        hidden.add(event);
      }
    }
    final Automaton result =
      mAutomatonMinimizer.getMinimizedAutomaton(mMinimizationOptions, hidden);
    result.setName("result");
    checkResult(des, result);
    getLogger().info("Done " + des.getName());
  }

  private void checkResult(final ProductDESProxy des,
                           final AutomatonProxy result)
  throws WatersMarshalException, IOException, AnalysisException, ParseException
  {
    final String name = des.getName();
    final String basename = appendSuffixes(name, mBindings);
    final String comment = "Test output from " +
      ProxyTools.getShortClassName(mAutomatonMinimizer) + '.';
    saveAutomaton(result, basename, comment);
    final AutomatonProxy after = getAutomaton(des, AFTER);
    final AutomatonProxy expected =
      after == null ? findAutomaton(des, BEFORE) : after;
    mIsomorphismChecker.checkIsomorphism(result, expected);
  }

  private void setNodeLimit()
  {
    final String prop = System.getProperty("waters.analysis.statelimit");
    if (prop != null) {
      final int limit = Integer.parseInt(prop);
      mMinimizationOptions.setComponentSizeLimit(limit);
    }
  }


  //#########################################################################
  //# Data Members
  private ProjectBuildFromWaters mAutomatonBuilder;
  private AutomatonMinimizer mAutomatonMinimizer;
  private MinimizationOptions mMinimizationOptions;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;


  //#########################################################################
  //# Class Constants
  private final String BEFORE = "before";
  private final String AFTER = "after";

}
