//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractGeneralisedConflictCheckerTest
//###########################################################################
//# $Id: AbstractGeneralisedConflictCheckerTest.java 4768 2009-10-09 03:16:33Z robi $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.despot.HISCAttributes;
import net.sourceforge.waters.despot.SICPropertyVBuilder;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractGeneralisedConflictCheckerTest extends
    AbstractConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractGeneralisedConflictCheckerTest()
  {
  }

  public AbstractGeneralisedConflictCheckerTest(final String name)
  {
    super(name);
  }


  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mBuilder = new SICPropertyVBuilder(factory);
  }

  protected void tearDown() throws Exception
  {
    mBuilder = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases --- paper (multi-coloured automata)
  public void testG1() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g1.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testG2() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g2.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testG3() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g3.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testG4() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g4.wmod";
    runModelVerifier(group, dir, name, false);
  }


  //#########################################################################
  //# Test Cases --- SIC Property V
  public void testSIC5__hisc8_low2__a1() throws Exception
  {
    testSICPropertyV("despot", "testHISC", "hisc8_low2.wmod", "a1", true);
  }

  public void testSIC5__hisc8_low2__a2_2() throws Exception
  {
    testSICPropertyV("despot", "testHISC", "hisc8_low2.wmod", "a2:2", false);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected void configureModelVerifier(final ProductDESProxy des)
  {
    super.configureModelVerifier(des);
    final Set<EventProxy> events = des.getEvents();
    // checks that this des does include the precondition marking :alpha
    for (final EventProxy event : events) {
      if (event.getName().equals(":alpha") &&
          event.getKind() == EventKind.PROPOSITION) {
        mAlpha = event;
        final ConflictChecker modelVer = getModelVerifier();
        modelVer.setGeneralisedPrecondition(event);
        return;
      }
    }
    fail("Model '" + des.getName() +
         "' does not contain a proposition named :alpha.");
  }

  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final ArrayList<String> propositions = new ArrayList<String>(2);
    propositions.add(":alpha");
    propositions.add(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(propositions);
  }

  /**
   * Checks the correctness of a conflict counterexample. A conflict
   * counterexample has to be a {@link ConflictTraceProxy}, its event sequence
   * has to be accepted by all automata in the given model, and it must take the
   * model to a blocking state which has the precondition marking :alpha. The
   * latter condition is checked by means of a language inclusion check.
   *
   * @see AbstractModelVerifierTest#checkCounterExample(ProductDESProxy,TraceProxy)
   * @see #createLanguageInclusionChecker(ProductDESProxy,ProductDESProxyFactory)
   */
  protected void checkCounterExample(final ProductDESProxy des,
      final TraceProxy trace) throws Exception
  {
    super.checkCounterExample(des, trace);
    // checks if the marking proposition :alpha is in the alphabet
    final Set<EventProxy> alphabet = des.getEvents();
    if (alphabet.contains(mAlpha)) {
      final Map<AutomatonProxy,StateProxy> endState = getEndState(des, trace);
      boolean marked = false;
      for (final AutomatonProxy aut : endState.keySet()) {
        final StateProxy state = endState.get(aut);
        for (final EventProxy proposition : state.getPropositions()) {
          if (proposition.equals(mAlpha)) {
            marked = true;
            break;
          }
        }
        if (!marked) {
          fail("Counterexample leads to an end state where automaton "
              + aut.getName() + " is in state " + state.getName()
              + " which does not contain the proposition named :alpha");
          ;
          return;
        }
      }
    }
  }


  //#########################################################################
  //# Testing SIC Property V
  private void testSICPropertyV(final String group,
                                final String subdir,
                                final String fileName,
                                final String eventName,
                                final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    final File dir = new File(groupdir, subdir);
    final File fullName = new File(dir, fileName);
    final ProductDESProxy origDES = getCompiledDES(fullName);
    mBuilder.setInputModel(origDES);
    final EventProxy answer = findAnswerEvent(origDES, eventName);
    final ProductDESProxy answerDES = mBuilder.createModelForAnswer(answer);
    final DocumentManager docman = getDocumentManager();
    final ProxyMarshaller<ProductDESProxy> marshaller =
      docman.findProxyMarshaller(ProductDESProxy.class);
    final String ext = marshaller.getDefaultExtension();
    final File outdir = getOutputDirectory();
    final String outname = answerDES.getName();
    final File outfile = new File(outdir, outname + ext);
    docman.saveAs(answerDES, outfile);
    runModelVerifier(answerDES, expect);
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventProxy findAnswerEvent(final ProductDESProxy des,
                                     final String eventName)
  {
    final EventProxy event = findEvent(des, eventName);
    final Map<String,String> attribs = event.getAttributes();
    if (HISCAttributes.getEventType(attribs) !=
        HISCAttributes.EventType.ANSWER) {
      fail("The event '" + eventName + "' in model '" + des.getName() +
           "'is not an answer event!");
    }
    return event;
  }

  private Map<AutomatonProxy,StateProxy> getEndState(final ProductDESProxy des,
      final TraceProxy trace)
  {
    final ConflictTraceProxy counterexample = (ConflictTraceProxy) trace;
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int size = automata.size();
    final Map<AutomatonProxy,StateProxy> tuple =
        new HashMap<AutomatonProxy,StateProxy>(size);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = checkCounterExample(aut, counterexample);
      tuple.put(aut, state);
    }
    return tuple;
  }


  //#########################################################################
  //# Data Members
  private SICPropertyVBuilder mBuilder;
  private EventProxy mAlpha = null;

}
