//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.deadlock;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * @author Hani al-Bahri, Robi Malik
 */

public class AnnotatorTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AnnotatorTest()
  {
  }

  public AnnotatorTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    // mSimplifier = createTransitionRelationSimplifier();
    mIntegrityChecker = ProductDESIntegrityChecker.getInstance();
    mIsomorphismChecker = new IsomorphismChecker(factory, false, true);
  }

  @Override
  protected void tearDown() throws Exception
  {
    // mSimplifier = null;
    mIntegrityChecker = null;
    mIsomorphismChecker = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  /**
   * <P>Tests the model in file {supremica}/examples/waters/tests/annotation/
   * annotate_01.wmod.</P>
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
  public void test_annotate_01() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_annotate_02() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_02.wmod");
    runTransitionRelationSimplifier(des);
  }

  /*public void test_annotate_03() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_03.wmod");
    runTransitionRelationSimplifier(des);
  }*/

  public void test_annotate_04() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_annotate_05() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_annotate_06() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_06.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_annotate_07() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "annotation", "annotate_07.wmod");
    runTransitionRelationSimplifier(des);
  }
  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runTransitionRelationSimplifier(final ProductDESProxy des)
    throws Exception
  {

    getLogger().info("Checking " + des.getName() + " ...");
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    // calculate annotated form
    final GeneralizedTransitionRelation tr = annotate(before);
    // calculate unannotated form
    final AutomatonProxy unannotated = unannotate(tr, des);
    checkResult(des, unannotated);
    getLogger().info("Done " + des.getName());

  }


  //#########################################################################
  //# Auxiliary Methods
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
      final String comment = "Test output after annotation and unannotation";
//      final String comment =
//        "Test output from " +
//        ProxyTools.getShortClassName(mSimplifier) + '.';
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



 public GeneralizedTransitionRelation annotate(final AutomatonProxy aut){
    final GeneralizedTransitionRelation tr = new GeneralizedTransitionRelation(aut, null);
    final Annotator annotatedAutomaton= new Annotator(tr);
    annotatedAutomaton.run();
    return tr;
  }

  public AutomatonProxy unannotate(final GeneralizedTransitionRelation tr, final ProductDESProxy des){
    final UnAnnotator2 ua = new UnAnnotator2(tr, null);
   // final UnAnnotator ua = new UnAnnotator(tr);
    final AutomatonProxy aut = ua.run(getProductDESProxyFactory(), des);
    return aut;
  }


  //#########################################################################
  //# To be Provided by Subclasses


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
  // private TransitionRelationSimplifier mSimplifier;
  private ProductDESIntegrityChecker mIntegrityChecker;
  private IsomorphismChecker mIsomorphismChecker;
  private List<ParameterBindingProxy> mBindings;

  //#########################################################################
  //# Class Constants
  protected static final String TAU = "tau";

  protected static final String BEFORE = "before";
  protected static final String AFTER = "after";

}
