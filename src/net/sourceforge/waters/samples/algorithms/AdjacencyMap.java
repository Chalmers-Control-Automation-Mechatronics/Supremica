//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   AdjacencyMap
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


public class AdjacencyMap {

  //#########################################################################
  //# Constructors
  public AdjacencyMap(final AutomatonProxy aut)
  {
    this(aut, BOTH);
  }

  public AdjacencyMap(final AutomatonProxy aut, final int modes)
  {
    this(aut.getStates(), modes);
    addTransitions(aut.getTransitions());
  }

  public AdjacencyMap(final Collection<StateProxy> states)
  {
    this(states, BOTH);
  }

  public AdjacencyMap(final Collection<StateProxy> states, final int modes)
  {
    mAdjacencyMap = new HashMap<StateProxy,Entry>(states.size());
    mModes = modes;
  }


  //#########################################################################
  //# Access Methods
  public void addTransitions(final Collection<TransitionProxy> transitions)
  {
    for (final TransitionProxy trans : transitions) {
      if ((mModes & EXITING) != 0) {
        final StateProxy source = trans.getSource();
        final Entry sourceentry = getEntry(source);
        sourceentry.addExitingTransition(trans);
      }
      if ((mModes & ENTERING) != 0) {
        final StateProxy target = trans.getTarget();
        final Entry targetentry = getEntry(target);
        targetentry.addEnteringTransition(trans);
      }
    }
  }

  public Collection<TransitionProxy>
    getEnteringTransitions(final StateProxy state)
  {
    if ((mModes & ENTERING) != 0) {
      final Entry entry = getEntry(state);
      return entry.getEnteringTransitions();
    } else {
      throw new IllegalStateException("Not in ENTERING mode!");
    }
  }

  public Collection<TransitionProxy>
    getExitingTransitions(final StateProxy state)
  {
    if ((mModes & EXITING) != 0) {
      final Entry entry = getEntry(state);
      return entry.getExitingTransitions();
    } else {
      throw new IllegalStateException("Not in EXITING mode!");
    }
  }

  public Set<EventProxy> getEligibleEvents(final StateProxy state)
  {
    final Collection<TransitionProxy> transitions =
      getExitingTransitions(state);
    final int numtrans = transitions.size();
    final Set<EventProxy> elig = new HashSet<EventProxy>(numtrans);
    for (final TransitionProxy trans : transitions) {
      final EventProxy event = trans.getEvent();
      elig.add(event);
    }
    return elig;
  }

  public StateProxy getSuccessorState(final StateProxy state,
                                      final EventProxy event)
  {
    final Collection<TransitionProxy> transitions =
      getExitingTransitions(state);
    for (final TransitionProxy trans : transitions) {
      if (trans.getEvent() == event) {
        return trans.getTarget();
      }
    }
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private Entry getEntry(final StateProxy state)
  {
    Entry entry = mAdjacencyMap.get(state);
    if (entry == null) {
      entry = new Entry();
      mAdjacencyMap.put(state, entry);
    }
    return entry;
  }


  //#########################################################################
  //# Local Class Entry
  private static class Entry
  {
    //#######################################################################
    //# Constructors
    private Entry()
    {
      mEnteringTransitions = new LinkedList<TransitionProxy>();
      mExitingTransitions = new LinkedList<TransitionProxy>();
    }

    //#######################################################################
    //# Simple Access
    private Collection<TransitionProxy> getEnteringTransitions()
    {
      return mEnteringTransitions;
    }

    private Collection<TransitionProxy> getExitingTransitions()
    {
      return mExitingTransitions;
    }

    private void addEnteringTransition(final TransitionProxy trans)
    {
      mEnteringTransitions.add(trans);
    }

    private void addExitingTransition(final TransitionProxy trans)
    {
      mExitingTransitions.add(trans);
    }

    //#######################################################################
    //# Data Members
    final Collection<TransitionProxy> mEnteringTransitions;
    final Collection<TransitionProxy> mExitingTransitions;
  }


  //#########################################################################
  //# Class Constants
  public static final int ENTERING = 0x01;
  public static final int EXITING = 0x02;
  public static final int BOTH = ENTERING | EXITING;


  //#########################################################################
  //# Data Members
  private final Map<StateProxy,Entry> mAdjacencyMap;
  private final int mModes;

}

