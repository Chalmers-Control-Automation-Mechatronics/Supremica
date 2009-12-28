//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AbstractAutomatonTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.des;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A JUnit test for the {@link ProductDESEqualityVisitor} class when applied
 * to {@link AutomatonProxy} objects.
 *
 * @author Brook Novak
 * @author Robi Malik
 */

public abstract class AbstractAutomatonTest
  extends TestCase
{

  //#########################################################################
  //# Test Cases
  /**
   * Tests whether an automaton is equal to itself. The two automata that
   * are compared are the same instance (the same reference).
   */
  public void testEqualSelf()
  {
    // Try more than one to make sure!
    for (final AutomatonProxy aut : mAutomata) {
      checkEqual("Expected self equality!", aut, aut);
    }
  }

  /**
   * Tests whether an automaton is equal to its clone. This method creates
   * clones of all sample automata and compares them to the original.
   */
  public void testEqualClones()
  {
    // Try more than one to make sure!
    for (final AutomatonProxy aut : mAutomata) {
      final AutomatonProxy clone = aut.clone();
      checkEqual("Expected equality to clone!", aut, clone);
    }
  }

  /**
   * Tests whether two identical automata are equal (different instances).
   */
  public void testEqualHandcraftedClone()
  {
    checkEqual("Clone expected equality!",
               mAutomata.get(7), mAutomata.get(8));
  }

  /**
   * Tests whether two identical automata with different names are not equal.
   */
  public void testEqualCloneRenamed()
  {
    checkNotEqual("Renamed clone expected equality!",
                  mAutomata.get(12), mAutomata.get(11));
  }

  /**
   * Tests whether passing <CODE>null</CODE> will return <CODE>false</CODE>
   * for equality.
   */
  public void testEqualNull()
  {
    for (final AutomatonProxy aut : mAutomata) {
      checkNotEqual("An automaton should NOT be equal to NULL!", aut, null);
      checkNotEqual("NULL should not be equal to an automaton!", null, aut);
    }
  }

  /**
   * Tests whether two automata are equal, even if <b>some</b> of their
   * <b>state names</b> are different.
   */
  public void testEqualSomeStates()
  {
    checkNotEqual("Machines equal despite some different state names!",
                  mAutomata.get(0), mAutomata.get(1));
  }

  /**
   * Tests whether two automata are equal, even if <b>every one</b> of
   * their <b>state names</b> are all different.
   */
  public void testEqualAllStates()
  {
    checkNotEqual("Machines equal despite all different state names!",
                  mAutomata.get(0), mAutomata.get(2));
  }

  /**
   * Tests whether two automata are equal, even if <b>some</b> of their
   * <b>event names</b> are different.
   */
  public void testEqualSomeEvents()
  {
    checkNotEqual("Machines equal despite some different event names!",
                  mAutomata.get(2), mAutomata.get(3));
  }

  /**
   * Tests whether two automata are equal, even if <b>every one</b> of
   * their <b>event names</b> are all different.
   */
  public void testEqualAllEvents()
  {
    checkNotEqual("Machines equal despite all different event names!",
                  mAutomata.get(2), mAutomata.get(4));
  }

  /**
   * Tests whether two automata are equal, even if one has an
   * <b>extra state</b>.
   */
  public void testEqualExtraState()
  {
    checkNotEqual("Machines equal despite one having an extra state!",
                  mAutomata.get(4), mAutomata.get(5));
  }

  /**
   * Tests whether two automata are equal, even if one has an
   * <b>extra event</b>.
   */
  public void testEqualExtraEvent()
  {
    checkNotEqual("Machines equal despite one having an extra event!",
                  mAutomata.get(5), mAutomata.get(6));
  }

  /**
   * Tests whether two automata are equal, even if one has an
   * <b>extra transition</b>.
   */
  public void testEqualExtraTransition()
  {
    checkNotEqual("Machines equal despite one having an extra transition!",
                  mAutomata.get(6), mAutomata.get(7));
  }

  /**
   * Tests whether two automata are equal, even if the transitions are
   * rearranged so the structure is different.
   */
  public void testEqualRearangedTransitions()
  {
    checkNotEqual("Machines equal despite transitions being rearranged!",
                  mAutomata.get(8), mAutomata.get(9));
  }

  /**
   * Tests whether two automata are equal, even if the transition
   * directions are flipped.
   */
  public void testEqualFlippedTransitions()
  {
    checkNotEqual("Machines equal despite transitions being reversed!",
                  mAutomata.get(9), mAutomata.get(10));
  }

  /**
   * Tests whether two automata are equal, even if the starting states are
   * different.
   */
  public void testEqualInitialState()
  {
    checkNotEqual("Machines equal despite different starting states!",
                  mAutomata.get(10), mAutomata.get(11));
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkEqual(final String msg, final AutomatonProxy aut1,
                          final AutomatonProxy aut2)
  {
    final ProductDESEqualityVisitor eq =
      ProductDESEqualityVisitor.getInstance();
    assertTrue(msg, eq.equals(aut1, aut2));
    assertTrue(msg, eq.equals(aut2, aut1));
  }

  private void checkNotEqual(final String msg, final AutomatonProxy aut1,
                             final AutomatonProxy aut2)
  {
    final ProductDESEqualityVisitor eq =
      ProductDESEqualityVisitor.getInstance();
    assertFalse(msg, eq.equals(aut1, aut2));
    assertFalse(msg, eq.equals(aut2, aut1));
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  /**
   * Creates the automata and stores them in memory for testing.
   */
  protected void setUp() throws Exception
  {
    super.setUp();

    // Get the factory instance
    final ProductDESProxyFactory factory = getProductDESProxyFactory();

    // Use linked list implementation for storing the different
    // finite-state machines.
    mAutomata = new ArrayList<AutomatonProxy>(12);

    // Create event list for automation 1
    final List<EventProxy> events = new LinkedList<EventProxy>();
    events.add(factory.createEventProxy("store", EventKind.CONTROLLABLE));
    events.add(factory.createEventProxy("free", EventKind.CONTROLLABLE));
    events.add(factory.createEventProxy("run", EventKind.CONTROLLABLE));
    events.add(factory.createEventProxy("stop", EventKind.CONTROLLABLE));

    // Create state list for automation 1
    final List<StateProxy> states = new LinkedList<StateProxy>();
    // Initial state
    states.add(factory.createStateProxy("HAPPY", true, null));
    states.add(factory.createStateProxy("SAD"));
    states.add(factory.createStateProxy("BORED"));

    // Create transition list for automation 1
    final List<TransitionProxy> transitions = new LinkedList<TransitionProxy>();
    transitions.add(factory.createTransitionProxy
		    (states.get(0), events.get(3), states.get(1)));
    transitions.add(factory.createTransitionProxy
		    (states.get(1), events.get(1), states.get(0)));
    transitions.add(factory.createTransitionProxy
		    (states.get(1), events.get(0), states.get(2)));
    transitions.add(factory.createTransitionProxy
		    (states.get(2), events.get(2), states.get(0)));

    // Create automaton 0
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 1 is the same as automation 0 except for one different
    // state name, reset state list for automation 1
    states.set(1, factory.createStateProxy("s1"));

    // Now have to update the effected transitions using the new state name
    transitions.set(0, factory.createTransitionProxy
		    (states.get(0), events.get(3), states.get(1)));
    transitions.set(1, factory.createTransitionProxy
		    (states.get(1), events.get(1), states.get(0)));
    transitions.set(2, factory.createTransitionProxy
		    (states.get(1), events.get(0), states.get(2)));

    // Create automaton 1
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 2 is the same as automation 0 except for all different
    // state names, reset state list for automation 2
    states.set(0, factory.createStateProxy("s0", true, null));
    states.set(2, factory.createStateProxy("s2"));

    // Now have to update the transitions using the new state names
    transitions.set(0, factory.createTransitionProxy
		    (states.get(0), events.get(3), states.get(1)));
    transitions.set(1, factory.createTransitionProxy
		    (states.get(1), events.get(1), states.get(0)));
    transitions.set(2, factory.createTransitionProxy
		    (states.get(1), events.get(0), states.get(2)));
    transitions.set(3, factory.createTransitionProxy
		    (states.get(2), events.get(2), states.get(0)));

    // Create automaton 2
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 3 is the same as automation 2 except it has some
    // different event names (leaving the first and last events as
    // the same names).
    events.set(1, factory.createEventProxy("e1", EventKind.CONTROLLABLE));
    events.set(2, factory.createEventProxy("e2", EventKind.CONTROLLABLE));

    // Now have to update the transitions using the new state names
    transitions.set(1, factory.createTransitionProxy
		    (states.get(1), events.get(1), states.get(0)));
    transitions.set(3, factory.createTransitionProxy
		    (states.get(2), events.get(2), states.get(0)));

    // Create automaton 3
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 4 is the same as automation 2 except it has all
    // different event names
    events.set(0, factory.createEventProxy("e0", EventKind.CONTROLLABLE));
    events.set(3, factory.createEventProxy("e3", EventKind.CONTROLLABLE));

    // Now have to update the transitions using the new state names
    transitions.set(2, factory.createTransitionProxy
		    (states.get(1), events.get(0), states.get(2)));
    transitions.set(0, factory.createTransitionProxy
		    (states.get(0), events.get(3), states.get(1)));

    // Create automaton 4
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 5 is the same as automation 4 except it has an
    // extra state
    states.add(factory.createStateProxy("s3"));

    // Create automaton 5
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 6 is the same as automation 5 except it has an extra event
    events.add(factory.createEventProxy("e4", EventKind.CONTROLLABLE));

    // Create automaton 6
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 7 is the same as automation 6 except it has an
    // extra transition
    transitions.add(factory.createTransitionProxy
		    (states.get(2), events.get(1), states.get(3)));

    // Create automaton 7
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 8 is the same as automation 7, create automaton 8
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 9 is the same as automation 8 except the
    // transitions are rearranged
    transitions.set(0, factory.createTransitionProxy
		    (states.get(0), events.get(4), states.get(1)));
    transitions.set(1, factory.createTransitionProxy
		    (states.get(0), events.get(3), states.get(3)));
    transitions.set(2, factory.createTransitionProxy
		    (states.get(3), events.get(2), states.get(1)));
    transitions.set(3, factory.createTransitionProxy
		    (states.get(2), events.get(0), states.get(1)));
    transitions.set(4, factory.createTransitionProxy
		    (states.get(2), events.get(1), states.get(3)));

    // Create automaton 9
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 10 is the same as automation 9 except the
    // transition directions are flipped
    transitions.set(0, factory.createTransitionProxy
		    (states.get(1), events.get(4), states.get(0)));
    transitions.set(1, factory.createTransitionProxy
		    (states.get(3), events.get(3), states.get(0)));
    transitions.set(2, factory.createTransitionProxy
		    (states.get(1), events.get(2), states.get(3)));
    transitions.set(3, factory.createTransitionProxy
		    (states.get(1), events.get(0), states.get(2)));
    transitions.set(4, factory.createTransitionProxy
		    (states.get(3), events.get(1), states.get(2)));

    // Create automaton 10
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 11 is the same as automation 10 except the
    // initial state is changed to state "s2"
    states.set(0, factory.createStateProxy("s0"));
    states.set(2, factory.createStateProxy("s2", true, null));

    // For some reason you must re-declare the transitions when you set
    // a new initial states, even if the state names remain the same.
    transitions.set(0, factory.createTransitionProxy
		    (states.get(1), events.get(4), states.get(0)));
    transitions.set(1, factory.createTransitionProxy
		    (states.get(3), events.get(3), states.get(0)));
    transitions.set(2, factory.createTransitionProxy
		    (states.get(1), events.get(2), states.get(3)));
    transitions.set(3, factory.createTransitionProxy
		    (states.get(1), events.get(0), states.get(2)));
    transitions.set(4, factory.createTransitionProxy
		    (states.get(3), events.get(1), states.get(2)));

    // Create automaton 11
    mAutomata.add(factory.createAutomatonProxy("machine",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

    // Automation 12 is the same as 11, except the machine has a different name
    mAutomata.add(factory.createAutomatonProxy("machineX",
					       ComponentKind.PLANT,
					       events,
					       states,
					       transitions));

  }

  protected void tearDown() throws Exception
  {
    mAutomata = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProductDESProxyFactory getProductDESProxyFactory();


  //#########################################################################
  //# Data Members
  private List<AutomatonProxy> mAutomata;

}
