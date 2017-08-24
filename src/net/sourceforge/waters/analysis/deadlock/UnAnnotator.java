//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.deadlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.annotation.AnnotatedMemStateProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;


public class UnAnnotator
{
  private final GeneralizedTransitionRelation mTransitionRelation;

  public UnAnnotator(final GeneralizedTransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
  }

  public AutomatonProxy run(final ProductDESProxyFactory factory,
                            final ProductDESProxy des, final EventProxy[] eventsToHide)
  {
    mTransitionRelation.removeUnnecessaryEvents();

    if(eventsToHide !=null)
    removePropWithEvent(eventsToHide);

    final Set<EventProxy> desEvents = des.getEvents();
    final Set<EventProxy> desProps = new HashSet<EventProxy>();
    for (final EventProxy ep : desEvents) {
      if (ep.getKind()==EventKind.PROPOSITION) {
        desProps.add(ep);
      }
    }

    final Map<TIntHashSet,TIntArrayList> statesWithAnnotation =
      new THashMap<TIntHashSet,TIntArrayList>();
    final List<TransitionProxy> newTransitions =
      new ArrayList<TransitionProxy>();
  //  final List<StateProxy> nextStates = new ArrayList<StateProxy>();
    final StateProxy[] nextStates = new StateProxy[mTransitionRelation.numberOfStates()];
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if(! mTransitionRelation.hasPredecessors(s))
         continue;

      final Set<TIntHashSet> annotations =
        mTransitionRelation.getAnnotation(s);
      if (annotations == null) {
        continue;
      }
      for (final TIntHashSet ann : annotations) {
        TIntArrayList states = statesWithAnnotation.get(ann);
        if (states == null) {
          states = new TIntArrayList();
          statesWithAnnotation.put(ann, states);
        }
        states.add(s);
      }
    }
    final TIntObjectHashMap<TIntArrayList> newStates =
      new TIntObjectHashMap<TIntArrayList>();
    final List<int[]> newtransitionsI = new ArrayList<int[]>();
    final Collection<EventProxy> eventsset = mTransitionRelation.getEvents();
    int statenum = 0;
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      if(! mTransitionRelation.hasPredecessors(s))
        continue;
      final TIntArrayList states = new TIntArrayList();
      states.add(statenum);
      newStates.put(s, states);
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {
          continue;
        }
        final int[] array = succs.toArray();
        for (int ti = 0; ti < array.length; ti++) {
          final int t = array[ti];
          newtransitionsI.add(new int[] {s, e, t});
        }
      }

      // get props for this state
      final Set<EventProxy> stateProp = new HashSet<EventProxy>();
       Set<TIntHashSet> annotations =
        mTransitionRelation.getAnnotation(s);
      if (annotations == null) {
      //  continue;
        annotations = new HashSet<TIntHashSet>();
     }
      for (final TIntHashSet ann : annotations) {
        for (final EventProxy ep : desProps) {
          final String[] tokens = ep.getName().split(":");
          boolean match = true;
          if (tokens.length == ann.size() + 1) {
            for (final TIntIterator it = ann.iterator(); it.hasNext();) {
              final int e = it.next();
              final EventProxy event = mTransitionRelation.getEvent(e);
              if (!Arrays.asList(tokens).contains(event.getName())) {
                match = false;
              }
            }
            if (match) {
              stateProp.add(ep);
              eventsset.add(ep);
            }
          }

        }
      }

      final boolean isInitial = mTransitionRelation.isInitial(s);
      final StateProxy sp =
        new AnnotatedMemStateProxy(statenum, stateProp, isInitial);
      //nextStates.add(sp);
      nextStates[s]= sp;
      statenum++;
      //assert (statenum == nextStates.size());
    }

    /*
     * for (final TIntHashSet ann : statesWithAnnotation.keySet()) { final
     * int[] states = statesWithAnnotation.get(ann).toArray(); for (int i = 0;
     * i < states.length; i++) { final int state = states[i];
     * Collection<EventProxy> used = notmarked; final TIntIterator it =
     * ann.iterator(); while (it.hasNext()) { final int event = it.next(); if
     * (mTransitionRelation.isMarkingEvent(event)) { used = markedcol;
     * continue; } final TIntHashSet succs =
     * mTransitionRelation.getSuccessors(state, event); final int[] array =
     * succs.toArray(); for (int suci = 0; suci < array.length; suci++) {
     * final int suc = array[suci]; newtransitionsI.add(new int[]{statenum,
     * event, suc}); } } final TIntArrayList sharedstates =
     * newStates.get(state); sharedstates.add(statenum); if (!containsmarked)
     * { used = notmarked; } final StateProxy sp = new
     * AnnotatedMemStateProxy(statenum, used, false);
     * //System.out.println("marking:" + used); nextStates.add(sp);
     * statenum++; assert(statenum == nextStates.size()); } }
     */
    // eventsset.add(tau);
    for (final int[] t : newtransitionsI) {
      final int s = t[0];
      final int e = t[1];
      final int ot = t[2];
//      final StateProxy source = nextStates.get(s);
      final StateProxy source = nextStates[s];
      final EventProxy event = mTransitionRelation.getEvent(e);
      //final StateProxy target = nextStates.get(ot);
      final StateProxy target = nextStates[ot];
      newTransitions
        .add(factory.createTransitionProxy(source, event, target));
    }
    //final EventProxy tau = factory.createEventProxy("tau:" + mTransitionRelation.getName(), EventKind.UNCONTROLLABLE);
    /*
     * for (int i = 0; i < newStates.size(); i++) { final StateProxy source =
     * nextStates.get(i); final TIntArrayList taus = newStates.get(i); for
     * (int j = 0; j < taus.size(); j++) { final int state = taus.get(j); if
     * (i == state) {continue;} final StateProxy target =
     * nextStates.get(state);
     * newTransitions.add(factory.createTransitionProxy(source, tau, target));
     * } }
     */
    // final Collection<EventProxy> eventsset = mTransitionRelation.getEvents();
    // eventsset.add(tau);
    /*
     * AutomatonProxy aut =
     * factory.createAutomatonProxy(mTransitionRelation.getName(),
     * ComponentKind.PLANT, eventsset, nextStates, newTransitions);
     */
    final List<StateProxy> nextStates1 = new ArrayList<StateProxy>();
    for (int i=0; i<nextStates.length; i++) {
      if(nextStates[i] != null)
          nextStates1.add(nextStates[i]);
    }
    final AutomatonProxy aut =
      factory.createAutomatonProxy("after", ComponentKind.PLANT, eventsset,
                                   nextStates1, newTransitions);

    return aut;
  }


  public void removePropWithEvent(final EventProxy[] events) {
    //final EventProxy event = this.getEvent(index);
    for (int i=0; i<events.length; i++){
    final EventProxy event = events[i];
    if(event == null)
      continue;
    for (final EventProxy ep : mTransitionRelation.getEvents()) {
      if(ep ==null)
        continue;
      if (ep.getKind()==EventKind.PROPOSITION) {
        final String[] tokens = ep.getName().split(":");
        if (Arrays.asList(tokens).contains(event.getName())) {
          mTransitionRelation.removeEvent(mTransitionRelation.eventToInt(ep));
        }
      }
    }
    }
  }

}
