//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Candidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Rachel Francis
 */

public class Candidate implements Comparable<Candidate>
{

  //#########################################################################
  //# Constructor
  public Candidate(final List<AutomatonProxy> autSet,
                   final Set<EventProxy> localEvents)
  {
    mAutomata = autSet;
    Collections.sort(autSet);
    mLocalEvents = localEvents;
    countEvents();
  }


  //#########################################################################
  //# Simple Access
  public List<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  public Set<EventProxy> getLocalEvents()
  {
    return mLocalEvents;
  }

  public int getLocalEventCount()
  {
    return mLocalEvents.size();
  }

  public int getNumberOfEvents()
  {
    return mEventCount;
  }


  //#########################################################################
  //# Interface java.util.Comparable<Candidate>
  public int compareTo(final Candidate cand)
  {
    return getLocalEventCount() - cand.getLocalEventCount();
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
    final StringBuffer buffer = new StringBuffer('{');
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
    final String name = buffer.toString();
    final Collection<EventProxy> events = getAllEvents();
    return factory.createProductDESProxy
      (name, "Automatically created from candidate.", null, events, mAutomata);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Set<EventProxy> getAllEvents()
  {
    // Mind that event ordering!
    final Set<EventProxy> events = new TreeSet<EventProxy>();
    for (final AutomatonProxy aut : mAutomata) {
      events.addAll(aut.getEvents());
    }
    return events;
  }

  private void countEvents()
  {
    final Set<EventProxy> events = new HashSet<EventProxy>();
    for (final AutomatonProxy aut : mAutomata) {
      events.addAll(aut.getEvents());
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