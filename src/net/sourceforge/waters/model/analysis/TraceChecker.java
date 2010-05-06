//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   TraceChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

/**
 * A debugging tool.
 * This class contains static methods to check whether a counterexample
 * (for a deterministic or nondeterministic model) is accepted by its
 * automata.
 *
 * @author Robi Malik
 */

public class TraceChecker
{

  //#########################################################################
  //# Invocation
  /**
   * Checks whether the given trace is accepted by all its automata.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample(final TraceProxy trace)
  {
    checkCounterExample(trace, false);
  }

  /**
   * Checks whether the given trace is accepted by all its automata.
   * @param  trace    The trace to be checked.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for
   *                  an automaton of the trace will also lead to an
   *                  exception.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample(final TraceProxy trace,
                                         final boolean sat)
  {
    for (final AutomatonProxy aut : trace.getAutomata()) {
      checkCounterExample(trace, aut, sat);
    }
  }

  /**
   * Checks whether the given trace is accepted by the given automata.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final TraceProxy trace,
     final Collection<AutomatonProxy> automata)
  {
    checkCounterExample(trace, automata, false);
  }

  /**
   * Checks whether the given trace is accepted by the given automata.
   * @param  trace    The trace to be checked.
   * @param  automata Collection of automata for which the trace is to be
   *                  checked.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for the
   *                  any of the given automata will also lead to an exception.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final TraceProxy trace,
     final Collection<AutomatonProxy> automata,
     final boolean sat)
  {
    for (final AutomatonProxy aut : automata) {
      checkCounterExample(trace, aut, sat);
    }
  }

  /**
   * Checks whether the given trace is accepted by the given automaton.
   * @return Predicted end state of the automaton after execution of the
   *         trace.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static StateProxy checkCounterExample(final TraceProxy trace,
                                               final AutomatonProxy aut)
  {
    return checkCounterExample(trace, aut, false);
  }

  /**
   * Checks whether the given trace is accepted by the given automaton.
   * @param  trace    The trace to be checked.
   * @param  aut      The automaton for which the trace is to be checked.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for the
   *                  given automaton will also lead to an exception.
   * @return Predicted end state of the automaton after execution of the
   *         trace.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static StateProxy checkCounterExample(final TraceProxy trace,
                                               final AutomatonProxy aut,
                                               final boolean sat)
  {
    assertTrue(trace.getAutomata().contains(aut),
               "Automaton " + aut.getName() + " is not mentioned in trace!");
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    return checkCounterExample(steps, aut, sat);
  }

  /**
   * Checks whether the given list of trace steps is accepted by each of
   * the given automata.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  {
    checkCounterExample(steps, automata, false);
  }

  /**
   * Checks whether the given list of trace steps is accepted by each of
   * the given automata.
   * @param  steps    List of trace steps to be checked.
   * @param  automata Collection of automata for which the trace steps are to
   *                  be checked.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for the
   *                  any of the given automata will also lead to an exception.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata,
     final boolean sat)
  {
    for (final AutomatonProxy aut : automata) {
      checkCounterExample(steps, aut, sat);
    }
  }

  /**
   * Checks whether the given list of trace steps is accepted by the given
   * automaton.
   * @return Predicted end state of the automaton after execution of the
   *         trace steps.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static StateProxy checkCounterExample(final List<TraceStepProxy> steps,
                                               final AutomatonProxy aut)
  {
    return checkCounterExample(steps, aut, false);
  }

  /**
   * Checks whether the given list of trace steps is accepted by the given
   * automaton.
   * @param  steps    List of trace steps to be checked.
   * @param  aut      The automaton for which the trace is to be checked.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for the
   *                  given automaton will also lead to an exception.
   * @return Predicted end state of the automaton after execution of the
   *         trace steps.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static StateProxy checkCounterExample(final List<TraceStepProxy> steps,
                                               final AutomatonProxy aut,
                                               final boolean sat)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final Iterator<TraceStepProxy> iter = steps.iterator();
    final TraceStepProxy initStep = iter.next();
    final Map<AutomatonProxy,StateProxy> initMap = initStep.getStateMap();
    StateProxy current = initMap.get(aut);
    if (current == null) {
      assertTrue(!sat,
                 "Missing entry for automaton " + aut.getName() +
                 " in initial step!");
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          assertTrue(current == null,
            "Trace specifies no initial state for automaton " +
            aut.getName() + ", which has more than one initial state!");
          current = state;
        }
      }
      assertTrue(current != null,
                 "The automaton " + aut.getName() + " has no initial state!");
    } else {
      assertTrue(current.isInitial(),
        "Trace initial state " + current.getName() + " for automaton " +
        aut.getName() + " is not an initial state of the automaton!");
    }
    while (iter.hasNext()) {
      final TraceStepProxy traceStep = iter.next();
      final EventProxy event = traceStep.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = traceStep.getStateMap();
      final StateProxy target = stepMap.get(aut);
      if (target == null) {
        assertTrue(!sat,
                   "Missing entry for automaton " + aut.getName() +
                   " in trace step!");
        if (events.contains(event)) {
          StateProxy next = null;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == current && trans.getEvent() == event) {
              assertTrue(next == null,
                "The counterexample does not contain a target state " +
                "for the nondeterministic transition in automaton " +
                aut.getName() + " from source state " + current.getName() +
                " with event " + event.getName() + "!");
              next = trans.getTarget();
            }
          }
          assertTrue(next != null,
                     "The automaton " + aut.getName() +
                     " has no successor state for event " + event.getName() +
                     " from state " + current.getName() + "!");
          current = next;
        }
      } else {
        if (events.contains(event)) {
          boolean found = false;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == current && trans.getEvent() == event
                && trans.getTarget() == target) {
              found = true;
            }
          }
          assertTrue(found,
            "There is no transition from state " + current.getName() +
            " to state " + target.getName() + " with event " +
            event.getName() + " in automaton " + aut.getName() +
            " as specified in the counterexample trace!");
          current = target;
        } else {
          assertTrue(current == target,
            "The target state specified in the counterexample for the " +
            "selflooped event " + event.getName() +
            " is different from the current state of automaton " +
            aut.getName() + "!");
        }
      }
    }
    // returns the end state of the counterexample trace
    return current;
  }


  //#########################################################################
  //# Assertions
  private static void assertTrue(final boolean cond, final String msg)
  {
    if (!cond) {
      throw new AssertionError(msg);
    }
  }

}
