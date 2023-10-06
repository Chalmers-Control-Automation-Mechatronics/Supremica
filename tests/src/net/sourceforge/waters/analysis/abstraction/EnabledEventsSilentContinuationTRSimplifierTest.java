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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the <I>Silent Continuation Rule</I>.
 *
 * @author Robi Malik
 */

public class EnabledEventsSilentContinuationTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(EnabledEventsSilentContinuationTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest
  @Override
  protected EnabledEventsSilentContinuationTRSimplifier createTransitionRelationSimplifier()
  {
    return new EnabledEventsSilentContinuationTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getEvent(aut, TAU);
    final int numEvents = aut.getEvents().size();
    final Collection<EventProxy> events = new ArrayList<>(numEvents);
    int uncontrollableCount = 0;
    for (final EventProxy event : aut.getEvents()) {
      //see if controllable
      if (event.getKind() == EventKind.UNCONTROLLABLE) {
        //put in the order you want to encode
        //don't need to worry about tau because of the constructor
        events.add(event);
        if (event != tau) {
          uncontrollableCount++;
        }
      }
    }
    for (final EventProxy event : aut.getEvents()) {
      //see if controllable
      if (event.getKind() != EventKind.UNCONTROLLABLE) {
        //put in the order you want to encode
        events.add(event);
      }
    }
    //returns a list where all uncontrollable events are first
    final EnabledEventsSilentContinuationTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setNumberOfEnabledEvents(uncontrollableCount);
    return new EventEncoding(events, translator, tau);
  }

  @Override
  protected EnabledEventsSilentContinuationTRSimplifier getTransitionRelationSimplifier()
  {
    return (EnabledEventsSilentContinuationTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_silentContinuation_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentContinuation_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentContinuation_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentContinuation_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentContinuation01()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation01.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation02()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation02.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation03()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation03.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation04()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation04.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation05()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation05.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation06()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation06.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation07()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation07.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation08()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation08.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_silentContinuation_1();
    test_silentContinuation_2();
    test_silentContinuation_1();
    test_silentContinuation_2();
  }

}
