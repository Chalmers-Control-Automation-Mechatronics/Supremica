//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Candidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Rachel Francis
 */

public class Candidate implements Comparable<Candidate>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new candidate.
   * @param  autList      List of automata in candidate in defined order.
   * @param  localEvents  Identified set of local events of this candidate.
   */
  public Candidate(final List<AutomatonProxy> autList,
                   final Set<EventProxy> localEvents)
  {
    mAutomata = autList;
    mLocalEvents = localEvents;
    countEvents();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the list of automata in this candidate.
   * @return List of automata in defined order as passed to constructor.
   */
  public List<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  /**
   * Gets the set of local events of this candidates.
   * @return Set of events as passed to constructor.
   *         Ordering may be randomised.
   */
  public Set<EventProxy> getLocalEvents()
  {
    return mLocalEvents;
  }

  /**
   * Gets the number of local events in this candidate.
   */
  public int getLocalEventCount()
  {
    return mLocalEvents.size();
  }

  /**
   * Gets the total number of events of this candidate.
   * @return The total number of distinct events found in all automata
   *         of the candidate (including propositions).
   */
  public int getNumberOfEvents()
  {
    return mEventCount;
  }


  //#########################################################################
  //# Interface java.util.Comparable<Candidate>
  /**
   * Implements default candidate ordering.
   * If both candidates have different numbers of automata, the candidate
   * with fewer automata is considered smaller. If the number of automata
   * is equal, the lists are compared lexicographically by automaton names.
   */
  public int compareTo(final Candidate candidate)
  {
    final List<AutomatonProxy> automata1 = mAutomata;
    final List<AutomatonProxy> automata2 = candidate.mAutomata;
    final int size1 = automata1.size();
    final int size2 = automata2.size();
    if (size1 != size2) {
      return size1 - size2;
    }
    final Iterator<AutomatonProxy> iter1 = automata1.iterator();
    final Iterator<AutomatonProxy> iter2 = automata2.iterator();
    while (iter1.hasNext()) {
      final AutomatonProxy aut1 = iter1.next();
      final AutomatonProxy aut2 = iter2.next();
      final int result = aut1.compareTo(aut2);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer("{");
    boolean first = true;
    for (final AutomatonProxy aut : mAutomata) {
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


  //#########################################################################
  //# Model Creation
  /**
   * Creates a product DES containing all the automata and events in this
   * candidate.
   * @param  factory   The factory to be used to create the proxies.
   */
  public ProductDESProxy createProductDESProxy
    (final ProductDESProxyFactory factory)
  {
    final String name = toString();
    final Collection<EventProxy> events = getAllEvents();
    return factory.createProductDESProxy
      (name, "Automatically created from candidate.", null, events, mAutomata);
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns an ordered list of all events in the automata of this candidate,
   * including propositions.
   */
  private List<EventProxy> getAllEvents()
  {
    final Set<EventProxy> set = new THashSet<EventProxy>();
    for (final AutomatonProxy aut : mAutomata) {
      set.addAll(aut.getEvents());
    }
    final List<EventProxy> list = new ArrayList<EventProxy>(set);
    Collections.sort(list);
    return list;
  }

  /**
   * Determines the total number of events in the automata of this candidate,
   * not including propositions, and stores the result in {@link #mEventCount}.
   */
  private void countEvents()
  {
    final Set<EventProxy> events = new THashSet<EventProxy>();
    for (final AutomatonProxy aut : mAutomata) {
      for (final EventProxy event : aut.getEvents()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          events.add(event);
        }
      }
    }
    mEventCount = events.size();
  }


  //#########################################################################
  //# Invocation
  /** The list of automata is sorted alphabetically by automaton names. */
  private final List<AutomatonProxy> mAutomata;
  private final Set<EventProxy> mLocalEvents;
  private int mEventCount;

}