//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class RemoveEvents
{
  final private AutomatonProxy mAut;
  final ProductDESProxyFactory mFactory;
  final TIntHashSet[][] mSuccs;
  TIntHashSet[] mActivePreds;
  final EventProxy[] mEvents;
  final EventProxy mMarked;
  final boolean[] mInitial;
  final boolean[][] mImPossible;
  final Set<EventProxy> mImpossibleEvent;
  final Set<EventProxy> mNotLocal;

  public RemoveEvents(final AutomatonProxy aut, final Map<EventProxy,
                                              Map<EventProxy,
                                                  Set<Set<EventProxy>>>> canthappen,
                      final EventProxy marked,
                      final ProductDESProxyFactory factory)
  {
    mMarked = marked;
    mAut = aut;
    mFactory = factory;
    mSuccs = new TIntHashSet[aut.getStates().size()][aut.getEvents().size()];
    mActivePreds = new TIntHashSet[aut.getStates().size()];
    mEvents = new EventProxy[aut.getEvents().size()];
    mImpossibleEvent = new THashSet<EventProxy>();
    mNotLocal = new THashSet<EventProxy>();
    mImPossible = new boolean[aut.getStates().size()][aut.getEvents().size()];
    mInitial = new boolean[aut.getStates().size()];
    final TObjectIntHashMap<StateProxy> statetoint =
      new TObjectIntHashMap<StateProxy>();
    final TObjectIntHashMap<EventProxy> eventtoint =
      new TObjectIntHashMap<EventProxy>();
    int i = 0;
    for (int s = 0; s < mSuccs.length; s++) {
      for (int e = 0; e < mSuccs[s].length; e++) {
        mSuccs[s][e] = new TIntHashSet();
      }
      mActivePreds[s] = new TIntHashSet();
    }
    for (final StateProxy state : aut.getStates()) {
      if (state.isInitial()) {
        mInitial[i] = true;
      }
      statetoint.put(state, i); i++;
    }
    i = 0;
    for (final EventProxy event : aut.getEvents()) {
      mEvents[i] = event;
      eventtoint.put(event, i);
      i++;
    }
    for (final TransitionProxy tran : aut.getTransitions()) {
      final int source = statetoint.get(tran.getSource());
      final int target = statetoint.get(tran.getTarget());
      final int event = eventtoint.get(tran.getEvent());
      mSuccs[source][event].add(target);
      if (source != target) {mActivePreds[target].add(event);}
    }
    //calculatePossible(canthappen);
  }

  public void calculatePossible(final Map<EventProxy,
                                    Map<EventProxy,
                                        Set<Set<EventProxy>>>> eventmap)
  {
    for (int s = 0; s < mInitial.length; s++) {
      if (mInitial[s]) {continue;}
      ACTIVE:
      for (int ei = 0; ei < mEvents.length; ei++) {
        final EventProxy event = mEvents[ei];
        final Map<EventProxy, Set<Set<EventProxy>>> tups = eventmap.get(event);
        if (tups == null || tups.isEmpty()) {continue;}
        final Set<EventProxy> selflooped = new THashSet<EventProxy>();
        final Set<EventProxy> incoming = new THashSet<EventProxy>();
        for (int e = 0; e < mEvents.length; e++) {
          final EventProxy eventpred = mEvents[e];
          if (mSuccs[s][e].contains(s)) {
            selflooped.add(eventpred);
          }
          if (!mActivePreds[s].contains(e)) {continue;}
          if (!tups.containsKey(eventpred)) {continue ACTIVE;}
          incoming.add(eventpred);
        }
        INCOMING:
        for (final EventProxy e : incoming) {
          final Set<Set<EventProxy>> required = tups.get(e);
          REQUIRED:
          for (final Set<EventProxy> req : required) {
            //if (!tr.getEvents().containsAll(required)) {continue;}
            for (final EventProxy self : selflooped) {
              if (req.contains(self)) {continue REQUIRED;}
            }
            // has no selfloops for this requires, next event
            continue INCOMING;
          }
          // failed each requires, can't do
          continue ACTIVE;
        }
        //System.out.println("removed transitions");
        //tr.removeOutgoing(s, arr[i]);
        mImPossible[s][ei] = true;
      }
    }
  }

  public void run()
  {
    Events:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e].equals(mMarked)) {continue;}
      boolean impossible = true;
      boolean notlocal = true;
      for (int s = 0; s < mSuccs.length; s++) {
        //if (mImPossible[s][e]) {continue;}
        if (!mSuccs[s][e].isEmpty()) {impossible = false;}
        if (!mSuccs[s][e].contains(s) || mSuccs[s][e].size() > 1) {
          notlocal = false;
        }
        if (!impossible && !notlocal) {continue Events;}
      }
      if (impossible) {mImpossibleEvent.add(mEvents[e]); System.out.println("impossible");}
      if (notlocal) {mNotLocal.add(mEvents[e]); System.out.println("notlocal");}
    }
  }

  public AutomatonProxy getAutomaton()
  {
    if (mNotLocal.isEmpty()) {return mAut;}
    final String name = mAut.getName();
    final Collection<EventProxy> mEvents = new THashSet<EventProxy>(mAut.getEvents());
    mEvents.removeAll(mNotLocal);
    final Collection<TransitionProxy> trans = new ArrayList<TransitionProxy>(mAut.getTransitions());
    final Iterator<TransitionProxy> it = trans.iterator();
    while (it.hasNext()) {
      final TransitionProxy tran = it.next();
      if (mNotLocal.contains(tran.getEvent())) {it.remove();}
    }
    final AutomatonProxy result = mFactory.createAutomatonProxy(name,
                                                          ComponentKind.PLANT,
                                                          mEvents,
                                                          mAut.getStates(), trans);
    return result;
  }

  public Set<EventProxy> getImpossible()
  {
    return mImpossibleEvent;
  }
}
