//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   Hiding
//###########################################################################
//# $Id: Hiding.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class Hiding {

  //#########################################################################
  //# Invocation
  public static AutomatonProxy hide(final AutomatonProxy aut,
				    final Collection events,
				    final boolean conflicteq)
    throws DuplicateNameException
  {
    final String name = aut.getName();
    return hide(aut, events, name, conflicteq);
  }

  public static AutomatonProxy hide(final AutomatonProxy aut,
				    final Collection events,
				    final String resultname,
				    final boolean conflicteq)
    throws DuplicateNameException
  {
    final Hiding solver = new Hiding(aut, events, resultname, conflicteq);
    return solver.run();
  }


  //#########################################################################
  //# Constructors
  private Hiding(final AutomatonProxy aut,
		 final Collection events,
		 final String resultname,
		 final boolean conflicteq)
  {
    mConflictEq = conflicteq;
    mAutomaton = aut;
    mResultName = resultname;
    mHiddenEvents = new IdentityHashMap(events.size());
    final Iterator iter = events.iterator();
    while (iter.hasNext()) {
      final Object event = iter.next();
      mHiddenEvents.put(event, event);
    }
    final Collection states = mAutomaton.getStates();
    mStateMap = new IdentityHashMap(states.size());
  }


  //#########################################################################
  //# Workhorse Methods
  private AutomatonProxy run()
    throws DuplicateNameException
  {
    final ComponentKind kind = mAutomaton.getKind();
    final Collection states = mAutomaton.getStates();
    final Collection transitions = mAutomaton.getTransitions();
    final Collection resulttrans = new HashSet(transitions.size());
    final EventProxy tau = new EventProxy("tau", EventKind.CONTROLLABLE);
    final AutomatonProxy result = new AutomatonProxy(mResultName, kind);
    Collection opentrans = new HashSet();

    final Iterator transiter = transitions.iterator();
    while (transiter.hasNext()) {
      final TransitionProxy trans = (TransitionProxy) transiter.next();
      final StateProxy source = trans.getSource();
      final StateProxy target = trans.getTarget();
      final EventProxy event = trans.getEvent();
      if (!isHidden(event)) {
	final TransitionProxy newtrans =
	  new TransitionProxy(source, target, event);
	resulttrans.add(newtrans);
      } else if (source != target) {
	final TransitionProxy newtrans =
	  new TransitionProxy(source, target, tau);
	opentrans.add(newtrans);
      }
    }
    final AdjacencyMap adjacency = new AdjacencyMap(states);
    adjacency.addTransitions(resulttrans);
    adjacency.addTransitions(opentrans);

    final Iterator stateiter = states.iterator();
    while (stateiter.hasNext()) {
      final StateProxy state = (StateProxy) stateiter.next();
      if (mConflictEq && !state.isInitial()) {
	final Collection elig = adjacency.getEligibleEvents(state);
	final Iterator eligiter = elig.iterator();
	boolean hidden = true;
	while (hidden && eligiter.hasNext()) {
	  final Object event = eligiter.next();
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
      final Collection newopentrans = new HashSet();
      final Iterator open = opentrans.iterator();
      while (open.hasNext()) {
	final TransitionProxy trans = (TransitionProxy) open.next();
	final StateProxy source = trans.getSource();
	final StateProxy target = trans.getTarget();
	final boolean initial = source.isInitial();
	final Collection props = target.getPropositions();
	setInitial(target, initial);
	setMarked(source, props);
	final Collection entering = adjacency.getEnteringTransitions(source);
	final Iterator enteriter = entering.iterator();
	while (enteriter.hasNext()) {
	  final TransitionProxy entertrans =
	    (TransitionProxy) enteriter.next();
	  final StateProxy entersource = entertrans.getSource();
	  final EventProxy event = entertrans.getEvent();
	  final TransitionProxy newtrans =
	    new TransitionProxy(entersource, target, event);
	  if (event != tau) {
	    resulttrans.add(newtrans);
	  } else if (entersource != target) {
	    newopentrans.add(newtrans);
	  }
	}
	if (!isHidden(source)) {
	  final Collection exiting = adjacency.getExitingTransitions(target);
	  final Iterator exititer = exiting.iterator();
	  while (exititer.hasNext()) {
	    final TransitionProxy exittrans =
	      (TransitionProxy) exititer.next();
	    final StateProxy exittarget = exittrans.getTarget();
	    final EventProxy event = exittrans.getEvent();
	    final TransitionProxy newtrans =
	      new TransitionProxy(source, exittarget, event);
	    if (event != tau) {
	      resulttrans.add(newtrans);
	    } else if (initial && source != exittarget) {
	      newopentrans.add(newtrans);
	    }
	  }
	}
      }
      opentrans = newopentrans;
    }

    final Iterator infoiter = mStateMap.values().iterator();
    while (infoiter.hasNext()) {
      final StateInfo info = (StateInfo) infoiter.next();
      if (info != null) {
	final StateProxy state = info.getNewState();
	result.addState(state);
      }
    }
    final Iterator resultiter = resulttrans.iterator();
    while (resultiter.hasNext()) {
      final TransitionProxy oldtrans = (TransitionProxy) resultiter.next();
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateInfo infotarget = (StateInfo) mStateMap.get(oldtarget);
      if (infotarget == null) {
	continue;
      }
      final StateProxy newtarget = infotarget.getNewState();
      final StateProxy oldsource = oldtrans.getSource();
      final StateInfo infosource = (StateInfo) mStateMap.get(oldsource);
      final StateProxy newsource = infosource.getNewState();
      if (oldsource == newsource && oldtarget == newtarget) {
	result.addTransition(oldtrans);
      } else {
	final EventProxy event = oldtrans.getEvent();
	final TransitionProxy newtrans =
	  new TransitionProxy(newsource, newtarget, event);
	result.addTransition(newtrans);
      }
    }

    System.err.println
      ("Hiding: " + states.size() + " >> " + mStateMap.size() + " states, " +
       transitions.size() + " >> " + resulttrans.size() + " transitions");

    return result;
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
      final StateInfo info = (StateInfo) mStateMap.get(state);
      if (info != null) {
	info.setInitial(true);
      }
    }
  }

  private void setMarked(final StateProxy state, final Collection props)
  {
    if (!props.isEmpty()) {
      final StateInfo info = (StateInfo) mStateMap.get(state);
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

    private void setMarked(final Collection propositions)
    {
      if (!propositions.isEmpty()) {
	if (mPropositions == null) {
	  mPropositions = new HashSet(propositions);
	} else {
	  mPropositions.addAll(propositions);
	}
      }
    }

    //#######################################################################
    //# Creating New States
    private StateProxy getNewState()
    {
      if (mNewState == null) {
	if (unchanged()) {
	  mNewState = mState;
	} else if (mPropositions == null) {
	  mNewState = new StateProxy(getName(), mInitial);
	} else {
	  mNewState = new StateProxy(getName(), mInitial, mPropositions);
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
	final Collection oldprops = new HashSet(mState.getPropositions());
	return mPropositions.equals(oldprops);
      }
    }	
     
    //#######################################################################
    //# Data Members
    private final StateProxy mState;
    private boolean mInitial;
    private Collection mPropositions;
    private StateProxy mNewState;

  }


  //#########################################################################
  //# Data Members
  private final boolean mConflictEq;
  private final AutomatonProxy mAutomaton;
  private final String mResultName;
  private final Map mHiddenEvents;
  private final Map mStateMap;

}

