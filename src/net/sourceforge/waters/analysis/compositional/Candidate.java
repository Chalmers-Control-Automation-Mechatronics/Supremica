//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   Candidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A set of automata considered for possible composition in compositional
 * verification algorithms. In addition to storing the automata and local
 * events, this class provides some support to evaluate different heuristics
 * for candidate selection.
 *
 * @see AbstractCompositionalModelAnalyzer
 * @author Rachel Francis
 */

public class Candidate implements Comparable<Candidate>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new candidate.
   *
   * @param autList
   *          List of automata in candidate in defined order.
   * @param localEvents
   *          Identified set of local events of this candidate.
   */
  public Candidate(final List<AutomatonProxy> autList,
                   final Set<EventProxy> localEvents)
  {
    mAutomata = autList;
    mLocalEvents = localEvents;
    mCommonEvents = -1;
    countEvents();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of automata in this candidate.
   */
  public int getNumberOfAutomata()
  {
    return mAutomata.size();
  }

  /**
   * Gets the list of automata in this candidate.
   *
   * @return List of automata in defined order as passed to constructor.
   */
  public List<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  /**
   * Gets the set of local events of this candidates.
   *
   * @return Set of events as passed to constructor. Ordering may be randomised.
   */
  public Set<EventProxy> getLocalEvents()
  {
    return mLocalEvents;
  }

  /**
   * Gets the count of events (excluding propositions) which are shared between
   * all automata of this candidate.
   *
   * @return Count of shared events.
   */
  public int getCommonEventCount()
  {
    if (mCommonEvents == -1) {
      identifyCommonEvents();
    }
    return mCommonEvents;
  }

  /**
   * Sets the local events for this candidate.
   *
   * @param localEvents
   */
  public void setLocalEvents(final Set<EventProxy> localEvents)
  {
    mLocalEvents = localEvents;
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
   *
   * @return The total number of distinct events found in all automata of the
   *         candidate (not including propositions).
   */
  public int getNumberOfEvents()
  {
    return mEventCount;
  }


  //#########################################################################
  //# Interface java.util.Comparable<Candidate>
  /**
   * Implements default candidate ordering. If both candidates have different
   * numbers of automata, the candidate with fewer automata is considered
   * smaller. If the number of automata is equal, the lists are compared
   * lexicographically by automaton names.
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
    return getCompositionName(mAutomata).replaceAll(":", "-");
  }

  /**
   *Two candidates are equal if they have the same list of automata.
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (getClass() == obj.getClass()) {
      final Candidate other = (Candidate) obj;
      return mAutomata.equals(other.mAutomata);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return mAutomata.hashCode();
  }


  //#########################################################################
  //# Subsumption
  /**
   * Returns whether this candidate strictly subsumes the given candidate.
   * A candidate A subsumes strictly another candidate B, if every automaton
   * of A also is an automaton of B, and A and B are not equal.
   */
  public boolean subsumes(final Candidate other)
  {
    final List<AutomatonProxy> otherAutomata = other.getAutomata();
    return
      mAutomata.size() < otherAutomata.size() &&
      otherAutomata.containsAll(mAutomata);
  }


  //#########################################################################
  //# Model Creation
  /**
   * Returns an ordered list of all events in the automata of this candidate,
   * including propositions.
   */
  public List<EventProxy> getOrderedEvents()
  {
    return getOrderedEvents(mAutomata);
  }


  //#########################################################################
  //# Auxiliary Methods
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

  /**
   * Initialises mCommonEvents by counting all the events (excluding
   * propositions) which are shared between all automata of this candidate.
   */
  private void identifyCommonEvents()
  {
    mCommonEvents = 0;
    for (final EventProxy event : getOrderedEvents()) {
      if (event.getKind() != EventKind.PROPOSITION) {
        boolean shared = true;
        for (final AutomatonProxy aut : getAutomata()) {
          if (!aut.getEvents().contains(event)) {
            shared = false;
            break;
          }
        }
        if (shared) {
          mCommonEvents++;
        }
      }
    }
  }


  //#########################################################################
  //# Static Methods
  /**
   * Calculates a name that can be given to a synchronous product automaton.
   *
   * @param automata
   *          List of automata constituting synchronous product.
   * @return A string consisting of the names of the given automata, with
   *         appropriate separators between them.
   */
  public static String getCompositionName(final List<AutomatonProxy> automata)
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
  public static String getCompositionName(final String prefix,
                                          final List<AutomatonProxy> automata)
  {
    final StringBuffer buffer = new StringBuffer(prefix);
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
   * Returns an set of all events in the given automata,
   * including propositions.
   */
  public static Set<EventProxy> getAllEvents
    (final Collection<AutomatonProxy> automata)
  {
    final Set<EventProxy> set = new THashSet<EventProxy>();
    for (final AutomatonProxy aut : automata) {
      set.addAll(aut.getEvents());
    }
    return set;
  }

  /**
   * Returns an ordered list of all events in the given automata,
   * including propositions.
   */
  public static List<EventProxy> getOrderedEvents
    (final Collection<AutomatonProxy> automata)
  {
    final Set<EventProxy> set = getAllEvents(automata);
    final List<EventProxy> list = new ArrayList<EventProxy>(set);
    Collections.sort(list);
    return list;
  }

  /**
   * Determines the total number of events in the given automaton, not
   * including propositions.
   */
  public static int countEvents(final AutomatonProxy aut)
  {
    int count = 0;
    for (final EventProxy event : aut.getEvents()) {
      if (event.getKind() != EventKind.PROPOSITION) {
        count++;
      }
    }
    return count;
  }


  //#########################################################################
  //# Data Members
  private final List<AutomatonProxy> mAutomata;
  private Set<EventProxy> mLocalEvents;
  private int mCommonEvents;
  private int mEventCount;

}
