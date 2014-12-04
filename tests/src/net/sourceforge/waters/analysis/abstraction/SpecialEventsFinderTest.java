//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SpecialEventsFinderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
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

  public void testSelfloopOnlyFind1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloopOnlyFind01.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    final SpecialEventsFinder finder = new SpecialEventsFinder();
    finder.setBlockedEventsDetected(true);
    finder.setFailingEventsDetected(true);
    finder.setSelfloopOnlyEventsDetected(true);
    finder.setAlwaysEnabledEventsDetected(true);
    return finder;
  }

  @Override
  protected SpecialEventsFinder getTransitionRelationSimplifier()
  {
    return (SpecialEventsFinder) super.getTransitionRelationSimplifier();
  }

  @Override
  protected byte[] getEventStatusReadFromAttributes()
  {
    return null;
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
    final EventEncoding enc = createEventEncoding(des, before);
    final StateEncoding inputStateEnc = new StateEncoding(before);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(before, enc, inputStateEnc, config);
    final SpecialEventsFinder finder = getTransitionRelationSimplifier();
    finder.setTransitionRelation(rel);
    configureTransitionRelationSimplifier();
    final boolean result = finder.run();
    assertFalse("Unexpected 'true' result from SpecialEventsFinder!", result);
    final byte[] computedStatus = finder.getComputedEventStatus();
    checkResult(enc, computedStatus);
  }

  private void checkResult(final EventEncoding enc,
                           final byte[] computedStatus)
  {
    final int numEvents = enc.getNumberOfProperEvents();
    assertEquals("Computed status array length does not match event encoding!",
                 numEvents, computedStatus.length);
    for (int e = EventEncoding.TAU; e < numEvents; e++) {
      final EventProxy event = enc.getProperEvent(e);
      if (event != null) {
        final Map<String,String> attribs = event.getAttributes();
        for (final byte flag : STATUS_FROM_ATTRIBUTES) {
          final String name = EventStatus.getStatusName(flag);
          final boolean expected = attribs.containsKey(name);
          final boolean computed = (computedStatus[e] & flag) != 0;
          assertEquals("Unexpected result for flag " + name +
                       " of event " + event.getName() + "!",
                       expected, computed);
        }
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private static final byte[] STATUS_FROM_ATTRIBUTES = {
    EventStatus.STATUS_SELFLOOP_ONLY,
    EventStatus.STATUS_ALWAYS_ENABLED,
    EventStatus.STATUS_BLOCKED,
    EventStatus.STATUS_FAILING
  };

}
