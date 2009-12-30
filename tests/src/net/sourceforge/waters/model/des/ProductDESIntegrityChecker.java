//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESIntegrityChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.des;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import net.sourceforge.waters.model.marshaller.DocumentIntegrityChecker;
import net.sourceforge.waters.xsd.base.EventKind;


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


  //#########################################################################
  //# Integrity Checking
  private void checkProductDESIntegrity(final ProductDESProxy des)
  {
    try {
      mProductDES = des;
      final Set<EventProxy> events = des.getEvents();
      final int numevents = events.size();
      final Set<String> names = new HashSet<String>(numevents);
      mProductDESEventSet = new HashSet<EventProxy>(numevents);
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
      mAutomatonEventSet = new HashSet<EventProxy>(aut.getEvents());
      final Set<StateProxy> states = aut.getStates();
      final int numstates = states.size();
      final Set<String> names = new HashSet<String>(numstates);
      mAutomatonStateSet = new HashSet<StateProxy>(numstates);
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
