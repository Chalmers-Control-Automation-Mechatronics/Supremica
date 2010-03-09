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
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author rmf18
 */
public class Candidate implements Comparable<Candidate>
{
  private List<AutomatonProxy> mAutomata;
  // TODO: at this stage there is no benefit from storing the local events for
  // a candidate...i do use the count of them
  private Set<EventProxy> mLocalEvents;
  private int eventCount;

  public Candidate(final List<AutomatonProxy> autSet,
                   final Set<EventProxy> localEvents)
  {
    mAutomata = autSet;
    mLocalEvents = localEvents;
    countEvents();
  }

  private void countEvents()
  {
    final Set<EventProxy> events = new HashSet<EventProxy>();
    for (final AutomatonProxy aut : mAutomata) {
      events.addAll(aut.getEvents());
    }
    eventCount = events.size();
  }

  public List<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  public void setAutomata(final List<AutomatonProxy> aut)
  {
    mAutomata = aut;
  }

  public void setLocalEvents(final Set<EventProxy> localevents)
  {
    mLocalEvents = localevents;
  }

  public int getLocalEventCount()
  {
    return mLocalEvents.size();
  }

  public int getNumberOfEvents()
  {
    return eventCount;
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