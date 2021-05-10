//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.des;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import junit.framework.Assert;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.marshaller.DocumentIntegrityChecker;


public class ProductDESIntegrityChecker
  extends DocumentIntegrityChecker<ProductDESProxy>
{

  //#########################################################################
  //# Singleton Pattern
  public static ProductDESIntegrityChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final ProductDESIntegrityChecker INSTANCE =
      new ProductDESIntegrityChecker();
  }


  //#########################################################################
  //# Constructor
  protected ProductDESIntegrityChecker()
  {
  }


  //#########################################################################
  //# Invocation
  public void check(final ProductDESProxy des)
    throws Exception
  {
    super.check(des);
    checkProductDESIntegrity(des);
  }

  public void check(final AutomatonProxy aut, final ProductDESProxy des)
    throws Exception
  {
    try {
      mProductDES = des;
      final Set<EventProxy> events = des.getEvents();
      mProductDESEventSet = new THashSet<EventProxy>(events);
      checkAutomatonIntegrity(aut);
    } finally {
      mProductDES = null;
      mProductDESEventSet = null;
    }
  }


  //#########################################################################
  //# Integrity Checking
  private void checkProductDESIntegrity(final ProductDESProxy des)
  {
    try {
      mProductDES = des;
      final Set<EventProxy> events = des.getEvents();
      final int numevents = events.size();
      final Set<String> names = new THashSet<String>(numevents);
      mProductDESEventSet = new THashSet<EventProxy>(numevents);
      for (final EventProxy event : events) {
        final String name = event.getName();
        if (names.add(name)) {
          mProductDESEventSet.add(event);
        } else {
          Assert.fail("The product DES '" + des.getName() +
                      "' declares two events with the name '" + name + "'!");
        }
      }
      for (final AutomatonProxy aut : des.getAutomata()) {
        checkAutomatonIntegrity(aut);
      }
    } finally {
      mProductDES = null;
      mProductDESEventSet = null;
    }
  }

  private void checkAutomatonIntegrity(final AutomatonProxy aut)
  {
    try {
      mAutomaton = aut;
      for (final EventProxy event : aut.getEvents()) {
        if (!mProductDESEventSet.contains(event)) {
          Assert.fail("The event '" + event.getName() +
                      "' found in the automaton alphabet of '" +
                      aut.getName() +
                      "' is not declared in the product DES '" +
                      mProductDES.getName() + "'!");
        }
      }
      mAutomatonEventSet = new THashSet<EventProxy>(aut.getEvents());
      final Set<StateProxy> states = aut.getStates();
      final int numstates = states.size();
      final Set<String> names = new THashSet<String>(numstates);
      mAutomatonStateSet = new THashSet<StateProxy>(numstates);
      for (final StateProxy state : states) {
        final String name = state.getName();
        if (names.add(name)) {
          checkStateIntegrity(state);
          mAutomatonStateSet.add(state);
        } else {
          Assert.fail("The automaton '" + aut.getName() +
                      "' in the product DES '" + mProductDES.getName() +
                      "' contains two states with the name '" + name + "'!");
        }
      }
      for (final TransitionProxy trans : aut.getTransitions()) {
        checkTransitionIntegrity(trans);
      }
    } finally {
      mAutomaton = null;
      mAutomatonEventSet = null;
      mAutomatonStateSet = null;
    }
  }

  private void checkStateIntegrity(final StateProxy state)
  {
    for (final EventProxy prop : state.getPropositions()) {
      if (prop.getKind() != EventKind.PROPOSITION) {
        Assert.fail("The event '" + prop.getName() +
                    "' found in the proposition list of state '" +
                    state.getName() + "' in automaton '" +
                    mAutomaton.getName() + "' is not a PROPOSITION!");
      } else if (!mAutomatonEventSet.contains(prop)) {
        Assert.fail("The proposition '" + prop.getName() +
                    "' found in the proposition list of state '" +
                    state.getName() + "' in automaton '" +
                    mAutomaton.getName() +
                    "' is not in the automaton alphabet!");
      }
    }
  }

  private void checkTransitionIntegrity(final TransitionProxy trans)
  {
    final StateProxy source = trans.getSource();
    final EventProxy event = trans.getEvent();
    final EventKind ekind = event.getKind();
    final StateProxy target = trans.getTarget();
    if (!mAutomatonStateSet.contains(source)) {
      Assert.fail("The source state of the transition {" +
                  source.getName() + " -[" + event.getName() + "]-> " +
                  target.getName() + "} in automaton '" +
                  mAutomaton.getName() +
                  " is not in the automaton state set!");
    } else if (ekind != EventKind.CONTROLLABLE &&
               ekind != EventKind.UNCONTROLLABLE) {
      Assert.fail("The event of the transition {" +
                  source.getName() + " -[" + event.getName() + "]-> " +
                  target.getName() + "} in automaton '" +
                  mAutomaton.getName() +
                  " is neither CONTROLLABLE nor UNCONTROLLABLE!");
    } else if (!mAutomatonEventSet.contains(event)) {
      Assert.fail("The event of the transition {" +
                  source.getName() + " -[" + event.getName() + "]-> " +
                  target.getName() + "} in automaton '" +
                  mAutomaton.getName() +
                  " is not in the automaton alphabet!");
    } else if (!mAutomatonStateSet.contains(target)) {
      Assert.fail("The target state of the transition {" +
                  source.getName() + " -[" + event.getName() + "]-> " +
                  target.getName() + "} in automaton '" +
                  mAutomaton.getName() +
                  " is not in the automaton state set!");
    }
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxy mProductDES;
  private Set<EventProxy> mProductDESEventSet;
  private AutomatonProxy mAutomaton;
  private Set<StateProxy> mAutomatonStateSet;
  private Set<EventProxy> mAutomatonEventSet;

}
