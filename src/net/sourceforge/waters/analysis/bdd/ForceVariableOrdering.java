//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters BDD
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   ForceVariableOrdering
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


class ForceVariableOrdering
  extends AbstractCollection<AutomatonProxy>
{

  //#########################################################################
  //# Constructor
  ForceVariableOrdering(final ProductDESProxy des,
                        final Collection<AutomatonProxy> preorder)
  {
    final int numAutomata = preorder.size();
    mAutomatonEntries = new ArrayList<AutomatonEntry>(numAutomata);
    final int numEvents = des.getEvents().size();
    final Map<EventProxy,EventEntry> eventMap =
      new HashMap<EventProxy,EventEntry>(numEvents);
    int index = 0;
    for (final AutomatonProxy aut : preorder) {
      final AutomatonEntry autEntry = new AutomatonEntry(aut, index++);
      mAutomatonEntries.add(autEntry);
      if (aut.getStates().size() > 1) {
        for (final EventProxy event : aut.getEvents()) {
          EventEntry eventEntry = eventMap.get(event);
          if (eventEntry == null) {
            eventEntry = new EventEntry();
            eventMap.put(event, eventEntry);
          }
          autEntry.addEventEntry(eventEntry);
          eventEntry.addAutomatonEntry(autEntry);
        }
      }
    }
    final Iterator<Map.Entry<EventProxy,EventEntry>> iter =
      eventMap.entrySet().iterator();
    while (iter.hasNext()) {
      final EventEntry entry = iter.next().getValue();
      if (entry.getNumberOfAutomata() <= 1) {
        entry.dispose();
        iter.remove();
      }
    }
    final int numEntries = mAutomatonEntries.size();
    final int iterCount = (int) Math.round(MAX_ITER * Math.log(numEntries));
    for (int i = 0; i < iterCount; i++) {
      for (final EventEntry eventEntry : eventMap.values()) {
        eventEntry.estimatePosition();
      }
      boolean change = false;
      double prevPos = Double.NEGATIVE_INFINITY;
      for (final AutomatonEntry autEntry : mAutomatonEntries) {
        final double newPos = autEntry.estimatePosition();
        change |= newPos < prevPos;
        prevPos = newPos;
      }
      if (!change) {
        break;
      }
      Collections.sort(mAutomatonEntries);
      index = 0;
      for (final AutomatonEntry autEntry : mAutomatonEntries) {
        autEntry.setIndex(index++);
      }
    }
    // Reverse the order if the centre of gravity (of states) is beyond
    // the half-way point.
    index = 0;
    double weightedStates = 0.0;
    double totalStates = 0.0;
    for (final AutomatonEntry autEntry : mAutomatonEntries) {
      final double numStates = autEntry.getNumberOfStates();
      weightedStates += numStates * index;
      totalStates += numStates;
      index++;
    }
    if (weightedStates > 0.5 * (mAutomatonEntries.size() - 1) * totalStates) {
      Collections.reverse(mAutomatonEntries);
    }
  }


  //#########################################################################
  //# Interface java.util.Collection<AutomatonProxy>
  @Override
  public int size()
  {
    return mAutomatonEntries.size();
  }

  @Override
  public Iterator<AutomatonProxy> iterator()
  {
    final Iterator<AutomatonEntry> entryIterator = mAutomatonEntries.iterator();
    return new VariableOrderingIterator(entryIterator);
  }


  //#########################################################################
  //# Inner Class AutomatonEntry
  private static class AutomatonEntry implements Comparable<AutomatonEntry> {

    //#######################################################################
    //# Constructor
    private AutomatonEntry(final AutomatonProxy aut, final int index)
    {
      mAutomaton = aut;
      mEstimatedPosition = mIndex = index;
      final int numEvents = aut.getEvents().size();
      mEventEntries = new ArrayList<EventEntry>(numEvents);
    }

    //#######################################################################
    //# Simple Access
    private AutomatonProxy getAutomaton()
    {
      return mAutomaton;
    }

    private int getNumberOfStates()
    {
      return mAutomaton.getStates().size();
    }

    private void addEventEntry(final EventEntry event)
    {
      mEventEntries.add(event);
    }

    private double getEstimatedPosition()
    {
      return mEstimatedPosition;
    }

    private void setIndex(final int index)
    {
      mEstimatedPosition = mIndex = index;
    }

    private double estimatePosition()
    {
      if (mEventEntries.isEmpty()) {
        mEstimatedPosition = Double.POSITIVE_INFINITY;
      } else {
        double sum = 0.0;
        for (final EventEntry event : mEventEntries) {
          sum += event.getEstimatedPosition();
        }
        mEstimatedPosition = sum / mEventEntries.size();
      }
      return mEstimatedPosition;
    }

    private void remove(final EventEntry entry)
    {
      mEventEntries.remove(entry);
    }

    //#######################################################################
    //# Interface java.util.Comparable
    @Override
    public int compareTo(final AutomatonEntry entry)
    {
      final double delta = mEstimatedPosition - entry.mEstimatedPosition;
      if (delta < 0) {
        return -1;
      } else if (delta > 0) {
        return 1;
      } else {
        return mIndex - entry.mIndex;
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private final Collection<EventEntry> mEventEntries;
    private int mIndex;
    private double mEstimatedPosition;

  }


  //#########################################################################
  //# Inner Class AutomatonEntry
  private static class EventEntry {

    //#######################################################################
    //# Constructor
    private EventEntry()
    {
      mAutomatonEntries = new ArrayList<AutomatonEntry>();
      mEstimatedPosition = 0.0;
    }

    //#######################################################################
    //# Simple Access
    private int getNumberOfAutomata()
    {
      return mAutomatonEntries.size();
    }

    private void addAutomatonEntry(final AutomatonEntry aut)
    {
      mAutomatonEntries.add(aut);
    }

    private double getEstimatedPosition()
    {
      return mEstimatedPosition;
    }

    private void estimatePosition()
    {
      double sum = 0.0;
      for (final AutomatonEntry aut : mAutomatonEntries) {
        final double autpos = aut.getEstimatedPosition();
        sum += autpos;
      }
      mEstimatedPosition = sum / mAutomatonEntries.size();
    }

    private void dispose()
    {
      for (final AutomatonEntry aut : mAutomatonEntries) {
        aut.remove(this);
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<AutomatonEntry> mAutomatonEntries;
    private double mEstimatedPosition;

  }


  //#########################################################################
  //# Inner Class VariableOrderingIterator
  private static class VariableOrderingIterator
    implements Iterator<AutomatonProxy>
  {

    //#######################################################################
    //# Constructor
    private VariableOrderingIterator
      (final Iterator<AutomatonEntry> entryIterator)
    {
      mEntryIterator = entryIterator;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      return mEntryIterator.hasNext();
    }

    @Override
    public AutomatonProxy next()
    {
      final AutomatonEntry entry = mEntryIterator.next();
      return entry.getAutomaton();
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("VariableOrderingIterator can't remove!");
    }

    //#######################################################################
    //# Data Members
    private final Iterator<AutomatonEntry> mEntryIterator;

  }


  //#########################################################################
  //# Data Members
  private final List<AutomatonEntry> mAutomatonEntries;


  //#########################################################################
  //# Class Constants
  private static final int MAX_ITER = 127;

}
