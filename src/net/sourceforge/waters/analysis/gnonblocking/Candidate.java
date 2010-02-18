//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   Candidate
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author rmf18
 */
public class Candidate implements Comparable<Candidate>
{
  private final Set<AutomatonProxy> automata;
  public final double mSize;
  // TODO: at this stage there is no benefit from storing the local events for
  // a candidate
  @SuppressWarnings("unused")
  private Set<EventProxy> localEvents;

  public Candidate(final Set<AutomatonProxy> autSet)
  {
    // TODO: can't leave -1 here
    this(autSet, -1);
  }

  public Candidate(final Set<AutomatonProxy> set, final double size)
  {
    automata = set;
    mSize = size;
    localEvents = null;
  }

  public Set<AutomatonProxy> getAutomata()
  {
    return automata;
  }

  public void setLocalEvents(final Set<EventProxy> localevents)
  {
    localEvents = localevents;
  }

  public int compareTo(final Candidate t)
  {
    if (mSize < t.mSize) {
      return -1;
    } else if (mSize == t.mSize) {
      return 0;
    } else {
      return 1;
    }
  }
}