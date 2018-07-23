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
    this(factory, new HashMap<String,EventProxy>());
  }

  //#########################################################################
  //# Invocation
  public AutomatonProxy clone(final AutomatonProxy aut, final String newName)
  {
    final AutomatonProxy clonedAut;
    final Map<StateProxy,StateProxy> mStateMap = new HashMap<StateProxy,StateProxy>();
    final Set<EventProxy> eventList = aut.getEvents();
    final Set<StateProxy> stateList = aut.getStates();
    final Collection<TransitionProxy> transitionList = aut.getTransitions();

    final Collection<EventProxy> copiedEvents =
      new ArrayList<EventProxy>(eventList.size());
    final Collection<StateProxy> copiedStates =
      new ArrayList<StateProxy>(stateList.size());
    final Collection<TransitionProxy> copiedTranisitions =
      new ArrayList<TransitionProxy>(transitionList.size());

    for (final EventProxy ep : eventList) {
      final String name = ep.getName();
      EventProxy newEvent = mEventMap.get(name);
      if (newEvent == null) {
        newEvent =
          mFactory.createEventProxy(ep.getName(), ep.getKind(),
                                    ep.isObservable(), ep.getAttributes());
        mEventMap.put(name, newEvent);
      }
      copiedEvents.add(newEvent);
    }
    for (final StateProxy sp : stateList) {
      final Collection<EventProxy> copiedPropostions =
        new ArrayList<EventProxy>();
      for (final EventProxy ep : sp.getPropositions())
        copiedPropostions.add(mEventMap.get(ep.getName()));
      final StateProxy copiedSP = mFactory
        .createStateProxy(sp.getName(), sp.isInitial(), copiedPropostions);
      copiedStates.add(copiedSP);
      mStateMap.put(sp, copiedSP);
    }
    for (final TransitionProxy tp : transitionList) {
      final StateProxy source = tp.getSource();
      final StateProxy target = tp.getTarget();
      final EventProxy event = tp.getEvent();
      final StateProxy newSource = mStateMap.get(source);
      final StateProxy newTarget = mStateMap.get(target);
      final EventProxy newEvent = mEventMap.get(event.getName());

      final TransitionProxy copiedTP =
        mFactory.createTransitionProxy(newSource, newEvent, newTarget);
      copiedTranisitions.add(copiedTP);
    }

    if (newName == null)
      clonedAut = mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                                copiedEvents, copiedStates,
                                                copiedTranisitions);
    else
      clonedAut =
        mFactory.createAutomatonProxy(newName, aut.getKind(), copiedEvents,
                                      copiedStates, copiedTranisitions);

    return clonedAut;
  }

  public AutomatonProxy clone(final AutomatonProxy aut)
  {
    return clone(aut, null);
  }

  public List<Proxy> getClonedList(final Collection<? extends Proxy> autList)
  {
    final List<Proxy> outputList = new ArrayList<Proxy>();
    for (final Proxy aut : autList) {
      outputList.add(clone((AutomatonProxy) aut));
    }
    return outputList;
  }

  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final Map<String,EventProxy> mEventMap;

}
