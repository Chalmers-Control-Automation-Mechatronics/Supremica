//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

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
   * Checks whether the given counterexample is accepted by all its automata.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample(final CounterExampleProxy counter)
  {
    checkCounterExample(counter, false);
  }

  /**
   * Checks whether the given counterexample is accepted by all its automata.
   * @param  counter  The counterexample to be checked.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for
   *                  an automaton of the trace will also lead to an
   *                  exception.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample(final CounterExampleProxy counter,
                                         final boolean sat)
  {
    for (final TraceProxy trace : counter.getTraces()) {
      for (final AutomatonProxy aut : counter.getAutomata()) {
        checkTrace(trace, aut, sat);
      }
    }
  }

  /**
   * Checks whether the given counterexample is accepted by the given automata.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> automata)
  {
    checkCounterExample(counter, automata, false);
  }

  /**
   * Checks whether the given counterexample is accepted by the given automata.
   * @param  counter  The counterexample to be checked.
   * @param  automata Collection of automata for which the trace is to be
   *                  checked.
   * @param  sat      A flag, indicating that the traces are expected to be
   *                  saturated. If true, any missing step entry for the
   *                  any of the given automata will also lead to an exception.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> automata,
     final boolean sat)
  {
    final Collection<AutomatonProxy> declared = counter.getAutomata();
    for (final AutomatonProxy aut : automata) {
      assertTrue(declared.contains(aut),
                 "Automaton " + aut.getName() +
                 " is not mentioned in counterexample!");
      for (final TraceProxy trace : counter.getTraces()) {
        checkTrace(trace, aut, sat);
      }
    }
  }

  /**
   * Checks whether the given trace is accepted by the given automaton.
   * @return Predicted end state of the automaton after execution of the
   *         trace.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static StateProxy checkTrace(final TraceProxy trace,
                                      final AutomatonProxy aut)
  {
    return checkTrace(trace, aut, false);
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
  public static StateProxy checkTrace(final TraceProxy trace,
                                      final AutomatonProxy aut,
                                      final boolean sat)
  {
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    return checkCounterExample(steps, aut, null, sat, false);
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
    checkCounterExample(steps, automata, null, sat);
  }

  /**
   * Checks whether the given list of trace steps is accepted by each of
   * the given automata.
   * @param  steps    List of trace steps to be checked.
   * @param  automata Collection of automata for which the trace steps are to
   *                  be checked.
   * @param  prop     Proposition that is expected to mark the end state of
   *                  the trace, or <CODE>null</CODE>. This can be used to
   *                  verify generalised nonblocking traces.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for the
   *                  any of the given automata will also lead to an exception.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata,
     final EventProxy prop,
     final boolean sat)
  {
    for (final AutomatonProxy aut : automata) {
      checkCounterExample(steps, aut, prop, sat, false);
    }
  }

  /**
   * Checks whether the given counterexample forms a correct safety error trace
   * for its automata. A safety error trace must be accepted by all its automata
   * except that at least one of the specifications must reject an
   * uncontrollable event in the final step.
   * @param  counter    The counterexample to be checked.
   * @param  sat        A flag, indicating that the trace is expected to be
   *                    saturated. If true, any missing step entry for the
   *                    any of the given automata will also lead to an exception.
   * @param  translator Kind translator used to distinguish plants and
   *                    specifications, and controllable and uncontrollable
   *                    events.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkSafetyCounterExample
    (final SafetyCounterExampleProxy counter,
     final boolean sat,
     final KindTranslator translator)
  {
    final TraceProxy trace = counter.getTrace();
    checkSafetyTrace(trace.getTraceSteps(), counter.getAutomata(),
                     sat, translator);
  }

  /**
   * Checks whether the given list of trace steps forms a correct safety
   * error trace for the given automata. A safety error trace must be
   * accepted by all the given automata except that at least one of the
   * specifications must reject an uncontrollable event in the final step.
   * @param  steps      List of trace steps to be checked.
   * @param  automata   Collection of automata for which the trace steps are to
   *                    be checked.
   * @param  sat        A flag, indicating that the trace is expected to be
   *                    saturated. If true, any missing step entry for the
   *                    any of the given automata will also lead to an exception.
   * @param  translator Kind translator used to distinguish plants and
   *                    specifications, and controllable and uncontrollable
   *                    events.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkSafetyTrace
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata,
     final boolean sat,
     final KindTranslator translator)
  {
    final int length = steps.size();
    assertTrue(length > 0, "Trace has no initial step!");
    if (length > 1) {
      final ListIterator<TraceStepProxy> iter = steps.listIterator(length);
      final TraceStepProxy lastStep = iter.previous();
      final EventProxy lastEvent = lastStep.getEvent();
      assertTrue(translator.getEventKind(lastEvent) == EventKind.UNCONTROLLABLE,
                 "Last event " + lastEvent.getName() +
                 " of safety trace is not uncontrollable!");
    }
    boolean rejected = false;
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
        checkCounterExample(steps, aut, null, sat, false);
      } else {
        rejected |= checkCounterExample(steps, aut, null, sat, true) == null;
      }
    }
    assertTrue(rejected, "Safety trace is not rejected by any specification!");
  }

  /**
   * Checks whether the given trace forms a correct conflict error trace for
   * its automata. A conflict error trace must be accepted by all automata and
   * end in a state marking by the precondition marking. Furthermore, the end
   * state must be blocking, i.e., it must not be possible to reach a terminal
   * state. The latter condition is verified using a language inclusion check,
   * which may be quite time consuming.
   * @param  counter    The counterexample to be checked.
   * @param  premarking Precondition marking for generalised nonblocking or
   *                    <CODE>null</CODE>.
   * @param  marking    Marking proposition to identify terminal states.
   * @param  sat        A flag, indicating that the trace is expected to be
   *                    saturated. If true, any missing step entry for the
   *                    any of the given automata will also lead to an exception.
   * @param  translator Kind translator used for filtering propositions when
   *                    creating language inclusion model.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkConflictCounterExample
    (final ConflictCounterExampleProxy counter,
     final EventProxy premarking,
     final EventProxy marking,
     final boolean sat,
     final KindTranslator translator)
  throws AnalysisException
  {
    final TraceProxy trace = counter.getTrace();
    checkConflictTrace(trace.getTraceSteps(), counter.getAutomata(),
                       premarking, marking, sat, translator);
  }

  /**
   * Checks whether the given list of trace steps forms a correct conflict
   * error trace for the given automata. A conflict error trace must be
   * accepted by all the given automata and end in a state marking by the
   * precondition marking. Furthermore, the end state must be blocking, i.e.,
   * it must not be possible to reach a terminal state. The latter condition
   * is verified using a language inclusion check, which may be quite time
   * consuming.
   * @param  steps      List of trace steps to be checked.
   * @param  automata   Collection of automata for which the trace steps are to
   *                    be checked.
   * @param  premarking Precondition marking for generalised nonblocking or
   *                    <CODE>null</CODE>.
   * @param  marking    Marking proposition to identify terminal states.
   * @param  sat        A flag, indicating that the trace is expected to be
   *                    saturated. If true, any missing step entry for the
   *                    any of the given automata will also lead to an exception.
   * @param  translator Kind translator used for filtering propositions when
   *                    creating language inclusion model.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static void checkConflictTrace
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata,
     final EventProxy premarking,
     final EventProxy marking,
     final boolean sat,
     final KindTranslator translator)
  throws AnalysisException
  {
    final int length = steps.size();
    assertTrue(length > 0, "Trace has no initial step!");
    final int numAutomata = automata.size();
    final Map<AutomatonProxy,StateProxy> map = new HashMap<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state =
        checkCounterExample(steps, aut, premarking, sat, false);
      map.put(aut, state);
    }
    final ProductDESProxy des =
      createLanguageInclusionModel(map, marking, translator);
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final LanguageInclusionChecker checker =
      new NativeLanguageInclusionChecker(des, factory);
    final boolean blocking = checker.run();
    assertTrue(blocking, "Counterexample does not lead to blocking state!");
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
    return checkCounterExample(steps, aut, null, false, false);
  }

  /**
   * Checks whether the given list of trace steps is accepted by the given
   * automaton.
   * @param  steps    List of trace steps to be checked.
   * @param  aut      The automaton for which the trace is to be checked.
   * @param  prop     Proposition that is expected to mark the end state of
   *                  the trace, or <CODE>null</CODE>. This can be used to
   *                  verify generalised nonblocking traces.
   * @param  sat      A flag, indicating that the trace is expected to be
   *                  saturated. If true, any missing step entry for the
   *                  given automaton will also lead to an exception.
   * @param  safety   A flag, indicating that the given automaton is a spec
   *                  or property in a safety check. For safety checks, the
   *                  final step does not have to be accepted by the automaton.
   * @return Predicted end state of the automaton after execution of the
   *         trace steps, or <CODE>null</CODE> if the trace fails to accept
   *         the last step in a safety check.
   * @throws AssertionError to indicate that something is wrong with the
   *                        trace.
   */
  public static StateProxy checkCounterExample(final List<TraceStepProxy> steps,
                                               final AutomatonProxy aut,
                                               final EventProxy prop,
                                               final boolean sat,
                                               final boolean safety)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final Iterator<TraceStepProxy> iter = steps.iterator();
    final TraceStepProxy initStep = iter.next();
    final Map<AutomatonProxy,StateProxy> initMap = initStep.getStateMap();
    StateProxy current = initMap.get(aut);
    if (current == null) {
      final boolean lastInSafety = safety && !iter.hasNext();
      assertTrue(lastInSafety || !sat,
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
      if (lastInSafety) {
        if (current != null) {
          assertTrue(current == null,
                     "Trace reports failure at initial state of specification " +
                     aut.getName() +
                     ", but the automaton has the initial state" +
                     current.getName() + "!");
        }
      } else {
        assertTrue(current != null,
                   "The automaton " + aut.getName() + " has no initial state!");
      }
    } else {
      assertTrue(current.isInitial(),
        "Trace initial state " + current.getName() + " for automaton " +
        aut.getName() + " is not an initial state of the automaton!");
    }
    while (iter.hasNext()) {
      final TraceStepProxy traceStep = iter.next();
      final boolean lastInSafety = safety && !iter.hasNext();
      final EventProxy event = traceStep.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = traceStep.getStateMap();
      final StateProxy target = stepMap.get(aut);
      if (target == null) {
        assertTrue(lastInSafety || !sat,
                   "Missing entry for automaton " + aut.getName() +
                   " in trace step!");
        if (events.contains(event)) {
          StateProxy next = null;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == current && trans.getEvent() == event) {
              assertTrue(next == null,
                         "The counterexample does not contain a target state " +
                         "for the nondeterministic transition in automaton " +
                         aut.getName() + " from source state " +
                         current.getName() + " with event " +
                         event.getName() + "!");
              next = trans.getTarget();
            }
          }
          if (lastInSafety) {
            if (next != null) {
              assertTrue(next == null,
                         "Trace reports failure on event " + event.getName() +
                         " from state " + current.getName() +
                         " in specification " + aut.getName() +
                         ", but the automaton has the successor state" +
                         next.getName() + "!");
            }
          } else {
            assertTrue(next != null,
                       "The automaton " + aut.getName() +
                       " has no successor state for event " + event.getName() +
                       " from state " + current.getName() + "!");
          }
          current = next;
        } else {
          assertTrue(!lastInSafety,
                     "Trace reports failure on event " + event.getName() +
                     " in specification " + aut.getName() +
                     ", but the event is not in the automaton alphabet!");
        }
      } else {
        if (events.contains(event)) {
          boolean found = false;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == current &&
                trans.getEvent() == event &&
                trans.getTarget() == target) {
              found = true;
              break;
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
    if (prop != null && events.contains(prop)) {
      final Collection<EventProxy> props = current.getPropositions();
      assertTrue(props.contains(prop),
                 "The counterexample's end state " + current.getName() +
                 " for automaton " + aut.getName() + " is not marked " +
                 " by the proposition " + prop.getName() + "!");
    }
    // returns the end state of the counterexample trace
    return current;
  }


  //#########################################################################
  //# Coreachability Model
  private static ProductDESProxy createLanguageInclusionModel
    (final Map<AutomatonProxy,StateProxy> inittuple,
     final EventProxy oldmarking,
     final KindTranslator translator)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final Collection<AutomatonProxy> oldautomata = inittuple.keySet();
    final Collection<EventProxy> events = new THashSet<>();
    final String markingname = oldmarking.getName();
    final EventProxy newmarking =
      factory.createEventProxy(markingname, EventKind.UNCONTROLLABLE);
    events.add(newmarking);
    for (final AutomatonProxy aut : oldautomata) {
      for (final EventProxy event : aut.getEvents()) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION) {
          events.add(event);
        }
      }
    }
    final int numaut = oldautomata.size();
    final Collection<AutomatonProxy> newautomata = new ArrayList<>(numaut + 1);
    for (final AutomatonProxy oldaut : oldautomata) {
      final StateProxy init = inittuple.get(oldaut);
      final AutomatonProxy newaut =
        createLanguageInclusionAutomaton(oldaut, init, oldmarking, newmarking);
      newautomata.add(newaut);
    }
    final AutomatonProxy prop = createPropertyAutomaton(newmarking);
    newautomata.add(prop);
    final String name = "coreachability-test";
    return factory.createProductDESProxy(name, events, newautomata);
  }

  private static AutomatonProxy createLanguageInclusionAutomaton
    (final AutomatonProxy aut,
     final StateProxy newinit,
     final EventProxy oldmarking,
     final EventProxy newmarking)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final Collection<EventProxy> oldevents = aut.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents = new ArrayList<>(numevents);
    for (final EventProxy oldevent : oldevents) {
      if (oldevent == oldmarking) {
        newevents.add(newmarking);
      } else if (oldevent.getKind() != EventKind.PROPOSITION) {
        newevents.add(oldevent);
      }
    }
    final Collection<StateProxy> oldstates = aut.getStates();
    final int numstates = oldstates.size();
    final Collection<StateProxy> newstates =
      new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> statemap =
      new HashMap<StateProxy,StateProxy>(numstates);
    final Collection<TransitionProxy> oldtransitions = aut.getTransitions();
    final int numtrans = oldtransitions.size();
    final Collection<TransitionProxy> newtransitions =
      new ArrayList<>(numstates + numtrans);
    for (final StateProxy oldstate : oldstates) {
      final String statename = oldstate.getName();
      final StateProxy newstate =
        factory.createStateProxy(statename, oldstate == newinit, null);
      newstates.add(newstate);
      statemap.put(oldstate, newstate);
      if (oldstate.getPropositions().contains(oldmarking)) {
        final TransitionProxy trans =
          factory.createTransitionProxy(newstate, newmarking, newstate);
        newtransitions.add(trans);
      }
    }
    for (final TransitionProxy oldtrans : oldtransitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final StateProxy newsource = statemap.get(oldsource);
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateProxy newtarget = statemap.get(oldtarget);
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
        factory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    return factory.createAutomatonProxy
      (autname, kind, newevents, newstates, newtransitions, null);
  }

  private static AutomatonProxy createPropertyAutomaton
    (final EventProxy newmarking)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final String name = ":never:" + newmarking.getName();
    final Collection<EventProxy> events =
      Collections.singletonList(newmarking);
    final StateProxy state = factory.createStateProxy("s0", true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return factory.createAutomatonProxy
      (name, ComponentKind.PROPERTY, events, states, null, null);
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
