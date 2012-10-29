//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AutomatonTools
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;


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
   * Returns whether the given automaton has at least one initial state.
   */
  public static boolean hasInitialState(final AutomatonProxy aut)
  {
    for (final StateProxy state : aut.getStates()) {
      if (state.isInitial()) {
        return true;
      }
    }
    return false;
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
     final Collection<AutomatonProxy> automata,
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
   * Returns whether the given product DES is deterministic.
   * An product DES is considered as deterministic if all its
   * automata are deterministic.
   * @see {@link #isDeterministic(AutomatonProxy)}
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
      new THashSet<TransitionProxy>(TransitionHashingStrategy.INSTANCE);
    for (final TransitionProxy trans : aut.getTransitions()) {
      if (!transitions.add(trans)) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Inner Class TransitionHashingStrategy
  private static class TransitionHashingStrategy
    implements TObjectHashingStrategy<TransitionProxy>
  {
    //#######################################################################
    //# Interface gnu.trove.TObjectHashingStrategy
    public int computeHashCode(final TransitionProxy trans)
    {
      return trans.getSource().hashCode() + 5 * trans.getEvent().hashCode();
    }


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
