//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   Hiding
//###########################################################################
//# $Id: Hiding.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class Hiding {

  //#########################################################################
  //# Invocation
  public static AutomatonProxy hide(final AutomatonProxy aut,
                                    final Collection<EventProxy> events,
                                    final boolean conflicteq,
                                    final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    return hide(aut, events, name, conflicteq, factory);
  }

  public static AutomatonProxy hide(final AutomatonProxy aut,
                                    final Collection<EventProxy> events,
                                    final String resultname,
                                    final boolean conflicteq,
                                    final ProductDESProxyFactory factory)
  {
    final Hiding solver =
      new Hiding(aut, events, resultname, conflicteq, factory);
    return solver.run();
  }


  //#########################################################################
  //# Constructors
  private Hiding(final AutomatonProxy aut,
                 final Collection<EventProxy> events,
                 final String resultname,
                 final boolean conflicteq,
                 final ProductDESProxyFactory factory)
  {
    mConflictEq = conflicteq;
    mAutomaton = aut;
    mResultName = resultname;
    mFactory = factory;
    mHiddenEvents = new IdentityHashMap<EventProxy,EventProxy>(events.size());
    for (final EventProxy event : events) {
      mHiddenEvents.put(event, event);
    }
    final Collection<StateProxy> states = mAutomaton.getStates();
    mStateMap = new IdentityHashMap<StateProxy,StateInfo>(states.size());
  }


  //#########################################################################
  //# Workhorse Methods
  private AutomatonProxy run()
  {
    final ComponentKind kind = mAutomaton.getKind();
    final Collection<StateProxy> states = mAutomaton.getStates();
    final Collection<TransitionProxy> transitions =
      mAutomaton.getTransitions();
    final Collection<TransitionProxy> temptrans =
      new HashSet<TransitionProxy>(transitions.size());
    final EventProxy tau =
      mFactory.createEventProxy("tau", EventKind.CONTROLLABLE, false);
    Collection<TransitionProxy> opentrans = new HashSet<TransitionProxy>();

    for (final TransitionProxy trans : transitions) {
      final StateProxy source = trans.getSource();
      final StateProxy target = trans.getTarget();
      final EventProxy event = trans.getEvent();
      if (!isHidden(event)) {
        final TransitionProxy newtrans =
          mFactory.createTransitionProxy(source, event, target);
        temptrans.add(newtrans);
      } else if (source != target) {
        final TransitionProxy newtrans =
          mFactory.createTransitionProxy(source, tau, target);
        opentrans.add(newtrans);
      }
    }
    final AdjacencyMap adjacency = new AdjacencyMap(states);
    adjacency.addTransitions(temptrans);
    adjacency.addTransitions(opentrans);

    for (final StateProxy state : states) {
      if (mConflictEq && !state.isInitial()) {
        final Collection<EventProxy> elig = adjacency.getEligibleEvents(state);
        boolean hidden = true;
        for (final EventProxy event : elig) {
          hidden = hidden && (event == tau);
        }
        if (hidden) {
          continue;
        }
      }
      final StateInfo stateinfo = new StateInfo(state);
      mStateMap.put(state, stateinfo);
    }

    while (!opentrans.isEmpty()) {
      final Collection<TransitionProxy> newopentrans =
        new HashSet<TransitionProxy>();
      for (final TransitionProxy trans : opentrans) {
        final StateProxy source = trans.getSource();
        final StateProxy target = trans.getTarget();
        final boolean initial = source.isInitial();
        final Collection<EventProxy> props = target.getPropositions();
        setInitial(target, initial);
        setMarked(source, props);
        final Collection<TransitionProxy> entering =
	  adjacency.getEnteringTransitions(source);
	for (final TransitionProxy entertrans : entering) {
          final StateProxy entersource = entertrans.getSource();
          final EventProxy event = entertrans.getEvent();
          final TransitionProxy newtrans =
            mFactory.createTransitionProxy(entersource, event, target);
          if (event != tau) {
            temptrans.add(newtrans);
          } else if (entersource != target) {
            newopentrans.add(newtrans);
          }
        }
        if (!isHidden(source)) {
          final Collection<TransitionProxy> exiting =
	    adjacency.getExitingTransitions(target);
	  for (final TransitionProxy exittrans : exiting) {
	    final StateProxy exittarget = exittrans.getTarget();
            final EventProxy event = exittrans.getEvent();
            final TransitionProxy newtrans =
              mFactory.createTransitionProxy(source, event, exittarget);
            if (event != tau) {
              temptrans.add(newtrans);
            } else if (initial && source != exittarget) {
              newopentrans.add(newtrans);
            }
          }
        }
      }
      opentrans = newopentrans;
    }

    final Collection<EventProxy> resultevents = new LinkedList<EventProxy>();
    for (final EventProxy event : mAutomaton.getEvents()) {
      if (!isHidden(event)) {
        resultevents.add(event);
      }
    }
    final Collection<StateProxy> resultstates = new LinkedList<StateProxy>();
    for (final StateInfo info : mStateMap.values()) {
      if (info != null) {
        final StateProxy state = info.getNewState(mFactory);
        resultstates.add(state);
      }
    }
    final Collection<TransitionProxy> resulttransitions =
      new LinkedList<TransitionProxy>();
    for (final TransitionProxy oldtrans : temptrans) {
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateInfo infotarget = mStateMap.get(oldtarget);
      if (infotarget == null) {
        continue;
      }
      final StateProxy newtarget = infotarget.getNewState(mFactory);
      final StateProxy oldsource = oldtrans.getSource();
      final StateInfo infosource = mStateMap.get(oldsource);
      final StateProxy newsource = infosource.getNewState(mFactory);
      if (oldsource == newsource && oldtarget == newtarget) {
        resulttransitions.add(oldtrans);
      } else {
        final EventProxy event = oldtrans.getEvent();
        final TransitionProxy newtrans =
          mFactory.createTransitionProxy(newsource, event, newtarget);
        resulttransitions.add(newtrans);
      }
    }

    System.err.println
      ("Hiding: " + states.size() + " >> " + mStateMap.size() + " states, " +
       transitions.size() + " >> " + temptrans.size() + " transitions");

    return mFactory.createAutomatonProxy
      (mResultName, kind, resultevents, resultstates, resulttransitions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isHidden(final EventProxy event)
  {
    return mHiddenEvents.containsKey(event);
  }

  private boolean isHidden(final StateProxy state)
  {
    return !mStateMap.containsKey(state);
  }

  private void setInitial(final StateProxy state, final boolean initial)
  {
    if (initial) {
      final StateInfo info = mStateMap.get(state);
      if (info != null) {
        info.setInitial(true);
      }
    }
  }

  private void setMarked(final StateProxy state,
                         final Collection<EventProxy> props)
  {
    if (!props.isEmpty()) {
      final StateInfo info = mStateMap.get(state);
      if (info != null) {
        info.setMarked(props);
      }
    }      
  }


  //#########################################################################
  //# Local Class StateInfo
  private static class StateInfo
  {

    //#######################################################################
    //# Constructors
    private StateInfo(final StateProxy state)
    {
      mState = state;
      mInitial = state.isInitial();
      mPropositions = null;
      mNewState = null;
      setMarked(state.getPropositions());
    }

    //#######################################################################
    //# Simple Access
    private String getName()
    {
      return mState.getName();
    }

    //#######################################################################
    //# Setting Properties
    private void setInitial(final boolean initial)
    {
      mInitial |= initial;
    }

    private void setMarked(final Collection<EventProxy> propositions)
    {
      if (!propositions.isEmpty()) {
        if (mPropositions == null) {
          mPropositions = new HashSet<EventProxy>(propositions);
        } else {
          mPropositions.addAll(propositions);
        }
      }
    }

    //#######################################################################
    //# Creating New States
    private StateProxy getNewState(final ProductDESProxyFactory factory)
    {
      if (mNewState == null) {
        if (unchanged()) {
          mNewState = mState;
        } else if (mPropositions == null) {
          final Collection<EventProxy> empty = Collections.emptySet();
          mNewState =
            factory.createStateProxy(getName(), mInitial, empty);
        } else {
          mNewState =
            factory.createStateProxy(getName(), mInitial, mPropositions);
        }
      }
      return mNewState;
    }

    private boolean unchanged()
    {
      if (mInitial != mState.isInitial()) {
        return false;
      } else if (mPropositions == null) {
        return mState.getPropositions().isEmpty();
      } else {
        final Collection<EventProxy> oldprops =
	  new HashSet<EventProxy>(mState.getPropositions());
        return mPropositions.equals(oldprops);
      }
    }   
     
    //#######################################################################
    //# Data Members
    private final StateProxy mState;
    private boolean mInitial;
    private Collection<EventProxy> mPropositions;
    private StateProxy mNewState;

  }


  //#########################################################################
  //# Data Members
  private final boolean mConflictEq;
  private final AutomatonProxy mAutomaton;
  private final String mResultName;
  private final ProductDESProxyFactory mFactory;
  private final Map<EventProxy,EventProxy> mHiddenEvents;
  private final Map<StateProxy,StateInfo> mStateMap;

}

