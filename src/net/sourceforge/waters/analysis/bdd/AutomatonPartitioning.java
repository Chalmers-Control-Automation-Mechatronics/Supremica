//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.bdd;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author Robi Malik
 */

class AutomatonPartitioning
  extends Partitioning<TransitionPartitionBDD>
{

  //#########################################################################
  //# Constructor
  AutomatonPartitioning(final BDDFactory factory,
                        final ProductDESProxy model,
                        final int partitioningSizeLimit)
  {
    super(factory, TransitionPartitionBDD.class, partitioningSizeLimit);
    final int numEvents = model.getEvents().size();
    mEventMap = new HashMap<EventProxy,EventInfo>(numEvents);
  }


  //#########################################################################
  //# Simple Access
  @Override
  void add(final TransitionPartitionBDD part)
  {
    super.add(part);
    final Map<EventProxy,PartitionBDD> comps = part.getComponents();
    assert comps.size() == 1 : "Input to partitioning already merged!";
    final EventProxy event = comps.keySet().iterator().next();
    final EventInfo info = new EventInfo(event, part);
    mEventMap.put(event, info);
  }


  //#########################################################################
  //# Algorithm
  @Override
  void merge(final AutomatonBDD[] automatonBDDs)
    throws AnalysisAbortException
  {
    final int numEvents = mEventMap.size();
    final int threshold = (int) Math.ceil(THRESHOLD * numEvents);
    for (final AutomatonBDD autBDD : automatonBDDs) {
      final AutomatonProxy aut = autBDD.getAutomaton();
      if (aut.getEvents().size() < threshold) {
        new AutomatonInfo(autBDD);
      }
    }
    final List<TransitionPartitionBDD> partitions = getFullPartition();
    partitions.clear();
    if (mEventMap.isEmpty()) {
      return;
    }

    final BDDFactory factory = getBDDFactory();
    final Collection<EventInfo> events = mEventMap.values();
    final EventInfo min0 = Collections.min(events);
    if (min0.getAutomata().isEmpty()) {
      final Map<BitSet,TransitionPartitionBDD> lostMap =
        new HashMap<BitSet,TransitionPartitionBDD>();
      for (EventInfo min = min0;
           min.getAutomata().isEmpty();
           min = Collections.min(events)) {
        checkAbort();
        TransitionPartitionBDD part = min.getPartitionBDD();
        final BitSet automata = part.getAutomata();
        final TransitionPartitionBDD lostPart = lostMap.get(automata);
        if (lostPart != null) {
          part = lostPart.compose(part, automatonBDDs, factory);
        }
        lostMap.put(automata, part);
        final EventProxy key = min.getEvent();
        mEventMap.remove(key);
        if (mEventMap.isEmpty()) {
          break;
        }
      }
      partitions.addAll(lostMap.values());
    }

    while (!mEventMap.isEmpty()) {
      checkAbort();
      final EventInfo min = Collections.min(events);
      final Collection<AutomatonInfo> minAutomata = min.getAutomata();
      AutomatonInfo aut = null;
      switch (minAutomata.size()) {
      case 0:
        final TransitionPartitionBDD part = min.getPartitionBDD();
        partitions.add(part);
        final EventProxy key = min.getEvent();
        mEventMap.remove(key);
        continue;
      case 1:
        aut = minAutomata.iterator().next();
        break;
      default:
        final THashSet<AutomatonInfo> automata =
          new THashSet<AutomatonInfo>(automatonBDDs.length);
        for (final EventInfo info : events) {
          automata.addAll(info.getAutomata());
        }
        aut = Collections.min(automata);
        break;
      }
      TransitionPartitionBDD part = null;
      for (final EventInfo event : aut.getEvents()) {
        checkAbort();
        final TransitionPartitionBDD epart = event.getPartitionBDD();
        if (part == null) {
          part = epart;
        } else {
          part = part.compose(epart, automatonBDDs, factory);
        }
        final EventProxy key = event.getEvent();
        mEventMap.remove(key);
      }
      partitions.add(part);
    }
    Collections.sort(partitions);
  }

  @Override
  boolean isStrictBFS()
  {
    return getFullPartition().size() <= 1;
  }

  @Override
  List<TransitionPartitionBDD> startIteration()
  {
    final List<TransitionPartitionBDD> partitions = getFullPartition();
    mIterator = partitions.iterator();
    if (mIterator.hasNext()) {
      mCurrentIsStable = true;
      mOldestStable = null;
      mCurrent = mIterator.next();
      return Collections.singletonList(mCurrent);
    } else {
      mOldestStable = mCurrent = null;
      return null;
    }
  }

  @Override
  List<TransitionPartitionBDD> nextGroup(final boolean stable)
  {
    if (stable) {
      if (mCurrentIsStable && mOldestStable == null) {
        mOldestStable = mCurrent;
      }
      if (!mIterator.hasNext()) {
        final List<TransitionPartitionBDD> partitions = getFullPartition();
        mIterator = partitions.iterator();
      }
      mCurrent = mIterator.next();
      if (mCurrent == mOldestStable) {
        return null;
      }
      mCurrentIsStable = true;
    } else {
      mCurrentIsStable = false;
      mOldestStable = null;
    }
    return Collections.singletonList(mCurrent);
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo implements Comparable<EventInfo> {

    //#######################################################################
    //# Constructor
    public EventInfo(final EventProxy event,
                     final TransitionPartitionBDD part)
    {
      mEvent = event;
      mPartitionBDD = part;
      mAutomata = new THashSet<AutomatonInfo>();
    }

    //#######################################################################
    //# Access
    EventProxy getEvent()
    {
      return mEvent;
    }

    void addAutomaton(final AutomatonInfo info)
    {
      mAutomata.add(info);
    }

    Collection<AutomatonInfo> getAutomata()
    {
      return mAutomata;
    }

    TransitionPartitionBDD getPartitionBDD()
    {
      return mPartitionBDD;
    }

    //#######################################################################
    //# Interface java.util.Comparable<EventInfo>
    @Override
    public int compareTo(final EventInfo info)
    {
      final int count1 = mAutomata.size();
      final int count2 = info.mAutomata.size();
      if (count1 != count2) {
        return count1 - count2;
      }
      return mEvent.compareTo(info.mEvent);
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mEvent;
    private final TransitionPartitionBDD mPartitionBDD;
    private final Collection<AutomatonInfo> mAutomata;
  }


  //#########################################################################
  //# Inner Class AutomatonInfo
  private class AutomatonInfo implements Comparable<AutomatonInfo> {

    //#######################################################################
    //# Constructor
    public AutomatonInfo(final AutomatonBDD autBDD)
    {
      mAutomatonBDD = autBDD;
      final AutomatonProxy aut = autBDD.getAutomaton();
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      mEvents = new ArrayList<EventInfo>(numEvents);
      for (final EventProxy event : events) {
        final EventInfo info = mEventMap.get(event);
        if (info != null) {
          mEvents.add(info);
          info.addAutomaton(this);
        }
      }
      mCloudSize = -1;
    }

    //#######################################################################
    //# Simple Access
    AutomatonBDD getAutomatonBDD()
    {
      return mAutomatonBDD;
    }

    Collection<EventInfo> getEvents()
    {
      return mEvents;
    }

    //#######################################################################
    //# Interface java.util.Comparable<AutomatonInfo>
    @Override
    public int compareTo(final AutomatonInfo info)
    {
      final int size1 = getCloudSize();
      final int size2 = info.getCloudSize();
      if (size1 != size2) {
        return size1 - size2;
      }
      final AutomatonProxy aut1 = mAutomatonBDD.getAutomaton();
      final AutomatonProxy aut2 = info.mAutomatonBDD.getAutomaton();
      return aut1.compareTo(aut2);
    }

    //#######################################################################
    //# Auxiliary Methods
    private int getCloudSize()
    {
      if (mCloudSize < 0) {
        final Collection<AutomatonInfo> cloud = new THashSet<AutomatonInfo>();
        for (final EventInfo event : mEvents) {
          cloud.addAll(event.getAutomata());
        }
        mCloudSize = 0;
        for (final AutomatonInfo aut : cloud) {
          final AutomatonBDD autBDD = aut.getAutomatonBDD();
          mCloudSize += autBDD.getNumberOfStateBits();
        }
      }
      return mCloudSize;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonBDD mAutomatonBDD;
    private final Collection<EventInfo> mEvents;
    private int mCloudSize;
  }


  //#########################################################################
  //# Data Members
  private final Map<EventProxy,EventInfo> mEventMap;
  private TransitionPartitionBDD mCurrent;
  private TransitionPartitionBDD mOldestStable;
  private boolean mCurrentIsStable;
  private Iterator<TransitionPartitionBDD> mIterator;


  //#########################################################################
  //# Class Constants
  /**
   * Threshold for alphabet size. Do not consider automata with a large
   * number of events as partition candidates. If an automaton has more
   * than the given share of the events of the model, it is ignored.
   */
  private static final double THRESHOLD = 0.4;

}
