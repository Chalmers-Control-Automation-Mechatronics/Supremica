//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   ListBufferTransitionRelationTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A simple test for the {@link ListBufferTransitionRelation} class.
 * This test creates transition relations for a few simple automata,
 * converts them back to automata, and checks whether the result is
 * bisimulation equivalent to the original.
 *
 * @author Robi Malik
 */

public class ListBufferTransitionRelationTest extends
    AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(ListBufferTransitionRelationTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testSimpleBuild_SUCC_alpharemoval_1() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_1.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_2() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_2.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_3() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_3.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_4() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_4.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_5() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_5.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_6() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_6.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_7() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_7.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_8() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_8.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_9() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_9.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_10() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_10.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_11() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_11.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_12() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_12.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_13() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_13.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_14() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_14.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_15() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_15.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_16() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_16.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_SUCC_alpharemoval_17() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_17.wmod";
    testSimpleBuild(group, subdir, name, config);
  }


  public void testSimpleBuild_PRED_alpharemoval_1() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_1.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_2() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_2.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_3() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_3.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_4() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_4.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_5() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_5.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_6() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_6.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_7() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_7.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_8() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_8.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_9() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_9.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_10() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_10.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_11() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_11.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_12() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_12.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_13() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_13.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_14() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_14.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_15() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_15.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_16() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_16.wmod";
    testSimpleBuild(group, subdir, name, config);
  }

  public void testSimpleBuild_PRED_alpharemoval_17() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_17.wmod";
    testSimpleBuild(group, subdir, name, config);
  }


  public void testDirectTransitionRemoval_SUCC_alpharemoval_18()
    throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_18.wmod";
    final String autname = "before";
    testDirectTransitionRemoval(group, subdir, name, autname, config);
  }

  public void testDirectTransitionRemoval_PRED_koordwsp() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "valid";
    final String subdir = "central_locking";
    final String name = "koordwsp.wmod";
    final String autname = "decoder";
    testDirectTransitionRemoval(group, subdir, name, autname, config);
  }


  public void testIteratorTransitionRemoval_SUCC_alpharemoval_18()
    throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_18.wmod";
    final String autname = "before";
    testIteratorTransitionRemoval(group, subdir, name, autname, config);
  }

  public void testIteratorTransitionRemoval_PRED_koordwsp() throws Exception
  {
    final int config = ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    final String group = "valid";
    final String subdir = "central_locking";
    final String name = "koordwsp.wmod";
    final String autname = "decoder";
    testIteratorTransitionRemoval(group, subdir, name, autname, config);
  }

  public void testReachability()
  throws Exception
  {
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      ("test", ComponentKind.PLANT, 2, 0, 2,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.setInitial(0, true);
    rel.addTransition(0, 1, 0);
    assertTrue(rel.isReachable(0));
    assertTrue(rel.isReachable(1));
    rel.checkReachability();
    assertTrue(rel.isReachable(0));
    assertFalse(rel.isReachable(1));
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractAnalysisTest
  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
  }


  //#########################################################################
  //# Test Templates
  private void testSimpleBuild(final String group,
                               final String subdir,
                               final String desname,
                               final int config)
    throws Exception
  {
    final String autname = "before";
    final AutomatonProxy aut =
      getCompiledAutomaton(group, subdir, desname, autname);
    testSimpleBuild(desname, aut, config);
  }

  private void testSimpleBuild(final String desname,
                               final AutomatonProxy aut,
                               final int config)
    throws Exception
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding enc = new EventEncoding(aut, translator);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, enc, config);
    rel.setName("output");
    rel.checkIntegrity();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final AutomatonProxy result = rel.createAutomaton(factory, enc);
    final String comment = "Test output from " +
      ProxyTools.getShortClassName(this) + '.';
    saveAutomaton(result, desname, comment);
    final IsomorphismChecker checker = new IsomorphismChecker(factory, false, true);
    checker.checkIsomorphism(result, aut);
  }


  private void testDirectTransitionRemoval(final String group,
                                           final String subdir,
                                           final String desname,
                                           final String autname,
                                           final int config)
    throws Exception
  {
    final AutomatonProxy aut =
      getCompiledAutomaton(group, subdir, desname, autname);
    testDirectTransitionRemoval(desname, aut, config);
  }

  private void testDirectTransitionRemoval(final String desname,
                                           final AutomatonProxy aut,
                                           final int config)
    throws Exception
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final StateEncoding stateEnc = new StateEncoding(aut);
    final EventEncoding eventEnc = new EventEncoding(aut, translator);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, eventEnc, stateEnc, config);
    rel.checkIntegrity();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final IsomorphismChecker checker = new IsomorphismChecker(factory, false, true);
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final int numTrans1 = transitions.size() - 1;
    for (final TransitionProxy trans : transitions) {
      final StateProxy source = trans.getSource();
      final int sourceId = stateEnc.getStateCode(source);
      final EventProxy event = trans.getEvent();
      final int eventId = eventEnc.getEventCode(event);
      final StateProxy target = trans.getTarget();
      final int targetId = stateEnc.getStateCode(target);
      rel.removeTransition(sourceId, eventId, targetId);
      rel.checkIntegrity();
      final AutomatonProxy result1 = rel.createAutomaton(factory, eventEnc);
      final Collection<TransitionProxy> transitions1 =
        new ArrayList<TransitionProxy>(numTrans1);
      for (final TransitionProxy trans1 : transitions) {
        if (trans1 != trans) {
          transitions1.add(trans1);
        }
      }
      final AutomatonProxy aut1 = factory.createAutomatonProxy
        (autname, kind, events, states, transitions1);
      checker.checkIsomorphism(result1, aut1);
      final boolean added = rel.addTransition(sourceId, eventId, targetId);
      assertTrue("Unexpected result reporting failure to add transition!",
                 added);
      rel.checkIntegrity();
      final AutomatonProxy result2 = rel.createAutomaton(factory, eventEnc);
      checker.checkIsomorphism(result2, aut);
    }
  }

  private void testIteratorTransitionRemoval(final String group,
                                             final String subdir,
                                             final String desname,
                                             final String autname,
                                             final int config)
    throws Exception
  {
    final AutomatonProxy aut =
      getCompiledAutomaton(group, subdir, desname, autname);
    testIteratorTransitionRemoval(desname, aut, config);
  }

  private void testIteratorTransitionRemoval(final String desname,
                                             final AutomatonProxy aut,
                                             final int config)
    throws Exception
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final StateEncoding stateEnc = new StateEncoding(aut);
    final EventEncoding eventEnc = new EventEncoding(aut, translator);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final IsomorphismChecker checker = new IsomorphismChecker(factory, false, true);
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final int numTrans = transitions.size();
    final int numTrans1 = numTrans - 1;
    for (int t = 0; t < numTrans; t++) {
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc, stateEnc, config);
      rel.checkIntegrity();
      final TransitionIterator iter =
        rel.createAllTransitionsModifyingIterator();
      for (int i = 0; i <= t; i++) {
        final boolean more = iter.advance();
        assertTrue("Unexpected end of iteration!", more);
      }
      final int sourceId = iter.getCurrentSourceState();
      final StateProxy source = stateEnc.getState(sourceId);
      final int eventId = iter.getCurrentEvent();
      final EventProxy event = eventEnc.getProperEvent(eventId);
      final int targetId = iter.getCurrentTargetState();
      final StateProxy target = stateEnc.getState(targetId);
      iter.remove();
      rel.checkIntegrity();
      final AutomatonProxy result1 = rel.createAutomaton(factory, eventEnc);
      final Collection<TransitionProxy> transitions1 =
        new ArrayList<TransitionProxy>(numTrans1);
      for (final TransitionProxy trans : transitions) {
        if (trans.getSource() != source ||
            trans.getEvent() != event ||
            trans.getTarget() != target) {
          transitions1.add(trans);
        }
      }
      final AutomatonProxy aut1 = factory.createAutomatonProxy
        (autname, kind, events, states, transitions1);
      checker.checkIsomorphism(result1, aut1);
      final boolean added = rel.addTransition(sourceId, eventId, targetId);
      assertTrue("Unexpected result reporting failure to add transition!",
                 added);
      rel.checkIntegrity();
      final AutomatonProxy result2 = rel.createAutomaton(factory, eventEnc);
      checker.checkIsomorphism(result2, aut);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private AutomatonProxy getCompiledAutomaton(final String group,
                                              final String subdir,
                                              final String desname,
                                              final String autname)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(group, subdir, desname);
    return findAutomaton(des, autname);
  }

}
