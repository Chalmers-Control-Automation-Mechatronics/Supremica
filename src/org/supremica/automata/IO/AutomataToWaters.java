//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Supremica Automata
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   AutomataToWaters
//###########################################################################
//# $Id$
//###########################################################################

/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.IO;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;


/**
 * A utility class to convert a Supremica model to Waters.
 *
 * This class creates a new {@link ProductDESProxy} representing a
 * given Supremica {@link Automata} object. In copying, it also ensures
 * object identity for events and states, which is not guaranteed for
 * Supremica models even though they implement the {@link ProductDESProxy}
 * interface.
 *
 * @author Robi Malik
 */

public class AutomataToWaters
{

  //#########################################################################
  //# Constructors
  public AutomataToWaters(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mMarkedProposition =
      factory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                               EventKind.PROPOSITION);
    mForbiddenProposition =
      factory.createEventProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME,
                               EventKind.PROPOSITION);
    mMarkedPropositions = Collections.singletonList(mMarkedProposition);
    mForbiddenPropositions = Collections.singletonList(mForbiddenProposition);
    mMarkedAndForbiddenPropositions = new ArrayList<EventProxy>(2);
    mMarkedAndForbiddenPropositions.add(mMarkedProposition);
    mMarkedAndForbiddenPropositions.add(mForbiddenProposition);
  }

  public AutomataToWaters(final ProductDESProxyFactory factory,
                          final ProductDESProxy context,
                          EventProxy defaultMarking)
  {
    mFactory = factory;
    EventProxy forbiddenMarking = null;
    final Collection<EventProxy> events = context.getEvents();
    final int numEvents = events.size();
    mEventList = new ArrayList<EventProxy>(numEvents);
    mEventMap = new HashMap<String,EventProxy>(numEvents);
    for (final EventProxy event : events) {
      final String name = event.getName();
      if (event.getKind() == EventKind.PROPOSITION) {
        if (name.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          if (defaultMarking == null) {
            defaultMarking = event;
          }
        } else if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          forbiddenMarking = event;
        }
      }
      mEventList.add(event);
      mEventMap.put(name, event);
    }
    mUsingFakeMarking = defaultMarking == null;
    mMarkedProposition = mUsingFakeMarking ?
      factory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                               EventKind.PROPOSITION) :
      defaultMarking;
    mForbiddenProposition = forbiddenMarking == null ?
      factory.createEventProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME,
                               EventKind.PROPOSITION) :
      forbiddenMarking;
    mMarkedPropositions = Collections.singletonList(mMarkedProposition);
    mForbiddenPropositions = Collections.singletonList(mForbiddenProposition);
    mMarkedAndForbiddenPropositions = new ArrayList<EventProxy>(2);
    mMarkedAndForbiddenPropositions.add(mMarkedProposition);
    mMarkedAndForbiddenPropositions.add(mForbiddenProposition);
  }


  //#########################################################################
  //# Configuration
  public void setSuppressingRedundantSelfloops(final boolean suppress)
  {
    mSuppressingRedundantSelfloops = suppress;
  }

  public boolean isSuppressingRedundantMarkingSelfloops()
  {
    return mSuppressingRedundantSelfloops;
  }


  //#########################################################################
  //# Invocation
  public ProductDESProxy convertAutomata(final Automata automata)
  {
    final boolean shared = mEventMap != null;
    try {
      if (!shared) {
        mEventList = new ArrayList<EventProxy>();
        mEventMap = new HashMap<String,EventProxy>();
      }
      final String name = automata.getName();
      final String comment = automata.getCommentOrNull();
      final int numAutomata = automata.size();
      final List<AutomatonProxy> proxies =
        new ArrayList<AutomatonProxy>(numAutomata);
      for(final Automaton aut : automata) {
        final AutomatonProxy proxy = convertAutomaton(aut);
        proxies.add(proxy);
      }
      Collections.sort(mEventList);
      return mFactory.createProductDESProxy(name, comment, null,
                                            mEventList, proxies);
    } finally {
      if (!shared) {
        mEventList = null;
        mEventMap = null;
      }
    }
  }

  public AutomatonProxy convertAutomaton(final Automaton aut)
  {
    final boolean shared = mEventMap != null;
    try {
      final String aname = aut.getName();
      final ComponentKind akind = aut.getKind();
      if (!shared) {
        final Alphabet alphabet = aut.getAlphabet();
        final int numEvents = alphabet.size();
        mEventMap = new HashMap<>(numEvents);
        mEventList = new ArrayList<>(numEvents);
      }
      final List<EventProxy> events = getNonRedundantAlphabet(aut);
      final Set<EventProxy> eventSet = new THashSet<>(events);
      final boolean marking = eventSet.contains(mMarkedProposition);
      final int numStates = aut.nbrOfStates();
      final Map<State,StateProxy> stateMap =
        new HashMap<State,StateProxy>(numStates);
      final List<StateProxy> states = new ArrayList<StateProxy>(numStates);
      for (final State state : aut) {
        final String name = state.getName();
        final boolean init = state.isInitial();
        final boolean marked = marking && state.isAccepting();
        final boolean forbidden = state.isForbidden();
        final Collection<EventProxy> props = getPropositions(marked, forbidden);
        final StateProxy sproxy = mFactory.createStateProxy(name, init, props);
        states.add(sproxy);
        stateMap.put(state, sproxy);
      }
      final Collection<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>();
      for (final State source : aut) {
        final StateProxy sproxy = stateMap.get(source);
        final Iterator<Arc> outgoingArcsIt = source.outgoingArcsIterator();
        while (outgoingArcsIt.hasNext()) {
          final Arc arc = outgoingArcsIt.next();
          final LabeledEvent label = arc.getEvent();
          final String ename = label.getName();
          final EventProxy event = mEventMap.get(ename);
          if (eventSet.contains(event)) {
            final State target = arc.getToState();
            final StateProxy tproxy = stateMap.get(target);
            final TransitionProxy trans =
              mFactory.createTransitionProxy(sproxy, event, tproxy);
            transitions.add(trans);
          }
        }
      }
      return mFactory.createAutomatonProxy(aname, akind,
                                           events, states, transitions);
    } finally {
      if (!shared) {
        mEventMap = null;
        mEventList = null;
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventProxy createEvent(final String name,
                                 final EventKind kind,
                                 final boolean observable)
  {
    final EventProxy found = mEventMap.get(name);
    if (found == null) {
      final EventProxy event =
        mFactory.createEventProxy(name, kind, observable);
      mEventMap.put(name, event);
      mEventList.add(event);
      return event;
    } else if (found.getKind() == kind && found.isObservable() == observable) {
      return found;
    } else {
      final String msg = "Inconsistent occurrences of event '" + name + "'!";
      throw new DuplicateNameException(msg);
    }
  }

  private Collection<EventProxy> getPropositions(final boolean marked,
                                                 final boolean forbidden)
  {
    if (!marked) {
      if (!forbidden) {
        return Collections.emptyList();
      } else {
        return mForbiddenPropositions;
      }
    } else {
      if (!forbidden) {
        return mMarkedPropositions;
      } else {
        return mMarkedAndForbiddenPropositions;
      }
    }
  }

  private List<EventProxy> getNonRedundantAlphabet(final Automaton aut)
  {
    final Alphabet alphabet = aut.getAlphabet();
    final int numEvents = alphabet.size();
    final List<EventProxy> events = new ArrayList<>(numEvents);
    for (final LabeledEvent label : alphabet) {
      final String name = label.getName();
      final EventKind kind = label.getKind();
      final boolean observable = label.isObservable();
      final EventProxy event = createEvent(name, kind, observable);
      events.add(event);
    }
    if (mSuppressingRedundantSelfloops || mUsingFakeMarking) {
      boolean marking = false;
      boolean forbidden = false;
      final Collection<EventProxy> selfloopOnlyEvents =
        mSuppressingRedundantSelfloops ? new THashSet<>(events) : Collections.emptyList();
      for (final State state : aut) {
        marking |= !state.isAccepting();
        forbidden |= state.isForbidden();
        if (!selfloopOnlyEvents.isEmpty()) {
          final Collection<EventProxy> disabledEvents = new THashSet<>(events);
          final Iterator<Arc> iter = state.outgoingArcsIterator();
          while (iter.hasNext()) {
            final Arc arc = iter.next();
            final LabeledEvent label = arc.getEvent();
            final String name = label.getName();
            final EventProxy event = mEventMap.get(name);
            disabledEvents.remove(event);
            if (arc.getToState() != state) {
              selfloopOnlyEvents.remove(event);
            }
          }
          selfloopOnlyEvents.removeAll(disabledEvents);
        }
      }
      events.removeAll(selfloopOnlyEvents);
      if (marking) {
        registerMarkedProposition();
        events.add(mMarkedProposition);
      }
      if (forbidden) {
        registerForbiddenProposition();
        events.add(mForbiddenProposition);
      }
    } else {
      registerMarkedProposition();
      events.add(mMarkedProposition);
      for (final State state : aut) {
        if (state.isForbidden()) {
          registerForbiddenProposition();
          events.add(mForbiddenProposition);
          break;
        }
      }
    }
    return events;
  }

  private void registerMarkedProposition()
  {
    if (mEventMap.put(mMarkedProposition.getName(), mMarkedProposition) == null) {
      mEventList.add(0, mMarkedProposition);
    }
  }

  private void registerForbiddenProposition()
  {
    if (mEventMap.put(mForbiddenProposition.getName(), mForbiddenProposition) == null) {
      mEventList.add(mForbiddenProposition);
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final EventProxy mMarkedProposition;
  private final EventProxy mForbiddenProposition;
  private final Collection<EventProxy> mMarkedPropositions;
  private final Collection<EventProxy> mForbiddenPropositions;
  private final Collection<EventProxy> mMarkedAndForbiddenPropositions;

  private boolean mSuppressingRedundantSelfloops = false;
  private boolean mUsingFakeMarking = false;

  private List<EventProxy> mEventList;
  private Map<String,EventProxy> mEventMap;

}
