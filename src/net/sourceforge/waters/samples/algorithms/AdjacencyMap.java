//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   AdjacencyMap
//###########################################################################
//# $Id: AdjacencyMap.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
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

  public AdjacencyMap(final Collection states)
  {
    this(states, BOTH);
  }

  public AdjacencyMap(final Collection states, final int modes)
  {
    mAdjacencyMap = new IdentityHashMap(states.size());
    mModes = modes;
  }


  //#########################################################################
  //# Access Methods
  public void addTransitions(final Collection transitions)
  {
    final Iterator iter = transitions.iterator();
    while (iter.hasNext()) {
      final TransitionProxy trans = (TransitionProxy) iter.next();
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

  public Collection getEnteringTransitions(final StateProxy state)
  {
    if ((mModes & ENTERING) != 0) {
      final Entry entry = getEntry(state);
      return entry.getEnteringTransitions();
    } else {
      throw new IllegalStateException("Not in ENTERING mode!");
    }
  }

  public Collection getExitingTransitions(final StateProxy state)
  {
    if ((mModes & EXITING) != 0) {
      final Entry entry = getEntry(state);
      return entry.getExitingTransitions();
    } else {
      throw new IllegalStateException("Not in EXITING mode!");
    }
  }

  public Set getEligibleEvents(final StateProxy state)
  {
    final Collection transitions = getExitingTransitions(state);
    final Iterator iter = transitions.iterator();
    final int numtrans = transitions.size();
    final Set elig = new HashSet(numtrans);
    while (iter.hasNext()) {
      final TransitionProxy trans = (TransitionProxy) iter.next();
      final EventProxy event = trans.getEvent();
      elig.add(event);
    }
    return elig;
  }


  //#########################################################################
  //# Auxiliary Methods
  private Entry getEntry(final StateProxy state)
  {
    Entry entry = (Entry) mAdjacencyMap.get(state);
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
      mEnteringTransitions = new LinkedList();
      mExitingTransitions = new LinkedList();
    }

    //#######################################################################
    //# Simple Access
    private Collection getEnteringTransitions()
    {
      return mEnteringTransitions;
    }

    private Collection getExitingTransitions()
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
    final Collection mEnteringTransitions;
    final Collection mExitingTransitions;
  }


  //#########################################################################
  //# Class Constants
  public static final int ENTERING = 0x01;
  public static final int EXITING = 0x02;
  public static final int BOTH = ENTERING | EXITING;


  //#########################################################################
  //# Data Members
  private final Map mAdjacencyMap;
  private final int mModes;

}

