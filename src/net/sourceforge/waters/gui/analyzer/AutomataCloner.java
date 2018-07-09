//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * @author George Hewlett
 */

public class AutomataCloner
{

  //#########################################################################
  //# Constructors
  public AutomataCloner(final ProductDESProxyFactory factory,
                        final Map<String,EventProxy> EventMap)
  {
    mFactory = factory;
    mEventMap = EventMap;
  }

  public AutomataCloner(final ProductDESProxyFactory factory)
  {
    // TODO Instead use: use this(factory, new HashMap<String,EventProxy>();
    mFactory = factory;
    mEventMap = null;
  }

  //#########################################################################
  //# Invocation
  public AutomatonProxy clone(final AutomatonProxy aut)
  {
    final AutomatonProxy clonedAut;
    final Set<EventProxy> eventList = aut.getEvents();
    final Set<StateProxy> stateList = aut.getStates();
    final Collection<TransitionProxy> transitionList = aut.getTransitions();

    final Collection<EventProxy> copiedEvents = new ArrayList<EventProxy>(eventList.size());
    final Collection<StateProxy> copiedStates = new ArrayList<StateProxy>(stateList.size());
    final Collection<TransitionProxy> copiedTranisitions =
      new ArrayList<TransitionProxy>(transitionList.size());

    // TODO No need to distinguish if event map initialised in constructor
    if (mEventMap == null) {
      mEventMap = new HashMap<String,EventProxy>();
      for (final EventProxy ep : eventList) {
        final EventProxy copiedEP =
          mFactory.createEventProxy(ep.getName(), ep.getKind(),
                                    ep.isObservable(), ep.getAttributes());
        mEventMap.put(copiedEP.getName(), copiedEP);
        copiedEvents.add(copiedEP);
      }
    } else {
      for (final EventProxy ep : eventList) {
        final EventProxy copiedEP =
          mFactory.createEventProxy(ep.getName(), ep.getKind(),
                                    ep.isObservable(), ep.getAttributes());
        // TODO Look up by name
        if (!mEventMap.containsValue(ep)) {
          mEventMap.put(copiedEP.getName(), copiedEP);
        }
        copiedEvents.add(copiedEP);
      }
    }
    // TODO Create and use state map
    for (final StateProxy sp : stateList) {
      final StateProxy copiedSP = mFactory
        .createStateProxy(sp.getName(), sp.isInitial(), sp.getPropositions());
      copiedStates.add(copiedSP);
    }
    for (final TransitionProxy tp : transitionList) {
//      if (copiedStates.contains(tp.getSource())
//          && copiedStates.contains(tp.getTarget())
//          && copiedEvents.contains(tp.getEvent())) {

        final StateProxy source = tp.getSource();
        final StateProxy target = tp.getTarget();
        final EventProxy event = tp.getEvent();
        final StateProxy newSource =
          mFactory.createStateProxy(source.getName(), source.isInitial(),
                                    source.getPropositions());
        final StateProxy newTarget =
          mFactory.createStateProxy(target.getName(), target.isInitial(),
                                    target.getPropositions());
        final EventProxy newEvent = mEventMap.get(event.getName());

        if(mStateMap == null)
          mStateMap = new HashMap<StateProxy,StateProxy>();
        mStateMap.put(newSource, newTarget);
        final TransitionProxy copiedTP =
          mFactory.createTransitionProxy(newSource, newEvent, newTarget);
        copiedTranisitions.add(copiedTP);
//      }
    }

    clonedAut = mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                              copiedEvents, copiedStates,
                                              copiedTranisitions);

    //      for (final StateProxy sp : stateList) {
    //        copiedStates.add(sp);
    //      }
    //      for (final TransitionProxy tp : transitionList) {
    //        if (copiedStates.contains(tp.getSource())
    //            && copiedStates.contains(tp.getTarget())
    //            && copiedEvents.contains(tp.getEvent())) {
    //          mStateMap.put(tp.getSource(), tp.getTarget());
    //          copiedTranisitions.add(tp);
    //        }
    //      }
    //
    //      clonedAut = mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
    //                                                copiedEvents, copiedStates,
    //                                                copiedTranisitions);

    return clonedAut;
  }

  public List<Proxy> getClonedList(final Collection<? extends Proxy> autList)
  {
    final List<Proxy> outputList = new ArrayList<Proxy>();
    for (final Proxy aut : autList) {
      outputList.add(clone((AutomatonProxy)aut));
    }
    return outputList;
  }

  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private Map<String,EventProxy> mEventMap;
  // TODO State map should be local variable---must be reset for each automaton
  private Map<StateProxy,StateProxy> mStateMap;

}
