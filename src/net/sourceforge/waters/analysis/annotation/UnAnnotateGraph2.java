//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class UnAnnotateGraph2
{
  private final TransitionRelation mTransitionRelation;
  private final EventProxy mMarked;

  public UnAnnotateGraph2(final TransitionRelation transitionrelation, final EventProxy marked)
  {
    mTransitionRelation = transitionrelation;
    mMarked = marked;
  }

  public AutomatonProxy run(final ProductDESProxyFactory factory)
  {
    final boolean containsmarked = mTransitionRelation.getEvents().contains(mMarked);
    final Map<TIntHashSet, TIntArrayList> statesWithAnnotation =
      new THashMap<TIntHashSet, TIntArrayList>();
    final List<TransitionProxy> newTransitions = new ArrayList<TransitionProxy>();
    final List<StateProxy> nextStates = new ArrayList<StateProxy>();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final Set<TIntHashSet> annotations = mTransitionRelation.getAnnotation(s);
      if (annotations == null) {continue;}
      //System.out.println("has annotation size:" + annotations.size());
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
    final Collection<EventProxy> markedcol = Collections.singleton(mMarked);
    final Collection<EventProxy> notmarked = Collections.emptySet();
    final List<int[]> newtransitionsI = new ArrayList<int[]>();
    int statenum = 0;
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TIntArrayList states = new TIntArrayList();
      states.add(statenum);
      newStates.put(s, states);
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {continue;}
        final int[] array = succs.toArray();
        for (int ti = 0; ti < array.length; ti++) {
          final int t = array[ti];
          newtransitionsI.add(new int[]{s, e, t});
        }
      }
      Collection<EventProxy> used = mTransitionRelation.isMarked(s) ?
                                    markedcol : notmarked;
      final boolean isInitial = mTransitionRelation.isInitial(s);
      if (!containsmarked) {
        used = notmarked;
      }
      final StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
      nextStates.add(sp);
      statenum++;
      assert(statenum == nextStates.size());
    }
    for (final TIntHashSet ann : statesWithAnnotation.keySet()) {
      final int[] states = statesWithAnnotation.get(ann).toArray();
      for (int i = 0; i < states.length; i++) {
        final int state = states[i];
        Collection<EventProxy> used = notmarked;
        final TIntIterator it = ann.iterator();
        while (it.hasNext()) {
          final int event = it.next();
          if (mTransitionRelation.isMarkingEvent(event)) {
            used = markedcol;
            continue;
          }
          final TIntHashSet succs = mTransitionRelation.getSuccessors(state, event);
          final int[] array = succs.toArray();
          for (int suci = 0; suci < array.length; suci++) {
            final int suc = array[suci];
            newtransitionsI.add(new int[]{statenum, event, suc});
          }
        }
        final TIntArrayList sharedstates = newStates.get(state);
        sharedstates.add(statenum);
        if (!containsmarked) {
          used = notmarked;
        }
        final StateProxy sp = new AnnotatedMemStateProxy(statenum, used, false);
        //System.out.println("marking:" + used);
        nextStates.add(sp);
        statenum++;
        assert(statenum == nextStates.size());
      }
    }
    for (final int[] t : newtransitionsI) {
      final int s = t[0];
      final int e = t[1];
      final int ot = t[2];
      final StateProxy source = nextStates.get(s);
      final EventProxy event = mTransitionRelation.getEvent(e);
      final StateProxy target = nextStates.get(ot);
      newTransitions.add(factory.createTransitionProxy(source, event, target));
    }
    final EventProxy tau = factory.createEventProxy("tau:" + mTransitionRelation.getName(), EventKind.UNCONTROLLABLE);
    for (int i = 0; i < newStates.size(); i++) {
      final StateProxy source = nextStates.get(i);
      final TIntArrayList taus = newStates.get(i);
      for (int j = 0; j < taus.size(); j++) {
        final int state = taus.get(j);
        if (i == state) {continue;}
        final StateProxy target = nextStates.get(state);
        newTransitions.add(factory.createTransitionProxy(source, tau, target));
      }
    }
    final Collection<EventProxy> eventsset = mTransitionRelation.getEvents();
    eventsset.add(tau);
    return factory.createAutomatonProxy(mTransitionRelation.getName(),
                                        ComponentKind.PLANT,
                                        eventsset,
                                        nextStates, newTransitions);
  }
}
