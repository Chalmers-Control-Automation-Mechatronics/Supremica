//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
  protected byte getEventStatusFromAttributes(final EventProxy event)
  {
    return EventStatus.STATUS_NONE;
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
        for (final byte flag : CHECKED_ATTRIBUTES) {
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
  private static final byte[] CHECKED_ATTRIBUTES = {
    EventStatus.STATUS_SELFLOOP_ONLY,
    EventStatus.STATUS_ALWAYS_ENABLED,
    EventStatus.STATUS_BLOCKED,
    EventStatus.STATUS_FAILING
  };

}
