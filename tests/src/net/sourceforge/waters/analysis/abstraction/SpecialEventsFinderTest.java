//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SpecialEventsFinderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


public class SpecialEventsFinderTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(SpecialEventsFinderTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testAlwaysEnabledFind1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "alwaysEnabledFind01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testAlwaysEnabledFind2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "alwaysEnabledFind02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testAlwaysEnabledFind3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "alwaysEnabledFind03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testAlwaysEnabledFind4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "alwaysEnabledFind04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testAlwaysEnabledFind5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "alwaysEnabledFind05.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    final SpecialEventsFinder finder = new SpecialEventsFinder();
    finder.setAlwaysEnabledEventsDetected(true);
    return finder;
  }

  @Override
  protected SpecialEventsFinder getTransitionRelationSimplifier()
  {
    return (SpecialEventsFinder) super.getTransitionRelationSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
   throws OverflowException
  {
    return createEventEncodingWithPropositions(des, aut);
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }

  @Override
  protected void runTransitionRelationSimplifier(final ProductDESProxy des)
    throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    runTransitionRelationSimplifier
      (des, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    runTransitionRelationSimplifier
      (des, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    getLogger().info("Done " + des.getName());
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void runTransitionRelationSimplifier(final ProductDESProxy des,
                                                 final int config)
    throws Exception
  {
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final Collection<EventProxy> events = applySimplifier(des, before, config);
    checkResult(des, events);
  }

  private Collection<EventProxy> applySimplifier(final ProductDESProxy des,
                                                 final AutomatonProxy aut,
                                                 final int config)
    throws AnalysisException
  {
    final EventEncoding eventEnc = createEventEncoding(des, aut);
    final StateEncoding inputStateEnc = new StateEncoding(aut);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc, config);
    final SpecialEventsFinder finder = getTransitionRelationSimplifier();
    finder.setTransitionRelation(rel);
    configureTransitionRelationSimplifier();
    final boolean result = finder.run();
    assertFalse("Unexpected 'true' result from SpecialEventsFinder!", result);
    final byte[] computedStatus = finder.getComputedEventStatus();
    final List<EventProxy> events = new ArrayList<>(computedStatus.length);
    for (int e = 0; e < computedStatus.length; e++) {
      if (EventStatus.isOutsideAlwaysEnabledEvent(computedStatus[e])) {
        final EventProxy event = eventEnc.getProperEvent(e);
        events.add(event);
      }
    }
    return events;
  }

  private void checkResult(final ProductDESProxy des,
                           final Collection<EventProxy> events)
  {
    for (final EventProxy event : events) {
      final Map<String,String> attribs = event.getAttributes();
      assertTrue("SpecialEventsFinder incorrectly asserts event '" +
                 event.getName() + "' to be always enabled!",
                 attribs.containsKey(KEY));
    }
    for (final EventProxy event : des.getEvents()) {
      final Map<String,String> attribs = event.getAttributes();
      if (attribs.containsKey(KEY)) {
        assertTrue("SpecialEventsFinder did not find always enabled event '" +
                   event.getName() + "'!",
                   events.contains(event));
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private static final String KEY = "AE";

}
