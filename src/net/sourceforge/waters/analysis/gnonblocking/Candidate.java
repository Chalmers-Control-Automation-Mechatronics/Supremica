//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Candidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author rmf18
 */
public class Candidate implements Comparable<Candidate>
{
  // the list of automata is sorted alphabetically by automaton names
  private final List<AutomatonProxy> mAutomata;
  private final Set<EventProxy> mLocalEvents;
  private int mEventCount;

  public Candidate(final List<AutomatonProxy> autSet,
                   final Set<EventProxy> localEvents)
  {
    mAutomata = autSet;
    Collections.sort(autSet);
    mLocalEvents = localEvents;
    countEvents();
  }

  private void countEvents()
  {
    final Set<EventProxy> events = new HashSet<EventProxy>();
    for (final AutomatonProxy aut : mAutomata) {
      events.addAll(aut.getEvents());
    }
    mEventCount = events.size();
  }

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

  public int compareTo(final Candidate t)
  {
    final int mSize = mLocalEvents.size();
    if (mSize < t.getLocalEventCount()) {
      return -1;
    } else if (mSize == t.getLocalEventCount()) {
      return 0;
    } else {
      return 1;
    }
  }
}