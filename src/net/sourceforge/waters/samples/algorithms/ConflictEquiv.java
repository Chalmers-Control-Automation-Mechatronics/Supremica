//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   ConflictEquiv
//###########################################################################
//# $Id: ConflictEquiv.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class ConflictEquiv {

  //#########################################################################
  //# Invocation
  public static AutomatonProxy reduce(final AutomatonProxy aut)
    throws DuplicateNameException
  {
    final String name = aut.getName();
    return reduce(aut, name);
  }

  public static AutomatonProxy reduce(final AutomatonProxy aut,
				      final String resultname)
    throws DuplicateNameException
  {
    final ConflictEquiv solver = new ConflictEquiv(aut, resultname);
    return solver.run();
  }


  //#########################################################################
  //# Constructors
  private ConflictEquiv(final AutomatonProxy aut, final String resultname)
  {
    final Collection states = aut.getStates();
    final int numstates = states.size();
    mAutomaton = aut;
    mResultName = resultname;
    mAdjacency = new AdjacencyMap(aut);
    mStateMap = new IdentityHashMap(numstates);
  }


  //#########################################################################
  //# Workhorse Methods
  private AutomatonProxy run()
    throws DuplicateNameException
  {
    final Collection states0 = mAutomaton.getStates();
    final List states = new ArrayList(states0);
    final int orignumstates = states.size();
    Collections.sort(states);

    int code = 0;
    int numstates = orignumstates;
    final Iterator iter1 = states.iterator();
    while (iter1.hasNext()) {
      final Object state = iter1.next();
      final Integer entry = new Integer(code++);
      mStateMap.put(state, entry);
    }

    int step = 0;
    while (true) {
      final Map infomap = new HashMap(orignumstates);
      final Iterator iter2 = states.iterator();
      while (iter2.hasNext()) {
	final StateProxy state = (StateProxy) iter2.next();
	final boolean initial = state.isInitial();
	final Collection incoming = mAdjacency.getEnteringTransitions(state);
	final Set demi = createDemiTransitions(incoming);
	final Set elig = mAdjacency.getEligibleEvents(state);
	final Collection props = state.getPropositions();
	elig.addAll(props);
	final StateInfo newinfo = new StateInfo(initial, demi, elig);
	StateInfo info = (StateInfo) infomap.get(newinfo);
	if (info == null) {
	  info = newinfo;
	  infomap.put(info, info);
	}
	info.addState(state);
      }

      final Set reducedstates = infomap.keySet();
      final int newnumstates = reducedstates.size();
      if (newnumstates == numstates) {
	break;
      }
      step++;
      numstates = newnumstates;

      code = 0;
      final Iterator iter3 = reducedstates.iterator();
      while (iter3.hasNext()) {
	final Integer entry = new Integer(code++);
	final StateInfo info = (StateInfo) iter3.next();
	final Collection infostates = info.getStates();
	final Iterator iter4 = infostates.iterator();
	while (iter4.hasNext()) {
	  final Object state = iter4.next();
	  mStateMap.put(state, entry);
	}
      }
    }

    final ComponentKind kind = mAutomaton.getKind();
    final AutomatonProxy result = new AutomatonProxy(mResultName, kind);
    final DemiState[] demistates = new DemiState[numstates];
    for (code = 0; code < numstates; code++) {
      demistates[code] = new DemiState(code);
    }
    final Iterator iter5 = states.iterator();
    while (iter5.hasNext()) {
      final StateProxy state = (StateProxy) iter5.next();
      final Integer entry = (Integer) mStateMap.get(state);
      code = entry.intValue();
      demistates[code].includeState(state);
    }
    final StateProxy[] newstates = new StateProxy[numstates];
    for (code = 0; code < numstates; code++) {
      final StateProxy state = demistates[code].createState();
      newstates[code] = state;
      result.addState(state);
    }
    final Collection transitions = mAutomaton.getTransitions();
    final Set newtransitions = new HashSet();
    final Iterator iter6 = transitions.iterator();
    while (iter6.hasNext()) {
      final TransitionProxy oldtrans = (TransitionProxy) iter6.next();
      final StateProxy oldsource = oldtrans.getSource();
      final Integer oldsourcecodeentry = (Integer) mStateMap.get(oldsource);
      final int oldsourcecode = oldsourcecodeentry.intValue();
      final StateProxy newsource = newstates[oldsourcecode];
      final StateProxy oldtarget = oldtrans.getTarget();
      final Integer oldtargetcodeentry = (Integer) mStateMap.get(oldtarget);
      final int oldtargetcode = oldtargetcodeentry.intValue();
      final StateProxy newtarget = newstates[oldtargetcode];
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
	new TransitionProxy(newsource, newtarget, event);
      if (newtransitions.add(newtrans)) {
	result.addTransition(newtrans);
      }
    }

    System.err.println
      ("B Reduction in " + step + " steps: " +
       states.size() + " >> " + numstates + " states, " +
       transitions.size() + " >> " + newtransitions.size() + " transitions");

    return result;
  }


  private Set createDemiTransitions(final Collection transitions)
  {
    final int numtrans = transitions.size();
    final Set result = new HashSet(numtrans);
    final Iterator iter = transitions.iterator();
    while (iter.hasNext()) {
      final TransitionProxy trans = (TransitionProxy) iter.next();
      final StateProxy source = trans.getSource();
      final EventProxy event = trans.getEvent();
      final Integer entry = (Integer) mStateMap.get(source);
      final int code = entry.intValue();
      final DemiTransition demi = new DemiTransition(code, event);
      result.add(demi);
    }
    return result;
  }


  //#########################################################################
  //# Local Class StateInfo
  private static class StateInfo {

    //#######################################################################
    //# Constructors
    private StateInfo(final boolean initial,
		      final Set incoming,
		      final Set elig)
    {
      mIsInitial = initial;
      mIncoming = incoming;
      mElig = elig;
      mStates = new LinkedList();
    }

    //#######################################################################
    //# Equals and Hashcode
    public boolean equals(final Object partner)
    {
      if (partner != null && getClass() == partner.getClass()) {
	final StateInfo info = (StateInfo) partner;
	return
	  mIsInitial == info.mIsInitial &&
	  mIncoming.equals(info.mIncoming) &&
	  mElig.equals(info.mElig);
      } else {
	return false;
      }
    }

    public int hashCode()
    {
      return
	mIncoming.hashCode() +
	5 * mElig.hashCode() +
	(mIsInitial ? 25 : 0);
    }

    //#######################################################################
    //# Simple Access
    private Collection getStates()
    {
      return mStates;
    }

    private void addState(final StateProxy state)
    {
      mStates.add(state);
    }

    //#######################################################################
    //# Data Members
    private final boolean mIsInitial;
    private final Set mIncoming;
    private final Set mElig;
    private final Collection mStates;

  }


  //#########################################################################
  //# Local Class DemiTransition
  private static class DemiTransition {

    //#######################################################################
    //# Constructors
    private DemiTransition(final int source, final EventProxy event)
    {
      mSource = source;
      mEvent = event;
    }

    //#######################################################################
    //# Equals and HashCode
    public boolean equals(final Object partner)
    {
      if (partner != null && partner.getClass() == getClass()) {
	final DemiTransition trans = (DemiTransition) partner;
	return mSource == trans.mSource && mEvent == trans.mEvent;
      } else {
	return false;
      }
    }

    public int hashCode()
    {
      return mEvent.hashCode() + 5 * mSource;
    }

    //#######################################################################
    //# Data Members
    private final int mSource;
    private final EventProxy mEvent;
  }


  //#########################################################################
  //# Local Class DemiState
  private static class DemiState {

    //#######################################################################
    //# Constructors
    private DemiState(final int code)
    {
      mCode = code;
      mName = new StringBuffer();
      mInitial = false;
      mPropositions = new LinkedList();
    }

    //#######################################################################
    //# Adding Information
    private void includeState(final StateProxy state)
    {
      if (mName != null) {
	final String name = state.getName();
	if (mName.length() + name.length() > 12 || name.indexOf(":") >= 0) {
	  mName = null;
	} else {
	  if (mName.length() > 0) {
	    mName.append(':');
	  }
	  mName.append(name);
	}
      }
      if (state.isInitial()) {
	mInitial = true;
      }
      mPropositions.addAll(state.getPropositions());
    }

    //#######################################################################
    //# Creating States
    private String getName()
    {
      if (mName == null) {
	return ":c" + mCode;
      } else {
	return mName.toString();
      }
    }

    private StateProxy createState()
    {
      return new StateProxy(getName(), mInitial, mPropositions);
    }

    //#######################################################################
    //# Data Members
    private final int mCode;
    private StringBuffer mName;
    private boolean mInitial;
    private Collection mPropositions;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final String mResultName;
  private final AdjacencyMap mAdjacency;
  private final Map mStateMap;

}

