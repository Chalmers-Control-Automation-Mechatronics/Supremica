//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Candidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author rmf18
 */
public class Candidate implements Comparable<Candidate>
{
  private final Set<AutomatonProxy> automata;
  // TODO: at this stage there is no benefit from storing the local events for
  // a candidate...i do use the count of them
  private Set<EventProxy> localEvents;
  private int eventCount;

  public Candidate(final Set<AutomatonProxy> autSet)
  {
    automata = autSet;
    localEvents = null;
    countEvents();
  }

  private void countEvents()
  {
    final Set<EventProxy> events = new HashSet<EventProxy>();
    for (final AutomatonProxy aut : automata) {
      events.addAll(aut.getEvents());
    }
    eventCount = events.size();
  }

  public Set<AutomatonProxy> getAutomata()
  {
    return automata;
  }

  public void setLocalEvents(final Set<EventProxy> localevents)
  {
    localEvents = localevents;
  }

  public int getLocalEventCount()
  {
    return localEvents.size();
  }

  public int getNumberOfEvents()
  {
    return eventCount;
  }

  public int compareTo(final Candidate t)
  {
    final int mSize = localEvents.size();
    if (mSize < t.getLocalEventCount()) {
      return -1;
    } else if (mSize == t.getLocalEventCount()) {
      return 0;
    } else {
      return 1;
    }
  }
}