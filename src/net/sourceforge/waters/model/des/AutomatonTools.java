//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import gnu.trove.strategy.HashingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.WatersHashSet;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.base.ComponentKind;


/**
 * A collection of static methods commonly used in combination with
 * automata.
 *
 * @author Robi Malik
 */

public final class AutomatonTools
{

  //#########################################################################
  //# Public Methods
  /**
   * Calculates binary logarithm.
   * @return The largest number <I>k</I> such that
   *         2<sup><I>k</I></sup>&nbsp;&lt;=&nbsp;<I>x</I>,
   *         i.e., the number of bits needed to encode numbers from
   *         0..<I>x</I>-1.
   */
  public static int log2(int x)
  {
    int result = 0;
    if (x > 1) {
      x--;
      do {
        x >>= 1;
        result++;
      } while (x > 0);
    }
    return result;
  }

  /**
   * Returns the first initial state of the given automaton,
   * or <CODE> null</CODE>.
   * This method does a sequential search and its time complexity is linear in
   * the number of states of the automaton.
   */
  public static StateProxy getFirstInitialState(final AutomatonProxy aut)
  {
    for (final StateProxy state : aut.getStates()) {
      if (state.isInitial()) {
        return state;
      }
    }
    return null;
  }

  /**
   * Returns the first successor state for the given state and event.
   * This method does a sequential search and its time complexity is linear in
   * the number of transitions of the automaton.
   * @param  aut       The automaton to be searched.
   * @param  source    The source state to be searched for.
   * @param  event     The event to be searched for.
   * @return The target state of the first transition in the given automaton
   *         with matching source state and event, or <CODE>null</CODE> if
   *         no such transition exists.
   */
  public static StateProxy getFirstSuccessorState(final AutomatonProxy aut,
                                                  final StateProxy source,
                                                  final EventProxy event)
  {
    for (final TransitionProxy trans : aut.getTransitions()) {
      if (trans.getSource() == source && trans.getEvent() == event) {
        return trans.getTarget();
      }
    }
    return null;
  }

  /**
   * Creates a product DES consisting of a single automaton.
   * @param  aut      The automaton to be used in the product DES.
   * @param  factory  Factory to construct objects.
   * @return A product DES with only one automaton. The product DES name
   *         and event list are taken from the given automaton.
   */
  public static ProductDESProxy createProductDESProxy
    (final AutomatonProxy aut, final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata = Collections.singletonList(aut);
    return factory.createProductDESProxy(name, events, automata);
  }

  /**
   * Creates a product DES consisting of the given automata.
   * @param  name     The name to be given to the product DES.
   * @param  automata The automata to be used for the product DES.
   * @param  factory  Factory to construct objects.
   * @return A product DES with only the given automata. The product DES
   *         event alphabet consists of the union of the events sets of its
   *         automata in deterministic order.
   */
  public static ProductDESProxy createProductDESProxy
    (final String name,
     final Collection<? extends AutomatonProxy> automata,
     final ProductDESProxyFactory factory)
  {
    int numEvents = 0;
    for (final AutomatonProxy aut : automata) {
      numEvents += aut.getEvents().size();
    }
    final Collection<EventProxy> eventSet =
      new THashSet<EventProxy>(numEvents);
    final Collection<EventProxy> eventList =
      new ArrayList<EventProxy>(numEvents);
    for (final AutomatonProxy aut : automata) {
      for (final EventProxy event : aut.getEvents()) {
        if (eventSet.add(event)) {
          eventList.add(event);
        }
      }
    }
    return factory.createProductDESProxy(name, eventList, automata);
  }

  /**
   * Creates a copy of the given automaton with another name.
   * @param  aut      The automaton to be duplicated.
   * @param  name     The name to be given to the copy.
   * @param  factory  Factory to construct automaton.
   */
  public static AutomatonProxy renameAutomaton(final AutomatonProxy aut,
                                               final String name,
                                               final ProductDESProxyFactory factory)
  {
    final ComponentKind kind = aut.getKind();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    return factory.createAutomatonProxy(name, kind, events, states, transitions);
  }

  /**
   * Returns whether the given product DES is deterministic.
   * An product DES is considered as deterministic if all its
   * automata are deterministic.
   * @see #isDeterministic(AutomatonProxy)
   */
  public static boolean isDeterministic(final ProductDESProxy des)
  {
    for (final AutomatonProxy aut : des.getAutomata()) {
      if (!isDeterministic(aut)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether the given automaton is deterministic.
   * An automaton is considered as deterministic if it has at most
   * one initial state, and whether there exists at most one transition
   * associated with any given pair of source state and event.
   * This method uses a hash table to perform the determinism check
   * in time complexity linear in the number of transitions.
   * @see #checkDeterministic(AutomatonProxy) checkDeterministic()
   */
  public static boolean isDeterministic(final AutomatonProxy aut)
  {
    boolean hasInit = false;
    for (final StateProxy state : aut.getStates()) {
      if (state.isInitial()) {
        if (hasInit) {
          return false;
        } else {
          hasInit = true;
        }
      }
    }
    final Set<TransitionProxy> transitions =
      new WatersHashSet<TransitionProxy>(TransitionHashingStrategy.INSTANCE);
    for (final TransitionProxy trans : aut.getTransitions()) {
      if (!transitions.add(trans)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the given automaton is deterministic, throwing an
   * exception if it is not.
   * An automaton is considered as deterministic if it has at most
   * one initial state, and whether there exists at most one transition
   * associated with any given pair of source state and event.
   * This method uses a hash table to perform the determinism check
   * in time complexity linear in the number of transitions.
   * @throws NondeterministicDESException to indicate that the given
   *   automaton is nondeterministic.
   * @see #isDeterministic(AutomatonProxy) isDeterministic()
   */
  public static void checkDeterministic(final AutomatonProxy aut)
    throws NondeterministicDESException
  {
    boolean hasInit = false;
    for (final StateProxy state : aut.getStates()) {
      if (state.isInitial()) {
        if (hasInit) {
          throw new NondeterministicDESException(aut, state);
        } else {
          hasInit = true;
        }
      }
    }
    final Set<TransitionProxy> transitions =
      new WatersHashSet<TransitionProxy>(TransitionHashingStrategy.INSTANCE);
    for (final TransitionProxy trans : aut.getTransitions()) {
      if (!transitions.add(trans)) {
        final StateProxy source = trans.getSource();
        final EventProxy event = trans.getEvent();
        throw new NondeterministicDESException(aut, source, event);
      }
    }
  }

  /**
   * Finds a first dump state in the given automaton.
   * A dump state is an unmarked state without any outgoing transitions.
   * @param  aut       The automaton to be searched.
   * @param  markings  The set of marking propositions to be considered.
   *                   An automaton can only have dump states, if all the
   *                   relevant markings are in its event set; otherwise
   *                   all states are implicitly marked by some marking
   *                   and there is no dump state.
   * @return The first dump state found in the automaton,
   *         or <CODE>null</CODE>.
   */
  public static StateProxy findDumpState(final AutomatonProxy aut,
                                         final Collection<EventProxy> markings)
  {
    if (aut.getEvents().containsAll(markings)) {
      final Set<StateProxy> states = aut.getStates();
      final int numStates = states.size();
      final Set<StateProxy> nonDumpStates = new THashSet<>(numStates);
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (nonDumpStates.add(trans.getSource()) &&
            nonDumpStates.size() == numStates) {
          return null;
        }
      }
      for (final StateProxy state : aut.getStates()) {
        if (state.getPropositions().isEmpty() &&
            !nonDumpStates.contains(state)) {
          return state;
        }
      }
    }
    return null;
  }

  /**
   * Calculates a name that can be given to a synchronous product automaton.
   *
   * @param automata
   *          List of automata constituting synchronous product.
   * @return A string consisting of the names of the given automata, with
   *         appropriate separators between them.
   */
  public static String getCompositionName
    (final Collection<TRAutomatonProxy> automata)
  {
    return getCompositionName("", automata);
  }

  /**
   * Calculates a name that can be given to a synchronous product automaton.
   *
   * @param prefix
   *          A string to be prepended to the result.
   * @param automata
   *          List of automata constituting synchronous product.
   * @return A string consisting of the prefix followed by the names of the
   *         given automata, with appropriate separators between them.
   */
  public static String getCompositionName
    (final String prefix,
     final Collection<? extends AutomatonProxy> automata)
  {
    final StringBuilder buffer = new StringBuilder(prefix);
    buffer.append('{');
    boolean first = true;
    for (final AutomatonProxy aut : automata) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
      }
      buffer.append(aut.getName());
    }
    buffer.append('}');
    return buffer.toString();
  }

  /**
   * Returns a string to describe the given component kind.
   * This method returns a string of only lower-case characters.
   * Possible return values are:
   * <CODE>&quot;plant&quot;</CODE>,
   * <CODE>&quot;property&quot;</CODE>,
   * <CODE>&quot;specification&quot;</CODE>, and
   * <CODE>&quot;supervisor&quot;</CODE>.
   */
  public static String getComponentKindStringLowerCase(final ComponentKind kind)
  {
    switch (kind) {
    case PLANT:
      return "plant";
    case PROPERTY:
      return "property";
    case SPEC:
      return "specification";
    case SUPERVISOR:
      return "supervisor";
    default:
      throw new IllegalArgumentException("Unknown component kind: " + kind
                                         + "!");
    }
  }
  /**
   * Returns a string to describe the given component kind.
   * This method returns a string with capitalised initial followed by
   * lower-case characters. Possible return values are:
   * <CODE>&quot;Plant&quot;</CODE>,
   * <CODE>&quot;Property&quot;</CODE>,
   * <CODE>&quot;Specification&quot;</CODE>, and
   * <CODE>&quot;Supervisor&quot;</CODE>.
   */
  public static String getComponentKindStringCapitalised(final ComponentKind kind)
  {
    switch (kind) {
    case PLANT:
      return "Plant";
    case PROPERTY:
      return "Property";
    case SPEC:
      return "Specification";
    case SUPERVISOR:
      return "Supervisor";
    default:
      throw new IllegalArgumentException("Unknown component kind: " + kind
                                         + "!");
    }
  }

  //#########################################################################
  //# Inner Class TransitionHashingStrategy
  private static class TransitionHashingStrategy
    implements HashingStrategy<TransitionProxy>
  {
    //#######################################################################
    //# Interface gnu.trove.TObjectHashingStrategy
    @Override
    public int computeHashCode(final TransitionProxy trans)
    {
      return trans.getSource().hashCode() + 5 * trans.getEvent().hashCode();
    }


    @Override
    public boolean equals(final TransitionProxy trans1,
                          final TransitionProxy trans2)
    {
      return
        trans1.getSource() == trans2.getSource() &&
        trans1.getEvent() == trans2.getEvent();
    }

    //#######################################################################
    //# Class Constants
    private static final TransitionHashingStrategy INSTANCE =
      new TransitionHashingStrategy();
    private static final long serialVersionUID = 1L;
  }

}
