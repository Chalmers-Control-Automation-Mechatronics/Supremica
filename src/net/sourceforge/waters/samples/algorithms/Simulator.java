//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   Simulator
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * A simple simulator class.
 *
 * @author Robi Malik
 */

public class Simulator
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simulator.
   * @param  des     The product DES to be simulated.
   */
  public Simulator(final ProductDESProxy des)
  {
    final Set<AutomatonProxy> automata = des.getAutomata();
    final Set<EventProxy> events = des.getEvents();
    mDES = des;
    mAutomataMap = new HashMap<String,AutomatonEntry>(automata.size());
    mEventMap =
      new HashMap<EventProxy,Collection<AutomatonEntry>>(events.size());
    for (final AutomatonProxy aut : automata) {
      final String name = aut.getName();
      final AutomatonEntry entry = new AutomatonEntry(aut);
      mAutomataMap.put(name, entry);
      for (final EventProxy event : aut.getEvents()) {
        Collection<AutomatonEntry> affected = mEventMap.get(event);
        if (affected == null) {
          affected = new LinkedList<AutomatonEntry>();
          mEventMap.put(event, affected);
        }
        affected.add(entry);
      }
    }
  }


  //#########################################################################
  //# Accessing the Model
  /**
   * Gets the set of automata simulated by this simulator.
   */
  public Set<AutomatonProxy> getAutomata()
  {
    return mDES.getAutomata();
  }

  /**
   * Gets the set of events used by this simulator.
   * Event objects are forced to be unique, i.e., object identity
   * can be used to check whether two objects refer to the same event.
   */
  public Set<EventProxy> getAllEvents()
  {
    return mDES.getEvents();
  }


  //#########################################################################
  //# Accessing the Current State
  /**
   * Gets the current state of an automaton.
   * @param  aut     The automaton to be checked.
   * @return The current state the given automaton in this simulation.
   */
  public StateProxy getCurrentState(final AutomatonProxy aut)
  {
    final AutomatonEntry entry = findEntry(aut);
    return entry.getState();
  }

  /**
   * Gets the set of currently eligible events.
   * This method computes the set of all events that are enabled by
   * all automata in the current state of this simulation.
   * @return An unmodifiable list containing all eligible events.
   *         The list is guaranteed not to contain any duplicate entries.
   */
  public Collection<EventProxy> getEligibleEvents()
  {
    if (mEligibleEvents == null) {
      final Collection<EventProxy> elig = new LinkedList<EventProxy>();
      for (final EventProxy event : getAllEvents()) {
        final Collection<AutomatonEntry> entries = mEventMap.get(event);
        boolean enabled = true;
        if (entries != null) {
          for (final AutomatonEntry entry : entries) {
            if (entry.getSuccessorState(event) == null) {
              enabled = false;
              break;
            }
          }
        }
        if (enabled) {
          elig.add(event);
        }
      }
      mEligibleEvents = Collections.unmodifiableCollection(elig);
    }
    return mEligibleEvents;
  }


  //#########################################################################
  //# Changing the State
  /**
   * Resets the state of all simulated automata to their initial state.
   */
  public void reset()
  {
    final Collection<AutomatonEntry> entries = mAutomataMap.values();
    for (final AutomatonEntry entry : entries) {
      entry.reset();
    }
  }

  /**
   * Executes and event.
   * This method performs state transitions for all automata using the
   * given event, and changes the state of all affected automata
   * accordingly.
   * @param  event   The event to be executed.
   * @throws IllegalArgumentException to indicate that the specified
   *                 event is not enabled by all automata in the
   *                 current state of this simulation.
   */
  public void executeEvent(final EventProxy event)
  {
    final Collection<AutomatonEntry> entries = mEventMap.get(event);
    if (entries != null) {
      final StateProxy[] newstate = new StateProxy[entries.size()];
      int index = 0;
      // Use two loops to guarantee the state remains unchanged if
      // an exception is thrown.
      for (final AutomatonEntry entry : entries) {
        newstate[index++] = entry.findSuccessorState(event);
      }
      index = 0;
      for (final AutomatonEntry entry : entries) {
        entry.setState(newstate[index++]);
      }
    }
  }

  /**
   * Changes the state of an automaton.
   * @param  aut     The automaton whose state is to be modified.
   * @param  state   The new state of the automaton.
   * @throws IllegalArgumentException to indicate that the specified
   *                 state is not in the state space of the specified
   *                 automaton.
   */
  public void setState(final AutomatonProxy aut, final StateProxy state)
  {
    final AutomatonEntry entry = findEntry(aut);
    entry.checkState(state);
    entry.setState(state);
  }


  //#########################################################################
  //# Auxiliary Methods
  private AutomatonEntry findEntry(final AutomatonProxy aut)
  {
    final String name = aut.getName();
    final AutomatonEntry entry = mAutomataMap.get(name);
    if (entry == null) {
      throw new IllegalArgumentException
      ("Simulator does not know anything about automaton '" + name + "'!");
    }
    return entry;
  }


  //#########################################################################
  //# Inner Class AutomatEntry
  private static class AutomatonEntry {

    //#######################################################################
    //# Constructors
    private AutomatonEntry(final AutomatonProxy aut)
    {
      mAutomaton = aut;
      mAdjacency = new AdjacencyMap(aut, AdjacencyMap.EXITING);
      reset();
    }


    //#######################################################################
    //# Simple Access
    private StateProxy getState()
    {
      return mState;
    }

    private StateProxy getSuccessorState(final EventProxy event)
    {
      return mAdjacency.getSuccessorState(mState, event);
    }

    private StateProxy findSuccessorState(final EventProxy event)
    {
      final StateProxy state = getSuccessorState(event);
      if (state == null) {
        throw new IllegalArgumentException
        ("Automaton '" + mAutomaton.getName() + "' cannot execute event '" +
            event.getName() + "' in state '" + mState.getName() + "'!");
      }
      return state;
    }

    private void checkState(final StateProxy state)
    {
      final Set<StateProxy> states = mAutomaton.getStates();
      if (!states.contains(state)) {
        throw new IllegalArgumentException
        ("Automaton '" + mAutomaton.getName() +
            "' does not have the state '" + state.getName() + "'!");
      }
    }

    //#######################################################################
    //# State Changes
    private void reset()
    {
      mState = null;
      for (final StateProxy state : mAutomaton.getStates()) {
        if (state.isInitial()) {
          mState = state;
          break;
        }
      }
      if (mState == null) {
        throw new IllegalArgumentException
        ("Automaton '" + mAutomaton.getName() + "' has no initial state!");
      }
    }

    private void setState(final StateProxy state)
    {
      mState = state;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private final AdjacencyMap mAdjacency;

    private StateProxy mState;

  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mDES;
  private final Map<String,AutomatonEntry> mAutomataMap;
  private final Map<EventProxy,Collection<AutomatonEntry>> mEventMap;

  private Collection<EventProxy> mEligibleEvents;

}
